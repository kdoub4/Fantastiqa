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
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val dog = createCard("Dog", Ability.GEM)
        val player = Player(name = "Computer", hand = listOf(dog), isComputer = true)
        val state = GameState(
            board = Board(),
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.OPEN
        )

        val action = strategy.evaluateNextAction(state)

        assertTrue(action is CardAction)
        val cardAction = action as CardAction
        assertEquals(CardAction.ActionType.SELECT_CARDS, cardAction.actionType)
        assertEquals(dog.id, cardAction.cards[0].id)
    }

    @Test
    fun `AI should use ability when Dog is selected`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val dog = createCard("Dog", Ability.GEM)
        val player = Player(name = "Computer", hand = listOf(dog), isComputer = true)
        val state = GameState(
            board = Board(),
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.OPEN,
            selectedCards = listOf(dog)
        )

        val action = strategy.evaluateNextAction(state)

        assertTrue(action is PlayerAction)
        val playerAction = action as PlayerAction
        assertEquals(PlayerAction.ActionType.USE_ABILITY, playerAction.actionType)
    }

    @Test
    fun `AI should select road for subduing an adjacent creature`() {
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
            gamePhase = GameState.GamePhase.OPEN
        )

        val action = strategy.evaluateNextAction(state)
        assertNotNull("Action should not be null", action)

        assertTrue("Should return SubdueAction to select road", action is SubdueAction)
        val subdueAction = action as SubdueAction
        assertEquals(SubdueAction.ActionType.SELECT_ROAD, subdueAction.actionType)
    }

    @Test
    fun `AI should select cards after selecting road`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val hills = Region(RegionName.HILLS, TowerName.QUEST)
        val knight = CreatureCard(UUID.randomUUID().toString(), "Knight", true, listOf(Symbol.SWORD), Symbol.WAND, Ability.NONE)
        val wandCard = createCard("WandCard", value = Symbol.WAND)
        
        val road = Road(knight, true)
        val board = Board().withRoad(forest, hills, road)
        val player = Player(name = "Computer", hand = listOf(wandCard), isComputer = true)
        
        val state = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.OPEN,
            selectedRoad = road
        )

        val action = strategy.evaluateNextAction(state)
        assertNotNull("Action should not be null", action)

        assertTrue("Should return CardAction to select cards", action is CardAction)
        val cardAction = action as CardAction
        assertEquals(wandCard.id, cardAction.cards[0].id)
    }

    @Test
    fun `AI should move after selecting road and combo`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val hills = Region(RegionName.HILLS, TowerName.QUEST)
        val knight = CreatureCard(UUID.randomUUID().toString(), "Knight", true, listOf(Symbol.SWORD), Symbol.WAND, Ability.NONE)
        val wandCard = createCard("WandCard", value = Symbol.WAND)
        
        val road = Road(knight, true)
        val board = Board().withRoad(forest, hills, road)
        val player = Player(name = "Computer", hand = listOf(wandCard), isComputer = true)
        
        val state = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.OPEN,
            selectedRoad = road,
            selectedCards = listOf(wandCard)
        )

        val action = strategy.evaluateNextAction(state)
        assertNotNull("Action should not be null", action)

        assertTrue("Should return MoveAction", action is MoveAction)
        val moveAction = action as MoveAction
        assertEquals(MoveType.ADJACENT, moveAction.moveType)
        assertEquals(hills.name, moveAction.destination.name)
    }

    @Test
    fun `AI should discard entire hand at turn end`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val p1 = createCard("P1", value = Symbol.FIRE)
        val p2 = createCard("P2", value = Symbol.WATER)
        
        val player = Player(name = "Computer", hand = listOf(p1, p2), isComputer = true)
        
        // Step 1: Select first card
        val state1 = GameState(
            board = Board(),
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.OPEN,
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

    @Test
    fun `AI should use Magic Carpet card to move towards quest if it can reach it`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val hills = Region(RegionName.HILLS, TowerName.BAZAAR)
        val mountains = Region(RegionName.TUNDRA, TowerName.ARTIFACT)
        
        // Road from forest to hills, and hills to mountains
        val board = Board()
            .withRoad(forest, hills, Road())
            .withRoad(hills, mountains, Road())

        val carpetCard = createCard("Witch", Ability.MAGIC_CARPET)
        val quest = Quest(UUID.randomUUID().toString(), "Mountain Quest", "Mountain Quest", 1, 3, Symbol.NONE, Symbol.NONE, RegionName.TUNDRA)
        
        val player = Player(name = "Computer", hand = listOf(carpetCard), isComputer = true)
        
        // Initial state: at forest, target is mountains
        val state = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.OPEN,
            selectedQuest = quest
        )

        // 1. Should select Magic Carpet card
        val action1 = strategy.evaluateNextAction(state)
        assertTrue("Should select Magic Carpet card", action1 is CardAction)
        assertEquals(carpetCard.id, (action1 as CardAction).cards[0].id)

        // 2. Should use MoveAction with ability
        val state2 = state.copy(selectedCards = listOf(carpetCard))
        val action2 = strategy.evaluateNextAction(state2)
        assertTrue("Should use MoveAction with ability", action2 is MoveAction)
        val moveAction = action2 as MoveAction
        assertTrue(moveAction.useAbility)
        assertEquals(hills.name, moveAction.destination.name)
    }

    @Test
    fun `AI should use Flying Carpet token to move towards quest if it can reach it`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val hills = Region(RegionName.HILLS, TowerName.BAZAAR)
        
        val board = Board().withRoad(forest, hills, Road())
        val quest = Quest(UUID.randomUUID().toString(), "Hills Quest", "Hills Quest", 1, 3, Symbol.NONE, Symbol.NONE, RegionName.HILLS)
        
        val player = Player(name = "Computer", hand = emptyList(), flyingCarpets = 1, isComputer = true)
        
        val state = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.OPEN,
            selectedQuest = quest
        )

        val action = strategy.evaluateNextAction(state)
        assertTrue("Should use MoveAction with flying carpet token", action is MoveAction)
        val moveAction = action as MoveAction
        assertEquals(MoveType.FLYING_CARPET, moveAction.moveType)
        assertEquals(hills.name, moveAction.destination.name)
    }

    @Test
    fun `AI should use Tower Key card to teleport if it helps reaching quest`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val wetlands = Region(RegionName.WETLANDS, TowerName.QUEST) // Same tower type
        
        val board = Board()
            .withRoad(forest, Region(RegionName.HILLS, TowerName.BAZAAR), Road()) // Distant
            .withRoad(wetlands, Region(RegionName.TUNDRA, TowerName.ARTIFACT), Road())
        
        val keyCard = createCard("Rabbits", Ability.TOWER_KEY)
        val quest = Quest(UUID.randomUUID().toString(), "Wetlands Quest", "Wetlands Quest", 1, 3, Symbol.NONE, Symbol.NONE, RegionName.WETLANDS)
        
        val player = Player(name = "Computer", hand = listOf(keyCard), isComputer = true)
        
        val state = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.OPEN,
            selectedQuest = quest
        )

        // 1. Should select Tower Key card
        val action1 = strategy.evaluateNextAction(state)
        assertTrue("Should select Tower Key card", action1 is CardAction)
        assertEquals(keyCard.id, (action1 as CardAction).cards[0].id)

        // 2. Should use USE_ABILITY
        val state2 = state.copy(selectedCards = listOf(keyCard))
        val action2 = strategy.evaluateNextAction(state2)
        assertTrue("Should use ability", action2 is PlayerAction)
        assertEquals(PlayerAction.ActionType.USE_ABILITY, (action2 as PlayerAction).actionType)

        // 3. Should move to destination via TOWER_KEY
        val state3 = state2.copy(isFreeTowerAction = true)
        val action3 = strategy.evaluateNextAction(state3)
        assertTrue("Should use MoveAction TOWER_KEY", action3 is MoveAction)
        assertEquals(MoveType.TOWER_KEY, (action3 as MoveAction).moveType)
        assertEquals(wetlands.name, action3.destination.name)
    }

    @Test
    fun `AI should use turn action if target is not reachable via free actions alone`() {
        val forest = Region(RegionName.FOREST, TowerName.QUEST)
        val hills = Region(RegionName.HILLS, TowerName.QUEST)
        
        // Road requires subduing knight
        val knight = CreatureCard(UUID.randomUUID().toString(), "Knight", true, listOf(Symbol.SWORD), Symbol.WAND, Ability.NONE)
        val wandCard = createCard("WandCard", value = Symbol.WAND)
        // Road requires subduing knight, and it's 2 steps away
        val board = Board()
            .withRoad(forest, Region(RegionName.WETLANDS, TowerName.QUEST), Road(knight))
            .withRoad(Region(RegionName.WETLANDS, TowerName.QUEST), hills, Road(knight))
        val quest = Quest(UUID.randomUUID().toString(), "Hills Quest", "Hills Quest", 1, 3, Symbol.NONE, Symbol.NONE, RegionName.HILLS)
        
        val player = Player(name = "Computer", hand = listOf(wandCard), gems = 2, isComputer = true)
        
        val state = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to forest),
            currentPlayerIndex = 0,
            gamePhase = GameState.GamePhase.OPEN,
            selectedQuest = quest
        )

        val action = strategy.evaluateNextAction(state)
        assertTrue("Should select road for subduing (turn action)", action is SubdueAction)
        assertEquals(SubdueAction.ActionType.SELECT_ROAD, (action as SubdueAction).actionType)
    }
}
