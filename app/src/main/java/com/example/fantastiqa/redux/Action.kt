package com.example.fantastiqa.redux;

/**
 * Base class for all game actions.
 * Implements the Command Pattern for unidirectional data flow.
 * All actions are immutable and describe what happened in the game.
 */
public abstract class Action {
    /**
     * @return A descriptive name of this action for debugging and logging
     */
    public abstract String getType();

    @Override
    public String toString() {
        return "Action{" +
                "type='" + getType() + '\'' +
                '}';
    }
}
