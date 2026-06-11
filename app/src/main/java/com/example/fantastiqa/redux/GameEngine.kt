package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.Ability
import com.example.fantastiqa.gameState.Artifact
import com.example.fantastiqa.gameState.ArtifactCard
import com.example.fantastiqa.gameState.Card
import com.example.fantastiqa.gameState.CreatureCard
import com.example.fantastiqa.gameState.Deck
import com.example.fantastiqa.gameState.Player
import com.example.fantastiqa.gameState.Quest
import com.example.fantastiqa.gameState.Symbol
import com.example.fantastiqa.pieces.TowerName
import com.example.fantastiqa.redux.actions.*
import org.apache.commons.math3.util.Combinations
import java.util.Collections

/**
 * Pure, side-effect-free game engine that processes actions and returns new game states.
 * This is the core of the Redux architecture - it has NO dependencies on Android or UI.
 */
class GameEngine {
    
    fun reduce(currentState: GameState, action: Action): GameState {
        requireNotNull(currentState) { "Current state cannot be null" }
        requireNotNull(action) { "Action cannot be null" }

        return when (action) {
            is PlayerAction -> handlePlayerAction(currentState, action)
            is QuestAction -> handleQuestAction(currentState, action)
            is CardAction -> handleCardAction(currentState, action)
            is TurnAction -> handleTurnAction(currentState, action)
            is MoveAction -> handleMovePlayer(currentState, action)
            else -> currentState
        }
    }

    private fun handlePlayerAction(state: GameState, action: PlayerAction): GameState {
        return when (action.getActionType()) {
            PlayerAction.ActionType.DRAW_CARDS -> handleDrawCards(state, action)
            PlayerAction.ActionType.USE_FLYING_CARPET -> handleUseFlyingCarpet(state, action)
            PlayerAction.ActionType.GAIN_GEMS -> handleGainGems(state, action)
            PlayerAction.ActionType.GAIN_TROPHIES -> handleGainTrophies(state, action)
            PlayerAction.ActionType.DISCARD_FROM_HAND -> handleDiscardCards(state, action)
            PlayerAction.ActionType.USE_ABILITY -> handleUseAbility(state, action)
            PlayerAction.ActionType.STORE_IN_BACKPACK -> handleBackpack(state, action)
            PlayerAction.ActionType.USE_TENT -> handleUseTent(state, action)
            PlayerAction.ActionType.RELEASE_CARDS -> handleReleaseCards(state, action)
            PlayerAction.ActionType.GAIN_CARD -> handleGainCardFromTower(state, action)
            PlayerAction.ActionType.START_TOWER_DRAW -> handleStartTowerDraw(state, action)
            PlayerAction.ActionType.RESOLVE_TOWER_DRAW -> handleResolveTowerDraw(state, action)
            else -> state
        }
    }

    private fun handleQuestAction(state: GameState, action: QuestAction): GameState {
        return when (action.getActionType()) {
            QuestAction.ActionType.COMPLETE_QUEST -> handleCompleteQuest(state, action)
            QuestAction.ActionType.STORE_CARD_FOR_QUEST -> handleStoreCardForQuest(state, action)
            QuestAction.ActionType.DRAW_QUEST -> handleDrawQuest(state, action)
            QuestAction.ActionType.SELECT_QUEST -> handleSelectQuest(state, action)
            else -> state
        }
    }

    private fun handleSelectQuest(state: GameState, action: QuestAction): GameState {
        val quest = action.quest
        val newState = if (state.selectedQuest?.id == quest?.id) {
            state.copy(selectedQuest = null)
        } else {
            state.copy(selectedQuest = quest)
        }

        // Auto-trigger store if applicable
        val selectedCards = newState.selectedCards.filterNotNull()
        val currentQuest = newState.selectedQuest
        if (selectedCards.size == 1 && currentQuest != null) {
            val player = newState.players.getOrNull(newState.currentPlayerIndex)
            val isPlayerQuest = player?.quests?.any { it.id == currentQuest.id } == true
            if (isPlayerQuest && currentQuest.canStoreCard(selectedCards[0])) {
                return handleStoreCardForQuest(newState, QuestAction(QuestAction.ActionType.STORE_CARD_FOR_QUEST, newState.currentPlayerIndex, currentQuest, listOf(selectedCards[0])))
            }
        }

        return newState
    }

