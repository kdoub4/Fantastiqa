package com.example.fantastiqa.ui.screens;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fantastiqa.ui.GameViewModel;

/**
 * Main game activity that hosts the Redux-powered game screens.
 * 
 * Responsibilities:
 * - Creates and provides GameViewModel to all fragments
 * - Manages navigation between screens
 * - Handles configuration changes (screen rotation, etc.)
 * 
 * The GameViewModel persists across configuration changes,
 * so the game state is never lost.
 */
public class GameActivity extends AppCompatActivity {
    
    private GameViewModel gameViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: setContentView(R.layout.activity_game);
        
        // Get or create the GameViewModel
        // It will survive configuration changes (screen rotation, etc.)
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        
        // If this is the first time, initialize the game
        if (gameViewModel.getCurrentGameState() == null) {
            gameViewModel.initializeGame();
        }
        
        // Load the main game board fragment if not already loaded
        if (savedInstanceState == null) {
            // TODO: Load GameBoardFragment
            // getSupportFragmentManager()
            //     .beginTransaction()
            //     .replace(R.id.fragment_container, new GameBoardFragment())
            //     .commit();
        }
    }
    
    /**
     * Get the shared GameViewModel for all fragments
     */
    public GameViewModel getGameViewModel() {
        return gameViewModel;
    }
}
