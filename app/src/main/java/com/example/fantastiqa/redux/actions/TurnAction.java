package com.example.fantastiqa.redux.actions;

import com.example.fantastiqa.redux.Action;
import com.example.fantastiqa.GameState.GameState;

/**
 * Actions related to game turns and phases
 */
public class TurnAction extends Action {
    public enum ActionType {
        NEXT_TURN,
        ADVANCE_PHASE,
        END_GAME,
        START_PLAYER_TURN
    }

    private final ActionType actionType;
    private final GameState.GamePhase newPhase;  // Used for ADVANCE_PHASE

    public TurnAction(ActionType actionType, GameState.GamePhase newPhase) {
        this.actionType = actionType;
        this.newPhase = newPhase;
    }

    public TurnAction(ActionType actionType) {
        this(actionType, null);
    }

    public ActionType getActionType() {
        return actionType;
    }

    public GameState.GamePhase getNewPhase() {
        return newPhase;
    }

    @Override
    public String getType() {
        return "TurnAction:" + actionType.name();
    }
}
