package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.pieces.TowerName
import com.example.fantastiqa.redux.actions.MoveAction
import com.example.fantastiqa.redux.actions.MoveType
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
}