package com.example.fantastiqa.redux.middleware

import com.example.fantastiqa.gameState.Ability
import com.example.fantastiqa.gameState.CreatureCard
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

    override fun evaluateNextAction(state: GameState): Action? {
        val player = state.currentPlayer ?: return null
        if (!player.isComputer) return null

        val hasMovedThisTurn = state.turnCount == lastMoveTurn && state.currentPlayerIndex == lastMovePlayerIndex
        val selectedCards = state.selectedCards.filterNotNull()
        val selectedRoad = state.selectedRoad
        val hand = player.hand

        // 1. If a road is selected, focus on subduing its creature
        if (selectedRoad != null) {
            val roadCreature = selectedRoad.creature
            if (roadCreature == null) {
                // Cannot subdue this creature (it might be gone), deselect road
                return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, null, null, null)
            }
            
            val validCombos = engine.canSubdue(roadCreature, hand)
            if (validCombos.isEmpty()) {
                // Cannot subdue this creature, deselect road
                return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, null, null, null)
            }

            val bestCombo = validCombos.first()
            if (selectedCards.containsAll(bestCombo)) {
                // All cards for combo selected, MOVE
                val currentRegion = state.playerPositions[player.name] ?: return null
                val destination = state.board?.getAdjacentAreas(currentRegion)?.find { it.first == selectedRoad }?.second ?: return null
                
                // Track that we moved
                lastMoveTurn = state.turnCount
                lastMovePlayerIndex = state.currentPlayerIndex
                
                return MoveAction(state.currentPlayerIndex, destination, MoveType.ADJACENT, useAbility = false)
            } else {
                // Need to select more cards for this combo
                val nextCard = bestCombo.find { it !in selectedCards }
                if (nextCard != null) {
                    return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(nextCard))
                }
            }
        }

        // 2. No road selected, look for subduable creatures or special abilities
        if (selectedCards.isEmpty()) {

            // Try to find a subduable creature on an adjacent road
            val currentRegion = state.playerPositions[player.name] ?: return null
            val adjacentRoads = state.board?.getAdjacentAreas(currentRegion)?.shuffled() ?: emptyList()
            
            for (pair in adjacentRoads) {
                val road = pair.first
                val roadCreature = road.creature ?: continue
                
                val validCombos = engine.canSubdue(roadCreature, hand)
                if (validCombos.isNotEmpty()) {
                    // SELECT ROAD FIRST
                    return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, road, null, null)
                }
            }

            // Try to fly to an adjacent region to subdue - ONLY IF NOT MOVED YET
            if (player.flyingCarpets > 0 && !hasMovedThisTurn) {
                for (pair in adjacentRoads) {
                    val destination = pair.second
                    val destAdj = state.board?.getAdjacentAreas(destination)?.shuffled() ?: emptyList()
                    
                    for (destPair in destAdj) {
                        val destRoad = destPair.first
                        val destCreature = destRoad.creature ?: continue
                        
                        val validCombos = engine.canSubdue(destCreature, hand)
                        if (validCombos.isNotEmpty()) {
                            // Track that we moved (will move by flying)
                            lastMoveTurn = state.turnCount
                            lastMovePlayerIndex = state.currentPlayerIndex

                            return MoveAction(state.currentPlayerIndex, destination, MoveType.FLYING_CARPET)
                        }
                    }
                }
            }

            // Check for Dog (GEM) and Peaceful Dragon (DRAGON) ability if in hand
            val specialCard = hand.find { it is CreatureCard && (it.ability == Ability.GEM || it.ability == Ability.DRAGON) }
            if (specialCard != null) {
                return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(specialCard))
            }

            // Discard entire hand at turn end
            if (hand.isNotEmpty()) {
                return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(hand.first()))
            }

            // Nothing left to do
            if (state.gamePhase == GameState.GamePhase.SUBDUE) {
                return TurnAction(TurnAction.ActionType.DONE_ADVENTURING)
            }
            return TurnAction(TurnAction.ActionType.NEXT_TURN)

        } else {
            // Cards are selected but no road selected (likely discarding or using ability)
            
            // If it's a special card, use ability
            if (selectedCards.size == 1) {
                val card = selectedCards[0]
                if (card is CreatureCard && (card.ability == Ability.GEM || card.ability == Ability.DRAGON)) {
                    return PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null)
                }
            }

            // If we are discarding
            if (selectedCards.size < hand.size) {
                val nextToDiscard = hand.find { it !in selectedCards }
                if (nextToDiscard != null) {
                    return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(nextToDiscard))
                }
            }
            // End turn
            return TurnAction(TurnAction.ActionType.NEXT_TURN)
        }
    }
}
