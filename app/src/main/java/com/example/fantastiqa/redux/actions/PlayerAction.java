package com.example.fantastiqa.redux.actions;

import com.example.fantastiqa.redux.Action;

/**
 * Base class for player-related actions.
 * Examples: drawing cards, using abilities, gaining resources
 */
public class PlayerAction extends Action {
    public enum ActionType {
        DRAW_CARDS,
        DISCARD_FROM_HAND,
        USE_FLYING_CARPET,
        USE_SHUFFLE_TOKEN,
        GAIN_GEMS,
        LOSE_GEMS,
        GAIN_TROPHIES,
        LOSE_TROPHIES,
        USE_TENT,
        STORE_CARD_FOR_QUEST,
        USE_ABILITY,
        STORE_IN_BACKPACK,
        RELEASE_CARDS,
        GAIN_CARD,
        START_TOWER_DRAW,
        RESOLVE_TOWER_DRAW,
        SELECT_PLUS_CARDS
    }

    protected final int playerIndex;
    protected final ActionType actionType;
    protected final Object payload;

    public PlayerAction(int playerIndex, ActionType actionType, Object payload) {
        this.playerIndex = playerIndex;
        this.actionType = actionType;
        this.payload = payload;
    }

    public com.example.fantastiqa.gameState.Player getCurrentPlayer(com.example.fantastiqa.redux.GameState state) {
        if (state.players != null && playerIndex >= 0 && playerIndex < state.players.size()) {
            return state.players.get(playerIndex);
        }
        return null;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String getType() {
        return "PlayerAction:" + actionType.name();
    }
}
