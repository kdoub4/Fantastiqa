package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.redux.actions.PlayerAction
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class BitterBrewTest {

    private val engine = GameEngine()

    private fun createCard(name: String) = Artifact(UUID.randomUUID().toString(), name, 0, Ability.NONE)
    private fun createBitterBrew() = Artifact(UUID.randomUUID().toString(), "Bitter Brew", 0, Ability.BITTER_BREW)

    @Test
    fun `BITTER_BREW from hand should move target from hand to opponent discard`() {
        val bitterBrew = createBitterBrew()
        val targetCard = createCard("Target")
        
        val p1 = Player("P1").copy(hand = listOf(bitterBrew, targetCard))
        val p2 = Player("P2").copy(hand = emptyList())
        
        val state = GameState(
            board = Board(),
            players = listOf(p1, p2),
            currentPlayerIndex = 0,
            selectedCards = listOf(bitterBrew, targetCard),
            gamePhase = GameState.GamePhase.OPEN
        )

        val action = PlayerAction(0, PlayerAction.ActionType.USE_ABILITY, null)
        val newState = engine.reduce(state, action)

        val updatedP1 = newState.players[0]
        val updatedP2 = newState.players[1]

        assertTrue("P1 hand should not have Bitter Brew", updatedP1.hand.none { it.id == bitterBrew.id })
        assertTrue("P1 hand should not have target card", updatedP1.hand.none { it.id == targetCard.id })
        assertTrue("P1 discard should have Bitter Brew", updatedP1.deck.discardPile.any { it.id == bitterBrew.id })
        
        assertTrue("P2 discard should have target card", updatedP2.deck.discardPile.any { it.id == targetCard.id })
    }

    @Test
    fun `BITTER_BREW from hand should move target from discard to opponent discard`() {
        val bitterBrew = createBitterBrew()
        val targetCard = createCard("Target")
        
        val p1 = Player("P1").copy(
            hand = listOf(bitterBrew),
            deck = Deck(discardPile = listOf(targetCard))
        )
        val p2 = Player("P2").copy(hand = emptyList())
        
        val state = GameState(
            board = Board(),
            players = listOf(p1, p2),
            currentPlayerIndex = 0,
            selectedCards = listOf(bitterBrew, targetCard),
            gamePhase = GameState.GamePhase.OPEN
        )

        val action = PlayerAction(0, PlayerAction.ActionType.USE_ABILITY, null)
        val newState = engine.reduce(state, action)

        val updatedP1 = newState.players[0]
        val updatedP2 = newState.players[1]

        assertTrue("P1 hand should not have Bitter Brew", updatedP1.hand.none { it.id == bitterBrew.id })
        assertTrue("P1 discard should not have target card", updatedP1.deck.discardPile.none { it.id == targetCard.id })
        assertTrue("P1 discard should have Bitter Brew", updatedP1.deck.discardPile.any { it.id == bitterBrew.id })
        
        assertTrue("P2 discard should have target card", updatedP2.deck.discardPile.any { it.id == targetCard.id })
    }
}
