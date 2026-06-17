package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.CreatureCards
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
            playerPositions = mapOf(player.name to startRegion),
            gamePhase = GameState.GamePhase.OPEN
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
            playerPositions = mapOf(player.name to startRegion),
            gamePhase = GameState.GamePhase.OPEN
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
    fun `canSubdueDouble test cases`() {
        val bear = CreatureCard("Bear", "Bear", true, listOf(Symbol.BAT, Symbol.BAT), Symbol.HELMET, Ability.TOWER_KEY)
        
        // Input : Bear, Satyr Output : Satyr
        val satyr = CreatureCard("Satyr", "Satyr", true, listOf(Symbol.HELMET, Symbol.HELMET), Symbol.NET, Ability.PLUS_CARD)
        val res1 = engine.canSubdueDouble(bear, listOf(satyr))
        assertEquals("double matching", 1, res1.size)
        assert(res1[0].contains(satyr))

        // Input : Bear, Dragon Output : Empty list
        val dragon = CreatureCard("Dragon", "Dragon", true, listOf(Symbol.FIRE, Symbol.FIRE), Symbol.SWORD, Ability.TOWER_KEY)
        val res2 = engine.canSubdueDouble(bear, listOf(dragon))
        assertEquals("double non matching", 0, res2.size)

        // Input : Bear, <Dragon, Dragon> Output : Dragon, Dragon
        val dragon2 = CreatureCard("Dragon2", "Dragon", true, listOf(Symbol.FIRE, Symbol.FIRE), Symbol.SWORD, Ability.TOWER_KEY)
        val res2a = engine.canSubdueDouble(bear, listOf(dragon, dragon2))
        assertEquals("2 doubles counting as wilds", 1, res2a.size)
        assert(res2a[0].contains(dragon) && res2a[0].contains(dragon2))

        // Input : Bear, <BillyGoat, Billygoat> Output : <Billygoat, Billygoat>
        val bg1 = CreatureCard("BG1", "BillyGoat", true, listOf(Symbol.HELMET), Symbol.NET, Ability.NONE)
        val bg2 = CreatureCard("BG2", "BillyGoat", true, listOf(Symbol.HELMET), Symbol.NET, Ability.NONE)
        val res3 = engine.canSubdueDouble(bear, listOf(bg1, bg2))
        assertEquals("2 matching singles", 1, res3.size)
        assert(res3[0].contains(bg1) && res3[0].contains(bg2))

        // Input Bear, <Billygoat, Troll> Output: Empty list
        val troll = CreatureCard("Troll", "Troll", true, listOf(Symbol.BAT), Symbol.HELMET, Ability.NONE)
        val res4 = engine.canSubdueDouble(bear, listOf(bg1, troll))
        assertEquals("2 singles, 1 match",0, res4.size)

        // Input Bear, <Rabbits, Rabbits, Billygoat> Output <Rabbits, Rabbits, Billygoat>
        val r1 = CreatureCard("R1", "Rabbits", false, listOf(Symbol.TOOTH), Symbol.BROOM, Ability.TOWER_KEY)
        val r2 = CreatureCard("R2", "Rabbits", false, listOf(Symbol.TOOTH), Symbol.BROOM, Ability.TOWER_KEY)
        val res5 = engine.canSubdueDouble(bear, listOf(r1, r2, bg1))
        assertEquals("3 singles, 1 match, 1 pair", 1, res5.size)
        assert(res5[0].contains(bg1) && res5[0].contains(r1) && res5[0].contains(r2))

        // Input Bear, <Witch, Witch, Troll, Troll> Output <Witch, Witch, Troll, Troll>
        val w1 = CreatureCard("W1", "Witch", false, listOf(Symbol.BROOM), Symbol.WATER, Ability.MAGIC_CARPET)
        val w2 = CreatureCard("W2", "Witch", false, listOf(Symbol.BROOM), Symbol.WATER, Ability.MAGIC_CARPET)
        val t1 = CreatureCard("T1", "Troll", true, listOf(Symbol.BAT), Symbol.HELMET, Ability.NONE)
        val t2 = CreatureCard("T2", "Troll", true, listOf(Symbol.BAT), Symbol.HELMET, Ability.NONE)
        val res6 = engine.canSubdueDouble(bear, listOf(w1, w2, t1, t2))
        assertEquals("4 singles, no match, 2 pair", 1, res6.size)
        assert(res6[0].contains(w1) && res6[0].contains(w2) && res6[0].contains(t1) && res6[0].contains(t2))

        val t3 = CreatureCard("T3", "Troll", true, listOf(Symbol.BAT), Symbol.HELMET, Ability.NONE)
        val res7 = engine.canSubdueDouble(bear, listOf(w1, t1, t2, t3))
        assertEquals("3 matching symbols should only combine for 1 match so 0 should be returned" , 0,res7.size)
    }

    @Test
    fun `LookingGlass ability should double a card's symbols when used`() {
        // Bear requires 2 HELMET symbols
        val bear = CreatureCard("Bear", "Bear", true, listOf(Symbol.BAT, Symbol.BAT), Symbol.HELMET, Ability.TOWER_KEY)

        // BillyGoat has 1 HELMET
        val bg1 = CreatureCard("BG1", "BillyGoat", true, listOf(Symbol.HELMET), Symbol.NET, Ability.NONE)
        val lg = Artifact("LG1", "LookingGlass", 0, Ability.LOOKING_GLASS)
        
        val player = Player(name = "Adventurer", hand = listOf(bg1, lg))
        val state = GameState(
            board = Board(),
            players = listOf(player),
            playerPositions = mapOf(player.name to Region(RegionName.FOREST, TowerName.QUEST)),
            gamePhase = GameState.GamePhase.OPEN,
            selectedCards = listOf(bg1, lg)
        )

        // 1. Confirm not subdue
        val res1 = engine.canSubdue(bear, player.hand)
        assertEquals("Should not be able to subdue with regular card", 0, res1.size)

        // 2. Use the ability
        val useAbilityAction = PlayerAction(0, PlayerAction.ActionType.USE_ABILITY, null)
        val stateAfterAbility = engine.reduce(state, useAbilityAction)
        
        val updatedPlayer = stateAfterAbility.players[0]
        val boostedBg = updatedPlayer.hand.find { it.id == bg1.id } as CreatureCard
        
        assertEquals("LookingGlass should be discarded", 1, updatedPlayer.hand.size)
        assertEquals("Target card symbols should be doubled", listOf(Symbol.HELMET, Symbol.HELMET), boostedBg.values)

        // 3. Now it should be able to subdue the bear
        val res = engine.canSubdue(bear, updatedPlayer.hand)
        assertEquals("Should be able to subdue with boosted card", 1, res.size)
    }

    @Test
    fun `TOWER_KEY ability should open tower menu as free action and not advance phase after draw`() {
        val startRegion = Region(RegionName.FOREST, TowerName.QUEST)
        val keyCard = Artifact("K1", "TowerKey", 0, Ability.TOWER_KEY)
        val player = Player(name = "Adventurer", hand = listOf(keyCard))
        val testQuest = Quest("Q1", "Q1", "Q1", 1, 3, Symbol.NET, Symbol.NONE, RegionName.HILLS)
        val state = GameState(
            board = Board(),
            players = listOf(player),
            playerPositions = mapOf(player.name to startRegion),
            gamePhase = GameState.GamePhase.OPEN,
            selectedCards = listOf(keyCard),
            questDeck = Deck(listOf(testQuest))
        )

        // 1. Use TOWER_KEY
        val useAbility = PlayerAction(0, PlayerAction.ActionType.USE_ABILITY, null)
        val stateAfterAbility = engine.reduce(state, useAbility)

        assertEquals("Tower menu should be open", true, stateAfterAbility.towerMenuOpen)
        assertEquals("Should be free tower action", true, stateAfterAbility.isFreeTowerAction)
        assertEquals("Key card should be discarded", 0, stateAfterAbility.players[0].hand.size)
        assertEquals("Phase should still be OPEN", GameState.GamePhase.OPEN, stateAfterAbility.gamePhase)

        // 2. Start Tower Draw
        val startDraw = PlayerAction(0, PlayerAction.ActionType.START_TOWER_DRAW, null)
        val stateAfterStartDraw = engine.reduce(stateAfterAbility, startDraw)
        assertEquals("Tower menu should be closed", false, stateAfterStartDraw.towerMenuOpen)
        assertEquals("Phase should be TOWER", GameState.GamePhase.TOWER, stateAfterStartDraw.gamePhase)

        // 3. Resolve Tower Draw
        val resolveDraw = PlayerAction(0, PlayerAction.ActionType.RESOLVE_TOWER_DRAW, stateAfterStartDraw.towerDrawnCards)
        val stateAfterResolve = engine.reduce(stateAfterStartDraw, resolveDraw)
        
        assertEquals("Phase should return to OPEN because it was a free action", GameState.GamePhase.OPEN, stateAfterResolve.gamePhase)
        assertEquals("isFreeTowerAction should be reset", false, stateAfterResolve.isFreeTowerAction)
        assertEquals("Tower menu should be closed", false, stateAfterResolve.towerMenuOpen)
    }

    @Test
    fun `TOWER_KEY ability should open tower menu as free action and not advance phase after teleport`() {
        val startRegion = Region(RegionName.FOREST, TowerName.QUEST)
        val endRegion = Region(RegionName.HIGHLANDS, TowerName.QUEST) // Same tower type
        val keyCard = Artifact("K1", "TowerKey", 0, Ability.TOWER_KEY)
        val player = Player(name = "Adventurer", hand = listOf(keyCard), gems = 2)
        val state = GameState(
            board = Board(),
            players = listOf(player),
            playerPositions = mapOf(player.name to startRegion),
            gamePhase = GameState.GamePhase.OPEN,
            selectedCards = listOf(keyCard)
        )

        // 1. Use TOWER_KEY
        val useAbility = PlayerAction(0, PlayerAction.ActionType.USE_ABILITY, null)
        val stateAfterAbility = engine.reduce(state, useAbility)

        // 2. Teleport
        val teleportAction = MoveAction(0, endRegion, MoveType.TOWER_KEY)
        val stateAfterTeleport = engine.reduce(stateAfterAbility, teleportAction)

        assertEquals("Player should have moved", endRegion, stateAfterTeleport.playerPositions[player.name])
        assertEquals("Phase should still be OPEN because it was a free action", GameState.GamePhase.OPEN, stateAfterTeleport.gamePhase)
        assertEquals("isFreeTowerAction should be reset", false, stateAfterTeleport.isFreeTowerAction)
        assertEquals("Tower menu should be closed", false, stateAfterTeleport.towerMenuOpen)
    }
}
