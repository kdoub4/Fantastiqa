# Redux Migration Guide: Logic Gaps

This document tracks the gameplay logic currently residing in the legacy `Game.java` that must be ported to `GameEngine.kt` to complete the Redux migration.

## 1. Quest Logic
**Status:** Missing in Engine
**Source:** `Game.java` (lines 135-212)

### Required Porting:
- [ ] **`canCompleteQuest(Quest, List<Card>)`**: Pure logic to verify if a set of cards satisfies the double/triple symbol requirements.
- [ ] **`completeQuest(Quest, List<Card>)`**: Logic to identify and remove the specific cards used for a quest.
- [ ] **`SortBySymbolCount`**: Implementation of the symbol-weighting comparator for requirement validation.
- [ ] **Storage Integration**: Update `QuestAction.STORE_CARD_FOR_QUEST` to immutably move cards from `Player.hand` to `Quest.stored`.

## 2. Combat & Subduing
**Status:** Missing in Engine
**Source:** `Game.java` (lines 222-340)

### Required Porting:
- [ ] **`canSubdueSingle(CreatureCard, List<Card>)`**: Logic to find valid subduing combinations for single-symbol creatures.
- [ ] **`canSubdueDouble(CreatureCard, List<Card>)`**: Logic for subduing creatures with two requirements.
- [ ] **Wildcard Logic**: Implementation of the "Double Symbol = Wildcard" rule during combat.
- [ ] **Combination Support**: Implementation of a pure Kotlin version of `org.apache.commons.math3.util.Combinations`.

## 3. Player Actions
**Status:** Stubbed in Engine
**Source:** `Player.java` / `Game.java`

### Required Porting:
- [ ] **`handleDrawCards`**: Immutably move $N$ cards from `Player.deck` to `Player.hand`.
- [ ] **`handleUseFlyingCarpet`**: Decrement `Player.flyingCarpets` and update state.
- [ ] **`handleGainTrophies`**: Update `Player.trophies` count.
- [ ] **`handleUseShuffleToken`**: Logic to manually trigger `Player.deck.shuffle(true)` via action.

## 4. Card Selection & "The Choice"
**Status:** Stubbed in Engine
**Source:** `Game.java` (Commented lines 105-133)

### Required Porting:
- [ ] **`handleSelectCards`**: 
    1. Draw $X$ cards from a target deck.
    2. Move them into `GameState.selectedCards`.
    3. Transition `GamePhase` to `CARD_SELECTION`.
- [ ] **Resolution Logic**: Handle the player's choice, add the kept card to hand/discard, and return others to the proper deck.

## 5. Game Constraints
- [ ] **Victory Check**: Port `VPgoal` check to `handleTurnAction` to set `isGameOver = true`.
- [ ] **Deck Depletion**: Add logic to handle cases where decks are completely empty during a refill.

---

### Porting Instructions:
1. All logic in `GameEngine.kt` must be **pure**. 
2. Never mutate parameters; always return `state.copy(...)`.
3. Use the helper methods in `Board`, `Player`, and `Deck` created during the immutability refactor.
