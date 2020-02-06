package com.example.fantastiqa.screens.views;

import android.os.Bundle;
import android.view.View;

import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Region;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.screens.GameStatus;
import com.example.fantastiqa.screens.spaceRegion;
import com.example.fantastiqa.screens.spaceRoad;

import java.util.List;
import java.util.Set;

/**
 * MVC view interface.
 * MVC view is a "dumb" component used for presenting information to the user.<br>
 * Please note that MVC view is not the same as Android View - MVC view will usually wrap one or
 * more Android View's while adding logic for communication with MVC Controller.
 */
public interface ViewMvc {

    /**
     * Get the root Android View which is used internally by this MVC View for presenting data
     * to the user.<br>
     * The returned Android View might be used by an MVC Controller in order to query or alter the
     * properties of either the root Android View itself, or any of its child Android View's.
     * @return root Android View of this MVC View
     */
    public View getRootView();

    /**
     * This method aggregates all the information about the state of this MVC View into Bundle
     * object. The keys in the returned Bundle must be provided as public constants inside the
     * implementations of concrete MVC views.<br>
     * The main use case for this method is exporting the state of editable Android Views underlying
     * the MVC view. This information can be used by MVC controller for e.g. processing user's
     * input or saving view's state during lifecycle events.
     * @return Bundle containing the state of this MVC View, or null if the view has no state
     */
    public Bundle getViewState();

    void bindLand(spaceRegion location, Region details);
    void bindRoad(spaceRoad location, Road details);
    void bindQuest(int location, Quest details, Boolean canComplete);
    void bindHand(List<Card> theHand);
	void bindStorage(List<Card> theHand);
	void bindPlayerStorage(List<Card> theHand);
	void bindQuestStorage(Quest theQuest, List<Card> theCards);

	void updateGems(int gems);
	void updateVps(int vps);
	void updateDeck(int size);
	void updateDiscard(int size);

    void highlightLand(int location);
    void validCardsForQuest(List<Card> matches);
    
    void setTowerTeleport(Boolean value);
	
	void onMoveClick(View v);
	void onMoveClick();
	void onDoneClick();
	void onTowerVisitClick();
    void onLandClick(View v);
    void onStoreCardsClick();
    void onHandClick(View v);
    void onStoreQuestClick();
    void selectKeyCards(List<Card> selectFrom);
    void selectSubdueOption(List<Set<Card>> choices, Road toSubdue);

    void selectCard(final List<? extends Card> selectFrom, List<Boolean> enabled);
	void onFlyingCarpetClick();

    void removeHandCard(Card card, List<Card> hand);
	void beginReleaseCards();
    
    interface ViewMvcListener {
		List<spaceRegion> getValidAdventuring();

        Boolean doMove(spaceRegion newSpace);

        spaceRegion towerTeleport();
		void finishPhase();
		void beginStoreCardsPrivate();

        void handClick(Card theCard);

        Boolean storeCardPrivate(Card theCard);
		void storeCardQuest(Card theCard);
		List<Card> getValidQuestCards();
		void beginStoreCardsQuest();
		void toast(String text);
		void beginVisitTowerCards();
		void endVisitTowerCards(int selection);
		void onSelectedKeyCards(List<Card> selections);
		List<spaceRegion> beginFlyingCarpet();

        void endFlyingCarpet(spaceRegion newSpace);

        Boolean canTowerTeleport();

        List<Card> beginReleaseCard();

        void beginDiscard();
        void discardFromStorage(Card aCard);

        Boolean canCompleteQuest(Quest aQuest);
        void beginCompleteQuest(Quest aQuest);
        Boolean subdue(Road toSubdue, Set<Card> selection);

    }

    public void setListener(ViewMvcListener listner);

    public void gameStateChange(GameStatus newState);
}
