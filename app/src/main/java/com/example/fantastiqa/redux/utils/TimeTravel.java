package com.example.fantastiqa.redux.utils;

import com.example.fantastiqa.redux.GameState;
import com.example.fantastiqa.redux.Action;

/**
 * Time-travel debugging utility for Redux.
 * Allows inspection of previous states and action history.
 */
public class TimeTravel {
    private final java.util.List<GameState> stateHistory;
    private final java.util.List<Action> actionHistory;
    private int currentIndex;

    public TimeTravel() {
        this.stateHistory = new java.util.ArrayList<>();
        this.actionHistory = new java.util.ArrayList<>();
        this.currentIndex = -1;
    }

    /**
     * Record a state and action
     */
    public void record(Action action, GameState newState) {
        // Remove any future states if we're not at the end
        if (currentIndex < stateHistory.size() - 1) {
            stateHistory.subList(currentIndex + 1, stateHistory.size()).clear();
            actionHistory.subList(currentIndex, actionHistory.size()).clear();
        }

        actionHistory.add(action);
        stateHistory.add(newState);
        currentIndex = stateHistory.size() - 1;
    }

    /**
     * Get the state at a specific index
     */
    public GameState getStateAt(int index) {
        if (index >= 0 && index < stateHistory.size()) {
            return stateHistory.get(index);
        }
        return null;
    }

    /**
     * Get the current state
     */
    public GameState getCurrentState() {
        return getStateAt(currentIndex);
    }

    /**
     * Get action that led to a state
     */
    public Action getActionAt(int index) {
        if (index >= 0 && index < actionHistory.size()) {
            return actionHistory.get(index);
        }
        return null;
    }

    /**
     * Travel back to a previous state
     */
    public void travelTo(int index) {
        if (index >= 0 && index < stateHistory.size()) {
            currentIndex = index;
        }
    }

    /**
     * Get the total number of recorded states
     */
    public int getSize() {
        return stateHistory.size();
    }
}
