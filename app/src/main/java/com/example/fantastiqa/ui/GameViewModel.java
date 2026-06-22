package com.example.fantastiqa.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.fantastiqa.redux.GameState;
import com.example.fantastiqa.redux.Store;
import com.example.fantastiqa.redux.actions.PlayerAction;
import com.example.fantastiqa.redux.actions.CardAction;
import com.example.fantastiqa.redux.actions.QuestAction;
import com.example.fantastiqa.redux.actions.SubdueAction;
import com.example.fantastiqa.redux.actions.TurnAction;
import com.example.fantastiqa.redux.utils.GameInitializer;
import com.example.fantastiqa.gameState.Card;
import com.example.fantastiqa.gameState.Quest;
import com.example.fantastiqa.gameState.Road;
import com.example.fantastiqa.gameState.CreatureCard;

import java.util.List;
import java.util.Set;

/**
 * ViewModel that bridges Redux Store with Android UI.
 * 
 * Responsibilities:
 * - Initialize and manage the Redux Store
 * - Expose GameState as LiveData for the UI to observe
 * - Provide convenience methods for dispatching actions
 * - Handle UI-related state (loading, errors, navigation)
 * 
 * The ViewModel ensures:
 * - Store survives configuration changes (screen rotation, etc.)
 * - Single source of truth for game state
 * - Clean separation between UI layer and Redux logic
 * - Reactive updates to UI when state changes
 */
public class GameViewModel extends ViewModel {
    
    private Store gameStore;
    private final MutableLiveData<GameState> gameStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<UIState> uiStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public GameViewModel() {
        uiStateLiveData.setValue(UIState.IDLE);
    }
    
    /**
     * UI state flags for managing loading, errors, dialogs, etc.
     */
    public enum UIState {
        IDLE,                    // Normal gameplay
        LOADING,                 // Loading game
        CARD_SELECTION,          // Waiting for card selection
        QUEST_COMPLETION,        // Completing a quest
        SUBDUING_CREATURE,       // Subduing a creature
        SHOWING_ERROR,           // Error dialog shown
        GAME_OVER                // Game has ended
    }
    
    /**
     * Initialize the ViewModel with a new game
     */
    public void initializeGame(GameInitializer.PlayerType p2Type) {
        uiStateLiveData.setValue(UIState.LOADING);
        
        try {
            // Create initial game state
            GameState initialState = GameInitializer.initializeNewGame(p2Type);
            
            // Create store
            gameStore = new Store(initialState);
            
            // Subscribe to store updates
            gameStore.subscribe((oldState, newState) -> {
                gameStateLiveData.postValue(newState);
                
                // Check if game is over
                if (newState.isGameOver) {
                    uiStateLiveData.postValue(UIState.GAME_OVER);
                } else {
                    uiStateLiveData.postValue(UIState.IDLE);
                }
            });
            
            // Set initial state
            gameStateLiveData.setValue(gameStore.getState());
            uiStateLiveData.setValue(UIState.IDLE);
            
        } catch (Exception e) {
            errorMessageLiveData.postValue("Failed to initialize game: " + e.getMessage());
            uiStateLiveData.postValue(UIState.SHOWING_ERROR);
        }
    }
    
    /**
     * Initialize game from a saved state (future feature)
     */
    public void initializeGameFromSave(GameState savedState) {
        uiStateLiveData.setValue(UIState.LOADING);
        
        try {
            gameStore = new Store(savedState);
            gameStore.subscribe((oldState, newState) -> {
                gameStateLiveData.postValue(newState);
            });
            gameStateLiveData.setValue(gameStore.getState());
            uiStateLiveData.setValue(UIState.IDLE);
        } catch (Exception e) {
            errorMessageLiveData.postValue("Failed to load game: " + e.getMessage());
            uiStateLiveData.postValue(UIState.SHOWING_ERROR);
        }
    }
    
    // ============ LiveData Getters ============
    
    /**
     * Get the current game state as LiveData
     * UI observes this and automatically updates when state changes
     */
    public LiveData<GameState> getGameState() {
        return gameStateLiveData;
    }
    
    /**
     * Get the UI state (loading, error, etc.)
     */
    public LiveData<UIState> getUIState() {
        return uiStateLiveData;
    }
    
