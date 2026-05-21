# The-Decider — Design Spec

**Date:** 2026-05-21
**Status:** Approved, ready for implementation planning
**Platform:** Android, single user

## Concept

Native Android app for a single user with ADHD. The app surfaces ONE household or sadhana task at a time as a swipeable card. The user swipes right to accept (then works through bite-sized steps) or swipes left to defer (with a chooser). The backend silently schedules the day around cadence and context, but the user is never shown the total daily load — that would trigger overwhelm.

Pressure builds quietly on overdue tasks: they appear more often in the queue, and their card gains a subtle color tint plus a factual "last done N days ago" chip.

## Guiding Principles

- **One decision at a time.** Never present a list of pending tasks to choose from.
- **Hide totals.** Never show "5 tasks left today" or any number that quantifies load.
- **Negotiate, don't dictate.** Every task can be deferred, but skipping carries silent consequence.
- **Pressure is honest, not loud.** Overdue tasks are surfaced more often and tinted, but never shouted about.
- **Bite-sized always.** Even a "small" task can be broken smaller on demand.

## Tech Stack

- **Kotlin + Jetpack Compose** — native Android, best swipe-gesture UX, single-language stack
- **Room** — local SQLite persistence
- **DataStore** — settings, including encrypted Anthropic API key
- **WorkManager** — periodic nudges, midnight rollover
- **Anthropic SDK (Claude Haiku)** — on-demand step sub-breakdown only; called when the user taps "break this smaller"
- **Minimum SDK:** Android 9 (API 28) — conservative target
- **Target SDK:** Android 14 (API 34)

## Data Model

Room entities:

### Task
```
id: Long (PK, autogen)
title: String
cadence: enum { daily, bidaily, weekly, biweekly, monthly, bimonthly, anytime, oneoff }
energy: enum { low, medium, high }
duration: enum { quick, short, medium, long }  // <5, <15, <30, >=30 minutes
time_window: enum { morning, afternoon, evening, anytime }
is_active: Boolean (default true)
created_at: Instant
```

`last_done_at` is not stored on `Task` — it is derived as `max(completed_at)` from the `Completion` table where `type = done` for that task. The repository layer exposes it via a join/aggregate query.

### Step
```
id: Long (PK, autogen)
task_id: Long (FK -> Task)
order: Int
content: String
```

### Completion
```
id: Long (PK, autogen)
task_id: Long (FK -> Task)
completed_at: Instant
type: enum { done, skipped_pressure_kept }
```

`done` completions update the effective `last_done_at` for the task. `skipped_pressure_kept` do NOT — they are logged but pressure keeps building.

For `oneoff` tasks, a `done` Completion also sets `is_active = false` so the task does not reappear.

### Snooze
```
id: Long (PK, autogen)
task_id: Long (FK -> Task)
until: Instant
kind: enum { later_today, tomorrow, skip_cycle }
created_at: Instant
```

A snooze is "active" if `now < until` and no later completion exists.

### Settings (DataStore, not Room)
```
nudge_window_start: LocalTime (default 09:00)
nudge_window_end: LocalTime (default 21:00)
max_nudges_per_day: Int (default 3)
anthropic_api_key: encrypted String?
mode_chip_presets: List<ModeChip>  // user-editable
```

## Selection Algorithm

When the user opens the app or completes a task, the next card is computed by `SelectionService.pickNext()`:

1. **Build candidate set:**
   - All `Task` rows where `is_active = true`
   - Where the cadence period has rolled (`last_done_at + cadence_length <= now`) OR `last_done_at IS NULL`
   - Exclude tasks with an active `Snooze` row (`now < snooze.until`)
   - Exclude tasks already completed in the current cycle (e.g., for `daily`, completed today)

2. **Filter by context:**
   - The task's `time_window` must match current time bucket, OR be `anytime`
   - Time buckets: morning = 05:00–11:59, afternoon = 12:00–16:59, evening = 17:00–22:59, night = 23:00–04:59 (only `anytime` tasks in night)
   - If a mode chip is active (e.g., "Low energy" or "10 min"), narrow further by matching `energy` or `duration`

3. **Compute pressure per candidate:**
   ```
   cadence_days = days_in_cadence(task.cadence)  // daily=1, bidaily=2, weekly=7, biweekly=14, monthly=30, bimonthly=60, anytime=null, oneoff=null
   days_since = (now - last_done_at) in days, or (now - created_at) if never done
   pressure = if cadence_days != null then max(0, (days_since - cadence_days) / cadence_days) else 0.0
   anytime_tasks_pressure = 0.05  // low constant so they show up sometimes
   ```

4. **Tier candidates:**
   - `overdue` if pressure > 1.0
   - `in_window` if 0 ≤ pressure ≤ 1.0 (cadence has rolled, not yet overdue)
   - `anytime` for `anytime`/`oneoff` tasks (use constant pressure of 0.05)
   - Pick from highest non-empty tier (priority: overdue > in_window > anytime)

5. **Weighted-random within tier:**
   - `weight = pressure + 1` (always positive)
   - Randomly sample one candidate

If the candidate set is empty (all snoozed, all done, nothing matches context), the queue screen shows an empty state with a "+" prompt.

## Screens

### 1. Queue (home)

- Single task **Card** centered, swipeable horizontally
- Card content:
  - Title (large)
  - Duration chip (e.g., "≤15 min")
  - Energy chip (e.g., "Low energy")
  - "Last done 9 days ago" chip (omitted if never done)
  - Edge tint: pressure tier color (neutral / soft amber / soft red)
