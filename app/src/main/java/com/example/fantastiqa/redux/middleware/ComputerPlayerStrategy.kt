package com.example.fantastiqa.redux.middleware

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.redux.Action
import com.example.fantastiqa.redux.GameEngine
import com.example.fantastiqa.redux.GameState
import com.example.fantastiqa.redux.actions.*
import java.util.PriorityQueue

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

        // Adventuring
        val adjacentRoads = board.getAdjacentAreas(currentRegion).shuffled()

        if (state.gamePhase == GameState.GamePhase.SUBDUE) {
            val nextRoad = findNextRoad(adjacentRoads, currentRegion, hand)
            return if (nextRoad != null)
                SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, nextRoad, null, null)
            else
                TurnAction(TurnAction.ActionType.DONE_ADVENTURING)
        }

        // 3. START OF TURN / OPEN PHASE DECISIONS
        if (state.gamePhase == GameState.GamePhase.OPEN && selectedCards.isEmpty() && selectedRoad == null && selectedQuest == null) {
            // E. Check for Dog/Dragon abilities
            // Nothing else can be done with these at this point
            // TODO Expansions
            val specialCard = hand.find { it is CreatureCard && (it.ability == Ability.GEM || it.ability == Ability.DRAGON) }
            if (specialCard != null) {
                return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(specialCard))
            }

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
            val nextRoad = findNextRoad(adjacentRoads, currentRegion, hand)
            if (nextRoad != null)
                return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, nextRoad, null, null)

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

            return TurnAction(TurnAction.ActionType.ADVANCE_PHASE)


        }

        // 4. MISC HANDLERS (Ability use, Discarding)
        if (selectedCards.size == 1 && selectedQuest == null && selectedRoad == null) {
            val card = selectedCards[0]
            if (card is CreatureCard && (card.ability == Ability.GEM || card.ability == Ability.DRAGON)) {
                return PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null)
            }
        }

        if (hand.isEmpty())
            return TurnAction(TurnAction.ActionType.NEXT_TURN)

        // Discard hand
        if (selectedCards.isEmpty()) {
            return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(hand.first()))
        }
        else {
            if (selectedCards.size < hand.size) {
                val nextToDiscard = hand.find { it !in selectedCards }
                if (nextToDiscard != null) return CardAction(
                    CardAction.ActionType.SELECT_CARDS,
                    state.currentPlayerIndex,
                    listOf(nextToDiscard)
                )
            }
            return PlayerAction(state.currentPlayerIndex,PlayerAction.ActionType.DISCARD_FROM_HAND,  selectedCards)
        }

        return TurnAction(TurnAction.ActionType.NEXT_TURN)
    }

    private fun findNextRoad(adjacentRoads: List<Pair<Road, Region>>? , currentRegion: Region, hand: List<Card>): Road? {
        if (adjacentRoads != null) {
            for (pair in adjacentRoads) {
                val road = pair.first
                val roadCreature = road.creature ?: continue
                if (engine.canSubdue(roadCreature, hand).isNotEmpty()) {
                    return road
                }
            }
        }
        return null
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

        // Use prioritized pathfinding
        val path = findShortestPath(currentRegion, target, board, hand, player.flyingCarpets > 0 && !usedToken, player.gems >= 2)
        if (path == null) {
            println("AI: No path found to $target from ${currentRegion.name}")
            return null
        }
        println("AI: Path to $target: ${path.map { it.moveType }} (isFree: ${path.map { it.isFree() }})")
        
        // Determine if we should prioritize free actions. 
        // We do this if the target can be reached using ONLY free actions (including token).
        val canReachTargetWithFreeOnly = path.all { it.isFree() }
        
        val nextStep = if (canReachTargetWithFreeOnly) {
            path.firstOrNull()
        } else {
            // Target is not reachable via free actions alone.
            // Find the first turn action in the path, or the first free action if it helps progress towards it.
            path.find { !it.isFree() } ?: path.firstOrNull()
        } ?: return null

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
                if (nextStep.useAbility) {
                    val keyCard = hand.find { it is CreatureCard && it.ability == Ability.TOWER_KEY }
                    if (keyCard != null) {
                        if (keyCard !in state.selectedCards) {
                            return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(keyCard))
                        }
                        // If it's the free action part (isFreeTowerAction is NOT yet set, we need to USE_ABILITY)
                        if (!state.isFreeTowerAction) {
                             return PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null)
                        }
                    }
                }
                // If it's the MoveAction itself (either turn action or after USE_ABILITY)
                MoveAction(state.currentPlayerIndex, nextStep.destination, MoveType.TOWER_KEY)
            }
        }
    }

    private data class PathStep(val destination: Region, val moveType: MoveType, val useAbility: Boolean = false) {
        fun isFree(): Boolean = moveType == MoveType.FLYING_CARPET || (moveType == MoveType.ADJACENT && useAbility) || (moveType == MoveType.TOWER_KEY && useAbility)
    }

    private data class Node(val region: Region, val path: List<PathStep>, val turnActionUsed: Boolean, val carpetTokenUsed: Boolean) : Comparable<Node> {
        // Priority: fewer turn actions, then shorter path
        override fun compareTo(other: Node): Int {
            if (this.turnActionUsed != other.turnActionUsed) {
                return if (this.turnActionUsed) 1 else -1
            }
            return this.path.size.compareTo(other.path.size)
        }
    }

    private fun findShortestPath(start: Region, targetName: RegionName, board: Board, hand: List<Card>, canUseToken: Boolean, canTeleport: Boolean): List<PathStep>? {
        val pq = PriorityQueue<Node>()
        pq.add(Node(start, emptyList(), false, !canUseToken))
        
        // visited: Region -> turnActionUsed -> carpetTokenUsed -> shortest path size
        val visited = mutableMapOf<Triple<Region, Boolean, Boolean>, Int>()

        val hasMagicCarpetCard = hand.any { it is CreatureCard && it.ability == Ability.MAGIC_CARPET }
        val hasTowerKeyCard = hand.any { it is CreatureCard && it.ability == Ability.TOWER_KEY }

        while (pq.isNotEmpty()) {
            val node = pq.poll() ?: continue
            val (current, path, turnActionUsed, carpetTokenUsed) = node
            if (current.name == targetName) return path

            val stateKey = Triple(current, turnActionUsed, carpetTokenUsed)
            if (visited.getOrDefault(stateKey, Int.MAX_VALUE) <= path.size) continue
            visited[stateKey] = path.size

            // 1. Adjacent Regions via Roads
            for (pair in board.getAdjacentAreas(current)) {
                val road = pair.first
                val destination = pair.second

                // Option A: Magic Carpet Card (Free Action)
                if (hasMagicCarpetCard) {
                    pq.add(Node(destination, path + PathStep(destination, MoveType.ADJACENT, useAbility = true), turnActionUsed, carpetTokenUsed))
                }

                // Option B: Flying Carpet Token (Free Action, at most 1 per turn)
                if (!carpetTokenUsed) {
                    pq.add(Node(destination, path + PathStep(destination, MoveType.FLYING_CARPET), turnActionUsed, true))
                }

                // Option C: Subdue (Turn Action, at most 1 per turn in OPEN phase)
                if (!turnActionUsed) {
                    val roadCreature = road.creature
                    if (roadCreature != null && engine.canSubdue(roadCreature, hand).isNotEmpty()) {
                        pq.add(Node(destination, path + PathStep(destination, MoveType.ADJACENT, useAbility = false), true, carpetTokenUsed))
                    }
                }
            }

            // 2. Tower Teleport
            if (current.tower != null) {
                val destination = board.getTowerMatch(current)
                if (destination != null) {
                    // Option A: Tower Key Card (Free Action)
                    if (hasTowerKeyCard) {
                        pq.add(Node(destination, path + PathStep(destination, MoveType.TOWER_KEY, useAbility = true), turnActionUsed, carpetTokenUsed))
                    }
                    
                    // Option B: Teleport (Turn Action, costs 2 gems)
                    if (!turnActionUsed && canTeleport) {
                        pq.add(Node(destination, path + PathStep(destination, MoveType.TOWER_KEY, useAbility = false), true, carpetTokenUsed))
                    }
                }
            }
        }
        return null
    }

}
