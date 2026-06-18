package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.redux.actions.PlayerAction
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class RoguesPurseTest {

    private val engine = GameEngine()

    private fun createRoguesPurse() = Artifact(UUID.randomUUID().toString(), "Rogues Purse", 0, Ability.ROGUES_PURSE)

    @Test
    fun `ROGUES_PURSE should take 1 gem from each opponent with more gems`() {
        // Arrange
        val roguesPurse = createRoguesPurse()
        val p1 = Player("P1").copy(hand = listOf(roguesPurse), gems = 2) // Current player
        val p2 = Player("P2").copy(gems = 5) // More gems (+1)
        val p3 = Player("P3").copy(gems = 2) // Equal gems (0)
        val p4 = Player("P4").copy(gems = 1) // Fewer gems (0)
        val p5 = Player("P5").copy(gems = 10) // More gems (+1)
        
        val state = GameState(
            board = Board(),
            players = listOf(p1, p2, p3, p4, p5),
            currentPlayerIndex = 0,
            selectedCards = listOf(roguesPurse),
            gamePhase = GameState.GamePhase.OPEN
        )

        val action = PlayerAction(0, PlayerAction.ActionType.USE_ABILITY, null)

        // Act
        val newState = engine.reduce(state, action)

        // Assert
        val up1 = newState.players[0]
        val up2 = newState.players[1]
        val up3 = newState.players[2]
        val up4 = newState.players[3]
        val up5 = newState.players[4]

        assertEquals("P1 should have 4 gems (2 initial + 2 gained)", 4, up1.gems)
        assertEquals("P2 should have 4 gems (5 - 1)", 4, up2.gems)
        assertEquals("P3 should have 2 gems (equal, no change)", 2, up3.gems)
        assertEquals("P4 should have 1 gem (fewer, no change)", 1, up4.gems)
        assertEquals("P5 should have 9 gems (10 - 1)", 9, up5.gems)
        
        assert(up1.deck.discardPile.any { it.id == roguesPurse.id }) { "Rogues Purse should be discarded" }
    }

    @Test
    fun `ROGUES_PURSE evaluation uses initial gem count`() {
        // This test ensures that if P1 gains a gem from P2, they still gain from P3 
        // even if P1's current gems now equal or exceed P3's.
        
        val roguesPurse = createRoguesPurse()
        val p1 = Player("P1").copy(hand = listOf(roguesPurse), gems = 2)
        val p2 = Player("P2").copy(gems = 3) // More than 2
        val p3 = Player("P3").copy(gems = 3) // More than 2
        
        val state = GameState(
            board = Board(),
            players = listOf(p1, p2, p3),
            currentPlayerIndex = 0,
            selectedCards = listOf(roguesPurse),
            gamePhase = GameState.GamePhase.OPEN
        )

        val action = PlayerAction(0, PlayerAction.ActionType.USE_ABILITY, null)
        val newState = engine.reduce(state, action)

        val up1 = newState.players[0]
        assertEquals("P1 should gain from both because both had more than initial 2", 4, up1.gems)
    }
}
