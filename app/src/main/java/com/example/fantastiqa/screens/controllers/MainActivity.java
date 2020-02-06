package com.example.fantastiqa.screens.controllers;

import android.util.Log;

import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.fantastiqa.GameState.Ability;
import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.CreatureCard;
import com.example.fantastiqa.GameState.Deck;
import com.example.fantastiqa.GameState.Game;
import com.example.fantastiqa.GameState.Player;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.Region;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.GameState.Symbol;
import com.example.fantastiqa.screens.GameStatus;
import com.example.fantastiqa.screens.spaceRegion;
import com.example.fantastiqa.screens.spaceRoad;
import com.example.fantastiqa.screens.views.RootViewMvcImpl;
import com.example.fantastiqa.screens.views.ViewMvc;
import com.example.fantastiqa.R;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.ListUtils;

public class MainActivity extends AppCompatActivity implements ViewMvc.ViewMvcListener, Player.playerListener {
    private Game theGame;
    private com.example.fantastiqa.screens.views.ViewMvc rootView;
    private Player currentPlayer;
    private Region currentLocation;

    private GameStatus gameState;
    private List<Pair<Road,Region>> moveTargets = new LinkedList<>();
    private List<spaceRegion> moveTargetIds = new LinkedList<>();
    private CreatureCard emptyRoadCard = new CreatureCard("",Symbol.NONE, false, Ability.NONE ,Symbol.NONE);
	private List<Card> playerChoices = new ArrayList<>(5);
    private Deck visitingDeck;

    private Map<spaceRegion,Region> spaceRegionMap = new EnumMap<spaceRegion, Region>(spaceRegion.class);
    private Map<Region,spaceRegion> regionSpaceMap = new HashMap<>();
    private Map<Road,spaceRoad> roadSpaceMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        rootView = new RootViewMvcImpl(this, null);
        rootView.setListener(this);




        setContentView(rootView.getRootView());
        //setContentView(R.layout.activity_main);
		Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
		myToolbar.setTitle("");
		setSupportActionBar(myToolbar);
		//getSupportActionBar().setDisplayShowTitleEnabled(false);

        //TODO ask for players and vps, starting player
        theGame = new Game();
        //
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
			//TODO ask for starting locations
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

        bindAllRoads();

        bindAllQuests();

