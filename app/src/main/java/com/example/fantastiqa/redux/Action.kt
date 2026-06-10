package com.example.fantastiqa.redux

/**
 * Base class for all game actions.
 * Implements the Command Pattern for unidirectional data flow.
 * All actions are immutable and describe what happened in the game.
 */
abstract class Action {
    /**
     * @return A descriptive name of this action for debugging and logging
     */
    abstract val type: String

    override fun toString(): String {
        return "Action{" +
                "type='" + this.type + '\'' +
                '}'
    }
}