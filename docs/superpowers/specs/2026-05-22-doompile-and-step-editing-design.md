# Doom-pile tasks + in-app step editing

**Date:** 2026-05-22
**Status:** approved (verbal — caleb)

## Why

ADHD-cleaner principle: separate the *physical sweep* of clutter from the *decision work* of sorting it. Sweeping is mindless and feels great; sorting needs focus and choices. The current app conflates them — both end up in the avoided-tasks pile.

The doom-pile method addresses this:

- **Collect** tasks sweep clutter from a room into a shared basket. No decisions. Just speed.
- **Sort** task processes the accumulated basket on its own cadence, with focus-time energy.

Separately, the user has step lists today but can't edit them — they're seed-managed and any in-app change would be wiped on the next launch by the reconciler. Editing in-app is needed both generally and specifically to let the user customize doom-pile text (e.g., picking the real basket staging spot once they choose one).

## Scope

In scope:
- Three new seeded tasks: *Clear lounge into the doom pile*, *Clear bedroom into the doom pile*, *Sort the doom pile*.
- An in-app step editor: add / edit text / edit duration / reorder / delete steps for any task.
- A `stepsEdited` flag on `TaskEntity` so the seed reconciler stops overwriting user-edited steps.

Out of scope:
- A doom-pile data type (`kind = COLLECT | SORT | NORMAL`). Decided against — option A in the brainstorm.
- A "doom-pile" zone (zones aren't introduced yet).
- Editing other task properties (title, cadence, energy, duration, time window).
- Dependency wiring between collect tasks and the sort task.
- Bathroom doom-pile task — explicitly excluded per the user.

## Design

### New tasks

Added to `app/src/main/assets/tasks-list.md` under the `## Weekly` section:

- `Clear lounge into the doom pile`
- `Clear bedroom into the doom pile`
- `Sort the doom pile`

Default categorization (handled by existing `defaultEnergy/Duration/TimeWindow` heuristics, plus targeted additions):

| Task | Cadence | Energy | Duration | Time window |
|---|---|---|---|---|
| Clear lounge into the doom pile | WEEKLY | LOW | SHORT | ANYTIME |
| Clear bedroom into the doom pile | WEEKLY | LOW | SHORT | ANYTIME |
| Sort the doom pile | WEEKLY | MEDIUM | MEDIUM | ANYTIME |

The defaulters in `TaskSeeder.kt` need new cases that match these titles so they don't fall through to `Energy.LOW / Duration.SHORT / TimeWindow.ANYTIME` defaults inappropriately. The lounge/bedroom collects are already covered by the default; the sort task needs explicit MEDIUM energy and MEDIUM duration.

### Seed steps

In `SeedSteps.kt`, register step lists for the three new titles.

**Collect tasks (lounge and bedroom, same shape, room name swapped):**

1. *"Grab the doom-pile basket from its spot."* — no duration.
2. *"5-min sweep: anything not where it lives goes in the basket."* — 300s.
3. *"Park the basket somewhere out of the walkway."* — no duration.

**Sort task:**

1. *"Bring the basket somewhere comfy — couch, table, floor."* — no duration.
2. *"5-min sort. For each thing: where does it LIVE? Put it there or set it on a 'donate / toss' pile."* — 300s.
3. *"5-min sort again — keep going while you've got it."* — 300s. (Two short rounds beats one long round for ADHD focus.)
4. *"Toss / donate / shelve the leftover piles."* — no duration.
5. *"Return the basket to its spot."* — no duration.

Copy notes: the LIVES framing in step 2 of sort is deliberate — reframes the decision as recall ("where does this thing live") rather than a sorting choice. Cleaner-with-ADHD reframe.

### In-app step editing

#### Entry point

A new overflow item / action on `TaskDetailScreen`'s top bar: **"Edit steps."** Tapping it navigates to a new `StepEditorScreen` for that task.

#### `StepEditorScreen`

Layout (top to bottom):

- TopAppBar: title = *"Edit steps — \[task title\]"*, back arrow.
- Scrollable list of step rows, each row showing:
  - Drag handle (long-press → drag to reorder).
  - Content text (tap → enters inline edit mode with a multi-line TextField).
  - Duration display (`≈ 5 min`, `no target`) — tap → opens a small picker (none / 30s / 1m / 2m / 5m / 10m / custom).
  - Delete button (small ✕ on the right). Confirms via snackbar with **undo** (5s window).
- FAB: **"+ Add step"** at bottom-right. Inserts a blank step at the end and immediately focuses its TextField.
- Auto-save: every edit persists immediately. No "Save" button. Back navigation just leaves.

#### Persistence

New DAO methods on `StepDao`:

- `suspend fun update(step: StepEntity)`
- `suspend fun delete(id: Long)`
- `suspend fun reorder(taskId: Long, idsInNewOrder: List<Long>)` — implemented as a transaction that rewrites `order` for each row to match the list index.

Existing `insertAll` covers the add case.

A repository wrapper (`StepRepository`) exposes the same operations, and additionally marks the parent task as `stepsEdited = true` on any mutation.

#### `stepsEdited` flag

`TaskEntity` gains:

```kotlin
val stepsEdited: Boolean = false
```

`TaskSeeder.reconcileSteps` is changed: if `task.stepsEdited == true`, skip reconciliation for that task entirely. Otherwise behaves as today.

New seed tasks that aren't yet in the DB still get their initial steps on first install (this path doesn't go through `reconcileSteps` — they're inserted fresh).

### Database migration

`MIGRATION_4_5`:

```sql
ALTER TABLE tasks ADD COLUMN stepsEdited INTEGER NOT NULL DEFAULT 0
```

`AppDatabase` version bumps to 5. The migration is added to the migrations list.

### Navigation

`MainActivity` adds a route `step_editor/{taskId}`. `TaskDetailScreen` exposes an `onEditSteps: (Long) -> Unit` callback. Wire-up follows existing patterns.

### UI strings & affordances

- The "Edit steps" action uses a tooltip / accessible label of `"Edit steps"`. Visible label: the same.
- Empty state in the editor (task has no steps): a centered hint *"No steps yet. Tap **+ Add step** to start a checklist."* with the FAB still available.

## Testing

- Unit: `TaskSeederTest` — when `stepsEdited = true`, reconciler does not modify steps. When `stepsEdited = false`, reconciler still overwrites mismatches.
- Unit: `StepRepositoryTest` — mutations flip `stepsEdited` to true on the parent.
- Manual UI test plan (not formal, just for the user):
  - Seed installs new tasks; their steps look right.
  - Edit a step on a seed task → restart app → step persists, not overwritten.
  - Add / delete / reorder steps round-trip through the editor.
  - Doom-pile flow from queue: collect feels light, sort feels focused.

## Risks

- **Reconciler skip is permanent.** Once a task is marked `stepsEdited`, future seed updates to its steps never propagate. Acceptable for now (user can reset by deleting + re-adding the task, or via a future "reset steps to default" action — not in this spec).
- **Drag reordering UX.** Compose drag-and-drop for lists is fiddly; if it slips, fall back to up/down chevrons per row.
- **Editing a step's duration affects in-flight focus mode timers.** Out of scope to handle gracefully; user can avoid by not editing while running.

## Files touched

- `app/src/main/assets/tasks-list.md` — add three task lines.
- `app/src/main/java/com/cpotzy/thedecider/data/seed/SeedSteps.kt` — add step lists for new titles.
- `app/src/main/java/com/cpotzy/thedecider/data/seed/TaskSeeder.kt` — handle sort-task defaults; skip reconcile when `stepsEdited`.
- `app/src/main/java/com/cpotzy/thedecider/data/db/entities/TaskEntity.kt` — add `stepsEdited` field.
- `app/src/main/java/com/cpotzy/thedecider/data/db/AppDatabase.kt` — version 5, MIGRATION_4_5.
- `app/src/main/java/com/cpotzy/thedecider/data/db/dao/StepDao.kt` — new mutation methods.
- `app/src/main/java/com/cpotzy/thedecider/data/repo/StepRepository.kt` — wrap mutations, mark `stepsEdited`.
- `app/src/main/java/com/cpotzy/thedecider/data/repo/TaskRepository.kt` — `markStepsEdited(taskId)` helper.
- `app/src/main/java/com/cpotzy/thedecider/ui/task/TaskDetailScreen.kt` — "Edit steps" action.
- `app/src/main/java/com/cpotzy/thedecider/ui/task/StepEditorScreen.kt` (new) — the editor.
- `app/src/main/java/com/cpotzy/thedecider/ui/task/StepEditorViewModel.kt` (new) — state + mutations.
- `app/src/main/java/com/cpotzy/thedecider/MainActivity.kt` — new nav route + AppGraph wiring.
- `app/src/test/java/com/cpotzy/thedecider/data/seed/TaskSeederTest.kt` (extend or new).
- `app/src/test/java/com/cpotzy/thedecider/data/repo/StepRepositoryTest.kt` (new).