        rootView.bindHand(currentPlayer.hand);
        rootView.bindPlayerStorage(currentPlayer.quests);
    }

    private void bindAllQuests() {
        for (Quest aQuest : theGame.board.quests) {
                rootView.bindQuest(theGame.board.quests.indexOf(aQuest)+1, aQuest, canCompleteQuest(aQuest));
            }
    }

    private void bindAllRoads() {
        for (Road aRoad: theGame.board.roads()) {
            rootView.bindRoad(roadSpaceMap.get(aRoad), aRoad);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actions_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.adventuring :
				rootView.onMoveClick();
				return true;
		}
		return false;
	}

    @Override
	public void toast(String text) {
		Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show();
	}

    @Override
    public void finishPhase()
    {
        if (gameState == GameStatus.OPEN) {
            beginDiscard();
        }
        else if (gameState == GameStatus.DISCARD){
            endTurn();
        }
        else {
            changeGameState(GameStatus.OPEN);
        }
	}

    @Override
    public Boolean canCompleteQuest(Quest aQuest) {
        return theGame.canCompleteQuest(aQuest, ListUtils.union(currentPlayer.storage, currentPlayer.hand));
    }

    @Override
    public void beginCompleteQuest(Quest aQuest) {
        if (canCompleteQuest(aQuest)) {
            List<Card> used = theGame.completeQuest(aQuest, ListUtils.union(currentPlayer.storage, currentPlayer.hand));
            if (used.size()>0) {
                currentPlayer.setVps(currentPlayer.getVps()+aQuest.vps);
                currentPlayer.changeGems(aQuest.gems);
                for (Card usedCard : used) {
                    currentPlayer.storage.remove(usedCard);
                    currentPlayer.hand.remove(usedCard);
                    currentPlayer.deck.discard(usedCard);
                }
                rootView.bindHand(currentPlayer.hand);
                rootView.bindPlayerStorage(currentPlayer.storage);
                //TODO remove quest from board
            }
            changeGameState(GameStatus.OPEN);
        }
    }

    private void endTurn() {
        toast(currentPlayer.deck.sizeToString());

        if (currentPlayer.getVps() >= theGame.VPgoal ) {
            toast("Game Over " + currentPlayer.name );
        }
        for (Road aRoad : theGame.board.roads()) {
            if (aRoad.creature == emptyRoadCard) {
                Card nextCard = theGame.creatureDeck.drawOne();
                if (nextCard instanceof CreatureCard) {
                    aRoad.creature = (CreatureCard)nextCard;
                }
                else {
                    //Event?
                }
            }
        }

        for (Quest newQuest : theGame.questDeck.draw(2-theGame.board.quests.size())) {
            theGame.board.quests.add(newQuest);
        }
        currentPlayer.drawCards(5-currentPlayer.hand.size());
        currentPlayer = theGame.nextPlayer(currentPlayer);
        for (Region anArea: theGame.board.regions()
                 ) {
				if (anArea.players.contains(currentPlayer)) {
					currentLocation = anArea;
				}
		}
        bindAllQuests();
        bindAllRoads();
        rootView.bindHand(currentPlayer.hand);
        rootView.bindPlayerStorage(currentPlayer.quests);
        rootView.bindStorage(currentPlayer.storage);
        for (Card aQuest : currentPlayer.quests) {
            rootView.bindQuestStorage((Quest)aQuest, ((Quest) aQuest).stored);
        }
        rootView.bindQuest(1, theGame.board.quests.get(0),canCompleteQuest(theGame.board.quests.get(0)));
        rootView.bindQuest(2, theGame.board.quests.get(1),canCompleteQuest(theGame.board.quests.get(1)));
        rootView.updateVps(currentPlayer.getVps());
        rootView.updateGems(currentPlayer.getGems());
        rootView.updateDeck(currentPlayer.deck.size());
        rootView.updateDeck(currentPlayer.deck.discardSize());
		changeGameState(GameStatus.OPEN);
        
        toast(currentPlayer.deck.sizeToString());

    }

    private void changeGameState(GameStatus newState) {
		rootView.gameStateChange(newState);
		gameState = newState;
		if (newState == GameStatus.OPEN) {
		    moveTargets.clear();
		    moveTargetIds.clear();
		    playerChoices.clear();
            visitingDeck=null;
        }
		rootView.updateDeck(currentPlayer.deck.size());
		rootView.updateDiscard(currentPlayer.deck.discardSize());
		rootView.updateGems(currentPlayer.getGems());
		rootView.updateVps(currentPlayer.getVps());

	}

	@Override
    public void beginDiscard() {
        changeGameState((GameStatus.DISCARD));
    }

	@Override
	public void onGemChange(int newGems, int oldGems) {
		if (oldGems < 2 && newGems > 2) {
			rootView.setTowerTeleport(true);
		}
		else if (oldGems > 2 && newGems < 2) {
			rootView.setTowerTeleport(false);
		}
        rootView.updateGems(currentPlayer.getGems());
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
        if (gameState == GameStatus.MOVING && moveTargetIds.contains(newSpace)) {
			//endAdventuring
            Pair<Road,Region> moveTarget = moveTargets.get(moveTargetIds.indexOf(newSpace));
            moveTargets.clear();
            moveTargets.add(moveTarget);
            if (subdue(moveTarget.first)) {
				return finishMove(moveTarget.first, moveTarget.second);
            }
        } else if (gameState == GameStatus.FLYING_CARPET && moveTargetIds.contains(newSpace)) {
			endFlyingCarpet(newSpace);
			return true;
		}
		return false;
    }
    
    private Boolean finishMove(Road moveTarget, Region regionTarget){
		    movePlayer(currentPlayer, regionTarget);
                rootView.bindHand(currentPlayer.hand);
                rootView.bindRoad(roadSpaceMap.get(moveTarget), moveTarget);
                changeGameState(GameStatus.OPEN);
                moveTargets.clear();
                moveTargetIds.clear();
                return true;
	}

	private Boolean subdue(Road toSubdue) {
        List<Set<Card>> subdueSets;
        subdueSets = theGame.canSubdueSingle(toSubdue.creature, currentPlayer.hand);
        if (subdueSets.size()==1) {
            //remove automatically
            return subdue(toSubdue, subdueSets.get(0));
        }
        else {
            //ask player to choose
            rootView.selectSubdueOption(subdueSets, toSubdue);
        }
            //TODO other methods of subdue

        return false;
	}

	@Override
    public Boolean subdue(Road toSubdue, Set<Card> subdueSet) {
        Log.d("fantastiqa", subdueSet.toString());
        if (moveTargets.size()==1) {
		
        Iterator<Card> setIter = subdueSet.iterator();
        while (setIter.hasNext()) {
            Card useCard = setIter.next();
            currentPlayer.discardFromHand(useCard);
        }
        currentPlayer.gainCard(toSubdue.creature);
        toSubdue.creature = emptyRoadCard;
        	return finishMove(moveTargets.get(0).first, moveTargets.get(0).second);
		}
		//could find it from road
		return false;
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
            changeGameState(GameStatus.MOVING);
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
            return theGame.canSubdueSingle(aCreature, currentPlayer.hand).size()>0;
        }
    }

    private Boolean canSubdueSingle(CreatureCard aCreature){
        Boolean matchFirst = false;
        Boolean hasDoubleSymbol = false;
        Boolean hasTwoCards = false;

        List<Symbol> handSymbols = new ArrayList<>();
        //Check hand for first symbol
        for (Card aCard:currentPlayer.hand
        ) {
            if (aCard instanceof CreatureCard && ((CreatureCard)aCard).values.get(0) != Symbol.NONE) {
                CreatureCard handCreature = (CreatureCard) aCard;
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
        return (symbolCount>=2);
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

	public void onTowerTeleportClick(View v) {
			towerTeleport();
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
    private void visitTowerCards(Deck towerDeck) {
        changeGameState(GameStatus.TOWER_BUY);
        visitingDeck = towerDeck;
        List<Card> selectableCards = currentPlayer.handContains(Ability.PLUS_CARD);
        if (selectableCards.size() > 0) {
            rootView.selectKeyCards(selectableCards);
        } else
            onSelectedKeyCards(new ArrayList<Card>());

    }
	@Override
	public void beginVisitTowerCards() {
        switch (currentLocation.tower) {
            case BAZAAR:
                visitTowerCards(theGame.bazaarDeck);
                break;
            case QUEST:
                visitTowerCards(theGame.questDeck);
                break;
            case ARTIFACT:
                break;
        }

	}

	@Override
	public void onSelectedKeyCards(List<Card> selections) {
		int keyCardCount = 0;
		for (Card selectedCard : selections) {
			keyCardCount++;
            currentPlayer.deck.discard(selectedCard);
			currentPlayer.hand.remove(selectedCard);
		}
        List<Boolean> canBuy = new ArrayList<>(3 + keyCardCount);
        playerChoices.addAll(visitingDeck.draw(3 + keyCardCount));
		for (int i = 0;i < 3 +keyCardCount ; i++) {
            //TODO card cost
            //TODO change to true
            if (false) { //currentPlayer.getGems()>=3) {
                canBuy.add(true);
            } else {
                canBuy.add(false);
            }
			}

		rootView.bindHand(currentPlayer.hand);
        rootView.selectCard(playerChoices, canBuy);
	}
	
	@Override
	public void endVisitTowerCards(int buy) {
		//TODO in Game possiblePurchase list
		if (visitingDeck == theGame.questDeck) {
            currentPlayer.quests.add(playerChoices.remove(buy));
            rootView.bindPlayerStorage(currentPlayer.quests);
        }
        else {
            currentPlayer.deck.discard(playerChoices.remove(buy));
            currentPlayer.changeGems(-3);
        }
		ListIterator<Card> cardIter = playerChoices.listIterator();
		while (cardIter.hasNext()) {

            visitingDeck.discard(cardIter.next());
			cardIter.remove();
		}

        toast(currentPlayer.deck.sizeToString());
		changeGameState(GameStatus.OPEN);

    }

	//Free Actions
	//Store cards
	@Override
	public void beginStoreCardsPrivate() {
        changeGameState(GameStatus.STORING_PRIVATE);
	}
	
	@Override
	public void beginStoreCardsQuest() {
        changeGameState(GameStatus.STORING_QUESTCARDS);
	}

	@Override
	public List<Card> getValidQuestCards() {
		List<Card> matchQuest = new ArrayList<>();
		//TODO multiple quest selection for (Card questCard : currentPlayer.quests) {
		Card questCard = currentPlayer.quests.get(0);
		for (Card handCard : currentPlayer.hand) {
			if (handCard instanceof CreatureCard) {
				 ListIterator<Symbol> questIter = ((Quest)questCard).getRequirements().listIterator(0);
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
            changeGameState(GameStatus.OPEN);
		}
		return matchQuest;
	}

	@Override
	public void storeCardQuest(Card aCard) {
        if (gameState == GameStatus.STORING_QUESTCARDS) {
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
    public Boolean storeCardPrivate(Card aCard) {
		if (gameState == GameStatus.STORING_PRIVATE) {
			if (aCard != null) {
				currentPlayer.hand.remove(aCard);
				currentPlayer.storage.add(aCard);
                rootView.bindStorage(currentPlayer.storage);
                return true;
			}
		}
        return false;
	}

	@Override
	public void discardFromStorage(Card aCard){
		if (gameState == GameStatus.STORING_PRIVATE || 
			gameState == GameStatus.DISCARD) {
				currentPlayer.storage.remove(aCard);
				currentPlayer.deck.discard(aCard);
				rootView.bindStorage(currentPlayer.storage);
			}
	}

	@Override
    public void handClick(Card aCard) {
        //TODO check if card eligible
	    //	Toast.makeText(MainActivity.this, gameState.toString(), Toast.LENGTH_SHORT);
        if (gameState == GameStatus.STORING_PRIVATE) {
            if (storeCardPrivate(aCard)) {
                rootView.removeHandCard(aCard, currentPlayer.hand);
            }
        } else if (gameState == GameStatus.STORING_QUESTCARDS) {
			storeCardQuest(aCard);
		} else if (gameState == GameStatus.RELEASE) {
            if (releaseCard(aCard)) {
                rootView.removeHandCard(aCard, currentPlayer.hand);
                if (currentPlayer.getGems() == 0) {
                    changeGameState(GameStatus.OPEN);
                }
            }
        }
        if (gameState == GameStatus.DISCARD) {
            currentPlayer.discardFromHand(aCard);
            rootView.removeHandCard(aCard, currentPlayer.hand);
        }
	}
	
	
	//Flying Carpet
	@Override
	public List<spaceRegion> beginFlyingCarpet() {
		if (currentPlayer.getFlyingCarpets()>0) {
            changeGameState(GameStatus.FLYING_CARPET);
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
        changeGameState(GameStatus.OPEN);
		moveTargets.clear();
        moveTargetIds.clear();
            
	}

    //Release card
    @Override
    public List<Card> beginReleaseCard() {
        List<Card> results = new ArrayList<>(5);
        if (currentPlayer.getGems() > 0) {
            for (Card handCard : currentPlayer.hand) {
                if (((CreatureCard) handCard).name != "Peaceful Dragon") {
                    results.add(handCard);
                }
            }
            changeGameState(GameStatus.RELEASE);
        }
        return results;
    }

    private Boolean releaseCard(Card aCard) {
        if (currentPlayer.getGems() > 0) {
            currentPlayer.hand.remove(aCard);
            currentPlayer.changeGems(-1);
            return true;
        }
        return false;
    }

}
