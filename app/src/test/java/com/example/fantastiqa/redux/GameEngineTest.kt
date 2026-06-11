package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.pieces.TowerName
import com.example.fantastiqa.redux.actions.MoveAction
import com.example.fantastiqa.redux.actions.MoveType
import com.example.fantastiqa.redux.actions.PlayerAction
import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineTest {

    private val engine = GameEngine()

    @Test
    fun `handleMovePlayer with FLYING_CARPET should move and consume carpet when road exists`() {
        // Arrange
        val startRegion = Region(RegionName.FOREST, TowerName.QUEST)
        val endRegion = Region(RegionName.HIGHLANDS, TowerName.BAZAAR)
        val board = Board().withRoad(startRegion, endRegion, Road())

        val player = Player(name = "Adventurer", flyingCarpets = 3)
        val initialState = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to startRegion)
        )

        val action = MoveAction(
            playerIndex = 0,
            destination = endRegion,
            moveType = MoveType.FLYING_CARPET
        )

        // Act
        val newState = engine.reduce(initialState, action)

        // Assert
        assertEquals(
            "Player should have moved to the destination",
            endRegion, newState.playerPositions[player.name]
        )
        assertEquals(
            "Player should have 2 carpets left",
            2, newState.players[0].flyingCarpets
        )
    }

    @Test
    fun `handleMovePlayer with FLYING_CARPET should fail if no road exists`() {
        // Arrange
        val startRegion = Region(RegionName.FOREST, TowerName.QUEST)
        val endRegion = Region(RegionName.HIGHLANDS, TowerName.BAZAAR)
        val board = Board() // No road

        val player = Player(name = "Adventurer", flyingCarpets = 3)
        val initialState = GameState(
            board = board,
            players = listOf(player),
            playerPositions = mapOf(player.name to startRegion)
        )

        val action = MoveAction(0, endRegion, MoveType.FLYING_CARPET)

        // Act
        val newState = engine.reduce(initialState, action)

        // Assert
        assertEquals(
            "Player should NOT have moved without a road",
            startRegion, newState.playerPositions[player.name]
        )
    }

    @Test
    fun `handleStartTowerDraw with PLUS_CARD should draw 4 cards and discard used creature`() {
        // Arrange
        val region = Region(RegionName.FOREST, TowerName.QUEST)
        val plusCard = CreatureCard("FenFairy", Symbol.FIRE, false, Ability.PLUS_CARD, Symbol.WATER)
        val player = Player(name = "P1", hand = listOf(plusCard))
        
        val quest1 = Quest("Q1", 1, 1, Symbol.FIRE, Symbol.NONE, RegionName.FOREST)
        val quest2 = Quest("Q2", 1, 1, Symbol.FIRE, Symbol.NONE, RegionName.FOREST)
        val quest3 = Quest("Q3", 1, 1, Symbol.FIRE, Symbol.NONE, RegionName.FOREST)
        val quest4 = Quest("Q4", 1, 1, Symbol.FIRE, Symbol.NONE, RegionName.FOREST)
        val quest5 = Quest("Q5", 1, 1, Symbol.FIRE, Symbol.NONE, RegionName.FOREST)
        val questDeck = Deck(listOf(quest1, quest2, quest3, quest4, quest5))

        val initialState = GameState(
            board = Board(),
            players = listOf(player),
            playerPositions = mapOf(player.name to region),
            questDeck = questDeck,
            selectedCards = listOf(plusCard)
        )

        val action = PlayerAction(0, PlayerAction.ActionType.START_TOWER_DRAW, null)

        // Act
        val newState = engine.reduce(initialState, action)

        // Assert
        assertEquals("Should draw 4 cards (3 + 1 plus card)", 4, newState.towerDrawnCards.size)
        assertEquals("Hand should be empty after discard", 0, newState.players[0].hand.size)
        assertEquals("FenFairy should be in discard pile", 1, newState.players[0].deck.discardSize())
        assertEquals("Selection should be cleared", 0, newState.selectedCards.size)
    }
}
