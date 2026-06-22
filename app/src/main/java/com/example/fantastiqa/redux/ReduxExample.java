package com.example.fantastiqa.redux;

import com.example.fantastiqa.redux.actions.PlayerAction;
import com.example.fantastiqa.redux.actions.TurnAction;
import com.example.fantastiqa.redux.utils.GameInitializer;
import java.util.ArrayList;
import java.util.List;

/**
 * Example usage of the Redux architecture.
 * Shows how to initialize the store and dispatch actions.
 */
public class ReduxExample {

    public static void main(String[] args) {
        // Step 1: Initialize game state
        System.out.println("=== Initializing Game ===");
        GameState initialState = GameInitializer.initializeNewGame(GameInitializer.PlayerType.COMPUTER);
        System.out.println("Initial state: " + initialState);

        // Step 2: Create store with middleware
        System.out.println("\n=== Creating Store with Middleware ===");
        Store store = new Store(initialState);
        System.out.println("Store created");

        // Step 3: Subscribe to state changes
        System.out.println("\n=== Subscribing to State Changes ===");
        store.subscribe((oldState, newState) -> {
            System.out.println("State changed!");
            System.out.println("  Old: " + oldState);
            System.out.println("  New: " + newState);
        });

        // Step 4: Dispatch some actions
        System.out.println("\n=== Dispatching Actions ===");
        
        // Player 0 draws cards
        System.out.println("\nAction 1: Player draws 5 cards");
        store.dispatch(new PlayerAction(0, PlayerAction.ActionType.DRAW_CARDS, 5));
        
        // Player gains gems
        System.out.println("\nAction 2: Player gains 3 gems");
        store.dispatch(new PlayerAction(0, PlayerAction.ActionType.GAIN_GEMS, 3));
        
        // Next turn
        System.out.println("\nAction 3: Next player's turn");
        store.dispatch(new TurnAction(TurnAction.ActionType.NEXT_TURN));

        // Step 5: Inspect final state
        System.out.println("\n=== Final State ===");
        GameState finalState = store.getState();
        System.out.println("Final state: " + finalState);
        System.out.println("Current player index: " + finalState.currentPlayerIndex);
    }
}
