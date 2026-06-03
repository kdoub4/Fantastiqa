package com.example.fantastiqa.redux.actions;

import com.example.fantastiqa.redux.Action;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.GameState.CreatureCard;
import java.util.Set;

/**
 * Actions for subduing creatures and conquering roads
 */
public class SubdueAction extends Action {
    public enum ActionType {
        SUBDUE_CREATURE,
        CONQUER_ROAD,
        PLACE_CREATURE_ON_ROAD
    }

    private final ActionType actionType;
    private final Road road;
    private final CreatureCard targetCreature;
    private final Set<CreatureCard> playedCards;
    private final int playerIndex;

    public SubdueAction(ActionType actionType, int playerIndex, Road road, CreatureCard targetCreature, Set<CreatureCard> playedCards) {
        this.actionType = actionType;
        this.playerIndex = playerIndex;
        this.road = road;
        this.targetCreature = targetCreature;
        this.playedCards = playedCards;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Road getRoad() {
        return road;
    }

    public CreatureCard getTargetCreature() {
        return targetCreature;
    }

    public Set<CreatureCard> getPlayedCards() {
        return playedCards;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    @Override
    public String getType() {
        return "SubdueAction:" + actionType.name();
    }
}