- Above the card: horizontal scroll row of **Mode Chips** — `All` (default), `Low energy`, `10 min`, `Quick`, plus any user-defined presets. Selecting one narrows the queue immediately.
- **Right swipe:** marks accepted, navigates to Task Detail
- **Left swipe:** opens bottom sheet chooser:
  - Later today
  - Tomorrow
  - Skip this cycle (with subtitle: "logs as skipped, pressure keeps building")
- **"+" FAB:** opens Quick-add sheet
- **Top app bar:** hamburger menu → Task management, History, Settings

### 2. Task Detail

- Default view: **Checklist** of `Step` rows. Tap a row's checkbox to mark done.
- **App bar toggle:** Focus mode. Collapses the list to a single full-screen card showing only the current step. Tap "Done" or swipe up to advance to the next step.
- **Per-step action:** "break this smaller →" button. Calls Claude Haiku via `BreakdownService`, which returns 2–5 sub-steps. The original step is replaced inline by the sub-steps (transient, not persisted unless the user taps "save these steps").
- **Bottom action:** "Done with task" button. Marks a `Completion(type=done)` and returns to Queue.

### 3. Quick-add

- Bottom sheet form:
  - Title (required)
  - Cadence picker (default: `oneoff`)
  - Energy + duration + time_window (sensible defaults: medium / short / anytime)
  - Optional inline steps editor (add/remove rows)
  - "Save" button
- If cadence is not `oneoff`, the task joins the master list and recurs.

### 4. Task Management

- List of all `Task` rows (filterable by cadence)
- Each row: title, cadence, active toggle
- Tap → Task Edit screen: full editor for title, cadence, tags, steps; delete button

### 5. History

- Reverse-chronological list of `Completion` rows
- Filter chips: `All / Done / Skipped`
- Each entry: task title, timestamp, type icon

### 6. Settings

- Nudge window (start/end time pickers)
- Max nudges per day (1–6)
- Anthropic API key field (masked, stored encrypted in DataStore)
- Mode chip presets editor (add/remove/edit chips, each chip = a saved energy/duration filter)

## Notifications

- **NudgeWorker** runs every hour via WorkManager.
- On each run, decide whether to fire a nudge:
  - Current time within `nudge_window_start`..`nudge_window_end`
  - Nudges already fired today < `max_nudges_per_day`
  - User has not opened the app in the last 2 hours
  - At least 1 hour since the last nudge
- Nudge content is intentionally soft: "Ready for the next one?" — no count, no urgency.
- Tapping the nudge deep-links into the Queue screen.

## Step Breakdown (LLM)

- Triggered by the per-step "break this smaller" button.
- `BreakdownService.breakDown(step: Step): List<String>` calls Claude Haiku with a prompt like:
  > "Break this household task step into 2–5 smaller, concrete, single-action sub-steps. Step: {content}. Respond as a JSON array of strings."
- Result is rendered inline, replacing the step visually but not persisted unless the user taps "save these steps" (which writes them as new `Step` rows in place of the original).
- If the API key is missing or the call fails, surface a small inline error: "Can't break this down right now."

## Module Layout

```
app/
  src/main/
    AndroidManifest.xml
    java/com/cpotzy/thedecider/
      App.kt
      data/
        db/
          AppDb.kt
          entities/ (Task, Step, Completion, Snooze)
          dao/ (TaskDao, StepDao, CompletionDao, SnoozeDao)
        repo/
          TaskRepository.kt
          CompletionRepository.kt
          SnoozeRepository.kt
        settings/
          SettingsStore.kt
      domain/
        select/
          SelectionService.kt
          PressureCalculator.kt
          ContextFilter.kt
        breakdown/
          BreakdownService.kt
      ui/
        queue/    QueueScreen.kt, QueueViewModel.kt, TaskCard.kt, SwipeChooserSheet.kt
        task/     TaskDetailScreen.kt, TaskDetailViewModel.kt, ChecklistView.kt, FocusModeView.kt
        manage/   TaskListScreen.kt, TaskEditScreen.kt, TaskListViewModel.kt
        history/  HistoryScreen.kt, HistoryViewModel.kt
        settings/ SettingsScreen.kt, SettingsViewModel.kt
        common/   theme/ (Color, Type, Shape), components/
      work/
        NudgeWorker.kt
        RolloverWorker.kt
      di/
        AppModule.kt (Hilt or manual DI)
```

## Seeding

On first launch, the app reads `tasks-list.md` (bundled as an asset) and seeds the `Task` table. Initial entries get sensible defaults for energy/duration/time_window:

- "Morning skincare" → morning, low, quick
- "Brush teeth night" → evening, low, quick
- "HIIT workout" → morning/afternoon, high, medium
- "Clean bathroom" → afternoon, medium, medium
- (...etc — full seed table generated during implementation)

The scoring annotations ("time-based scoring", "per-minute scoring") in `tasks-list.md` are stripped — sadhana tasks become plain done/not-done like everything else.

## Out of Scope (v1)

- Cloud sync / multi-device
- Multi-user accounts
- Task dependencies (washing chain)
- Weather/location-aware filters
- Streaks, badges, gamification
- Daily completion summary screen (would expose load)
- Voice input
- Widgets
- Wear OS companion

## Open Implementation Questions (to resolve during planning)

- DI: Hilt vs manual. Manual is fine for a one-user app; Hilt adds compile-time complexity that may not be worth it.
- Anthropic SDK: official Kotlin SDK if available, otherwise OkHttp + JSON.
- Encryption for API key: EncryptedSharedPreferences vs Tink directly.
- Animation polish for swipe physics — Compose `swipeable` modifier is sufficient for v1.
