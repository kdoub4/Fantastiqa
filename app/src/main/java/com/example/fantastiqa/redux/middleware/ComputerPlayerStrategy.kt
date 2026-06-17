package com.example.fantastiqa.redux.middleware

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.redux.Action
import com.example.fantastiqa.redux.GameEngine
import com.example.fantastiqa.redux.GameState
import com.example.fantastiqa.redux.actions.*

interface ComputerPlayerStrategy {
    fun evaluateNextAction(state: GameState): Action?
}

class BasicComputerStrategy : ComputerPlayerStrategy {
    private val engine = GameEngine()
    private var lastMoveTurn = -1
    private var lastMovePlayerIndex = -1
    private var usedCarpetTokenTurn = -1

    override fun evaluateNextAction(state: GameState): Action? {
        val player = state.currentPlayer ?: return null
        if (!player.isComputer) return null

        val currentRegion = state.playerPositions[player.name] ?: return null
        val hand = player.hand
        val storage = player.storage
        val selectedCards = state.selectedCards.filterNotNull()
        val selectedRoad = state.selectedRoad
        val selectedQuest = state.selectedQuest
        val board = state.board ?: return null

        // 1. If a quest is selected, focus on reaching its region and completing it
        if (selectedQuest != null) {
            val isPlayerQuest = player.quests.any { it.id == selectedQuest.id }
            val availablePool = if (isPlayerQuest) hand + selectedQuest.stored else hand + storage
            val combo = findFulfillmentCombo(selectedQuest, availablePool)

            if (combo == null) {
                // Cannot fulfill anymore, deselect
                return QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, null, emptyList())
            }

            if (currentRegion.name == selectedQuest.land) {
                // At target region!
                if (selectedCards.containsAll(combo) && selectedCards.size == combo.size) {
                    // All cards selected, complete quest
                    return QuestAction(QuestAction.ActionType.COMPLETE_QUEST, state.currentPlayerIndex, selectedQuest, emptyList())
                } else {
                    // Need to select cards (from hand or storage)
                    val nextToSelect = combo.find { it !in selectedCards } ?: selectedCards.find { it !in combo }
                    if (nextToSelect != null) {
                        return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(nextToSelect))
                    }
                }
            } else if (state.gamePhase == GameState.GamePhase.OPEN) {
                // Not at region yet, move towards it
                val usedTokenThisTurn = usedCarpetTokenTurn == state.turnCount && lastMovePlayerIndex == state.currentPlayerIndex

                val nextMove = findNextActionTowards(selectedQuest.land, state, usedTokenThisTurn)
                if (nextMove != null) return nextMove
            }
        }

        // 2. If a road is selected, focus on subduing its creature (Turn Action)
        if (selectedRoad != null) {
            val roadCreature = selectedRoad.creature
            if (roadCreature == null) {
                return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, null, null, null)
            }
            
            val validCombos = engine.canSubdue(roadCreature, hand)
            if (validCombos.isEmpty()) {
                return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, null, null, null)
            }

            val bestCombo = validCombos.first()
            if (selectedCards.containsAll(bestCombo)) {
                val destination = board.getAdjacentAreas(currentRegion).find { it.first == selectedRoad }?.second ?: return null
                
                // Track movement
                lastMoveTurn = state.turnCount
                lastMovePlayerIndex = state.currentPlayerIndex
                
                return MoveAction(state.currentPlayerIndex, destination, MoveType.ADJACENT, useAbility = false)
            } else {
                val nextCard = bestCombo.find { it !in selectedCards }
                if (nextCard != null) {
                    return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(nextCard))
                }
            }
        }

        // 3. START OF TURN / OPEN PHASE DECISIONS
        if (state.gamePhase == GameState.GamePhase.OPEN && selectedCards.isEmpty() && selectedRoad == null && selectedQuest == null) {
            
            // A. Check Board Quests (Hand + Storage)
            for (quest in board.quests.filterNotNull()) {
                if (findFulfillmentCombo(quest, hand + storage) != null) {
                    return QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, quest, emptyList())
                }
            }

            // B. Check Personal Quests (Hand + Stored)
            for (quest in player.quests.filterIsInstance<Quest>()) {
                if (findFulfillmentCombo(quest, hand + quest.stored) != null) {
                    return QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, quest, emptyList())
                }
            }

            // TODO: Add Tower Visit logic (Quest, Bazaar, Artifact)
            
            // C. Look for subduable creatures adjacent to expand hand/resources
            val adjacentRoads = board.getAdjacentAreas(currentRegion).shuffled()
            for (pair in adjacentRoads) {
                val road = pair.first
                val roadCreature = road.creature ?: continue
                if (engine.canSubdue(roadCreature, hand).isNotEmpty()) {
                    return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, road, null, null)
                }
            }

            // D. Try to fly to an adjacent region to subdue (ONLY IF NOT MOVED YET)
            val hasMovedThisTurn = state.turnCount == lastMoveTurn && state.currentPlayerIndex == lastMovePlayerIndex
            if (player.flyingCarpets > 0 && !hasMovedThisTurn) {
                for (pair in adjacentRoads) {
                    val destination = pair.second
                    val destAdj = board.getAdjacentAreas(destination).shuffled()
                    for (destPair in destAdj) {
                        val destRoad = destPair.first
                        val destCreature = destRoad.creature ?: continue
                        if (engine.canSubdue(destCreature, hand).isNotEmpty()) {
                            lastMoveTurn = state.turnCount
                            lastMovePlayerIndex = state.currentPlayerIndex
                            return MoveAction(state.currentPlayerIndex, destination, MoveType.FLYING_CARPET)
                        }
                    }
                }
            }

            // E. Check for Dog/Dragon abilities
            val specialCard = hand.find { it is CreatureCard && (it.ability == Ability.GEM || it.ability == Ability.DRAGON) }
            if (specialCard != null) {
                return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(specialCard))
            }

            // F. Discard hand if nothing else to do
            if (hand.isNotEmpty()) {
                return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(hand.first()))
            }
        }

        // 4. MISC HANDLERS (Ability use, Discarding)
        if (selectedCards.size == 1 && selectedQuest == null && selectedRoad == null) {
            val card = selectedCards[0]
            if (card is CreatureCard && (card.ability == Ability.GEM || card.ability == Ability.DRAGON)) {
                return PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null)
            }
        }

        if (selectedCards.isNotEmpty() && selectedQuest == null && selectedRoad == null) {
            if (selectedCards.size < hand.size) {
                val nextToDiscard = hand.find { it !in selectedCards }
                if (nextToDiscard != null) return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(nextToDiscard))
            }
            return TurnAction(TurnAction.ActionType.NEXT_TURN)
        }

        // 5. PHASE ENDS
        if (state.gamePhase == GameState.GamePhase.SUBDUE) {
            return TurnAction(TurnAction.ActionType.DONE_ADVENTURING)
        }
        
        return TurnAction(TurnAction.ActionType.NEXT_TURN)
    }

    private fun findFulfillmentCombo(quest: Quest, cards: List<Card>): List<Card>? {
        val reqs = quest.getRequirements()
        val creaturePool = cards.filterIsInstance<CreatureCard>().toMutableList()
        val remainingReqs = reqs.toMutableList()
        val combo = mutableListOf<Card>()

        while (remainingReqs.isNotEmpty()) {
            val req = remainingReqs[0]
            val card = creaturePool.find { it.values.contains(req) } ?: return null
            combo.add(card)
            creaturePool.remove(card)
            // Remove all symbols this card provides from current needs
            for (symbol in card.values) {
                remainingReqs.remove(symbol)
            }
        }
        return combo
    }

    private fun findNextActionTowards(target: RegionName, state: GameState, usedToken: Boolean): Action? {
        val player = state.currentPlayer ?: return null
        val currentRegion = state.playerPositions[player.name] ?: return null
        val board = state.board ?: return null
        val hand = player.hand

        // Use a more robust pathfinding that accounts for move types
        val path = findShortestPath(currentRegion, target, board, hand, player.flyingCarpets > 0 && !usedToken, player.gems >= 2) ?: return null
        val nextStep = path.firstOrNull() ?: return null

        return when (nextStep.moveType) {
            MoveType.ADJACENT -> {
                if (nextStep.useAbility) {
                    // Magic Carpet card
                    val carpetCard = hand.find { it is CreatureCard && it.ability == Ability.MAGIC_CARPET }
                    if (carpetCard != null) {
                        if (carpetCard !in state.selectedCards) {
                            return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(carpetCard))
                        }
                        return MoveAction(state.currentPlayerIndex, nextStep.destination, MoveType.ADJACENT, useAbility = true)
                    }
                } else {
                    // Road Subdue (Turn Action)
                    val road = board.getRoad(currentRegion, nextStep.destination) ?: return null
                    if (road.creature != null) {
                        if (engine.canSubdue(road.creature, hand).isNotEmpty()) {
                            return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, road, null, null)
                        }
                    }
                }
                null
            }
            MoveType.FLYING_CARPET -> {
                usedCarpetTokenTurn = state.turnCount
                lastMovePlayerIndex = state.currentPlayerIndex
                MoveAction(state.currentPlayerIndex, nextStep.destination, MoveType.FLYING_CARPET)
            }
            MoveType.TOWER_KEY -> {
                // Tower Key (could be card ability or free teleport if I add that, but here it's MoveType)
                // If it's a card ability, we should select the card first
                val keyCard = hand.find { it is CreatureCard && it.ability == Ability.TOWER_KEY }
                if (keyCard != null && keyCard !in state.selectedCards) {
                     // Actually, current MoveAction for TOWER_KEY just checks gems. 
                     // But if the user meant using card abilities for teleport, I should select it.
                     // The request says "use as many hand card abilities (flying_carpet and/or tower_key for tower teleport) as needed"
                     return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(keyCard))
                }
                MoveAction(state.currentPlayerIndex, nextStep.destination, MoveType.TOWER_KEY)
            }
        }
    }

    private data class PathStep(val destination: Region, val moveType: MoveType, val useAbility: Boolean = false)

    private fun findShortestPath(start: Region, targetName: RegionName, board: Board, hand: List<Card>, canUseToken: Boolean, canTeleport: Boolean): List<PathStep>? {
        val queue = mutableListOf(start to emptyList<PathStep>())
        val visited = mutableSetOf(start)
        
        val hasMagicCarpetCard = hand.any { it is CreatureCard && it.ability == Ability.MAGIC_CARPET }
        val hasTowerKeyCard = hand.any { it is CreatureCard && it.ability == Ability.TOWER_KEY }

        while (queue.isNotEmpty()) {
            val (current, path) = queue.removeAt(0)
            if (current.name == targetName) return path

            // 1. Adjacent Regions via Roads
            for (pair in board.getAdjacentAreas(current)) {
                val road = pair.first
                val destination = pair.second
                if (destination in visited) continue

                // Option A: Magic Carpet Card (Free Action)
                if (hasMagicCarpetCard) {
                    visited.add(destination)
                    queue.add(destination to path + PathStep(destination, MoveType.ADJACENT, useAbility = true))
                    continue
                }

                // Option B: Flying Carpet Token (Free Action, at most 1 per turn)
                if (canUseToken && path.none { it.moveType == MoveType.FLYING_CARPET }) {
                    visited.add(destination)
                    queue.add(destination to path + PathStep(destination, MoveType.FLYING_CARPET))
                    continue
                }

                // Option C: Subdue (Turn Action, at most 1 per turn in OPEN phase)
                if (road.creature != null && engine.canSubdue(road.creature, hand).isNotEmpty()) {
                    if (path.none { it.moveType == MoveType.ADJACENT && !it.useAbility && !isTeleport(it.moveType) }) {
                        visited.add(destination)
                        queue.add(destination to path + PathStep(destination, MoveType.ADJACENT, useAbility = false))
                    }
                }
            }

            // 2. Tower Teleport (Turn Action)
            if (canTeleport && current.tower != null) {
                val destination = board.getTowerMatch(current)
                if (destination != null && destination !in visited) {
                    // Only 1 turn action allowed
                    if (path.none { (it.moveType == MoveType.ADJACENT && !it.useAbility) || it.moveType == MoveType.TOWER_KEY }) {
                        visited.add(destination)
                        queue.add(destination to path + PathStep(destination, MoveType.TOWER_KEY))
                    }
                }
            }
        }
        return null
    }

    private fun isTeleport(type: MoveType): Boolean = type == MoveType.TOWER_KEY
}
