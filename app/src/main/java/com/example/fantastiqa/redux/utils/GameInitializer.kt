package com.example.fantastiqa.redux.utils

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.CreatureCards
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.redux.GameState

/**
 * Utility class for initializing a new game state.
 * This is used to set up the initial GameState that will be used by the Store.
 */
object GameInitializer {
    /**
     * Create a new game with initialized state
     */
    @JvmStatic
    fun initializeNewGame(): GameState {
        // Create initial empty board structure
        var board = Board.createInitialBoard()

        // Create and shuffle decks
        var creatureDeck = initializeCreatureDeck()
        val bazaarDeck = initializeBazaarDeck()
        var questDeck = initializeQuestDeck()
        val artifactDeck = initializeArtifactDeck()

        // Set up initial quests on board
        val (initialQuests, remainingQuestDeck) = questDeck.draw(2)
        questDeck = remainingQuestDeck

        val boardQuests = initialQuests.map { q ->
            Quest(
                q._id,
                q._name,
                q.title,
                q.vps + 1,
                q.gems,
                q.doubleReq,
                q.tripleReq,
                q.land,
                q.stored
            )
        }
        board = board.copy(quests = boardQuests, adjacencies = board.adjacencies)

        // Place creatures on roads
        for (road in board.roads()) {
            val (card, nextDeck) = creatureDeck.drawOne()
            val roadCreature = card as CreatureCard
            creatureDeck = nextDeck

            // Find regions for this road to update board immutably
            var reg1: Region? = null
            var reg2: Region? = null
            for (r in board.regions()) {
                val innerMap = board.adjacencies[r] ?: continue
                for ((targetRegion, targetRoad) in innerMap) {
                    if (targetRoad === road) {
                        reg1 = r
                        reg2 = targetRegion
                        break
                    }
                }
                if (reg1 != null) break
            }

            if (reg1 != null && reg2 != null) {
                board = board.withRoad(reg1, reg2, Road(roadCreature, roadCreature.gem))
            }
        }

        // Create and initialize players
        val startingRegion = board.regions().getOrElse(1) { board.regions().first() }
        
        var player1 = Player("Player 1").drawCards(5)
        var player2 = Player("Computer", isComputer = true).drawCards(5)

        // Add one quest to each player immutably
        val (p1Quest, qDeck2) = questDeck.drawOne()
        player1 = player1.copy(quests = player1.quests + listOfNotNull(p1Quest))
        
        val (p2Quest, qDeck3) = qDeck2.drawOne()
        player2 = player2.copy(quests = player2.quests + listOfNotNull(p2Quest))
        
        questDeck = qDeck3
        val players = listOf(player1, player2)

        // Initialize player positions
        //TODO random or select
        val playerPositions = mapOf(
            player1.name to startingRegion,
            player2.name to startingRegion
        )

        // Build and return initial game state
        return GameState(
            board = board,
            vpGoal = 4,
            creatureDeck = creatureDeck,
            artifactDeck = artifactDeck,
            bazaarDeck = bazaarDeck,
            questDeck = questDeck,
            players = players,
            currentPlayerIndex = 0,
            playerPositions = playerPositions,
            selectedCards = emptyList(),
            gamePhase = GameState.GamePhase.OPEN,
            isGameOver = false
        )
    }

    /**
     * Initialize the creature deck
     */
    private fun initializeCreatureDeck(): Deck<Card> {
        val singleSymbolCards = CreatureCards.entries
            .filter { it.value2 == Symbol.NONE }
            .flatMap { aCard ->
                List(2) {
                    CreatureCard(
                        java.util.UUID.randomUUID().toString(),
                        aCard.name,
                        aCard.isGem,
                        listOf(aCard.value1),
                        aCard.subduedBy,
                        if (aCard.isGem) Ability.NONE else aCard.ability
                    )
                }
            }.shuffled()

        val doubleSymbolCards = CreatureCards.entries
            .filter { it.value2 != Symbol.NONE }
            .flatMap { aCard ->
                List(2) {
                    CreatureCard(
                        java.util.UUID.randomUUID().toString(),
                        aCard.name,
                        aCard.isGem,
                        listOf(aCard.value1, aCard.value2),
                        aCard.subduedBy,
                        if (aCard.isGem) Ability.NONE else aCard.ability
                    )
                }
            }.shuffled()

        return Deck<Card>(singleSymbolCards + doubleSymbolCards)
    }

    /**
     * Initialize the bazaar deck
     */
    private fun initializeBazaarDeck(): Deck<CreatureCard> {
        val bazaarCards = CreatureCards.entries
            .filter { it.value2 != Symbol.NONE }
            .flatMap { aCard ->
                List(3) {
                    CreatureCard(
                        java.util.UUID.randomUUID().toString(),
                        aCard.name,
                        false,
                        listOf(aCard.value1, aCard.value2),
                        aCard.subduedBy,
                        aCard.ability
                    )
                }
            }
        return Deck(bazaarCards).shuffle(true)
    }

    /**
     * Initialize the quest deck
     */
    private fun initializeQuestDeck(): Deck<Quest> {
        val quests = listOf(
            Quest(java.util.UUID.randomUUID().toString(), "FIRE Q", "FIRE Q", 1, 3, Symbol.FIRE, Symbol.NONE, RegionName.WETLANDS),
            Quest(java.util.UUID.randomUUID().toString(), "WATER Q", "WATER Q", 1, 3, Symbol.WATER, Symbol.NONE, RegionName.FIELDS),
            Quest(java.util.UUID.randomUUID().toString(), "BAT Q", "BAT Q", 1, 3, Symbol.BAT, Symbol.NONE, RegionName.HIGHLANDS),
            Quest(java.util.UUID.randomUUID().toString(), "BROOM Q", "BROOM Q", 1, 3, Symbol.BROOM, Symbol.NONE, RegionName.FIELDS),
            Quest(java.util.UUID.randomUUID().toString(), "NET Q", "NET Q", 1, 3, Symbol.NET, Symbol.NONE, RegionName.HILLS),
            Quest(java.util.UUID.randomUUID().toString(), "HELMET Q", "HELMET Q", 1, 3, Symbol.HELMET, Symbol.NONE, RegionName.TUNDRA),
            Quest(java.util.UUID.randomUUID().toString(), "SWORD Q", "SWORD Q", 1, 3, Symbol.SWORD, Symbol.NONE, RegionName.HILLS),
            Quest(java.util.UUID.randomUUID().toString(), "TOOTH Q", "TOOTH Q", 1, 3, Symbol.TOOTH, Symbol.NONE, RegionName.TUNDRA),
            Quest(java.util.UUID.randomUUID().toString(), "WAND Q", "WAND Q", 1, 3, Symbol.WAND, Symbol.NONE, RegionName.WETLANDS)
        )
        return Deck(quests).shuffle(true)
    }

    /**
     * Initialize the artifact deck
     */
    private fun initializeArtifactDeck(): Deck<Artifact> {
        val artifacts = List(6) {
            Artifact(java.util.UUID.randomUUID().toString(), "LookingGlass", 2, Ability.LOOKING_GLASS)
        }
        return Deck(artifacts).shuffle(true)
    }
}
