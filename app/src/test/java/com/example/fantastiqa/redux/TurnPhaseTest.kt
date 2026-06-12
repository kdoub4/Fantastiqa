package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.pieces.TowerName
import com.example.fantastiqa.redux.actions.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TurnPhaseTest {

    private val engine = GameEngine()

    private fun createInitialState(): GameState {
        val startRegion = Region(RegionName.FOREST, TowerName.QUEST)
        val endRegion = Region(RegionName.HIGHLANDS, TowerName.BAZAAR)
        val creature = CreatureCard("C1", "C1", false, listOf(Symbol.FIRE), Symbol.FIRE, Ability.NONE)
        val road = Road(creature, false)
        val board = Board().withRoad(startRegion, endRegion, road)

        val player = Player(name = "P1", hand = listOf(
            CreatureCard("H1", "H1", false, listOf(Symbol.FIRE), Symbol.FIRE, Ability.GEM)
        ))
        
        return GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to startRegion),
            gamePhase = GameState.GamePhase.OPEN,
            creatureDeck = Deck(emptyList()),
            questDeck = Deck(emptyList())
        )
    }

    @Test
    fun `START phase should auto-refill and move to OPEN`() {
        val state = createInitialState().copy(gamePhase = GameState.GamePhase.START)
        
        // Refill logic is triggered by any action or we can just call reduce with a dummy action
        // Actually, in our implementation, reduce calls processAutomatedTransitions at the end.
        // So we need an action to trigger it.
        val action = TurnAction(TurnAction.ActionType.ADVANCE_PHASE) 
        
        val newState = engine.reduce(state, action)
        
        assertEquals(GameState.GamePhase.OPEN, newState.gamePhase)
    }

    @Test
    fun `Subdue in OPEN should move to SUBDUE phase`() {
        val state = createInitialState()
        val player = state.players[0]
        val road = state.board!!.getRoad(state.playerPositions[player.name]!!, state.board!!.regions()[1])!!
        
        // Select cards first
        val stateWithSelection = state.copy(selectedCards = listOf(player.hand[0]))
        
        val action = MoveAction(0, state.board!!.regions()[1], MoveType.ADJACENT, false)
        
        val newState = engine.reduce(stateWithSelection, action)
        
        assertEquals(GameState.GamePhase.SUBDUE, newState.gamePhase)
    }

    @Test
    fun `Discard (Free Action) should be disabled in SUBDUE phase`() {
        val state = createInitialState().copy(gamePhase = GameState.GamePhase.SUBDUE)
        val card = state.players[0].hand[0]
        
        // Try Discard (Free Action)
        val discardAction = PlayerAction(0, PlayerAction.ActionType.DISCARD_FROM_HAND, listOf(card))
        val newState = engine.reduce(state, discardAction)
        
        assertEquals("Discard action should be ignored in SUBDUE phase", state, newState)
    }

    @Test
    fun `DONE_ADVENTURING should move from SUBDUE to DISCARD_OPEN`() {
        val state = createInitialState().copy(gamePhase = GameState.GamePhase.SUBDUE)
        val action = TurnAction(TurnAction.ActionType.DONE_ADVENTURING)
        
        val newState = engine.reduce(state, action)
        
        assertEquals(GameState.GamePhase.DISCARD_OPEN, newState.gamePhase)
    }

    @Test
    fun `NEXT_TURN should move to DRAW then NEXT_TURN then START then OPEN`() {
        // This tests the chain of automated transitions:
        // NEXT_TURN action -> phase = DRAW
        // DRAW phase -> handleDrawPhase -> phase = NEXT_TURN
        // NEXT_TURN phase -> handleAdvancePlayer -> phase = START
        // START phase -> handleStartPhase -> phase = OPEN
        
        val state = createInitialState().copy(gamePhase = GameState.GamePhase.DISCARD_OPEN)
        val action = TurnAction(TurnAction.ActionType.NEXT_TURN)
        
        val newState = engine.reduce(state, action)
        
        assertEquals(GameState.GamePhase.OPEN, newState.gamePhase)
        // Verify player index reset (if multiple players, but here it's 0 -> 0)
        assertEquals(0, newState.currentPlayerIndex)
    }

    @Test
    fun `PLUS_CARD selection should be allowed in TOWER phase`() {
        val plusCard = CreatureCard("P1", "P1", false, listOf(Symbol.FIRE), Symbol.FIRE, Ability.PLUS_CARD)
        val state = createInitialState().copy(
            gamePhase = GameState.GamePhase.TOWER,
            players = listOf(Player(name = "P1", hand = listOf(plusCard)))
        )
        
        val selectAction = CardAction(CardAction.ActionType.SELECT_CARDS, 0, listOf(plusCard))
        val newState = engine.reduce(state, selectAction)
        
        assertNotEquals("Selection should have changed", state, newState)
        assertEquals(1, newState.selectedCards.size)
    }

    @Test
    fun `Card selection should be allowed in SUBDUE phase`() {
        val state = createInitialState().copy(gamePhase = GameState.GamePhase.SUBDUE)
        val card = state.players[0].hand[0]
        
        val selectAction = CardAction(CardAction.ActionType.SELECT_CARDS, 0, listOf(card))
        val newState = engine.reduce(state, selectAction)
        
        assertNotEquals("Selection should have changed", state, newState)
        assertEquals(1, newState.selectedCards.size)
    }

    @Test
    fun `RESOLVE_TOWER_DRAW should move to DISCARD_OPEN phase`() {
        val state = createInitialState().copy(
            gamePhase = GameState.GamePhase.TOWER,
            towerDrawnCards = listOf(CreatureCard("D1", "D1", false, listOf(Symbol.FIRE), Symbol.FIRE, Ability.NONE))
        )
        
        val action = PlayerAction(0, PlayerAction.ActionType.RESOLVE_TOWER_DRAW, emptyList<Card>())
        val newState = engine.reduce(state, action)
        
        assertEquals(GameState.GamePhase.DISCARD_OPEN, newState.gamePhase)
    }
}
