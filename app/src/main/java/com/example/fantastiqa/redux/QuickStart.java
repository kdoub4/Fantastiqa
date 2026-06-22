package com.example.fantastiqa.redux;

import com.example.fantastiqa.redux.utils.GameInitializer;

/**
 * Quick start guide for the Redux architecture.
 * Shows the recommended way to initialize and use the system.
 */
public class QuickStart {
    
    /**
     * Initialize the Redux system with middleware
     */
    public static Store initializeGameStore() {
        // Create initial game state
        GameState initialState = GameInitializer.initializeNewGame(GameInitializer.PlayerType.COMPUTER);
        
        // Create store
        Store store = new Store(initialState);
        
        // Add logging middleware
        store.subscribe((oldState, newState) -> {
            System.out.println("State updated: " + newState);
        });
        
        return store;
    }
    
    /**
     * Example: How to dispatch an action
     */
    public static void dispatchExample(Store store) {
        // Get current state
        GameState currentState = store.getState();
        System.out.println("Current state: " + currentState);
        
        // Dispatch an action
        // The Store will:
        // 1. Call GameEngine.reduce(currentState, action)
        // 2. Get back new GameState
        // 3. Update StateFlow
        // 4. Notify listeners
        // 5. UI automatically recomposes
        
        store.dispatch(
            new com.example.fantastiqa.redux.actions.PlayerAction(
                0,  // playerIndex
                com.example.fantastiqa.redux.actions.PlayerAction.ActionType.DRAW_CARDS,
                5   // number of cards to draw
            )
        );
        
        // Get updated state
        GameState updatedState = store.getState();
        System.out.println("Updated state: " + updatedState);
    }
    
    /**
     * Integration with Android ViewModel
     * 
     * Here's how to use Redux with Android's ViewModel:
     * 
     * public class GameViewModel extends ViewModel {
     *     private Store gameStore;
     *     private final MutableLiveData<GameState> gameStateLiveData = new MutableLiveData<>();
     *     
     *     public GameViewModel() {
     *         gameStore = QuickStart.initializeGameStore();
     *         
     *         // Subscribe to store updates
     *         gameStore.subscribe((oldState, newState) -> {
     *             gameStateLiveData.postValue(newState);
     *         });
     *         
     *         // Initial state
     *         gameStateLiveData.setValue(gameStore.getState());
     *     }
     *     
     *     public LiveData<GameState> getGameState() {
     *         return gameStateLiveData;
     *     }
     *     
     *     public void drawCards(int playerIndex, int count) {
     *         gameStore.dispatch(
     *             new PlayerAction(
     *                 playerIndex,
     *                 PlayerAction.ActionType.DRAW_CARDS,
     *                 count
     *             )
     *         );
     *     }
     *     
     *     public void nextTurn() {
     *         gameStore.dispatch(new TurnAction(TurnAction.ActionType.NEXT_TURN));
     *     }
     * }
     */
}
