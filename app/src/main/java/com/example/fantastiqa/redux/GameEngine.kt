package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.CreatureCards
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

        val phase = currentState.gamePhase

        // 1. Check Free Action Constraints
        if (isFreeAction(action)) {
            if (phase != GameState.GamePhase.OPEN && phase != GameState.GamePhase.DISCARD_OPEN) {
                // Exceptions:
                // - SELECT_CARDS during SUBDUE phase
                // - PLUS_CARD selection during TOWER phase
                val isSelectDuringSubdue = phase == GameState.GamePhase.SUBDUE && isSelectCardsAction(action)
                val isPlusDuringTower = phase == GameState.GamePhase.TOWER && isTowerPlusCardSelection(currentState, action)
                val isAbilityAction = isUseAbilityAction(action)
                
                if (!isSelectDuringSubdue && !isPlusDuringTower && !isAbilityAction) {
                    return currentState
                }
            }
        }

        // 2. Main Reduction
        val stateAfterAction = when (action) {
            is PlayerAction -> handlePlayerAction(currentState, action)
            is QuestAction -> handleQuestAction(currentState, action)
            is SubdueAction -> handleSubdueAction(currentState, action)
            is CardAction -> handleCardAction(currentState, action)
            is TurnAction -> handleTurnAction(currentState, action)
            is MoveAction -> handleMovePlayer(currentState, action)
            else -> currentState
        }

        val stateAfterWinCheck = checkWinConditions(stateAfterAction)

        // 3. Automated Phase Transitions
        return processAutomatedTransitions(stateAfterWinCheck)
    }

    private fun checkWinConditions(state: GameState): GameState {
        if (state.isGameOver) return state
        
        val winner = state.players.find { it.totalCardCount() >= 25 }
        return if (winner != null) {
            state.copy(isGameOver = true)
        } else {
            state
        }
    }

    private fun isFreeAction(action: Action): Boolean {
        return when (action) {
            is PlayerAction -> when (action.getActionType()) {
                PlayerAction.ActionType.STORE_FOR_BOARD_QUEST,
                PlayerAction.ActionType.DISCARD_FROM_HAND,
                PlayerAction.ActionType.USE_ABILITY,
                PlayerAction.ActionType.USE_TENT,
                PlayerAction.ActionType.USE_FLYING_CARPET -> true
                else -> false
            }
            is CardAction -> action.getActionType() == CardAction.ActionType.SELECT_CARDS
            else -> false
        }
    }

    private fun isTowerPlusCardSelection(state: GameState, action: Action): Boolean {
        if (action !is CardAction || action.getActionType() != CardAction.ActionType.SELECT_CARDS) return false
        val card = action.getCards()?.firstOrNull() ?: return false
        return card is CreatureCard && card.ability == Ability.PLUS_CARD
    }

    private fun isSelectCardsAction(action: Action): Boolean {
        return action is CardAction && action.actionType == CardAction.ActionType.SELECT_CARDS
    }

    private fun isUseAbilityAction(action: Action): Boolean {
        return action is PlayerAction && action.getActionType() == PlayerAction.ActionType.USE_ABILITY
    }

    private fun processAutomatedTransitions(state: GameState): GameState {
        var currentState = state
        var lastPhase: GameState.GamePhase? = null
        
        // Loop as long as we are in an automated transition phase and the phase actually changed
        while (currentState.gamePhase != lastPhase) {
            lastPhase = currentState.gamePhase
            currentState = when (currentState.gamePhase) {
                GameState.GamePhase.START -> handleStartPhase(currentState)
                GameState.GamePhase.QUEST -> handleQuestPhase(currentState)
                GameState.GamePhase.DRAW -> handleDrawPhase(currentState)
                GameState.GamePhase.NEXT_TURN -> handleAdvancePlayer(currentState)
                else -> return currentState // Break early if not an automated phase
            }
        }
        return currentState
    }

    private fun handleStartPhase(state: GameState): GameState {
        var currentBoard = state.board ?: return state
        var currentCreatureDeck = state.creatureDeck ?: return state
        var currentQuestDeck = state.questDeck ?: return state

        // Refill Empty Roads
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

        // Refill Empty Quests
        val updatedQuests = currentBoard.quests.toMutableList()
        for (i in updatedQuests.indices) {
            if (updatedQuests[i] == null) {
                val (newQuest, nextDeck) = currentQuestDeck.drawOne()
                if (newQuest != null) {
                    updatedQuests[i] = BoardQuest(
                        newQuest.id,
                        newQuest.name,
                        newQuest.title,
                        newQuest.vps + 1,
                        newQuest.gems,
                        newQuest.doubleReq,
                        newQuest.tripleReq,
                        newQuest.land
                    )
                    currentQuestDeck = nextDeck
                }
            }
        }
        currentBoard = currentBoard.copy(quests = updatedQuests)

        return state.copy(
            board = currentBoard,
            creatureDeck = currentCreatureDeck,
            questDeck = currentQuestDeck,
            gamePhase = GameState.GamePhase.OPEN
        )
    }

    private fun handleQuestPhase(state: GameState): GameState {
        // Quest completion logic is already handled in handleCompleteQuest.
        // This automated transition just moves it to DISCARD_OPEN.
        return state.copy(gamePhase = GameState.GamePhase.DISCARD_OPEN)
    }

    private fun handleDrawPhase(state: GameState, action: Action? = null): GameState {
        val playerIndex = state.currentPlayerIndex
        var player = state.players.getOrNull(playerIndex) ?: return state

        // 1. Revert any boosted cards (LookingGlass effect)
        val originalHand = player.hand.map { card ->
            if (card is CreatureCard) {
                // Find matching base creature template
                val base = CreatureCards.entries.find { it.name == card.name }
                if (base != null) {
                    val baseValues = if (base.value2 == Symbol.NONE) listOf(base.value1) else listOf(base.value1, base.value2)
                    if (card.values != baseValues) {
                        card.copy(values = baseValues)
                    } else card
                } else card
            } else card
        }
        player = player.copy(hand = originalHand)

        // 2. Discard selected cards
        val selectedCards = state.selectedCards.filterNotNull()
        player = player.discard(selectedCards)

        // 3. Draw up to 5
        val cardsToDraw = (5 - player.hand.size).coerceAtLeast(0)
        if (cardsToDraw > 0) {
            player = player.drawCards(cardsToDraw)
        }

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[playerIndex] = player

        return state.copy(
            players = updatedPlayers,
            gamePhase = GameState.GamePhase.NEXT_TURN
        )
    }

    private fun handleAdvancePlayer(state: GameState): GameState {
        val nextPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextTurnCount = state.turnCount + 1

        return state.copy(
            currentPlayerIndex = nextPlayerIndex,
            turnCount = nextTurnCount,
            selectedCards = emptyList(),
            selectedQuest = null,
            gamePhase = GameState.GamePhase.START,
            statusMessage = null
        )
    }

    private fun handlePlayerAction(state: GameState, action: PlayerAction): GameState {
        return when (action.getActionType()) {
            PlayerAction.ActionType.DRAW_CARDS -> handleDrawCards(state, action)
            PlayerAction.ActionType.USE_FLYING_CARPET -> handleUseFlyingCarpet(state, action)
            PlayerAction.ActionType.GAIN_GEMS -> handleGainGems(state, action)
            PlayerAction.ActionType.GAIN_TROPHIES -> handleGainTrophies(state, action)
            PlayerAction.ActionType.DISCARD_FROM_HAND -> handleDiscardCards(state, action)
            PlayerAction.ActionType.USE_ABILITY -> handleUseAbility(state, action)
            PlayerAction.ActionType.STORE_FOR_BOARD_QUEST -> handleBoardQuest(state, action)
            PlayerAction.ActionType.USE_TENT -> handleUseTent(state, action)
            PlayerAction.ActionType.RELEASE_CARDS -> handleReleaseCards(state, action)
            PlayerAction.ActionType.GAIN_CARD -> handleGainCardFromTower(state, action)
            PlayerAction.ActionType.START_TOWER_DRAW -> handleStartTowerDraw(state, action)
            PlayerAction.ActionType.RESOLVE_TOWER_DRAW -> handleResolveTowerDraw(state, action)
            PlayerAction.ActionType.SET_TOWER_MENU -> state.copy(towerMenuOpen = action.payload as Boolean)
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
            
            when (currentQuest) {
                is PlayerQuest -> {
                    if (currentQuest.canStoreCard(selectedCards[0])) {
                        return handleStoreCardForQuest(
                            newState,
                            QuestAction(
                                QuestAction.ActionType.STORE_CARD_FOR_QUEST,
                                newState.currentPlayerIndex,
                                currentQuest,
                                listOf(selectedCards[0])
                            )
                        )
                    }
                }
                is BoardQuest -> {
                    // Selection for BoardQuest triggers store for board quest (subdue-like)
                    return newState //handleBoardQuest(newState, PlayerAction(newState.currentPlayerIndex, PlayerAction.ActionType.STORE_FOR_BOARD_QUEST, selectedCards))
                }
            }
        }

        return newState
    }

    private fun handleSubdueAction(state: GameState, action: SubdueAction): GameState {
        return when (action.getActionType()) {
            SubdueAction.ActionType.SELECT_ROAD -> {
                val road = action.road ?: return state
                if (state.gamePhase == GameState.GamePhase.WARDROBE) {
                    val currentSelection = state.selectedRoads.toMutableList()
                    if (currentSelection.any { it.id == road.id }) {
                        currentSelection.removeAll { it.id == road.id }
                        state.copy(selectedRoads = currentSelection)
                    } else {
                        currentSelection.add(road)
                        if (currentSelection.size == 2) {
                            handleWardrobeComplete(state, currentSelection[0], currentSelection[1])
                        } else {
                            state.copy(selectedRoads = currentSelection)
                        }
                    }
                } else {
                    if (state.selectedRoad == road) {
                        state.copy(selectedRoad = null)
                    } else {
                        state.copy(selectedRoad = road, selectedQuest = null)
                    }
                }
            }
            else -> state
        }
    }

    private fun handleWardrobeComplete(state: GameState, road1: Road, road2: Road): GameState {
        var board = state.board ?: return state
        
        val regions1 = findRegionsForRoad(board, road1) ?: return state
        val regions2 = findRegionsForRoad(board, road2) ?: return state

        val newRoad1 = road1.copy(creature = road2.creature)
        val newRoad2 = road2.copy(creature = road1.creature)

        board = board.withRoad(regions1.first, regions1.second, newRoad1)
        board = board.withRoad(regions2.first, regions2.second, newRoad2)

        return state.copy(
            board = board,
            selectedRoads = emptyList(),
            gamePhase = state.previousPhase ?: GameState.GamePhase.OPEN,
            previousPhase = null
        )
    }

    private fun findRegionsForRoad(board: Board, road: Road): Pair<Region, Region>? {
        for ((r1, adj) in board.adjacencies) {
            for ((r2, r) in adj) {
                if (r.id == road.id) {
                    return Pair(r1, r2)
                }
            }
        }
        return null
    }

    private fun handleCardAction(state: GameState, action: CardAction): GameState {
        return when (action.actionType) {
            CardAction.ActionType.SELECT_CARDS -> handleSelectCards(state, action)
            else -> state
        }
    }

    private fun handleTurnAction(state: GameState, action: TurnAction): GameState {
        return when (action.getActionType()) {
            TurnAction.ActionType.NEXT_TURN -> state.copy(gamePhase = GameState.GamePhase.DRAW)
            TurnAction.ActionType.DONE_ADVENTURING -> state.copy(gamePhase = GameState.GamePhase.DISCARD_OPEN)
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

        // 1. Validate Phase for Flying Carpet
        if (action.moveType == MoveType.FLYING_CARPET) {
            if (state.gamePhase != GameState.GamePhase.OPEN && state.gamePhase != GameState.GamePhase.DISCARD_OPEN) {
                return state
            }
        }

        // 2. Validate the specific movement rules
        val selectedCards = state.selectedCards.filterNotNull()
        
        // Storage cards cannot be used for movement/subduing
        if (selectedCards.any { it in player.storage }) return state

        val isLegal = when (action.moveType) {
            MoveType.ADJACENT -> {
                val theRoad = state.board?.getRoad(currentRegion, action.destination) ?: return state
                if (action.useAbility) {
                    // Region selection: Must use Magic Carpet ability (Free Action)
                    (state.gamePhase == GameState.GamePhase.OPEN || state.gamePhase == GameState.GamePhase.DISCARD_OPEN) &&
                    selectedCards.size == 1 &&
                            selectedCards[0] is CreatureCard &&
                            (selectedCards[0] as CreatureCard).ability == Ability.MAGIC_CARPET
                } else {
                    // Road selection: Subdue or clear road (Turn Action)
                    if (theRoad.creature == null) {
                        false // Road is used, can not move except by flying
                    } else {
                        if (state.gamePhase != GameState.GamePhase.OPEN && state.gamePhase != GameState.GamePhase.SUBDUE) {
                            false
                        } else if (selectedCards.isEmpty()) {
                            false // Must select cards to subdue
                        } else {
                            // NORMAL SUBDUE
                            val validCombos = canSubdue(theRoad.creature, selectedCards)
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
                state.gamePhase == GameState.GamePhase.OPEN &&
                currentRegion.tower != null && currentRegion.tower == action.destination.tower && player.gems >= 2
            }
        }

        if (!isLegal) return state

        // 3. Apply "Costs" and update board
        var updatedBoard = state.board
        var updatedPlayer = player
        var nextPhase = state.gamePhase

        when (action.moveType) {
            MoveType.FLYING_CARPET -> updatedPlayer = player.useFlyingCarpet()
            MoveType.ADJACENT -> {
                val theRoad = state.board?.getRoad(currentRegion, action.destination) ?: return state
                if (action.useAbility) {
                    // Just discard the card and move (don't remove creature from board)
                    updatedPlayer = player.discard(selectedCards)
                } else if (theRoad.creature != null) {
                    // Normal subdue: remove creature and gain it
                    updatedBoard = state.board?.withRoad(currentRegion, action.destination, theRoad.copy(creature = null))
                    val gemBonus = if (theRoad.creature.gem) 1 else 0
                    updatedPlayer = player.gainCard(theRoad.creature).discard(selectedCards).withGems(player.gems + gemBonus)
                    nextPhase = GameState.GamePhase.SUBDUE
                }
            }
            MoveType.TOWER_KEY -> {
                updatedPlayer = player.withGems(player.gems - 2)
                nextPhase = if (state.isFreeTowerAction) state.gamePhase else GameState.GamePhase.DISCARD_OPEN // Teleporting is a Turn Action normally
            }
            else -> {}
        }

        // 4. Update the state
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer

        val updatedPositions = state.playerPositions.toMutableMap()
        updatedPositions[player.name] = action.destination

        return state.copy(
            board = updatedBoard,
            players = updatedPlayers,
            playerPositions = updatedPositions,
            selectedCards = emptyList(),
            selectedRoad = null,
            gamePhase = nextPhase,
            isFreeTowerAction = false,
            towerMenuOpen = false
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
        var roguesPurseMessage: String? = null

        if (selectedCards.isEmpty()) return state
        
        // Find the card being used as the "source" of the ability
        val sourceCard = selectedCards.find { it is Artifact && it.ability != Ability.NONE } 
                         ?: selectedCards.find { it is CreatureCard && it.ability != Ability.NONE }
                         ?: return state

        // Storage cards cannot use abilities
        if (player.storage.contains(sourceCard)) return state

        val updatedPlayers = state.players.toMutableList()
        var updatedPlayer = player 

        val ability = when (sourceCard) {
            is CreatureCard -> sourceCard.ability
            is Artifact -> sourceCard.ability
            else -> Ability.NONE
        }

        updatedPlayer = when (ability) {
            Ability.GEM -> {
                player.discard(listOf(sourceCard)).withGems(player.gems + 1)
            }
            Ability.DRAGON -> {
                val currentRegion = state.playerPositions[player.name]
                val otherPlayerIndex = state.players.indexOfFirst { 
                    it.name != player.name && state.playerPositions[it.name] == currentRegion 
                }
                
                if (otherPlayerIndex != -1) {
                    val otherPlayer = state.players[otherPlayerIndex]
                    updatedPlayers[otherPlayerIndex] = otherPlayer.gainCard(sourceCard)
                    // Remove from hand without discarding to own pile
                    player.copy(hand = player.hand - sourceCard)
                } else {
                    // No other player, just discard normally
                    player.discard(listOf(sourceCard))
                }
            }
            Ability.LOOKING_GLASS -> {
                // Requires source (LookingGlass) AND target (CreatureCard) to be selected
                val targetCard = selectedCards.find { it is CreatureCard && it != sourceCard } as? CreatureCard
                if (targetCard != null) {
                    // Double the values of the target card temporarily
                    val boostedCard = targetCard.copy(values = targetCard.values + targetCard.values)
                    
                    // Replace targetCard with boostedCard in hand
                    val newHand = player.hand.map { if (it.id == targetCard.id) boostedCard else it }
                    
                    // Discard the LookingGlass
                    player.copy(hand = newHand).discard(listOf(sourceCard))
                } else {
                    player // Do nothing if no valid target
                }
            }
            Ability.SUMMONING -> {
                // Bell of Summoning: Draw 3, Pick 1, other 2 go to discard
                // Handled in handleUseAbility to initiate the draw
                player.discard(listOf(sourceCard))
            }
            Ability.TOWER_KEY -> {
                player.discard(listOf(sourceCard))
            }
            Ability.BITTER_BREW -> {
                val targetCard = selectedCards.find { it != sourceCard }
                if (targetCard != null) {
                    val opponentIndex = (action.playerIndex + 1) % state.players.size
                    val isInHand = player.hand.contains(targetCard)
                    val isInDiscard = player.deck.discardPile.contains(targetCard)

                    if (isInHand || isInDiscard) {
                        updatedPlayers[opponentIndex] = updatedPlayers[opponentIndex].copy(
                            deck = updatedPlayers[opponentIndex].deck.discard(targetCard)
                        )
                        val playerWithoutTarget = if (isInHand) {
                            player.copy(hand = player.hand - targetCard)
                        } else {
                            player.copy(deck = player.deck.remove(targetCard))
                        }
                        playerWithoutTarget.discard(listOf(sourceCard))
                    } else player.discard(listOf(sourceCard))
                } else player.discard(listOf(sourceCard))
            }
            Ability.ROGUES_PURSE -> {
                val startingGems = player.gems
                var gemsGained = 0
                state.players.forEachIndexed { index, opponent ->
                    if (index != action.playerIndex && opponent.gems > startingGems) {
                        updatedPlayers[index] = opponent.withGems((opponent.gems - 1).coerceAtLeast(0))
                        gemsGained++
                    }
                }
                // ponytail: statusMessage is transient; cleared on next turn advance
                roguesPurseMessage = if (gemsGained > 0) "Rogue's Purse: stole $gemsGained gem(s)!" else "Rogue's Purse: no gems to steal."
                player.discard(listOf(sourceCard)).withGems(player.gems + gemsGained)
            }
            Ability.WARDROBE -> {
                player.discard(listOf(sourceCard))
            }
            else -> player.discard(listOf(sourceCard))
        }

        updatedPlayers[action.playerIndex] = updatedPlayer

        // If Summoning, we need to trigger the draw phase
        if (ability == Ability.SUMMONING) {
            val (drawn, nextDeck) = updatedPlayer.deck.draw(3)
            updatedPlayer = updatedPlayer.copy(deck = nextDeck)
            updatedPlayers[action.playerIndex] = updatedPlayer
            return state.copy(
                players = updatedPlayers,
                selectedCards = emptyList(),
                towerDrawnCards = drawn,
                gamePhase = GameState.GamePhase.SUMMONING,
                previousPhase = state.gamePhase
            )
        }

        return state.copy(
            players = updatedPlayers,
            selectedCards = emptyList(),
            gamePhase = if (ability == Ability.WARDROBE) GameState.GamePhase.WARDROBE else state.gamePhase,
            previousPhase = if (ability == Ability.WARDROBE) state.gamePhase else state.previousPhase,
            towerMenuOpen = if (ability == Ability.TOWER_KEY) true else state.towerMenuOpen,
            isFreeTowerAction = if (ability == Ability.TOWER_KEY) true else state.isFreeTowerAction,
            statusMessage = roguesPurseMessage
        )
    }

    private fun handleBoardQuest(state: GameState, action: PlayerAction): GameState {
        val player = action.getCurrentPlayer(state) ?: return state
        val selectedCards = state.selectedCards.filterNotNull()

        if (selectedCards.isEmpty()) return state
        
        // Cannot store cards already in storage
        if (selectedCards.any { it in player.storage }) return state

        val updatedPlayer = player.storeForBoardQuest(selectedCards)
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
            val targetQuest = player.quests[qIndex] as? PlayerQuest ?: return state

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

            updatedPlayer = player.discard(selectedCards).copy(
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
            selectedQuest = null,
            gamePhase = GameState.GamePhase.QUEST
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

        val targetQuest = playerQuests[qIndex] as? PlayerQuest ?: return state
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
        val quest = (action.quest as? BoardQuest) ?: return state

        val updatedPlayer = player.drawQuest(quest)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer

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
        
        return state.copy(
            players = updatedPlayers,
            selectedCards = emptyList(),
            isFreeTowerAction = false,
            towerMenuOpen = false
        )
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
            updatedPlayer = player.discard(plusCards)
        }
        
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            towerDrawnCards = drawn,
            bazaarDeck = if (tower == TowerName.BAZAAR) nextDeck as? Deck<CreatureCard> else state.bazaarDeck,
            questDeck = if (tower == TowerName.QUEST) nextDeck as? Deck<BoardQuest> else state.questDeck,
            artifactDeck = if (tower == TowerName.ARTIFACT) nextDeck as? Deck<Artifact> else state.artifactDeck,
            selectedCards = emptyList(), // Clear selection after use
            gamePhase = GameState.GamePhase.TOWER,
            previousPhase = state.gamePhase,
            towerMenuOpen = false
        )
    }

    private fun handleResolveTowerDraw(state: GameState, action: PlayerAction): GameState {
        val selectedCards = action.payload as? List<Card> ?: emptyList()
        val player = action.getCurrentPlayer(state) ?: return state
        
        if (state.gamePhase == GameState.GamePhase.SUMMONING) {
            // Special handling for Bell of Summoning
            val allDrawn = state.towerDrawnCards
            val unselected = allDrawn.filter { card -> selectedCards.none { it.id == card.id } }
            
            var updatedPlayer = player
            // Take the selected card (usually just 1) to hand
            selectedCards.forEach { card ->
                updatedPlayer = updatedPlayer.copy(hand = updatedPlayer.hand + card)
            }
            
            // Unselected go to player's discard pile
            unselected.forEach { card ->
                updatedPlayer = updatedPlayer.copy(deck = updatedPlayer.deck.discard(card))
            }
            
            val updatedPlayers = state.players.toMutableList()
            updatedPlayers[action.playerIndex] = updatedPlayer
            
            return state.copy(
                players = updatedPlayers,
                towerDrawnCards = emptyList(),
                gamePhase = state.previousPhase ?: GameState.GamePhase.DISCARD_OPEN,
                previousPhase = null
            )
        }

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
                    TowerName.QUEST -> if (card is BoardQuest) updatedQuestDeck = updatedQuestDeck?.putOnBottom(listOf(card))
                    TowerName.ARTIFACT -> if (card is Artifact) updatedArtifactDeck = updatedArtifactDeck?.putOnBottom(listOf(card))
                }
            }
        }

        // Unselected go to bottom
        when (tower) {
            TowerName.BAZAAR -> updatedBazaarDeck = updatedBazaarDeck?.putOnBottom(unselected.filterIsInstance<CreatureCard>())
            TowerName.QUEST -> updatedQuestDeck = updatedQuestDeck?.putOnBottom(unselected.filterIsInstance<BoardQuest>())
            TowerName.ARTIFACT -> updatedArtifactDeck = updatedArtifactDeck?.putOnBottom(unselected.filterIsInstance<Artifact>())
        }

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[action.playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            bazaarDeck = updatedBazaarDeck,
            questDeck = updatedQuestDeck,
            artifactDeck = updatedArtifactDeck,
            towerDrawnCards = emptyList(),
            gamePhase = if (state.isFreeTowerAction) (state.previousPhase ?: GameState.GamePhase.OPEN) else GameState.GamePhase.DISCARD_OPEN,
            previousPhase = if (state.isFreeTowerAction) null else state.previousPhase,
            isFreeTowerAction = false,
            towerMenuOpen = false
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
            when (quest) {
                is PlayerQuest -> {
                    if (quest.canStoreCard(currentSelection[0])) {
                        return handleStoreCardForQuest(
                            newState,
                            QuestAction(
                                QuestAction.ActionType.STORE_CARD_FOR_QUEST,
                                newState.currentPlayerIndex,
                                quest,
                                listOf(currentSelection[0])
                            )
                        )
                    }
                }

                is BoardQuest -> {
                    return handleStorePrivate(
                        state, PlayerAction(
                            newState.currentPlayerIndex,
                            PlayerAction.ActionType.STORE_FOR_BOARD_QUEST,
                            listOf(currentSelection[0])
                        )
                    )
                }
            }
        }

        return newState
    }

    private fun handleStorePrivate(state: GameState, action: PlayerAction): GameState {
        val cardsToStore = action.payload as? List<Card> ?: return state
        val playerIndex = action.playerIndex
        val player =  action.getCurrentPlayer(state) ?: return state

        val updatedPlayer = player.storeForBoardQuest(cardsToStore)

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

        val updatedPlayer = player.discard(cardsToDiscard)

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            selectedCards = emptyList(),
            gamePhase = state.gamePhase ?: GameState.GamePhase.PLAYER_TURN
        )
    }

    private fun handleAdvancePhase(state: GameState, action: TurnAction): GameState {
        return when (state.gamePhase) {
            GameState.GamePhase.OPEN -> state.copy(gamePhase = GameState.GamePhase.DISCARD_OPEN)
            GameState.GamePhase.SUBDUE -> state.copy(gamePhase = GameState.GamePhase.DISCARD_OPEN)
            GameState.GamePhase.TOWER -> state.copy(gamePhase = GameState.GamePhase.DISCARD_OPEN)
            GameState.GamePhase.QUEST -> state.copy(gamePhase = GameState.GamePhase.DISCARD_OPEN)
            else -> state.copy(gamePhase = GameState.GamePhase.DISCARD_OPEN)
        }
    }

    fun canSubdue(
        aCreature: CreatureCard?,
        aHand: List<Card>?
    ): List<MutableSet<Card>> {
        if (aCreature == null) return emptyList()
        return if (aCreature.values.size > 1) {
            canSubdueDouble(aCreature, aHand)
        } else {
            canSubdueSingle(aCreature, aHand)
        }
    }

    //TODO multi symbol beasts
    fun canSubdueSingle(
        aCreature: CreatureCard?,
        aHand: List<Card>?
    ): List<MutableSet<Card>> {
        val fullList: MutableList<MutableSet<Card>> = ArrayList<MutableSet<Card>>()

        val handSymbols: MutableList<Symbol?> = ArrayList<Symbol?>()
        //Check hand for first symbol
        if (aHand != null) {
            for (aCard in aHand) {
                if (aCard is CreatureCard && aCard.values.isNotEmpty() && aCard.values[0] != Symbol.NONE) {
                    val handCreature = aCard
                    if (aCreature?.subduedBy == handCreature.values[0]) {
                        //symbol match
                        fullList.add(mutableSetOf<Card>(aCard))
                        continue
                    }
                    if (handCreature.values.size > 1 && handCreature.values[0] == handCreature.values[1])
                    {
                        //Double symbol as wildcard
                        fullList.add(mutableSetOf<Card>(aCard))
                        continue
                    }
                    if (handSymbols.contains(handCreature.values[0])) {
                        //this is at least second symbol in the hand so wildcard is in effect but
                        //add these cards later after we have completed this first walkthrough of the hand
                    } else {
                        handSymbols.add(handCreature.values[0])
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
                        aCard.values.isNotEmpty() &&
                        aCard.values[0] == match
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

    //TODO symbol list >2 eg. LookingGlass on a double symbol
    fun canSubdueDouble(
        toSubdue: CreatureCard?,
        aHand: List<Card>?
    ): List<MutableSet<Card>> {
        val fullList: MutableList<MutableSet<Card>> = ArrayList<MutableSet<Card>>()
        val singleWildSets: MutableList<MutableList<Card>> = ArrayList<MutableList<Card>>()
        val singleNonMatch: MutableList<MutableList<Card>> = ArrayList<MutableList<Card>>()
        if (aHand != null)
          for (aCard in aHand) {
            if (aCard is CreatureCard && aCard.values.isNotEmpty() && aCard.values[0] != Symbol.NONE) {
                val handCreature = aCard
                if (handCreature.values.size > 1 && handCreature.values[0] == handCreature.values[1]
                ) {
                    //double symbol
                    if (toSubdue?.subduedBy == handCreature.values[0]) {
                        fullList.add(mutableSetOf<Card>(aCard))
                    } else {
                        singleWildSets.add(mutableListOf<Card>(aCard))
                    }
                } else if (toSubdue?.subduedBy == handCreature.values[0]) {
                    //single symbol match
                    singleWildSets.add(mutableListOf<Card>(aCard))
                } else {
                    //single miss
                    //is the symbol already in the list
                    var bInSingles = false
                    for (singleSet in singleNonMatch) {
                        if (singleSet.isNotEmpty() && (singleSet[0] as CreatureCard).values[0] == handCreature.values[0]
                        ) {
                            singleSet.add(aCard)
                            bInSingles = true
                            continue
                        }
                    }
                    if (!bInSingles) {
                        val newSingle: MutableList<Card> = ArrayList<Card>()
                        newSingle.add(aCard)
                        singleNonMatch.add(newSingle)
                    }
                }
            }
        }

        var combos: Combinations?
        var comboCards: MutableList<Card>?
        for (singles in singleNonMatch) {
            for (i in 0 until (singles.size - 1) step 2) {
                val comboCards = MutableList<Card>(1, { _ -> singles[i] })
                comboCards.add(singles[i + 1])
                singleWildSets.add(comboCards)
            }
        }
        if (singleWildSets.size >= 2) {
            combos = Combinations(singleWildSets.size, 2)
            for (pairing in combos) {
                val comboCardSet = HashSet<Card>()
                comboCardSet.addAll(singleWildSets[pairing[0]])
                comboCardSet.addAll(singleWildSets[pairing[1]])
                fullList.add(comboCardSet)
            }
        }

        return fullList
    }

}
