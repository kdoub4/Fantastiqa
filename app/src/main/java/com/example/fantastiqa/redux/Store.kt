package com.example.fantastiqa.redux

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Central store for game state using reactive StateFlow.
 * The Store holds the current GameState and processes Actions through the GameEngine.
 *
 * UI components observe the StateFlow and automatically recompose when state changes.
 * This implements the Observer pattern for reactive state management.
 */
class Store(initialState: GameState) {
    private val gameEngine = GameEngine()
    
    private val _stateFlow = MutableStateFlow(initialState)
    
    /**
     * Get the StateFlow for reactive observation.
     * Use this in Composable functions or with collectLatest in Coroutines.
     */
    val stateFlow: StateFlow<GameState> = _stateFlow.asStateFlow()

    private val history = mutableListOf<GameState>()
    private val _canUndo = MutableStateFlow(false)
    val canUndoStateFlow: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val listeners = mutableListOf<StateChangeListener>()

    /**
     * Functional interface for state changes, compatible with Java SAM.
     */
    fun interface StateChangeListener {
        fun onStateChanged(oldState: GameState, newState: GameState)
    }

    /**
     * Get the current immutable GameState.
     * This is accessible from Java via getState().
     */
    val state: GameState get() = _stateFlow.value

    /**
     * Dispatch an action to the store.
     * The action is processed by the GameEngine, which returns a new state.
     * If the new state is different, the StateFlow is updated and listeners are notified.
     *
     * @param action The action to dispatch
     */
    fun dispatch(action: Action) {
        val currentState = state
        val newState = gameEngine.reduce(currentState, action)

        // Only update if state actually changed (Redux principle)
        if (newState !== currentState) {
            history.add(currentState)
            if (history.size > 100) {
                history.removeAt(0)
            }
            _canUndo.value = history.any { it.currentPlayer?.isComputer != true }
            _stateFlow.value = newState
            notifyListeners(currentState, newState)
        }
    }

    /**
     * Revert to the last state where the current player was a human player.
     */
    fun undo() {
        val currentState = state
        var targetState: GameState? = null
        
        while (history.isNotEmpty()) {
            val last = history.removeAt(history.size - 1)
            val lastPlayer = last.currentPlayer
            if (lastPlayer == null || !lastPlayer.isComputer) {
                targetState = last
                break
            }
        }
        
        if (targetState != null) {
            _canUndo.value = history.any { it.currentPlayer?.isComputer != true }
            _stateFlow.value = targetState
            notifyListeners(currentState, targetState)
        }
    }

    /**
     * Add a listener to be notified of state changes.
     * Useful for legacy Java components or non-coroutine observers.
     */
    fun subscribe(listener: StateChangeListener) {
        if (listener !in listeners) {
            listeners.add(listener)
        }
    }

    /**
     * Remove a listener from state change notifications.
     */
    fun unsubscribe(listener: StateChangeListener) {
        listeners.remove(listener)
    }

    /**
     * Notify all listeners of a state change.
     */
    private fun notifyListeners(oldState: GameState, newState: GameState) {
        listeners.forEach { it.onStateChanged(oldState, newState) }
    }

    /**
     * Clear all listeners (useful for cleanup).
     */
    fun clearListeners() {
        listeners.clear()
    }
}