    private fun handleCardAction(state: GameState, action: CardAction): GameState {
        return when (action.actionType) {
            CardAction.ActionType.SELECT_CARDS -> handleSelectCards(state, action)
            else -> state
        }
    }

    private fun handleTurnAction(state: GameState, action: TurnAction): GameState {
        return when (action.getActionType()) {
            TurnAction.ActionType.NEXT_TURN -> handleNextTurn(state, action)
            TurnAction.ActionType.ADVANCE_PHASE -> handleAdvancePhase(state, action)
            else -> state
        }
    }

    private fun handleDrawCards(state: GameState, action: PlayerAction): GameState {
        val amount = action.payload as? Int ?: return state
        val player = state.players.getOrNull(action.playerIndex) ?: return state
        
        val updatedPlayer = player.drawCards(amount)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer
        
        return state.copy(players = updatedPlayers)
    }
    private fun handleMovePlayer(state: GameState, action: MoveAction): GameState {
        val player = state.players.getOrNull(action.playerIndex) ?: return state
        val currentRegion = state.playerPositions[player.name] ?: return state

        // 1. Validate the specific movement rules
        val selectedCards = state.selectedCards.filterNotNull()
        
        // Storage cards cannot be used for movement/subduing
        if (selectedCards.any { it in player.storage }) return state

        val isLegal = when (action.moveType) {
            MoveType.ADJACENT -> {
                val theRoad = state.board?.getRoad(currentRegion, action.destination) ?: return state
                if (action.useAbility) {
                    // Region selection: Must use Magic Carpet ability
                    selectedCards.size == 1 &&
                            selectedCards[0] is CreatureCard &&
                            (selectedCards[0] as CreatureCard).ability == Ability.MAGIC_CARPET
                } else {
                    // Road selection: Subdue or clear road
                    if (theRoad.creature == null) {
                        false // Road is used, can not move except by flying
                    } else {
                        if (selectedCards.isEmpty()) {
                            false // Must select cards to subdue
                        } else {
                            // NORMAL SUBDUE
                            val validCombos = canSubdueSingle(theRoad.creature, selectedCards) + canSubdueDouble(theRoad.creature, selectedCards)
                            validCombos.any { it.size == selectedCards.size && it.containsAll(selectedCards) }
                        }
                    }
                }
            }
            MoveType.FLYING_CARPET -> {
                val hasRoad = state.board?.getRoad(currentRegion, action.destination) != null
                hasRoad && player.flyingCarpets > 0 && selectedCards.isEmpty()
            }
            MoveType.TOWER_KEY -> {
                // currentRegion and action.destination must have the same tower type
                currentRegion.tower != null && currentRegion.tower == action.destination.tower && player.gems >= 2
            }
        }

        if (!isLegal) return state

        // 2. Apply "Costs" and update board
        var updatedBoard = state.board
        var updatedPlayer = player
        when (action.moveType) {
            MoveType.FLYING_CARPET -> updatedPlayer = player.useFlyingCarpet()
            MoveType.ADJACENT -> {
                val theRoad = state.board?.getRoad(currentRegion, action.destination) ?: return state
                if (action.useAbility) {
                    // Just discard the card and move (don't remove creature from board)
                    updatedPlayer = player.discardFromHand(selectedCards)
                } else if (theRoad.creature != null) {
                    // Normal subdue: remove creature and gain it
                    updatedBoard = state.board?.withRoad(currentRegion, action.destination, theRoad.copy(creature = null))
                    val gemBonus = if (theRoad.creature.gem) 1 else 0
                    updatedPlayer = player.gainCard(theRoad.creature).discardFromHand(selectedCards).withGems(player.gems + gemBonus)
                }
            }
            MoveType.TOWER_KEY -> updatedPlayer = player.withGems(player.gems - 2)
            else -> {}
        }

        // 3. Update the state
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer

        val updatedPositions = state.playerPositions.toMutableMap()
        updatedPositions[player.name] = action.destination

        return state.copy(
            board = updatedBoard,
            players = updatedPlayers,
            playerPositions = updatedPositions,
            selectedCards = emptyList()
        )
    }

    private fun handleUseFlyingCarpet(state: GameState, action: PlayerAction): GameState = state

