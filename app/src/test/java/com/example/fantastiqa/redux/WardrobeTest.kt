package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.pieces.TowerName
import com.example.fantastiqa.redux.actions.PlayerAction
import com.example.fantastiqa.redux.actions.SubdueAction
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class WardrobeTest {

    private val engine = GameEngine()

    private fun createWardrobe() = Artifact(UUID.randomUUID().toString(), "Wardrobe", 0, Ability.WARDROBE)
    private fun createCreature(name: String) = CreatureCard(UUID.randomUUID().toString(), name, false, listOf(Symbol.WAND), Symbol.SWORD, Ability.NONE)

    @Test
    fun `WARDROBE ability should trigger WARDROBE phase and swap creatures on 2 roads`() {
        // Arrange
        val wardrobe = createWardrobe()
        val c1 = createCreature("Creature 1")
        val c2 = createCreature("Creature 2")

        val r1 = Region(RegionName.FOREST, TowerName.QUEST)
        val r2 = Region(RegionName.HIGHLANDS, TowerName.BAZAAR)
        val r3 = Region(RegionName.TUNDRA, TowerName.ARTIFACT)
        
        val road1 = Road(creature = c1)
        val road2 = Road(creature = c2)

        var board = Board().withRoad(r1, r2, road1)
        board = board.withRoad(r2, r3, road2)

        val player = Player("P1").copy(hand = listOf(wardrobe))
        val state = GameState(
            board = board,
            players = listOf(player),
            currentPlayerIndex = 0,
            selectedCards = listOf(wardrobe),
            gamePhase = GameState.GamePhase.OPEN
        )

        // Act 1: Use Ability
        val action1 = PlayerAction(0, PlayerAction.ActionType.USE_ABILITY, null)
        val stateAfterAbility = engine.reduce(state, action1)

        assertEquals("Phase should be WARDROBE", GameState.GamePhase.WARDROBE, stateAfterAbility.gamePhase)
        assert(stateAfterAbility.players[0].deck.discardPile.any { it.id == wardrobe.id }) { "Wardrobe should be in discard" }

        // Act 2: Select first road
        val action2 = SubdueAction(SubdueAction.ActionType.SELECT_ROAD, 0, road1, null, null)
        val stateAfterRoad1 = engine.reduce(stateAfterAbility, action2)

        assertEquals(1, stateAfterRoad1.selectedRoads.size)
        assertEquals(road1.id, stateAfterRoad1.selectedRoads[0].id)
        assertEquals(GameState.GamePhase.WARDROBE, stateAfterRoad1.gamePhase)

        // Act 3: Select second road
        val action3 = SubdueAction(SubdueAction.ActionType.SELECT_ROAD, 0, road2, null, null)
        val stateFinal = engine.reduce(stateAfterRoad1, action3)

        // Assert
        assertEquals("Phase should return to OPEN", GameState.GamePhase.OPEN, stateFinal.gamePhase)
        assertEquals(0, stateFinal.selectedRoads.size)

        val updatedRoad1 = stateFinal.board?.getRoad(r1, r2)
        val updatedRoad2 = stateFinal.board?.getRoad(r2, r3)

        assertEquals("Road 1 should now have Creature 2", c2.id, updatedRoad1?.creature?.id)
        assertEquals("Road 2 should now have Creature 1", c1.id, updatedRoad2?.creature?.id)
    }
}
