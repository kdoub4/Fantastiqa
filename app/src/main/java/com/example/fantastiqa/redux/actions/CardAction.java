package com.example.fantastiqa.redux.actions;

import com.example.fantastiqa.redux.Action;
import com.example.fantastiqa.gameState.Card;
import java.util.List;

/**
 * Action for card-related events:
 * - Selecting cards from various decks
 * - Discarding cards
 * - Drawing cards
 */
public class CardAction extends Action {
    public enum ActionType {
        SELECT_CARDS,
        DISCARD_CARDS,
        DRAW_CARDS,
        RETURN_UNSELECTED_CARDS
    }

    private final ActionType actionType;
    private final List<Card> cards;
    private final int playerIndex;

    public CardAction(ActionType actionType, int playerIndex, List<Card> cards) {
        this.actionType = actionType;
        this.playerIndex = playerIndex;
        this.cards = cards;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public List<Card> getCards() {
        return cards;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    @Override
    public String getType() {
        return "CardAction:" + actionType.name();
    }
}
