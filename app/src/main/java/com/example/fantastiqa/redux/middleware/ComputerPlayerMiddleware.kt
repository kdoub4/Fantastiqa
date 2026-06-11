package com.example.fantastiqa.redux.middleware

import com.example.fantastiqa.redux.GameState
import com.example.fantastiqa.redux.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Middleware that listens for state changes and triggers computer player actions.
 */
class ComputerPlayerMiddleware(
    private val store: Store,
    private val strategy: ComputerPlayerStrategy
) : Store.StateChangeListener {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var isProcessing = false

    override fun onStateChanged(oldState: GameState, newState: GameState) {
        val player = newState.currentPlayer ?: return
        
        if (player.isComputer && 
            newState.gamePhase == GameState.GamePhase.PLAYER_TURN && 
            !newState.isGameOver &&
            !isProcessing) {
            
            isProcessing = true
            scope.launch {
                var currentState = newState
                while (currentState.currentPlayer?.isComputer == true && 
                       currentState.gamePhase == GameState.GamePhase.PLAYER_TURN &&
                       !currentState.isGameOver) {
                    
                    delay(1000) // Simulate "thinking" time
                    val action = strategy.evaluateNextAction(currentState)
                    if (action != null) {
                        store.dispatch(action)
                        // Update local state to check loop condition correctly
                        currentState = store.state
                        // If it's a TurnAction (NEXT_TURN), break the loop as game phase or player will change
                        if (action is com.example.fantastiqa.redux.actions.TurnAction && 
                            action.getActionType() == com.example.fantastiqa.redux.actions.TurnAction.ActionType.NEXT_TURN) {
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