    /**
     * Get error messages
     */
    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }
    
    /**
     * Get current game state (immediate, not LiveData)
     * Use for logic that doesn't need observation
     */
    public GameState getCurrentGameState() {
        return gameStore != null ? gameStore.getState() : null;
    }
    
    // ============ Player Actions ============
    
    /**
     * Player draws cards from their deck
     */
    public void playerDrawCards(int playerIndex, int count) {
        if (gameStore == null) return;
        gameStore.dispatch(new PlayerAction(playerIndex, PlayerAction.ActionType.DRAW_CARDS, count));
    }
    
    /**
     * Player uses a flying carpet ability
     */
    public void playerUseFlyingCarpet(int playerIndex) {
        if (gameStore == null) return;
        gameStore.dispatch(new PlayerAction(playerIndex, PlayerAction.ActionType.USE_FLYING_CARPET, null));
    }
    
    /**
     * Player uses a shuffle token
     */
    public void playerUseShuffle(int playerIndex) {
        if (gameStore == null) return;
        gameStore.dispatch(new PlayerAction(playerIndex, PlayerAction.ActionType.USE_SHUFFLE_TOKEN, null));
    }
    
    /**
     * Player gains gems
     */
    public void playerGainGems(int playerIndex, int gemCount) {
        if (gameStore == null) return;
        gameStore.dispatch(new PlayerAction(playerIndex, PlayerAction.ActionType.GAIN_GEMS, gemCount));
    }
    
    /**
     * Player loses gems
     */
    public void playerLoseGems(int playerIndex, int gemCount) {
        if (gameStore == null) return;
        gameStore.dispatch(new PlayerAction(playerIndex, PlayerAction.ActionType.LOSE_GEMS, gemCount));
    }
    
    /**
     * Player gains trophies
     */
    public void playerGainTrophies(int playerIndex, int trophyCount) {
        if (gameStore == null) return;
        gameStore.dispatch(new PlayerAction(playerIndex, PlayerAction.ActionType.GAIN_TROPHIES, trophyCount));
    }
    
    /**
     * Player loses trophies
     */
    public void playerLoseTrophies(int playerIndex, int trophyCount) {
        if (gameStore == null) return;
        gameStore.dispatch(new PlayerAction(playerIndex, PlayerAction.ActionType.LOSE_TROPHIES, trophyCount));
    }
    
    /**
     * Player uses a tent
     */
    public void playerUseTent(int playerIndex) {
        if (gameStore == null) return;
        gameStore.dispatch(new PlayerAction(playerIndex, PlayerAction.ActionType.USE_TENT, null));
    }
    
    // ============ Card Actions ============
    
    /**
     * Player selects cards from a choice list
     */
    public void selectCards(int playerIndex, List<Card> selectedCards) {
        if (gameStore == null) return;
        uiStateLiveData.setValue(UIState.CARD_SELECTION);
        gameStore.dispatch(new CardAction(CardAction.ActionType.SELECT_CARDS, playerIndex, selectedCards));
    }
    
    /**
     * Player discards cards from their hand
     */
    public void discardCards(int playerIndex, List<Card> cardsToDiscard) {
        if (gameStore == null) return;
        gameStore.dispatch(new CardAction(CardAction.ActionType.DISCARD_CARDS, playerIndex, cardsToDiscard));
    }
    
    /**
     * Return unselected cards to deck
     */
    public void returnUnselectedCards(int playerIndex, List<Card> cardsToReturn) {
        if (gameStore == null) return;
        gameStore.dispatch(new CardAction(CardAction.ActionType.RETURN_UNSELECTED_CARDS, playerIndex, cardsToReturn));
    }
    
    // ============ Quest Actions ============
    
     /**
     * Player completes a quest
     */
    public void completeQuest(int playerIndex, Quest quest, List<Card> requiredCards) {
        if (gameStore == null) return;
        uiStateLiveData.setValue(UIState.QUEST_COMPLETION);
        gameStore.dispatch(new QuestAction(QuestAction.ActionType.COMPLETE_QUEST, playerIndex, quest, requiredCards));
    }
    
    /**
     * Player stores a card for a quest
     */
    public void storeCardForQuest(int playerIndex, Quest quest, Card cardToStore) {
        if (gameStore == null) return;
        gameStore.dispatch(new QuestAction(
            QuestAction.ActionType.STORE_CARD_FOR_QUEST,
            playerIndex,
            quest,
            java.util.Collections.singletonList(cardToStore)
        ));
    }
    
    /**
     * Draw a new quest
     */
    public void drawQuest(int playerIndex) {
        if (gameStore == null) return;
        gameStore.dispatch(new QuestAction(QuestAction.ActionType.DRAW_QUEST, playerIndex, null, null));
    }
    
    // ============ Subdue/Combat Actions ============
    
    /**
     * Player subdues a creature on a road
     */
    public void subdueCreature(int playerIndex, Road road, CreatureCard targetCreature, Set<CreatureCard> playedCards) {
        if (gameStore == null) return;
        uiStateLiveData.setValue(UIState.SUBDUING_CREATURE);
        gameStore.dispatch(new SubdueAction(
            SubdueAction.ActionType.SUBDUE_CREATURE,
            playerIndex,
            road,
            targetCreature,
            playedCards
        ));
    }
    
    /**
     * Player conquers a road
     */
    public void conquestRoad(int playerIndex, Road road) {
        if (gameStore == null) return;
        gameStore.dispatch(new SubdueAction(
            SubdueAction.ActionType.CONQUER_ROAD,
            playerIndex,
            road,
            null,
            null
        ));
    }
    
    /**
     * Place a creature on a road
     */
    public void placeCreatureOnRoad(int playerIndex, Road road, CreatureCard creature) {
        if (gameStore == null) return;
        gameStore.dispatch(new SubdueAction(
            SubdueAction.ActionType.PLACE_CREATURE_ON_ROAD,
            playerIndex,
            road,
            creature,
            null
        ));
    }
    
    // ============ Turn/Phase Actions ============
    
    /**
     * Advance to the next player's turn
     */
    public void nextTurn() {
        if (gameStore == null) return;
        gameStore.dispatch(new TurnAction(TurnAction.ActionType.NEXT_TURN));
    }
    
    /**
     * Advance to the next game phase
     */
    public void advancePhase(GameState.GamePhase newPhase) {
        if (gameStore == null) return;
        gameStore.dispatch(new TurnAction(TurnAction.ActionType.ADVANCE_PHASE, newPhase));
    }
    
    /**
     * End the game
     */
    public void endGame() {
        if (gameStore == null) return;
        gameStore.dispatch(new TurnAction(TurnAction.ActionType.END_GAME));
        uiStateLiveData.setValue(UIState.GAME_OVER);
    }
    
    /**
     * Start a player's turn
     */
    public void startPlayerTurn(int playerIndex) {
        if (gameStore == null) return;
        gameStore.dispatch(new TurnAction(TurnAction.ActionType.START_PLAYER_TURN));
    }
    
    // ============ Cleanup ============
    
    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up listeners when ViewModel is destroyed
        if (gameStore != null) {
            gameStore.clearListeners();
        }
    }
}
