package com.example.fantastiqa.screens.controllers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import com.example.fantastiqa.GameState.Ability;
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

public class MainActivity extends AppCompatActivity implements ViewMvc.ViewMvcListener, Player.playerListener {
    private Button buttonMove;
    private Game theGame;
    private com.example.fantastiqa.screens.views.ViewMvc rootView;
    private Player currentPlayer;
    private Region currentLocation;
    private String gameState;
    private List<Pair<Road,Region>> moveTargets = new LinkedList<>();
    private List<Integer> moveTargetIds = new LinkedList<>();
    private CreatureCard emptyRoadCard = new CreatureCard("",Symbol.NONE, false, Ability.NONE ,Symbol.NONE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = new RootViewMvcImpl(this, null);
        rootView.setListener(this);

        setContentView(rootView.getRootView());
        //setContentView(R.layout.activity_main);

        theGame = new Game();
        for (Player aPlayer : theGame.players) {
			aPlayer.setPlayerListener(this);
		}
		currentPlayer = theGame.players.get(0);
		
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

            rootView.bindHand(currentPlayer.hand);
            Toast.makeText(MainActivity.this, currentPlayer.hand.get(0).name, Toast.LENGTH_SHORT);
            rootView.bindPlayerQuest(currentPlayer.quests);
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

    public List<Pair<Road,Region>> getMoves(){
        List<Pair<Road,Region>> results = new LinkedList<>();
        for (Pair<Road, Region> adjPair: getAdjacentLands(currentPlayer)){
            if (canSubdue(adjPair.first.creature)) {
                results.add(adjPair);
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
        if (aCreature.subduedBy == Symbol.NONE) {
			return false;
		}
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
        //Toast.makeText(MainActivity.this, Integer.toString(currentPlayer.hand.size()),Toast.LENGTH_SHORT);
        for (Card aCard:currentPlayer.hand
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
        for (Card aCard:currentPlayer.hand
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
        for (Pair<Road,Region> aPair:getMoves()
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
                if (aLocation.players.contains(currentPlayer)) {
					Region matchingRegion = getMatchingTowerRegion(aLocation);
					matchingRegion.players.add(currentPlayer);
					aLocation.players.remove(currentPlayer);
					rootView.bindLand(theGame.board.regions().indexOf(matchingRegion) + 1, matchingRegion);
					rootView.bindLand(theGame.board.regions().indexOf(aLocation) + 1, aLocation);
					currentPlayer.changeGems(-2);
					return;
             }
		 }
		
	}

    @Override
    public void onLandClick(View v) {
		Pair<Road,Region> moveTarget = moveTargets.get(moveTargetIds.indexOf(Integer.parseInt((String)v.getTag())));
        Toast.makeText(MainActivity.this, v.getTag().toString(), Toast.LENGTH_SHORT).show();
        if (gameState == "moving" && moveTargetIds.contains(Integer.parseInt((String)v.getTag()))) {
            for (Region currentLocation : theGame.board.regions()
            ) {
                if (currentLocation.players.contains(currentPlayer)) {
                    currentLocation.players.remove(currentPlayer);
                }
            }
            theGame.board.regions().get(Integer.parseInt((String)v.getTag())-1).players.add(currentPlayer);
            subdue(moveTarget.first);
            rootView.bindHand(currentPlayer.hand);
            for (Region anArea : theGame.board.regions()
            ) {
                rootView.bindLand(theGame.board.regions().indexOf(anArea) + 1, anArea);
            }
            rootView.bindRoad(theGame.board.roads().indexOf(moveTarget.first)+1, moveTarget.first);
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
					currentPlayer.hand.remove(transferCard);
					rootView.bindHand(currentPlayer.hand);
					((TextView)v).setTextColor(Color.WHITE);
					//TODO Clear card highlighting
					((Quest)currentPlayer.quests.get(0)).stored.add(transferCard);
					rootView.bindQuestStorage((Quest)currentPlayer.quests.get(0), ((Quest)currentPlayer.quests.get(0)).stored);
				}
			}
		}
		else if (gameState == "storingPrivate") {
			Toast.makeText(MainActivity.this, "storingPrivate", Toast.LENGTH_SHORT).show();
			transferCard = getTransferCard(v);
			if (transferCard != null) {
				currentPlayer.hand.remove(transferCard);
				currentPlayer.publicQuest.add(transferCard);
				rootView.bindHand(currentPlayer.hand);
				rootView.bindStorage(currentPlayer.publicQuest);
			}
		}
	}
	
	private Card getTransferCard(View v) {
				for (Card aHandCard : currentPlayer.hand) {
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
		for (Card questCard : currentPlayer.quests) {
		for (Card handCard : currentPlayer.hand) {
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
	
	@Override
	public void onGemChange(int newGems, int oldGems) {
		if (oldGems < 2 && newGems > 2) {
			rootView.setTowerTeleport(true);
		}
		else if (oldGems > 2 && newGems < 2) {
			rootView.setTowerTeleport(false);
		}
		
	}
	
	private void subdue(Road toSubdue) {
	        for (Card aCard : currentPlayer.hand
        ) {
            if (aCard instanceof CreatureCard) {
                CreatureCard handCreature = (CreatureCard) aCard;
                //Toast.makeText(MainActivity.this, handCreature.values.get(0).toString(), Toast.LENGTH_SHORT).show();
                if (toSubdue.creature.subduedBy == handCreature.values.get(0)) {
                    currentPlayer.hand.remove(handCreature);
                    currentPlayer.discard.add(handCreature);
                    currentPlayer.discard.add(toSubdue.creature);
                    toSubdue.creature = emptyRoadCard;
                    break;
                }
            }
            //TODO other methods of subdue
        }
	}
}
