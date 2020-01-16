package com.example.fantastiqa.screens.controllers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import com.example.fantastiqa.GameState.Area;
import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.CreatureCard;
import com.example.fantastiqa.GameState.Game;
import com.example.fantastiqa.GameState.LoopingLinkedList;
import com.example.fantastiqa.GameState.LoopingListIterator;
import com.example.fantastiqa.GameState.Player;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Region;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.GameState.Symbol;
import com.example.fantastiqa.R;
import com.example.fantastiqa.screens.views.RootViewMvcImpl;
import com.example.fantastiqa.screens.views.ViewMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity implements ViewMvc.ViewMvcListener {
    private Button buttonMove;
    private Game theGame;
    private com.example.fantastiqa.screens.views.ViewMvc rootView;
    private Player currentPlayer;
    private Region currentLocation;
    private String gameState;
    private List<Pair<Symbol,Region>> moveTargets = new LinkedList<>();
    private List<Integer> moveTargetIds = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = new RootViewMvcImpl(this, null);
        rootView.setListener(this);

        setContentView(rootView.getRootView());
        //setContentView(R.layout.activity_main);

        theGame = new Game();

            for (Region anArea: theGame.board.regions()
                 ) {
                rootView.bindLand(theGame.board.regions().indexOf(anArea)+1, anArea);
            }

            for (Road aRoad: theGame.board.roads()) {
                rootView.bindRoad(theGame.board.roads().indexOf(aRoad)+1, aRoad);
            }

            for (Quest aQuest : theGame.board.quests) {
                rootView.bindQuest(theGame.board.quests.indexOf(aQuest)+1, aQuest);
            }

            rootView.bindHand(theGame.players.get(0).hand);
            Toast.makeText(MainActivity.this, theGame.players.get(0).hand.get(0).name, Toast.LENGTH_SHORT);
            rootView.bindPlayerQuest(theGame.players.get(0).quests);
    }


    //@Override
    public void onClick(View v) {
        TextView currentLocation= null;
        TextView nextLocation = null;
        LinkedList<View> locations = new LinkedList<View>(){ {
            add(findViewById(R.id.land1));
            add(findViewById(R.id.land2));
            add(findViewById(R.id.land3));
            add(findViewById(R.id.land4));
            add(findViewById(R.id.land5));
            add(findViewById(R.id.land6));
        }};
        currentLocation = (TextView)locations.getFirst();
        int i = 1;
        while (!currentLocation.getText().toString().contains("P1") && i<6) {
            currentLocation = (TextView) locations.get(i);
            i++;
        }
        if (currentLocation.getText().toString().contains("P1")) {
            currentLocation.setText(currentLocation.getText().toString().replace("P1",""));
            nextLocation = (TextView) locations.get(i<6 ? i : 0);
            nextLocation.setText(nextLocation.getText().toString().contains("P") ?
                    nextLocation.getText().toString().concat(" P1"):
                    nextLocation.getText().toString().concat("P1"));
        }

    }

    public List<Pair<Symbol,Region>> getMoves(){
        List<Pair<Symbol,Region>> results = new LinkedList<>();
        for (Pair<Road, Region> adjPair: getAdjacentLands(theGame.players.get(0))){
            if (canSubdue(adjPair.first.creature)) {
                results.add(new Pair(adjPair.first.creature.subduedBy, adjPair.second));
            }
        }
        return results;
    }

    private List<Pair<Road, Region>> getAdjacentLands(Player thePlayer){
        Region playerArea=null;
        for (Region aRegion : theGame.board.regions()) {
            if (aRegion.players.contains(thePlayer)){
                playerArea = aRegion;
                break;
            }
        }
        return theGame.board.getAdjacentAreas(playerArea);
    }

    private Boolean canSubdue(CreatureCard aCreature){
        if (aCreature.values.size()>1) {
            return canSubdueDouble(aCreature);
        }
        else {
            return canSubdueSingle(aCreature);
        }
    }

    private Boolean canSubdueSingle(CreatureCard aCreature){
        Boolean matchFirst = false;
        Boolean hasDoubleSymbol = false;
        Boolean hasTwoCards = false;

        List<Symbol> handSymbols = new ArrayList<>();
        //Toast.makeText(MainActivity.this, aCreature.subduedBy.toString(), Toast.LENGTH_SHORT).show();
        //Check hand for first symbol
        //Toast.makeText(MainActivity.this, Integer.toString(theGame.players.get(0).hand.size()),Toast.LENGTH_SHORT);
        for (Card aCard:theGame.players.get(0).hand
        ) {
            if (aCard instanceof CreatureCard) {
                CreatureCard handCreature = (CreatureCard) aCard;
                //Toast.makeText(MainActivity.this, handCreature.values.get(0).toString(), Toast.LENGTH_SHORT).show();
                if (aCreature.subduedBy == handCreature.values.get(0)) {
                    matchFirst = true;
                    break;
                }
                if (handCreature.values.size() > 1 && handCreature.values.get(0) == handCreature.values.get(1)) {
                    hasDoubleSymbol = true;
                }
                if (handSymbols.contains(handCreature.values.get(0))) {
                    hasTwoCards = true;
                }
                else {
                    handSymbols.add(handCreature.values.get(0));
                }
            }
        }

        if (hasDoubleSymbol || matchFirst || hasTwoCards) {
            return true;
        }
        return  false;
    }
    private Boolean canSubdueDouble(CreatureCard aCreature){
        Integer symbolCount = 0;

        List<Symbol> handSymbols = new ArrayList<>();

        //Check hand for first symbol
        for (Card aCard:theGame.players.get(0).hand
        ) {
            if(aCard instanceof CreatureCard) {
                CreatureCard handCreature = (CreatureCard)aCard;
                //Match double symbol first
                if (handCreature.values.size()>1 && aCreature.subduedBy == handCreature.values.get(0))
                {
                        symbolCount = 2;
                        break;
                }
                if (handCreature.values.size() > 1 && handCreature.values.get(0) == handCreature.values.get(1)) {
                    symbolCount+=2;
                }
                else {
                    if (aCreature.subduedBy == handCreature.values.get(0)) {
                        symbolCount++;
                    }
                    else {
                        if (handSymbols.contains(handCreature.values.get(0))) {
                            symbolCount++;
                        }
                        else {
                            handSymbols.add(handCreature.values.get(0));
                        }
                    }

                }
            }
        }
        if (symbolCount>=2) return  true;
        return  false;
    }

    @Override
    public void onMoveClick() {
        for (Pair<Symbol,Region> aPair:getMoves()
             ) {
            Integer regionId = theGame.board.regions().indexOf(aPair.second) + 1;
            rootView.highlightLand(regionId);
            moveTargets.add(aPair);
            moveTargetIds.add(regionId);
        }
        if (moveTargets.size() > 0)
            gameState =  "moving";
    }
    
    @Override
    public void onTowerVisitClick() {
		for (Region aLocation : theGame.board.regions()
            ) {
				//TODO check for enough gems
                if (aLocation.players.contains(theGame.players.get(0))) {
					Region matchingRegion = getMatchingTowerRegion(aLocation);
					matchingRegion.players.add(theGame.players.get(0));
					aLocation.players.remove(theGame.players.get(0));
					rootView.bindLand(theGame.board.regions().indexOf(matchingRegion) + 1, matchingRegion);
					rootView.bindLand(theGame.board.regions().indexOf(aLocation) + 1, aLocation);
					theGame.players.get(0).gems-=2;
					return;
             }
		 }
		
	}

    @Override
    public void onLandClick(View v) {
        Toast.makeText(MainActivity.this, v.getTag().toString(), Toast.LENGTH_SHORT).show();
        if (gameState == "moving" && moveTargetIds.contains(Integer.parseInt((String)v.getTag()))) {
            for (Region currentLocation : theGame.board.regions()
            ) {
                if (currentLocation.players.contains(theGame.players.get(0))) {
                    currentLocation.players.remove(theGame.players.get(0));
                }
            }
            theGame.board.regions().get(Integer.parseInt((String)v.getTag())-1).players.add(theGame.players.get(0));
            theGame.players.get(0).subdue(moveTargets.get(moveTargetIds.indexOf(Integer.parseInt((String)v.getTag()))).first);
            rootView.bindHand(theGame.players.get(0).hand);
            for (Region anArea : theGame.board.regions()
            ) {
                rootView.bindLand(theGame.board.regions().indexOf(anArea) + 1, anArea);
            }
            gameState="open";
        }

    }
    
    @Override
    public void onHandClick(View  v) {
		Card transferCard = null;
		if (gameState == "storingQuestCards") {
			//TODO check if legal card
			//if (((TextView)v).getTextColor == Color.YELLOW)
			 {
				transferCard = getTransferCard(v);
				if (transferCard !=null) {
					theGame.players.get(0).hand.remove(transferCard);
					rootView.bindHand(theGame.players.get(0).hand);
					((TextView)v).setTextColor(Color.WHITE);
					//TODO Clear card highlighting
					((Quest)theGame.players.get(0).quests.get(0)).stored.add(transferCard);
					rootView.bindQuestStorage((Quest)theGame.players.get(0).quests.get(0), ((Quest)theGame.players.get(0).quests.get(0)).stored);
				}
			}
		}
		else if (gameState == "storingPrivate") {
			Toast.makeText(MainActivity.this, "storingPrivate", Toast.LENGTH_SHORT).show();
			transferCard = getTransferCard(v);
			if (transferCard != null) {
				theGame.players.get(0).hand.remove(transferCard);
				theGame.players.get(0).publicQuest.add(transferCard);
				rootView.bindHand(theGame.players.get(0).hand);
				rootView.bindStorage(theGame.players.get(0).publicQuest);
			}
		}
	}
	
	private Card getTransferCard(View v) {
				for (Card aHandCard : theGame.players.get(0).hand) {
				if (((TextView)v).getText() == aHandCard.name) {
					return aHandCard;
				}
			}
		return null;
	}
	private Region getMatchingTowerRegion(Region startingRegion) {
		for (Region aLocation : theGame.board.regions()
            ) {
				if (startingRegion!=aLocation && aLocation.tower == startingRegion.tower) {
					return aLocation;
				}
			}
		return startingRegion;
	}
	
	@Override
	public void onStoreCardsClick() {
		gameState = "storingPrivate";
		rootView.gameStateChange(gameState);
	}
	
	@Override
	public void onStoreQuestClick() {
		gameState = "storingQuestCards";
		List<Card> matchQuest = new ArrayList<>();
		for (Card questCard : theGame.players.get(0).quests) {
		for (Card handCard : theGame.players.get(0).hand) {
			if (handCard instanceof CreatureCard) {
				 ListIterator<Symbol> questIter = ((Quest)questCard).requirements.listIterator(0);
				 while (questIter.hasNext()) {
					if (((CreatureCard)handCard).values.get(0) == questIter.next()) {
						if (!matchQuest.contains(handCard)) {
							matchQuest.add(handCard);
						}
						break;
					}
				}
			}
		}
		}
		rootView.validCardsForQuest(matchQuest);
		rootView.gameStateChange(gameState);
	}
	
	@Override
	public void onDoneClick() {
			Toast.makeText(MainActivity.this, "onDone Main", Toast.LENGTH_SHORT).show();
		gameState = "open";
		rootView.gameStateChange(gameState);
	}
}
