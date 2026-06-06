package com.example.fantastiqa.redux;

import kotlinx.coroutines.flow.MutableStateFlow;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.StateFlowKt;

import java.util.ArrayList;
import java.util.List;

/**
 * Central store for game state using reactive StateFlow.
 * The Store holds the current GameState and processes Actions through the GameEngine.
 * 
 * UI components observe the StateFlow and automatically recompose when state changes.
 * This implements the Observer pattern for reactive state management.
 */
public class Store {
    private final GameEngine gameEngine;
    private final MutableStateFlow<GameState> stateFlow;
    private final List<StateChangeListener> listeners;

    public interface StateChangeListener {
        void onStateChanged(GameState oldState, GameState newState);
    }

    /**
     * Create a new Store with an initial game state
     * @param initialState The initial GameState
     */
    public Store(GameState initialState) {
        this.gameEngine = new GameEngine();
        this.stateFlow = StateFlowKt.MutableStateFlow(initialState);
        this.listeners = new ArrayList<>();
    }

    /**
     * Get the current game state
     * @return The current immutable GameState
     */
    public GameState getState() {
        return stateFlow.getValue();
    }

    /**
     * Get the StateFlow for reactive observation
     * @return StateFlow that emits whenever the state changes
     */
    public StateFlow<GameState> getStateFlow() {
        return stateFlow;
    }

    /**
     * Dispatch an action to the store.
     * The action is processed by the GameEngine, which returns a new state.
     * If the new state is different, listeners are notified and the StateFlow is updated.
     * 
     * @param action The action to dispatch
     */
    public void dispatch(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }

        GameState currentState = getState();
        GameState newState = gameEngine.reduce(currentState, action);

        // Only update if state actually changed
        if (newState != currentState) {
            stateFlow.setValue(newState);
            notifyListeners(currentState, newState);
        }
    }

    /**
     * Add a listener to be notified of state changes
     * @param listener The listener to add
     */
    public void subscribe(StateChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener from state change notifications
     * @param listener The listener to remove
     */
    public void unsubscribe(StateChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners of a state change
     */
    private void notifyListeners(GameState oldState, GameState newState) {
        for (StateChangeListener listener : listeners) {
            listener.onStateChanged(oldState, newState);
        }
    }

    /**
     * Clear all listeners (useful for cleanup)
     */
    public void clearListeners() {
        listeners.clear();
    }
}
