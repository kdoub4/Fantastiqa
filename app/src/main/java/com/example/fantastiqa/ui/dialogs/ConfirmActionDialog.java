package com.example.fantastiqa.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fantastiqa.redux.GameState;
import com.example.fantastiqa.ui.GameViewModel;

/**
 * Dialog for confirming game actions.
 * Can be used for turn actions, phase transitions, etc.
 */
public class ConfirmActionDialog extends DialogFragment {
    
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private GameViewModel gameViewModel;
    private OnConfirmListener confirmListener;
    
    public interface OnConfirmListener {
        void onConfirm();
        void onCancel();
    }
    
    public static ConfirmActionDialog newInstance(String title, String message) {
        ConfirmActionDialog dialog = new ConfirmActionDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        dialog.setArguments(args);
        return dialog;
    }
    
    public void setConfirmListener(OnConfirmListener listener) {
        this.confirmListener = listener;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        gameViewModel = new ViewModelProvider(requireActivity(), new ViewModelProvider.NewInstanceFactory()).get(GameViewModel.class);
        
        String title = getArguments() != null ? getArguments().getString(ARG_TITLE) : "Confirm";
        String message = getArguments() != null ? getArguments().getString(ARG_MESSAGE) : "Are you sure?";
        
        return new AlertDialog.Builder(requireActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (confirmListener != null) {
                        confirmListener.onConfirm();
                    }
                    dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (confirmListener != null) {
                        confirmListener.onCancel();
                    }
                    dismiss();
                })
                .create();
    }
}
