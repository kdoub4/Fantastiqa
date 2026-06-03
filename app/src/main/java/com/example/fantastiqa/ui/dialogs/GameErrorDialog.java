package com.example.fantastiqa.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fantastiqa.redux.GameState;
import com.example.fantastiqa.ui.GameViewModel;

/**
 * Dialog for game errors and messages.
 * Automatically shown when errors occur.
 */
public class GameErrorDialog extends DialogFragment {
    
    private static final String ARG_MESSAGE = "message";
    private GameViewModel gameViewModel;
    
    public static GameErrorDialog newInstance(String message) {
        GameErrorDialog dialog = new GameErrorDialog();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        dialog.setArguments(args);
        return dialog;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);
        
        String message = getArguments() != null ? getArguments().getString(ARG_MESSAGE) : "Unknown error";
        
        return new AlertDialog.Builder(requireActivity())
                .setTitle("Game Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dismiss())
                .create();
    }
}
