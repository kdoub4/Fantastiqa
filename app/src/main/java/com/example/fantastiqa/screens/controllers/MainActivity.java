package com.example.fantastiqa.screens.controllers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.fantastiqa.screens.deckCards;
import com.example.fantastiqa.screens.spaceRoad;
import com.example.fantastiqa.screens.spaceRegion;
import com.example.fantastiqa.screens.gameStatus;

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
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements ViewMvc.ViewMvcListener, Player.playerListener {
    private Button buttonMove;
    private Game theGame;
    private com.example.fantastiqa.screens.views.ViewMvc rootView;
    private Player currentPlayer;
    private Region currentLocation;
    private gameStatus gameState;
    private List<Pair<Road,Region>> moveTargets = new LinkedList<>();
    private List<spaceRegion> moveTargetIds = new LinkedList<>();
    private CreatureCard emptyRoadCard = new CreatureCard("",Symbol.NONE, false, Ability.NONE ,Symbol.NONE);
    private Map<spaceRegion,Region> spaceRegionMap = new EnumMap<>(spaceRegion.class);
    private Map<Region,spaceRegion> regionSpaceMap = new HashMap<>();
	private Map<Road,spaceRoad> roadSpaceMap = new HashMap<>();
	private List<Card> playerChoices = new ArrayList<>(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        rootView = new RootViewMvcImpl(this, null);
        rootView.setListener(this);

		
        

        setContentView(rootView.getRootView());
        //setContentView(R.layout.activity_main);
		Toolbar myToolbar = (Toolbar) findViewById(R.id.tower_menu);
		setSupportActionBar(myToolbar);

        theGame = new Game();
        for (Player aPlayer : theGame.players) {
			aPlayer.setPlayerListener(this);
		}
		currentPlayer = theGame.players.get(0);
		
            for (Region anArea: theGame.board.regions()
                 ) {
				if (anArea.players.contains(currentPlayer)) {
					currentLocation = anArea;
				}
				spaceRegion compassRegion =spaceRegion.values()[theGame.board.regions().indexOf(anArea)]; 
				spaceRegionMap.put(compassRegion, anArea);
				regionSpaceMap.put(anArea, compassRegion);
				rootView.bindLand(compassRegion, anArea);
            }
			
			roadSpaceMap.put(theGame.board.getRoad(
				spaceRegionMap.get(spaceRegion.NW), spaceRegionMap.get( spaceRegion.NE)),spaceRoad.N);
			roadSpaceMap.put(theGame.board.getRoad(
				spaceRegionMap.get(spaceRegion.NE), spaceRegionMap.get( spaceRegion.E)),spaceRoad.NE);
			roadSpaceMap.put(theGame.board.getRoad(
				spaceRegionMap.get(spaceRegion.E), spaceRegionMap.get( spaceRegion.SE)),spaceRoad.SE);
			roadSpaceMap.put(theGame.board.getRoad(
				spaceRegionMap.get(spaceRegion.SE), spaceRegionMap.get( spaceRegion.SW)),spaceRoad.S);
			roadSpaceMap.put(theGame.board.getRoad(
				spaceRegionMap.get(spaceRegion.SW), spaceRegionMap.get( spaceRegion.W)),spaceRoad.SW);
			roadSpaceMap.put(theGame.board.getRoad(
				spaceRegionMap.get(spaceRegion.W), spaceRegionMap.get( spaceRegion.NW)),spaceRoad.NW);
			roadSpaceMap.put(theGame.board.getRoad(
				spaceRegionMap.get(spaceRegion.W), spaceRegionMap.get( spaceRegion.E)),spaceRoad.MID);

            for (Road aRoad: theGame.board.roads()) {
                rootView.bindRoad(roadSpaceMap.get(aRoad), aRoad);
            }

            for (Quest aQuest : theGame.board.quests) {
                rootView.bindQuest(theGame.board.quests.indexOf(aQuest)+1, aQuest);
            }

            rootView.bindHand(currentPlayer.hand);
            rootView.bindPlayerQuest(currentPlayer.quests);
    }
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
		
		return true;
	}*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tower_menu, menu);
		return true;
	}

	@Override
	public void toast(String text) {
		Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show();
	}

    @Override
    public void finishPhase() {
		changeGameState(gameStatus.OPEN);
	}
	
    private void changeGameState(gameStatus newState) {
		rootView.gameStateChange(newState);
		gameState = newState;
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
	
	private void movePlayer(Player thePlayer, Region newRegion) {
		currentLocation.players.remove(currentPlayer);
        newRegion.players.add(currentPlayer);
            currentLocation= newRegion;
            for (Region anArea : theGame.board.regions()
            ) {
                rootView.bindLand(regionSpaceMap.get(anArea),anArea);
            }
	}

	//Go Adventuring
	@Override
    public Boolean doMove(spaceRegion newSpace) {
		Toast.makeText(MainActivity.this, newSpace.toString(), Toast.LENGTH_SHORT).show();
        if (gameState == gameStatus.MOVING && moveTargetIds.contains(newSpace)){
			Pair<Road,Region> moveTarget = moveTargets.get(moveTargetIds.indexOf(newSpace));
            movePlayer(currentPlayer, moveTarget.second);
            subdue(moveTarget.first);
            rootView.bindHand(currentPlayer.hand);
            rootView.bindRoad(roadSpaceMap.get(moveTarget.first), moveTarget.first);
            changeGameState(gameStatus.OPEN);
            moveTargets.clear();
            moveTargetIds.clear();
            return true;
        }
        else if (gameState == gameStatus.FLYING_CARPET && moveTargetIds.contains(newSpace)) {
			endFlyingCarpet(newSpace);
			return true;
		}
		return false;
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
    @Override
    public List<spaceRegion> getValidAdventuring() {
        for (Pair<Road,Region> aPair:getMoves()
             ) {
            //Integer regionId = theGame.board.regions().indexOf(aPair.second);
            //rootView.highlightLand(regionId);
            moveTargets.add(aPair);
            moveTargetIds.add(regionSpaceMap.get(aPair.second));    
        }
        if (moveTargets.size() > 0){
            changeGameState(gameStatus.MOVING);
		}
        return moveTargetIds;
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
        Toast.makeText(MainActivity.this, aCreature.subduedBy.toString(), Toast.LENGTH_SHORT).show();
        //Check hand for first symbol
        Toast.makeText(MainActivity.this, Integer.toString(currentPlayer.hand.size()),Toast.LENGTH_SHORT);
        for (Card aCard:currentPlayer.hand
        ) {
            if (aCard instanceof CreatureCard && ((CreatureCard)aCard).values.get(0) != Symbol.NONE) {
                CreatureCard handCreature = (CreatureCard) aCard;
                Toast.makeText(MainActivity.this, handCreature.values.get(0).toString(), Toast.LENGTH_SHORT).show();
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
			Toast.makeText(MainActivity.this, hasDoubleSymbol?"double":hasTwoCards?"two":"mustbeFirst", Toast.LENGTH_SHORT).show();
            return true;
        }
        toast("false");
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
        if (thePlayer == currentPlayer) {
			playerArea=currentLocation;
		}
		else {
          for (Region aRegion : theGame.board.regions()) {
            if (aRegion.players.contains(thePlayer)){
                playerArea = aRegion;
                break;
            }
          }
		}
		if (playerArea == null) return null;
        return theGame.board.getAdjacentAreas(playerArea);
    }

	//Visit Tower
	//Tower Teleport
		
	@Override
	public Boolean canTowerTeleport() {
		return currentPlayer.getGems() >= 2;
	}

    @Override
    public spaceRegion towerTeleport() {
				//TODO check for enough gems
                	Region matchingRegion = theGame.board.getTowerMatch(currentLocation);
					matchingRegion.players.add(currentPlayer);
					currentLocation.players.remove(currentPlayer);
					rootView.bindLand(regionSpaceMap.get(matchingRegion), matchingRegion);
					rootView.bindLand(regionSpaceMap.get(currentLocation),currentLocation);
					currentPlayer.changeGems(-2);
					currentLocation = matchingRegion;
					return regionSpaceMap.get(currentLocation);
	}

	//New cards
	//Beast Bazaar
	@Override
	public void beginVisitBazaar() {
		List<Card> selectableCards = currentPlayer.handContains(Ability.PLUS_CARD);
		if (selectableCards.size()>0) {
			rootView.selectKeyCards(selectableCards);
		}
		else 
			onSelectedKeyCards(new ArrayList<Card>());
		changeGameState(gameStatus.TOWER_BUY);
	}

	@Override
	public void onSelectedKeyCards(List<Card> selections) {
		int keyCardCount = 0;
		for (Card selectedCard : selections) {
			keyCardCount++;
			currentPlayer.discard.add(selectedCard);
			currentPlayer.hand.remove(selectedCard);
		}
		for (int i = 0;i < 3 +keyCardCount ; i++) {
			if (theGame.bazaarDeck.size() > 0) {
				playerChoices.add(theGame.bazaarDeck.remove(0));
			}
		}
		rootView.bindHand(currentPlayer.hand);
		rootView.selectCard(playerChoices);
	}
	
	@Override
	public void endVisitBazaar(int buy) {
		//TODO in Game possiblePurchase list
		Toast.makeText(MainActivity.this, "endBazaar", Toast.LENGTH_SHORT).show();
		changeGameState(gameStatus.OPEN);
		currentPlayer.discard.add( playerChoices.remove(buy));
		ListIterator<Card> cardIter = playerChoices.listIterator();
		while (cardIter.hasNext()) {
			
			theGame.bazaarDeck.add((CreatureCard)cardIter.next());
			cardIter.remove();
		}
	}

	//Free Actions
	//Store cards
	@Override
	public void beginStoreCardsPrivate() {
		changeGameState(gameStatus.STORING_PRIVATE);
	}
	
	@Override
	public void beginStoreCardsQuest() {
		changeGameState(gameStatus.STORING_QUESTCARDS);
	}

	@Override
	public List<Card> getValidQuestCards() {
		List<Card> matchQuest = new ArrayList<>();
		//TODO multiple quest selection for (Card questCard : currentPlayer.quests) {
		Card questCard = currentPlayer.quests.get(0);
		for (Card handCard : currentPlayer.hand) {
			if (handCard instanceof CreatureCard) {
				 ListIterator<Symbol> questIter = ((Quest)questCard).requirements.listIterator(0);
				 while (questIter.hasNext()) {
					 Symbol requiredSymbol = questIter.next();
					 Toast.makeText(MainActivity.this, ((CreatureCard)handCard).values.get(0).toString() + requiredSymbol.toString(), Toast.LENGTH_SHORT).show();
					if (((CreatureCard)handCard).values.get(0) == requiredSymbol) {
						if (!matchQuest.contains(handCard)) {
							matchQuest.add(handCard);
						}
						break;
					}
				}
			}
		}
		//rootView.validCardsForQuest(matchQuest);
		//rootView.gameStateChange(gameState);
		if (matchQuest.size()==0) {
			changeGameState(gameStatus.OPEN);
		}
		return matchQuest;
	}

	@Override
	public void storeCardQuest(Card aCard) {
		if (gameState == gameStatus.STORING_QUESTCARDS) {
			//TODO check if legal card
			//TODO other quests
			//if (((TextView)v).getTextColor == Color.YELLOW)
			 {
				//transferCard = getTransferCard(v);
				if (aCard !=null) {
					currentPlayer.hand.remove(aCard);
					//TODO single card bind
					rootView.bindHand(currentPlayer.hand);
					//((TextView)v).setTextColor(Color.WHITE);
					//TODO Clear card highlighting
					((Quest)currentPlayer.quests.get(0)).stored.add(aCard);
					rootView.bindQuestStorage((Quest)currentPlayer.quests.get(0), ((Quest)currentPlayer.quests.get(0)).stored);
				}
			}
		}
	}
	
    @Override
    public void storeCardPrivate(Card aCard) {
		Card transferCard = aCard;
		if (gameState == gameStatus.STORING_PRIVATE) {
			if (transferCard != null) {
				currentPlayer.hand.remove(transferCard);
				currentPlayer.publicQuest.add(transferCard);
				rootView.bindHand(currentPlayer.hand);
				rootView.bindStorage(currentPlayer.publicQuest);
			}
		}
	}

	@Override
	public void storeCard(Card aCard) {
		Toast.makeText(MainActivity.this, gameState.toString(), Toast.LENGTH_SHORT);
		if (gameState == gameStatus.STORING_PRIVATE) {
			storeCardPrivate(aCard);
		}
		else if (gameState == gameStatus.STORING_QUESTCARDS) {
			storeCardQuest(aCard);
		}
	}
	
	//Flying Carpet
	@Override
	public List<spaceRegion> beginFlyingCarpet() {
		if (currentPlayer.getFlyingCarpets()>0) {
			changeGameState(gameStatus.FLYING_CARPET);
			for(Pair<Road,Region> aRegion : theGame.board.getAdjacentAreas(currentLocation)) {
				moveTargetIds.add(regionSpaceMap.get(aRegion.second));
			}
		}
		else {
			//TODO message can't do
		}
		return moveTargetIds;
	}
	
	@Override
	public void endFlyingCarpet(spaceRegion newSpace) {
		Region moveTarget = spaceRegionMap.get(newSpace);
		movePlayer(currentPlayer, moveTarget);
		currentPlayer.useFlyingCarpet();
		changeGameState(gameStatus.OPEN);
		moveTargets.clear();
        moveTargetIds.clear();
            
	}
}
