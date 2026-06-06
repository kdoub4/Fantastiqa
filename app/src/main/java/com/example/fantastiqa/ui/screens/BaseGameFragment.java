package com.example.fantastiqa.ui.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fantastiqa.redux.GameState;
import com.example.fantastiqa.ui.GameViewModel;

/**
 * Base Fragment for game screens.
 * Provides common ViewModel initialization and state observation.
 * 
 * All game UI screens should extend this to access the Redux GameViewModel.
 */
public abstract class BaseGameFragment extends Fragment {
    
    protected GameViewModel gameViewModel;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the shared GameViewModel
        gameViewModel = new ViewModelProvider(requireActivity(), new ViewModelProvider.NewInstanceFactory()).get(GameViewModel.class);
        
        // If this is the first fragment, initialize the game
        if (gameViewModel.getCurrentGameState() == null) {
            gameViewModel.initializeGame();
        }
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Observe game state changes
        gameViewModel.getGameState().observe(getViewLifecycleOwner(), this::onGameStateChanged);
        
        // Observe UI state changes
        gameViewModel.getUIState().observe(getViewLifecycleOwner(), this::onUIStateChanged);
        
        // Observe error messages
        gameViewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::onErrorMessage);
    }
    
    /**
     * Called whenever the game state changes.
     * Subclasses should override to update their UI.
     */
    protected abstract void onGameStateChanged(GameState gameState);
    
    /**
     * Called whenever the UI state changes (loading, error, etc.)
     */
    protected void onUIStateChanged(GameViewModel.UIState uiState) {
        switch (uiState) {
            case IDLE:
                // Normal state, no special handling
                break;
            case LOADING:
                // Show loading indicator
                onShowLoading();
                break;
            case SHOWING_ERROR:
                // Error already displayed via onErrorMessage
                break;
            case GAME_OVER:
                // Navigate to game over screen
                onGameOver();
                break;
            case CARD_SELECTION:
                onCardSelectionMode();
                break;
            case QUEST_COMPLETION:
                onQuestCompletionMode();
                break;
            case SUBDUING_CREATURE:
                onSubduingCreatureMode();
                break;
        }
    }
    
    /**
     * Called when an error occurs
     */
    protected void onErrorMessage(String message) {
        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Called when game is loading
     */
    protected void onShowLoading() {
        // Subclasses can override to show loading UI
    }
    
    /**
     * Called when game ends
     */
    protected void onGameOver() {
        // Subclasses can override for navigation
    }
    
    /**
     * Called when entering card selection mode
     */
    protected void onCardSelectionMode() {
        // Subclasses can override
    }
    
    /**
     * Called when entering quest completion mode
     */
    protected void onQuestCompletionMode() {
        // Subclasses can override
    }
    
    /**
     * Called when player is subduing a creature
     */
    protected void onSubduingCreatureMode() {
        // Subclasses can override
    }
}
