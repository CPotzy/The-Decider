# How The-Decider picks the next task

A walkthrough of the selection pipeline, from "open the app" to "this card appears."

---

## 1. Top-level flow

Every time the queue refreshes (app open, swipe, mode change, snooze, screen resume), this pipeline runs.

```mermaid
flowchart TD
    A[Refresh queue] --> B[Load all active tasks]
    B --> C[Filter: cadence eligible?]
    C --> D[Filter: dependencies satisfied?]
    D --> E[Filter: not currently snoozed]
    E --> F[Filter: matches mode + time window]
    F --> G{Any candidates?}
    G -->|yes| H[Tier + weighted pick]
    G -->|no| I[Fallback: skip cadence + dependency filters]
    I --> J[Re-run snooze + context filter]
    J --> K{Any candidates?}
    K -->|yes| L[Show task with 'Ahead of schedule' hint]
    K -->|no| M[Show empty state]
    H --> N[Show task card]
```

The fallback path is the ADHD-friendly hook: if you're "caught up" on cadence, you still get a card with a small *"Ahead of schedule — early start"* label so the app never tells you to come back later.

---

## 2. Cadence eligibility

Each task has a cadence with a `cadenceDays` value:

| Cadence    | `cadenceDays` |
|------------|---------------|
| DAILY      | 1             |
| BIDAILY    | 2             |
| WEEKLY     | 7             |
| BIWEEKLY   | 14            |
| MONTHLY    | 30            |
| BIMONTHLY  | 60            |
| ANYTIME    | `null`        |
| ONEOFF     | `null`        |

A task is **cadence-eligible** when enough time has passed since it was last done (or since it was created, if never done).

```mermaid
flowchart TD
    A[Task] --> B{cadenceDays is null?}
    B -->|yes ANYTIME / ONEOFF| C[Eligible]
    B -->|no| D[ref = lastDoneAt ?? createdAt]
    D --> E[hoursSince = now - ref]
    E --> F{hoursSince >= cadenceDays * 24?}
    F -->|yes| C
    F -->|no| G[Not eligible yet]
```

Example: Vacuum is DAILY (1). Done at 10 a.m. yesterday → eligible again at 10 a.m. today.

---

## 3. Dependency check

Some tasks declare prerequisites in `SeedDependencies.byTitle`. A dependent is held until its prerequisite is *fresh*.

```mermaid
flowchart TD
    A[Task with dependsOnTitles] --> B[For each prerequisite title]
    B --> C{Prerequisite exists?}
    C -->|no| D[Treat as satisfied]
    C -->|yes| E{Has lastDoneAt?}
    E -->|no| F[Not satisfied -> dependent ineligible]
    E -->|yes| G{Prerequisite cadence is ANYTIME?}
    G -->|yes| H[Satisfied -> any completion counts]
    G -->|no| I{hoursSince prereq.lastDoneAt < prereq.cadenceDays * 24?}
    I -->|yes prereq is fresh| H
    I -->|no prereq is stale| F
```

Currently seeded:

| Dependent      | Requires                          |
|----------------|-----------------------------------|
| Mop the floor  | Vacuum downstairs and upstairs    |

Mop only appears in the queue if Vacuum was done within the last 24 hours.

---

## 4. Snooze filter

Snoozes have an `until` timestamp. While `until > now`, the task is hidden.

```mermaid
flowchart LR
    A[Swipe left] --> B{Pick option}
    B -->|Later today| C[until = now + 3h]
    B -->|Tomorrow| D[until = tomorrow 05:00 local]
    B -->|Skip cycle| E[Mark SKIPPED -> keeps pressure clock running]
```

`Skip cycle` does *not* add a snooze — it records a `SKIPPED` completion so the task drops off the surface but pressure keeps mounting. (Reread `PressureCalculator`: only `DONE` completions reset the pressure clock; the reference timestamp uses `lastDoneAt`, which means `lastOfType(DONE)`.)

---

## 5. Context filter (mode + time of day)

Each task has a `timeWindow` (MORNING / AFTERNOON / EVENING / ANYTIME) and the user may pick a mode chip (All / Low energy / 10 min / Quick).

```mermaid
flowchart TD
    A[Task] --> B{task.timeWindow == ANYTIME?}
    B -->|yes| C[Time check passes]
    B -->|no| D[Compute current window from local time]
    D --> E{Current window == NIGHT?}
    E -->|yes| F[Time check fails]
    E -->|no| G{task.timeWindow == current window?}
    G -->|yes| C
    G -->|no| F
    C --> H{Mode has energyFilter?}
    F --> X[Excluded]
    H -->|yes| I{task.energy == filter?}
    H -->|no| J[Energy check passes]
    I -->|yes| J
    I -->|no| X
    J --> K{Mode has maxDuration?}
    K -->|yes| L{task.duration.maxMinutes <= max?}
    K -->|no| Y[Included]
    L -->|yes| Y
    L -->|no| X
```

Time windows:

| Window     | Local hours |
|------------|-------------|
| MORNING    | 05:00–11:59 |
| AFTERNOON  | 12:00–16:59 |
| EVENING    | 17:00–22:59 |
| NIGHT      | 23:00–04:59 |

Mode chips:

| Chip        | Effect                                  |
|-------------|-----------------------------------------|
| All         | no filter                               |
| Low energy  | only `energy = LOW`                     |
| 10 min      | only `duration <= SHORT` (≤15 min)      |
| Quick       | only `duration <= QUICK` (≤5 min)       |

---

## 6. Pressure

