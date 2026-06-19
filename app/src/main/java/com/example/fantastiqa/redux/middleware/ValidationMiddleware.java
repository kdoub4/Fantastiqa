package com.example.fantastiqa.redux.middleware;

import com.example.fantastiqa.redux.Action;
import com.example.fantastiqa.redux.Store;

/**
 * Validation middleware that checks if actions are valid before processing
 *
 * @deprecated Use validation logic inside GameEngine or observers/StateFlow flow operators.
 */
@Deprecated
public class ValidationMiddleware implements Middleware {
    
    @Override
    public void process(Store store, Next next, Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        
        // Validate action-specific requirements
        if (!isActionValid(store, action)) {
            throw new IllegalStateException("Invalid action for current game state: " + action.getType());
        }
        
        // Action is valid, proceed
        next.dispatch(action);
    }

    /**
     * Validate the action against current game state
     */
    private boolean isActionValid(Store store, Action action) {
        // TODO: Implement game-specific validation rules
        // Examples:
        // - Can a player only act when it's their turn?
        // - Do they have the required resources?
        // - Is the game phase appropriate for this action?
        return true;
    }
}
