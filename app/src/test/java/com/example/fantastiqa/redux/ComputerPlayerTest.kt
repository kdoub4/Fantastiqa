package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.pieces.TowerName
import com.example.fantastiqa.redux.actions.*
import com.example.fantastiqa.redux.middleware.BasicComputerStrategy
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class ComputerPlayerTest {

    private val strategy = BasicComputerStrategy()

    private fun createCard(name: String, ability: Ability = Ability.NONE, value: Symbol = Symbol.NONE): CreatureCard {
        return CreatureCard(UUID.randomUUID().toString(), name, false, listOf(value), Symbol.NONE, ability)
    }

    @Test
    fun `AI should prioritize Dog ability`() {
        val dog = createCard("Dog", Ability.GEM)
        val player = Player(name = "Computer", hand = listOf(dog), isComputer = true)
        val state = GameState(
            board = Board(),
            players = listOf(player),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.PLAYER_TURN
        )

        val action = strategy.evaluateNextAction(state)

        assertTrue(action is CardAction)
        val cardAction = action as CardAction
        assertEquals(CardAction.ActionType.SELECT_CARDS, cardAction.actionType)
        assertEquals(dog.id, cardAction.cards[0].id)
    }

    @Test
    fun `AI should use ability when Dog is selected`() {
        val dog = createCard("Dog", Ability.GEM)
        val player = Player(name = "Computer", hand = listOf(dog), isComputer = true)
        val state = GameState(
            board = Board(),
            players = listOf(player),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.PLAYER_TURN,
            selectedCards = listOf(dog)
        )

        val action = strategy.evaluateNextAction(state)

        assertTrue(action is PlayerAction)
        val playerAction = action as PlayerAction
        assertEquals(PlayerAction.ActionType.USE_ABILITY, playerAction.actionType)
    }

    @Test
    fun `AI should select cards for subduing an adjacent creature`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val hills = Region(RegionName.HILLS, TowerName.QUEST)
        
        // Knight is subdued by WAND
        val knight = CreatureCard(UUID.randomUUID().toString(), "Knight", true, listOf(Symbol.SWORD), Symbol.WAND, Ability.NONE)
        val wandCard = createCard("WandCard", value = Symbol.WAND)
        
        val board = Board().withRoad(forest, hills, Road(knight, true))
        val player = Player(name = "Computer", hand = listOf(wandCard), isComputer = true)
        
        val state = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.PLAYER_TURN
        )

        val action = strategy.evaluateNextAction(state)

        assertTrue(action is CardAction)
        val cardAction = action as CardAction
        assertEquals(wandCard.id, cardAction.cards[0].id)
    }

    @Test
    fun `AI should move after selecting full combo`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val hills = Region(RegionName.HILLS, TowerName.QUEST)
        val knight = CreatureCard(UUID.randomUUID().toString(), "Knight", true, listOf(Symbol.SWORD), Symbol.WAND, Ability.NONE)
        val wandCard = createCard("WandCard", value = Symbol.WAND)
        
        val board = Board().withRoad(forest, hills, Road(knight, true))
        val player = Player(name = "Computer", hand = listOf(wandCard), isComputer = true)
        
        val state = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.PLAYER_TURN,
            selectedCards = listOf(wandCard)
        )

        val action = strategy.evaluateNextAction(state)

        assertTrue(action is MoveAction)
        val moveAction = action as MoveAction
        assertEquals(MoveType.ADJACENT, moveAction.moveType)
        assertEquals(hills.name, moveAction.destination.name)
    }

    @Test
    fun `AI should discard entire hand at turn end`() {
        val p1 = createCard("P1", value = Symbol.FIRE)
        val p2 = createCard("P2", value = Symbol.WATER)
        
        val player = Player(name = "Computer", hand = listOf(p1, p2), isComputer = true)
        
        // Step 1: Select first card
        val state1 = GameState(
            board = Board(),
            players = listOf(player),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.PLAYER_TURN,
            selectedCards = emptyList()
        )
        val action1 = strategy.evaluateNextAction(state1) as CardAction
        assertEquals(p1.id, action1.cards[0].id)

        // Step 2: Select second card
        val state2 = state1.copy(selectedCards = listOf(p1))
        val action2 = strategy.evaluateNextAction(state2) as CardAction
        assertEquals(p2.id, action2.cards[0].id)

        // Step 3: End turn
        val state3 = state2.copy(selectedCards = listOf(p1, p2))
        val action3 = strategy.evaluateNextAction(state3)
        assertTrue(action3 is TurnAction)
        assertEquals(TurnAction.ActionType.NEXT_TURN, (action3 as TurnAction).actionType)
    }
}
