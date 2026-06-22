package com.example.fantastiqa.redux.middleware

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.redux.Action
import com.example.fantastiqa.redux.GameEngine
import com.example.fantastiqa.redux.GameState
import com.example.fantastiqa.redux.actions.*
import java.util.*

class RogueMouserStrategy : ComputerPlayerStrategy {
    private val engine = GameEngine()
    private val random = Random()

    override fun evaluateNextAction(state: GameState): Action? {
        val player = state.currentPlayer ?: return null
        if (!player.isMouser) return null

        return when (state.gamePhase) {
            GameState.GamePhase.OPEN -> planOpenPhase(state, player)
            GameState.GamePhase.TOWER, GameState.GamePhase.SUMMONING -> handleTowerPhase(state)
            GameState.GamePhase.DISCARD_OPEN -> TurnAction(TurnAction.ActionType.NEXT_TURN)
            else -> null
        }
    }

    private fun planOpenPhase(state: GameState, player: Player): Action? {
        // 5. Public Quests
        val publicQuestAction = planQuest(state, player, state.board?.quests?.filterNotNull() ?: emptyList())
        if (publicQuestAction != null) return publicQuestAction

        // 6. Roll 2d10 (Simulated by random action)
        val roll = random.nextInt(10) + 1
        return when (roll) {
            in 1..7 -> planSubdue(state, player)
            8 -> planPersonalQuest(state, player)
            9 -> PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.START_TOWER_DRAW, null)
            10 -> PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null) // Rogue's Purse
            else -> TurnAction(TurnAction.ActionType.ADVANCE_PHASE)
        }
    }

    private fun planQuest(state: GameState, player: Player, quests: List<Quest>): Action? {
        for (quest in quests) {
            val reqs = quest.getRequirements()
            if (canFulfill(reqs, player.storage)) {
                val carpet = player.hand.find { it is CreatureCard && it.ability == Ability.WITCH_BROOM } // Using broom as carpet for simplicity if no carpet
                if (carpet != null || player.flyingCarpets > 0) {
                     return QuestAction(QuestAction.ActionType.COMPLETE_QUEST, state.currentPlayerIndex, quest, emptyList())
                }
            }
        }
        return null
    }

    private fun planSubdue(state: GameState, player: Player): Action? {
        val roads = state.board?.roads()?.filter { it.creature != null } ?: return null
        if (roads.isEmpty()) return TurnAction(TurnAction.ActionType.ADVANCE_PHASE)
        
        val road = roads.random()
        val creature = road.creature!!
        val combos = engine.canSubdue(creature, player.hand)
        if (combos.isNotEmpty()) {
             return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, road, null, null)
        }
        return TurnAction(TurnAction.ActionType.ADVANCE_PHASE)
    }

    private fun planPersonalQuest(state: GameState, player: Player): Action? {
        if (player.quests.isEmpty()) {
            return PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.START_TOWER_DRAW, null) // Draw quest if none
        }
        return planQuest(state, player, player.quests)
    }

    private fun handleTowerPhase(state: GameState): Action? {
        val options = state.towerDrawnCards
        if (options.isNotEmpty()) {
            val choice = options.random()
            return PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.RESOLVE_TOWER_DRAW, listOf(choice))
        }
        return TurnAction(TurnAction.ActionType.ADVANCE_PHASE)
    }

    private fun canFulfill(reqs: List<Symbol>, cards: List<Card>): Boolean {
        val symbols = cards.filterIsInstance<CreatureCard>().flatMap { it.values }.toMutableList()
        return reqs.all { symbols.remove(it) }
    }
}
