package com.example.fantastiqa.ui.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fantastiqa.redux.GameState;

/**
 * Example game board screen showing how to use GameViewModel with Redux.
 * 
 * This demonstrates:
 * - Observing game state
 * - Dispatching actions via ViewModel
 * - Responding to state changes
 * - Handling different UI states
 */
public class GameBoardFragment extends BaseGameFragment {
    
    private TextView currentPlayerTextView;
    private TextView gamePhaseTextView;
    private TextView player1GemsTextView;
    private TextView player2GemsTextView;
    private View nextTurnButton;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO: Inflate your layout
        // return inflater.inflate(R.layout.fragment_game_board, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views (example)
        // currentPlayerTextView = view.findViewById(R.id.current_player);
        // gamePhaseTextView = view.findViewById(R.id.game_phase);
        // player1GemsTextView = view.findViewById(R.id.player1_gems);
        // player2GemsTextView = view.findViewById(R.id.player2_gems);
        // nextTurnButton = view.findViewById(R.id.next_turn_button);
        
        // Set up click listeners
        // nextTurnButton.setOnClickListener(v -> onNextTurnClicked());
    }
    
    @Override
    protected void onGameStateChanged(GameState gameState) {
        // Update UI to reflect new game state
        if (gameState == null) return;
        
        // Update current player display
        if (currentPlayerTextView != null && gameState.getCurrentPlayer() != null) {
            currentPlayerTextView.setText("Current Player: " + gameState.getCurrentPlayer().name);
        }
        
        // Update game phase display
        if (gamePhaseTextView != null) {
            gamePhaseTextView.setText("Phase: " + gameState.gamePhase.name());
        }
        
        // Update player resources
        if (!gameState.players.isEmpty()) {
            if (player1GemsTextView != null && gameState.players.size() > 0) {
                player1GemsTextView.setText("Gems: " + gameState.players.get(0).getGems());
            }
            if (player2GemsTextView != null && gameState.players.size() > 1) {
                player2GemsTextView.setText("Gems: " + gameState.players.get(1).getGems());
            }
        }
        
        // TODO: Update board display, player hands, quests, etc.
    }
    
    /**
     * Example action dispatch - player clicks next turn button
     */
    private void onNextTurnClicked() {
        gameViewModel.nextTurn();
    }
    
    /**
     * Example: Player draws cards
     */
    public void onPlayerDrawCards(int playerIndex, int cardCount) {
        gameViewModel.playerDrawCards(playerIndex, cardCount);
    }
    
    /**
     * Example: Player gains gems
     */
    public void onPlayerGainGems(int playerIndex, int gemCount) {
        gameViewModel.playerGainGems(playerIndex, gemCount);
    }
}
