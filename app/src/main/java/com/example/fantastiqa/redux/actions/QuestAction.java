package com.example.fantastiqa.redux.actions;

import com.example.fantastiqa.redux.Action;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Card;
import java.util.List;

/**
 * Actions related to quests:
 * - Completing quests
 * - Storing cards for quest requirements
 * - Drawing new quests
 */
public class QuestAction extends Action {
    public enum ActionType {
        COMPLETE_QUEST,
        STORE_CARD_FOR_QUEST,
        DRAW_QUEST,
        QUEST_AVAILABLE
    }

    private final ActionType actionType;
    private final Quest quest;
    private final List<Card> requiredCards;
    private final int playerIndex;

    public QuestAction(ActionType actionType, int playerIndex, Quest quest, List<Card> requiredCards) {
        this.actionType = actionType;
        this.playerIndex = playerIndex;
        this.quest = quest;
        this.requiredCards = requiredCards;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Quest getQuest() {
        return quest;
    }

    public List<Card> getRequiredCards() {
        return requiredCards;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    @Override
    public String getType() {
        return "QuestAction:" + actionType.name();
    }
}