    private fun handleGainGems(state: GameState, action: PlayerAction): GameState {
        val amount = action.payload as? Int ?: return state
        val player = state.players.getOrNull(action.playerIndex) ?: return state
        
        val updatedPlayer = player.withGems(player.gems + amount)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer
        
        return state.copy(players = updatedPlayers)
    }
    private fun handleGainTrophies(state: GameState, action: PlayerAction): GameState {
        val amount = action.payload as? Int ?: return state
        val player = state.players.getOrNull(action.playerIndex) ?: return state
        
        val updatedPlayer = player.withTrophies(player.trophies + amount)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer
        
        return state.copy(players = updatedPlayers)
    }
    private fun handleUseAbility(state: GameState, action: PlayerAction): GameState {
        val player = action.getCurrentPlayer(state) ?: return state
        val selectedCards = state.selectedCards.filterNotNull()

        if (selectedCards.size != 1) return state
        val card = selectedCards[0]
        if (card !is CreatureCard) return state

        // Storage cards cannot use abilities
        if (player.storage.contains(card)) return state

        val updatedPlayers = state.players.toMutableList()
        var updatedPlayer = player // Start with current player

        // Execute ability
        updatedPlayer = when (card.ability) {
            Ability.GEM -> {
                player.discardFromHand(listOf(card)).withGems(player.gems + 1)
            }
            Ability.DRAGON -> {
                val currentRegion = state.playerPositions[player.name]
                val otherPlayerIndex = state.players.indexOfFirst { 
                    it.name != player.name && state.playerPositions[it.name] == currentRegion 
                }
                
                if (otherPlayerIndex != -1) {
                    val otherPlayer = state.players[otherPlayerIndex]
                    updatedPlayers[otherPlayerIndex] = otherPlayer.gainCard(card)
                    // Remove from hand without discarding to own pile
                    player.copy(hand = player.hand - card)
                } else {
                    // No other player, just discard normally
                    player.discardFromHand(listOf(card))
                }
            }
            else -> player.discardFromHand(listOf(card))
        }

        updatedPlayers[action.playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            selectedCards = emptyList()
        )
    }

