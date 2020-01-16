package com.example.fantastiqa.screens.views;

import android.os.Bundle;
import android.view.View;

import com.example.fantastiqa.GameState.Area;
import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Region;
import com.example.fantastiqa.GameState.Road;

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

    void bindLand(int location, Region details);
    void bindRoad(int location, Road details);
    void bindQuest(int location, Quest details);
    void bindHand(List<Card> theHand);
	void bindStorage(List<Card> theHand);
	void bindPlayerQuest(List<Card> theHand);
	void bindQuestStorage(Quest theQuest, List<Card> theCards);

    void highlightLand(int location);
    void validCardsForQuest(List<Card> matches);

    interface ViewMvcListener {

        void onMoveClick();
        void onTowerVisitClick();
        void onStoreCardsClick();
        void onStoreQuestClick();
        void onDoneClick();
        
        void onLandClick(View v);
		void onHandClick(View v);
    }

    public void setListener(ViewMvcListener listner);
    
    public void gameStateChange(String newState);
}
