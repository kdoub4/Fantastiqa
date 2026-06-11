package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.redux.actions.TurnAction
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class WinConditionTest {

    private val engine = GameEngine()

    @Test
    fun `game should end when a player reaches 25 cards`() {
        val manyCards = List(25) { CreatureCard(UUID.randomUUID().toString(), "Card", false, emptyList(), Symbol.NONE, Ability.NONE) }
        val player = Player(name = "Winner", hand = manyCards)
        
        val state = GameState(
            board = Board(),
            players = listOf(player),
            currentPlayerIndex = 0,
            isGameOver = false
        )

        // Dispatch any action to trigger win check
        val action = TurnAction(TurnAction.ActionType.ADVANCE_PHASE)
        val newState = engine.reduce(state, action)

        assertTrue("Game should be over", newState.isGameOver)
    }

    @Test
    fun `game should not end when a player has less than 25 cards`() {
        val fewCards = List(24) { CreatureCard(UUID.randomUUID().toString(), "Card", false, emptyList(), Symbol.NONE, Ability.NONE) }
        val player = Player(name = "NotWinnerYet", hand = fewCards)
        
        val state = GameState(
            board = Board(),
            players = listOf(player),
            currentPlayerIndex = 0,
            isGameOver = false
        )

        val action = TurnAction(TurnAction.ActionType.ADVANCE_PHASE)
        val newState = engine.reduce(state, action)

        assertFalse("Game should NOT be over", newState.isGameOver)
    }
}