Pressure quantifies "how overdue" a task is, normalized by its own cadence.

```
pressure = max(0, (daysSinceRef - cadenceDays) / cadenceDays)
```

- Just done: `daysSinceRef ≈ 0` → `pressure = 0`
- Done exactly on cadence boundary: `pressure = 0`
- One full cadence overdue (e.g., daily task last done 2 days ago): `pressure = 1.0`
- Two cadences overdue: `pressure = 2.0`
- ANYTIME tasks: fixed `0.05` (always selectable but low priority)

Pressure feeds two things:
- **Tier** for selection bucketing
- **Tint** on the task card (visible only as color, not as a number)

```mermaid
flowchart LR
    A[pressure] --> B{cadence is ANYTIME or ONEOFF?}
    B -->|yes| C[ANYTIME tier]
    B -->|no| D{pressure > 1.0?}
    D -->|yes| E[OVERDUE tier]
    D -->|no| F[IN_WINDOW tier]
```

---

## 7. Selection (tiering + weighted pick)

After all filters, candidates are split into tiers and the highest non-empty tier is sampled.

```mermaid
flowchart TD
    A[Filtered candidates] --> B[Split into OVERDUE / IN_WINDOW / ANYTIME buckets]
    B --> C{OVERDUE bucket has items?}
    C -->|yes| D[weightedPick from OVERDUE]
    C -->|no| E{IN_WINDOW has items?}
    E -->|yes| F[weightedPick from IN_WINDOW]
    E -->|no| G{ANYTIME has items?}
    G -->|yes| H[weightedPick from ANYTIME]
    G -->|no| I[Return null -> trigger fallback]
```

`weightedPick`: each task in the bucket gets a weight of `pressure + 1.0`, then a uniform random roll picks one proportional to its weight. So within a tier, more-overdue tasks are likelier but not certain — keeps the queue from feeling deterministic.

---

## 8. Fallback (early start)

When the eligible candidate list is empty (everything's fresh), the ViewModel runs the selection again over **all active tasks** — bypassing the cadence and dependency filters, but still respecting snooze and context. If a task is picked this way, the card shows *"Ahead of schedule — early start."*

```mermaid
flowchart TD
    A[pickNext over eligible set] --> B{returned a task?}
    B -->|yes| C[Show normally]
    B -->|no| D[pickNext over ALL active tasks]
    D --> E{returned a task?}
    E -->|yes| F[Show with 'Ahead of schedule' label]
    E -->|no| G[Genuinely empty state]
```

`G` only triggers when literally no active task matches the current context (e.g., NIGHT and you have zero ANYTIME tasks, or every task is snoozed).

---

## 9. End-to-end example

Walk through a concrete morning.

**Setup at 09:00 local:**
- 18 daily tasks, all active.
- You did `Vacuum downstairs and upstairs` at 17:00 yesterday (16 h ago).
- You did `Brush teeth night` at 22:00 yesterday (11 h ago).
- Everything else: never done since install 3 days ago.
- Mode: `All`. No snoozes.

**Pipeline:**

```mermaid
flowchart TD
    A[18 daily tasks loaded] --> B[Cadence filter]
    B -->|16 still over 24h since last done or never done| C[16 remaining]
    B -.->|Vacuum 16h ago| X1[Excluded]
    B -.->|Brush night 11h ago| X2[Excluded]
    C --> D[Dependency filter]
    D -->|Mop: Vacuum is 16h fresh| Y[Mop kept]
    D -->|others have no deps| C2[16 remaining]
    C2 --> E[Snooze filter: 16 remaining]
    E --> F[Context filter at 09:00 MORNING]
    F -->|Morning skincare keeps morning slot| F2[All ANYTIME + MORNING-tagged remain ≈ 16]
    F2 --> G[Pressure tiering]
    G --> H[OVERDUE bucket = tasks 3+ days overdue from install]
    H --> I[Weighted pick - higher pressure -> higher weight]
    I --> J[Card shown — e.g., 'Dust altar' if older + higher weight]
```

Tomorrow at 09:00 after doing everything tonight: queue runs again, almost everything is cadence-eligible (24 h passed), the OVERDUE bucket is empty, IN_WINDOW gets sampled, and you see a freshly-rotated task.

---

## 10. Quick reference: what triggers a refresh?

| Event                              | Triggers refresh? |
|------------------------------------|-------------------|
| App opens / resumes                | yes               |
| Swipe right (start task)           | yes (after detail) |
| Swipe left → Later/Tomorrow/Skip   | yes               |
| Mode chip change                   | yes               |
| Task completed via Done button     | yes (on return)   |
| Custom task added via QuickAdd     | yes               |
| Settings change                    | next refresh      |
| Nudge fired                        | no (separate path)|

---

## File map

| Concern                | File                                                          |
|------------------------|---------------------------------------------------------------|
| Pipeline orchestration | `ui/queue/QueueViewModel.kt`                                  |
| Cadence + dependency   | `data/repo/TaskRepository.kt#listEligibleForSelection`        |
| Pressure               | `domain/select/PressureCalculator.kt`                         |
| Tier                   | `domain/model/PressureTier.kt`                                |
| Context filter         | `domain/select/ContextFilter.kt`                              |
| Tier + weighted pick   | `domain/select/SelectionService.kt`                           |
| Snooze rules           | `data/repo/SnoozeRepository.kt`                               |
| Seed dependencies      | `data/seed/SeedDependencies.kt`                               |
| Mode chips             | `domain/select/ModeChip.kt`                                   |
| Time windows           | `domain/model/TimeWindow.kt`                                  |
