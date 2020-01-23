package com.example.fantastiqa.screens.views;

import android.os.Bundle;
import android.view.View;

import com.example.fantastiqa.GameState.Area;
import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Region;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.GameState.CreatureCard;

import com.example.fantastiqa.screens.deckCards;
import com.example.fantastiqa.screens.spaceRoad;
import com.example.fantastiqa.screens.spaceRegion;
import com.example.fantastiqa.screens.gameStatus;

import java.util.List;

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
    void bindQuest(int location, Quest details);
    void bindHand(List<Card> theHand);
	void bindStorage(List<Card> theHand);
	void bindPlayerQuest(List<Card> theHand);
	void bindQuestStorage(Quest theQuest, List<Card> theCards);

    void highlightLand(int location);
    void validCardsForQuest(List<Card> matches);
    
    void setTowerTeleport(Boolean value);
	
	void onMoveClick(View v);
	void onDoneClick();
	void onTowerVisitClick();
    void onLandClick(View v);
    void onStoreCardsClick();
    void onHandClick(View v);
    void onStoreQuestClick();
    void selectKeyCards(List<Card> selectFrom);
	void selectCard(final List<Card> selectFrom);
	void onFlyingCarpetClick();
	
    interface ViewMvcListener {
		List<spaceRegion> getValidAdventuring();
		spaceRegion towerTeleport();
		Boolean doMove(spaceRegion newSpace);
		void finishPhase();
		void beginStoreCardsPrivate();
		void storeCard(Card theCard);
		void storeCardPrivate(Card theCard);
		void storeCardQuest(Card theCard);
		List<Card> getValidQuestCards();
		void beginStoreCardsQuest();
		void toast(String text);
		void beginVisitBazaar();
		Boolean canTowerTeleport();
		void endVisitBazaar(int selection);
		void onSelectedKeyCards(List<Card> selections);
		List<spaceRegion> beginFlyingCarpet();
		public void endFlyingCarpet(spaceRegion newSpace);
    }

    public void setListener(ViewMvcListener listner);
    
    public void gameStateChange(gameStatus newState);
}
