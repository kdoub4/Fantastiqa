package com.example.fantastiqa.redux.middleware

import com.example.fantastiqa.redux.GameState
import com.example.fantastiqa.redux.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Middleware that listens for state changes and triggers computer player actions.
 */
class ComputerPlayerMiddleware(
    private val store: Store,
    private val standardStrategy: ComputerPlayerStrategy,
    private val mouserStrategy: ComputerPlayerStrategy,
    private val scope: CoroutineScope
) {

    private var isProcessing = false

    init {
        scope.launch {
            store.stateFlow.collect { newState ->
                val player = newState.currentPlayer ?: return@collect
                
                if (player.isComputer && 
                    isComputerActivePhase(newState.gamePhase) && 
                    !newState.isGameOver &&
                    !isProcessing) {
                    
                    isProcessing = true
                    launch {
                        var currentState = newState
                        while (currentState.currentPlayer?.isComputer == true && 
                               isComputerActivePhase(currentState.gamePhase) &&
                               !currentState.isGameOver) {
                            
                            delay(700) // Simulate "thinking" time
                            val strategy = if (currentState.currentPlayer?.isMouser == true) mouserStrategy else standardStrategy
                            val action = strategy.evaluateNextAction(currentState)
                            if (action != null) {
                                store.dispatch(action)
                                // Update local state to check loop condition correctly
                                currentState = store.state
                                // If it's a TurnAction (NEXT_TURN), break the loop as game phase or player will change
                                if (action is com.example.fantastiqa.redux.actions.TurnAction && 
                                    (action.getActionType() == com.example.fantastiqa.redux.actions.TurnAction.ActionType.NEXT_TURN )) {
                                    break
                                }
                            } else {
                                break
                            }
                        }
                        isProcessing = false
                    }
                }
            }
        }
    }

    private fun isComputerActivePhase(phase: GameState.GamePhase?): Boolean {
        return when (phase) {
            GameState.GamePhase.OPEN,
            GameState.GamePhase.SUBDUE,
            GameState.GamePhase.TOWER,
            GameState.GamePhase.QUEST,
            GameState.GamePhase.DISCARD_OPEN,
            GameState.GamePhase.PLAYER_TURN,
            GameState.GamePhase.CARD_SELECTION -> true
            else -> false
        }
    }
}
