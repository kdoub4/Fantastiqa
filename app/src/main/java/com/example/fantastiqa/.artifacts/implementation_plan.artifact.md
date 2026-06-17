# Implement TOWER_KEY Ability

This plan adds support for the `TOWER_KEY` ability in `GameEngine`. When used, it opens the Tower menu as a free action, allowing the player to perform a Tower action (Draw, Release, or Teleport) without advancing the turn phase (i.e., they can still perform a Turn Action afterwards).

## Proposed Changes

### [Redux State & Actions]

We need to track the Tower menu state and the "free action" status in the Redux state, and add an action to toggle the menu.

#### [GameState.kt](file:///C:/Users/info/StudioProjects/Fantastiqa/app/src/main/java/com/example/fantastiqa/redux/GameState.kt)

- Add `towerMenuOpen: Boolean = false` to `GameState` data class.
- Add `isFreeTowerAction: Boolean = false` to `GameState` data class.

#### [PlayerAction.java](file:///C:/Users/info/StudioProjects/Fantastiqa/app/src/main/java/com/example/fantastiqa/redux/actions/PlayerAction.java)

- Add `SET_TOWER_MENU` to `ActionType` enum.

---

### [Game Engine]

The core logic to handle the ability and its consequences.

#### [GameEngine.kt](file:///C:/Users/info/StudioProjects/Fantastiqa/app/src/main/java/com/example/fantastiqa/redux/GameEngine.kt)

- **`handlePlayerAction`**: Add case for `SET_TOWER_MENU` to update `state.towerMenuOpen`.
- **`handleUseAbility`**: Add case for `Ability.TOWER_KEY`:
    - Discard the source card.
    - Set `towerMenuOpen = true`.
    - Set `isFreeTowerAction = true`.
- **`handleStartTowerDraw`**:
    - Set `towerMenuOpen = false`.
- **`handleResolveTowerDraw`**:
    - If `isFreeTowerAction` is true, set `gamePhase` to the previous phase (or `OPEN`) instead of `DISCARD_OPEN`.
    - Set `isFreeTowerAction = false`.
    - Set `towerMenuOpen = false`.
- **`handleMovePlayer`**:
    - For `MoveType.TOWER_KEY`, if `isFreeTowerAction` is true, set `nextPhase` to current phase instead of `DISCARD_OPEN`.
    - Set `isFreeTowerAction = false`.
    - Set `towerMenuOpen = false`.
- **`handleReleaseCards`**:
    - Set `isFreeTowerAction = false`.
    - Set `towerMenuOpen = false`.

---

### [UI]

Connect the UI state to the Redux state.

#### [MainActivity.kt](file:///C:/Users/info/StudioProjects/Fantastiqa/app/src/main/java/com/example/fantastiqa/MainActivity.kt)

- In `BoardGameContent`, replace local `towerMenuOpen` state with `state.towerMenuOpen`.
- Update `onTowerMenuToggle` to dispatch `PlayerAction.ActionType.SET_TOWER_MENU`.
- Remove manual `towerMenuOpen = false` in `TowerDrawDialog`'s `onDone`, as it will be handled by the engine.

---

## Verification Plan

### Automated Tests
- Run `GameEngineTest.kt` to ensure no regressions.
- Add a new test case in `GameEngineTest.kt` for `TOWER_KEY` ability:
    - Verify using `TOWER_KEY` card discards it and sets `towerMenuOpen = true`.
    - Verify subsequent `START_TOWER_DRAW` -> `RESOLVE_TOWER_DRAW` returns phase to `OPEN`.
    - Verify subsequent `MoveAction(TOWER_KEY)` returns phase to `OPEN`.

### Manual Verification
- Deploy the app.
- Select a card with `TOWER_KEY` ability (if none in hand, use a debug tool or modify initializer).
- Click "Ability".
- Verify Tower menu opens.
- Click "Draw", select cards, click "Done".
- Verify "End Turn" button is still green (OPEN phase), not blue (DISCARD_OPEN phase).