    private fun handleBackpack(state: GameState, action: PlayerAction): GameState {
        val player = action.getCurrentPlayer(state) ?: return state
        val selectedCards = state.selectedCards.filterNotNull()

        if (selectedCards.isEmpty()) return state
        
        // Cannot backpack cards already in storage
        if (selectedCards.any { it in player.storage }) return state

        val updatedPlayer = player.storeCards(selectedCards)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            selectedCards = emptyList()
        )
    }

    private fun handleCompleteQuest(state: GameState, action: QuestAction): GameState {
        val playerIndex = action.playerIndex
        val player = state.players.getOrNull(playerIndex) ?: return state
        val quest = action.quest

        val isPlayerQuest = player.quests.any { it.id == quest.id }
        val isBoardQuest = state.board?.quests?.any { it?.id == quest.id } == true

        if (!isPlayerQuest && !isBoardQuest) return state

        // Check region
        val playerRegion = state.playerPositions[player.name] ?: return state
        if (playerRegion.name != quest.land) return state

        var updatedBoard = state.board
        var updatedPlayer = player
        val selectedCards = state.selectedCards.filterNotNull()

        if (isPlayerQuest) {
            val qIndex = player.quests.indexOfFirst { it.id == quest.id }
            val targetQuest = player.quests[qIndex] as? Quest ?: return state

            // Check requirements
            val reqs = targetQuest.getRequirements()
            val fulfilled = targetQuest.getFulfilledIndices()
            if (fulfilled.size < reqs.size) return state

            // Complete player quest
            val updatedQuests = player.quests.toMutableList()
            updatedQuests.removeAt(qIndex)

            var updatedDeck = player.deck
            targetQuest.stored.forEach { updatedDeck = updatedDeck.discard(it) }

            updatedPlayer = player.copy(
                quests = updatedQuests,
                deck = updatedDeck,
                vps = player.vps + targetQuest.vps,
                gems = player.gems + targetQuest.gems,
                trophies = player.trophies + targetQuest.vps
            )
        } else if (isBoardQuest) {
            // Check board quest fulfillment with selected cards
            if (!canCompleteQuestWithCards(quest, selectedCards)) return state

            // Complete board quest
            val updatedQuests = state.board?.quests?.toMutableList() ?: return state
            val qIndex = updatedQuests.indexOfFirst { it?.id == quest.id }
            if (qIndex != -1) {
                updatedQuests[qIndex] = null
            }
            updatedBoard = state.board.copy(quests = updatedQuests)

            updatedPlayer = player.discardFromHand(selectedCards).copy(
                vps = player.vps + quest.vps,
                gems = player.gems + quest.gems,
                trophies = player.trophies + quest.vps
            )
        }

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            board = updatedBoard,
            selectedCards = emptyList(),
            selectedQuest = null
        )
    }

    private fun canCompleteQuestWithCards(quest: Quest, cards: List<Card>): Boolean {
        val reqs = quest.getRequirements()
        val providedSymbols = cards.filterIsInstance<CreatureCard>().flatMap { it.values }.toMutableList()

        for (req in reqs) {
            if (!providedSymbols.remove(req)) {
                return false
            }
        }
        return true
    }

    private fun handleStoreCardForQuest(state: GameState, action: QuestAction): GameState {
        val playerIndex = action.playerIndex
        val player = state.players.getOrNull(playerIndex) ?: return state
        val quest = action.quest
        val card = action.requiredCards.firstOrNull() ?: return state

        if (card !is CreatureCard) return state
        
        // Storage cards cannot be stored on quests
        if (player.storage.contains(card)) return state

        val playerQuests = player.quests.toMutableList()
        val qIndex = playerQuests.indexOfFirst { it.id == quest.id }
        if (qIndex == -1) return state

        val targetQuest = playerQuests[qIndex] as? Quest ?: return state
        if (!targetQuest.canStoreCard(card)) return state

        val updatedQuest = targetQuest.copy(stored = targetQuest.stored + card)
        playerQuests[qIndex] = updatedQuest

        val updatedPlayer = player.copy(
            hand = player.hand - card,
            quests = playerQuests
        )

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[playerIndex] = updatedPlayer

        return state.copy(players = updatedPlayers, selectedCards = emptyList())
    }

    private fun handleDrawQuest(state: GameState, action: QuestAction): GameState {
        val playerIndex = action.playerIndex
        val player = state.players.getOrNull(playerIndex) ?: return state
        val quest = action.quest ?: return state

        val updatedPlayer = player.drawQuest(quest)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[playerIndex] = updatedPlayer

        // Also remove the quest from the quest deck if it was drawn from there
        val updatedQuestDeck = state.questDeck?.remove(quest)

        return state.copy(
            players = updatedPlayers,
            questDeck = updatedQuestDeck
        )
    }

    private fun handleUseTent(state: GameState, action: PlayerAction): GameState {
        val player = action.getCurrentPlayer(state) ?: return state
        // Shuffling using a tent
        val updatedPlayer = player.copy(
            tents = (player.tents - 1).coerceAtLeast(0),
            deck = player.deck.shuffle(true)
        )
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer
        return state.copy(players = updatedPlayers)
    }

    private fun handleReleaseCards(state: GameState, action: PlayerAction): GameState {
        val player = action.getCurrentPlayer(state) ?: return state
        val cardsToRelease = action.payload as? List<Card> ?: return state
        
        if (player.gems < cardsToRelease.size) return state

        // Storage cards cannot be released
        if (cardsToRelease.any { it in player.storage }) return state
        
        val updatedPlayer = player.removeFromHand(cardsToRelease).withGems(player.gems - cardsToRelease.size)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer
        
        return state.copy(players = updatedPlayers, selectedCards = emptyList())
    }

    private fun handleGainCardFromTower(state: GameState, action: PlayerAction): GameState {
        val player = action.getCurrentPlayer(state) ?: return state
        val card = action.payload as? Card ?: return state
        
        val playerPos = state.playerPositions[player.name] ?: return state
        val tower = playerPos.tower ?: return state
        
        val cost = when(tower) {
            TowerName.BAZAAR -> 3
            else -> 0
        }
        
        if (player.gems < cost) return state
        
        val updatedPlayer = player.withGems(player.gems - cost).gainCard(card)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer
        
        val updatedBazaarDeck = if (tower == TowerName.BAZAAR && card is CreatureCard) {
            state.bazaarDeck?.remove(card)
        } else state.bazaarDeck
        
        return state.copy(
            players = updatedPlayers,
            bazaarDeck = updatedBazaarDeck
        )
    }

    private fun handleStartTowerDraw(state: GameState, action: PlayerAction): GameState {
        val player = action.getCurrentPlayer(state) ?: return state
        val playerPos = state.playerPositions[player.name] ?: return state
        val tower = playerPos.tower ?: return state

        // Check for PLUS_CARD ability in selected cards
        val plusCards = state.selectedCards.filter { 
            it is CreatureCard && it.ability == Ability.PLUS_CARD && it in player.hand
        }.filterNotNull()
        
        val drawAmount = 3 + plusCards.size

        val result = when (tower) {
            TowerName.BAZAAR -> state.bazaarDeck?.draw(drawAmount)
            TowerName.QUEST -> state.questDeck?.draw(drawAmount)
            TowerName.ARTIFACT -> state.artifactDeck?.draw(drawAmount)
            else -> null
        }

        val drawn = result?.first ?: emptyList<Card>()
        val nextDeck = result?.second

        // Discard the used plus cards
        var updatedPlayer = player
        if (plusCards.isNotEmpty()) {
            updatedPlayer = player.discardFromHand(plusCards)
        }
        
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            towerDrawnCards = drawn,
            bazaarDeck = if (tower == TowerName.BAZAAR) nextDeck as? Deck<CreatureCard> else state.bazaarDeck,
            questDeck = if (tower == TowerName.QUEST) nextDeck as? Deck<Quest> else state.questDeck,
            artifactDeck = if (tower == TowerName.ARTIFACT) nextDeck as? Deck<Artifact> else state.artifactDeck,
            selectedCards = emptyList() // Clear selection after use
        )
    }

    private fun handleResolveTowerDraw(state: GameState, action: PlayerAction): GameState {
        val selectedCards = action.payload as? List<Card> ?: emptyList()
        val player = action.getCurrentPlayer(state) ?: return state
        val playerPos = state.playerPositions[player.name] ?: return state
        val tower = playerPos.tower ?: return state

        val allDrawn = state.towerDrawnCards
        val unselected = allDrawn.filter { card -> selectedCards.none { it.id == card.id } }

        var updatedPlayer = player
        var updatedBazaarDeck = state.bazaarDeck
        var updatedQuestDeck = state.questDeck
        var updatedArtifactDeck = state.artifactDeck

        for (card in selectedCards) {
            val cost = when (tower) {
                TowerName.BAZAAR -> 3
                TowerName.ARTIFACT -> (card as? ArtifactCard)?.cost ?: (card as? Artifact)?.cost ?: 0
                else -> 0
            }
            if (updatedPlayer.gems >= cost) {
                updatedPlayer = updatedPlayer.withGems(updatedPlayer.gems - cost)
                updatedPlayer = if (tower == TowerName.QUEST && card is Quest) {
                    updatedPlayer.drawQuest(card)
                } else {
                    updatedPlayer.gainCard(card)
                }
            } else {
                // Cannot afford, goes to bottom
                when (tower) {
                    TowerName.BAZAAR -> if (card is CreatureCard) updatedBazaarDeck = updatedBazaarDeck?.putOnBottom(listOf(card))
                    TowerName.QUEST -> if (card is Quest) updatedQuestDeck = updatedQuestDeck?.putOnBottom(listOf(card))
                    TowerName.ARTIFACT -> if (card is Artifact) updatedArtifactDeck = updatedArtifactDeck?.putOnBottom(listOf(card))
                }
            }
        }

        // Unselected go to bottom
        when (tower) {
            TowerName.BAZAAR -> updatedBazaarDeck = updatedBazaarDeck?.putOnBottom(unselected.filterIsInstance<CreatureCard>())
            TowerName.QUEST -> updatedQuestDeck = updatedQuestDeck?.putOnBottom(unselected.filterIsInstance<Quest>())
            TowerName.ARTIFACT -> updatedArtifactDeck = updatedArtifactDeck?.putOnBottom(unselected.filterIsInstance<Artifact>())
        }

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            bazaarDeck = updatedBazaarDeck,
            questDeck = updatedQuestDeck,
            artifactDeck = updatedArtifactDeck,
            towerDrawnCards = emptyList()
        )
    }
    private fun handleSelectCards(state: GameState, action: CardAction): GameState {
        val card = action.cards.firstOrNull() ?: return state
        val currentSelection = state.selectedCards.filterNotNull().toMutableList()
        
        if (currentSelection.contains(card)) {
            currentSelection.remove(card)
        } else {
            currentSelection.add(card)
        }
        
        val newState = state.copy(selectedCards = currentSelection)

        // Auto-trigger store if applicable
        if (currentSelection.size == 1 && newState.selectedQuest != null) {
            val quest = newState.selectedQuest
            val player = newState.players.getOrNull(newState.currentPlayerIndex)
            val isPlayerQuest = player?.quests?.any { it.id == quest?.id } == true
            if (isPlayerQuest && quest != null && quest.canStoreCard(currentSelection[0])) {
                return handleStoreCardForQuest(newState, QuestAction(QuestAction.ActionType.STORE_CARD_FOR_QUEST, newState.currentPlayerIndex, quest, listOf(currentSelection[0])))
            }
        }

        return newState
    }

    private fun handleStorePrivate(state: GameState, action: PlayerAction): GameState {
        val cardsToStore = action.payload as? List<Card> ?: return state
        val playerIndex = action.playerIndex
        val player =  action.getCurrentPlayer(state) ?: return state

        val updatedPlayer = player.storeCards(cardsToStore)

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            selectedCards = emptyList(),
            gamePhase = state.gamePhase ?: GameState.GamePhase.PLAYER_TURN
        )
    }
    private fun handleDiscardCards(state: GameState, action: PlayerAction): GameState {
        val cardsToDiscard = action.payload as? List<Card> ?: return state
        val playerIndex = action.playerIndex
        val player =  action.getCurrentPlayer(state) ?: return state

        val updatedPlayer = player.discardFromHand(cardsToDiscard)

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            selectedCards = emptyList(),
            gamePhase = state.gamePhase ?: GameState.GamePhase.PLAYER_TURN
        )
    }

    private fun handleNextTurn(state: GameState, action: TurnAction): GameState {
        var currentBoard = state.board ?: return state
        var currentCreatureDeck = state.creatureDeck ?: return state
        var currentQuestDeck = state.questDeck ?: return state

        // 1. Process current player's end-of-turn (Discard selected and draw up to 5)
        val currentPlayerIndex = state.currentPlayerIndex
        var currentPlayer = state.players.getOrNull(currentPlayerIndex) ?: return state
        
        // Discard selected cards
        val selectedCards = state.selectedCards.filterNotNull()
        currentPlayer = currentPlayer.discardFromHand(selectedCards)
        
        // Draw up to 5
        val cardsToDraw = (5 - currentPlayer.hand.size).coerceAtLeast(0)
        if (cardsToDraw > 0) {
            currentPlayer = currentPlayer.drawCards(cardsToDraw)
        }

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[currentPlayerIndex] = currentPlayer

        // 2. Refill Empty Roads
        val regions = currentBoard.regions()
        for (r1 in regions) {
            val adj = currentBoard.adjacencies[r1] ?: continue
            for ((r2, road) in adj) {
                if (road.creature == null) {
                    val (newCreature, nextDeck) = currentCreatureDeck.drawOne()
                    if (newCreature is CreatureCard) {
                        currentBoard = currentBoard.withRoad(r1, r2, road.copy(creature = newCreature))
                        currentCreatureDeck = nextDeck
                    }
                }
            }
        }

        // 3. Refill Empty Quests
        val updatedQuests = currentBoard.quests.toMutableList()
        for (i in updatedQuests.indices) {
            if (updatedQuests[i] == null) {
                val (newQuest, nextDeck) = currentQuestDeck.drawOne()
                if (newQuest != null) {
                    updatedQuests[i] = newQuest.copy(vps = newQuest.vps + 1)
                    currentQuestDeck = nextDeck
                }
            }
        }
        currentBoard = currentBoard.copy(quests = updatedQuests)

        // 4. Advance Player Index
        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextTurnCount = state.turnCount + 1

        return state.copy(
            board = currentBoard,
            creatureDeck = currentCreatureDeck,
            questDeck = currentQuestDeck,
            players = updatedPlayers,
            currentPlayerIndex = nextPlayerIndex,
            turnCount = nextTurnCount,
            selectedCards = emptyList(),
            selectedQuest = null,
            gamePhase = GameState.GamePhase.PLAYER_TURN
        )
    }

    private fun handleAdvancePhase(state: GameState, action: TurnAction): GameState = state

    fun canSubdueSingle(
        aCreature: CreatureCard?,
        aHand: List<Card>?
    ): List<MutableSet<Card>> {
        val fullList: MutableList<MutableSet<Card>> = ArrayList<MutableSet<Card>>()

        val handSymbols: MutableList<Symbol?> = ArrayList<Symbol?>()
        //Check hand for first symbol
        if (aHand != null) {
            for (aCard in aHand) {
                if (aCard is CreatureCard && aCard.values.get(0) != Symbol.NONE) {
                    val handCreature = aCard
                    if (aCreature?.subduedBy == handCreature.values.get(0)) {
                        //symbol match
                        fullList.add(mutableSetOf<Card>(aCard))
                        continue
                    }
                    if (handCreature.values.size > 1 && handCreature.values.get(0) == handCreature.values.get(1))
                    {
                        //Double symbol as wildcard
                        fullList.add(mutableSetOf<Card>(aCard))
                        continue
                    }
                    if (handSymbols.contains(handCreature.values.get(0))) {
                        //this is at least second symbol in the hand so wildcard is in effect but
                        //add these cards later after we have completed this first walkthrough of the hand
                    } else {
                        handSymbols.add(handCreature.values.get(0))
                    }
                }
            }
        }
        val matches: MutableList<Card> = ArrayList<Card>()
        var comboCards: MutableSet<Card>
        for (match in handSymbols) {
            if (aHand != null) {
                for  (aCard in aHand) {
                    if (aCard is CreatureCard &&
                        aCard.values.get(0) == match
                    ) {
                        matches.add(aCard)
                    }
                }
            }
            if (matches.size > 1) {
                val combos: Combinations = Combinations(matches.size, 2)
                for (pairing in combos) {
                    comboCards = HashSet<Card>(2)
                    Collections.addAll<Card>(
                        comboCards,
                        matches.get(pairing[0]),
                        matches.get(pairing[1])
                    )
                    fullList.add(comboCards)
                }
            }
            matches.clear()
        }
        return fullList
    }
    fun canSubdueDouble(
        toSubdue: CreatureCard?,
        aHand: List<Card>?
    ): List<MutableSet<Card>> {
        val fullList: MutableList<MutableSet<Card>> = ArrayList<MutableSet<Card>>()
        val singleWildSets: MutableList<MutableList<Card>> = ArrayList<MutableList<Card>>()
        val singleNonMatch: MutableList<MutableList<Card>> = ArrayList<MutableList<Card>>()
        if (aHand != null)
          for (aCard in aHand) {
            if (aCard is CreatureCard && aCard.values.get(0) != Symbol.NONE) {
                val handCreature = aCard
                if (handCreature.values.size > 1 && handCreature.values.get(0) == handCreature.values.get(
                        1
                    )
                ) {
                    //double symbol
                    if (toSubdue?.subduedBy == handCreature.values.get(0)) {
                        fullList.add(mutableSetOf<Card>(aCard))
                    } else {
                        singleWildSets.add(mutableListOf<Card>(aCard))
                    }
                } else if (toSubdue?.subduedBy == handCreature.values.get(0)) {
                    //single symbol match
                    singleWildSets.add(mutableListOf<Card>(aCard))
                } else {
                    //single miss
                    //is the symbol already in the list
                    for (singleSet in singleNonMatch) {
                        if ((singleSet.iterator()
                                .next() as CreatureCard).values.get(0) == handCreature.values.get(0)
                        ) {
                            singleSet.add(aCard)
                            continue
                        }
                    }
                    val newSingle: MutableList<Card> = ArrayList<Card>()
                    newSingle.add(aCard)
                    singleNonMatch.add(newSingle)
                }
            }
        }

        var combos: Combinations?
        var comboCards: MutableList<Card>?
        for (singles in singleNonMatch) {
            if (singles.size >= 2) {
                combos = Combinations(singles.size, 2)
                for (pairing in combos) {
                    comboCards = ArrayList<Card>(2)
                    Collections.addAll<Card?>(
                        comboCards,
                        singles.get(pairing[0]),
                        singles.get(pairing[1])
                    )
                    singleWildSets.add(comboCards)
                }
            }
        }
        var comboCardSet: MutableSet<Card>?
        for (singleWilds in singleWildSets) {
            if (singleWilds.size >= 2) {
                combos = Combinations(singleWilds.size, 2)
                for (pairing in combos) {
                    comboCardSet = HashSet<Card>(2)
                    Collections.addAll<Card>(
                        comboCardSet,
                        singleWilds.get(pairing[0]),
                        singleWilds.get(pairing[1])
                    )
                    fullList.add(comboCardSet)
                }
            }
        }

        return fullList
    }

}


