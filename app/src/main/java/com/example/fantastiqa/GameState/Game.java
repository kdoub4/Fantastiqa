package com.example.fantastiqa.GameState;

import android.widget.ArrayAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Combinations;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.ListIterator;

public class Game {
    public Board board;
    public int VPgoal=4;
    public Deck<Card> creatureDeck;
    public Deck<Artifact> artifactDeck;
    public Deck<CreatureCard> bazaarDeck;
    public Deck<Quest> questDeck;
    public List<Player> players = new ArrayList<>();
	public List<Card> selectCards = new ArrayList<>(5);
//	public deckType selectType;
	
    public Game() {
        Random random = new Random();
        //Create creatureDeck
        //TopDeck
        ArrayList<Card> tempDeck = new ArrayList<>();
        ArrayList<Card> tempTop = new ArrayList<>();
        ArrayList<CreatureCard> tempBazaar = new ArrayList<>();

        for (CreatureCards aCard: CreatureCards.values()) {
            if (aCard.getValue2()== Symbol.NONE) {
                for (int j = 0; j < 2; j++) {
                    tempDeck.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
                }
            }
        }
        //TODO Ravens
        tempTop.addAll(tempDeck);
        Collections.shuffle(tempTop);
        tempDeck.clear();

        //BottomDeck
        //BeastBazaar
        for (CreatureCards aCard: CreatureCards.values()) {
            if (aCard.getValue2() ==Symbol.NONE) {
                tempDeck.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
            }
            else {
                tempDeck.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(),aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
                tempDeck.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
                for (int i = 0; i < 3;i++){
                    tempBazaar.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), false, aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
                }
            }
        }
        bazaarDeck = new Deck<>(tempBazaar);
        bazaarDeck.shuffle(true);

        Collections.shuffle(tempDeck);
        tempTop.addAll(tempDeck);
        creatureDeck = new Deck<>(tempTop);

        //QuestDeck
        tempDeck.clear();
        ArrayList<Quest> tempQuest = new ArrayList<Quest>();
        tempQuest.add(new Quest("FIRE", 1, 3, Symbol.FIRE, Symbol.NONE, RegionName.WETLANDS));

        tempQuest.add(new Quest("WATER", 1, 3, Symbol.WATER, Symbol.NONE , RegionName.FIELDS));

        tempQuest.add(new Quest("BAT", 1, 3, Symbol.BAT,Symbol.NONE, RegionName.HIGHLANDS));

        tempQuest.add(new Quest("BROOM", 1, 3, Symbol.BROOM, Symbol.NONE, RegionName.FIELDS));

        tempQuest.add(new Quest("NET", 1, 3, Symbol.NET, Symbol.NONE, RegionName.HILLS));

        tempQuest.add(new Quest("HELMET", 1, 3, Symbol.HELMET, Symbol.NONE, RegionName.TUNDRA));

        tempQuest.add(new Quest("SWORD", 1, 3, Symbol.SWORD, Symbol.NONE, RegionName.HILLS));

        tempQuest.add(new Quest("TOOTH", 1, 3, Symbol.TOOTH, Symbol.NONE, RegionName.TUNDRA));

        tempQuest.add(new Quest("WAND", 1, 3, Symbol.WAND, Symbol.NONE, RegionName.WETLANDS));

        questDeck = new Deck<>(tempQuest);
        questDeck.shuffle(true);
        board = new Board();
        for (Card aQuest : questDeck.draw(2)) {
            board.quests.add((Quest) aQuest);
        }

        //for (int i =0;i<13;i+=2){
		for (Road aRoad : board.roads()) {
            CreatureCard roadCreature = (CreatureCard) creatureDeck.drawOne();
            aRoad.creature= roadCreature;
            aRoad.gem = roadCreature.gem;
        }

        players.add(new Player("P1"));
        players.add(new Player("P2"));
        for (Player someone: players) {
            ((Region)board.regions().get(1)).players.add(someone);
            someone.drawCards(5);
            someone.quests.add(questDeck.drawOne());
        }
    }
    
    public void useShuffleToken (Player thePlayer) {
		thePlayer.deck.shuffle(true);
	}
	
	
/*
	public List<T extends Card> getCardChoices (int number, deckTypes theDeck) {
		for (int i=0; i < number; i++) {
			switch (theDeck) {
				case BAZAAR:
					selectCards.add(bazaarDeck.remove(0);
					break;
				case QUEST:
					selectCards.add(questDeck.remove(0);
					break;
				case ARTIFACT:
					selectCards.add(artifactDeck.remove(0);
					break;
			}
		}
		selectType = theDeck;
	}
	
	public void selectCardChoice(Card selection) {
		for (Card aDiscard : selectCards) {
			if (aDiscard == selection) break;
			switch (selectType) {
				case BAZAAR:
					bazaarDeck.add(aDiscard);
					break;
				case QUEST:
					questDeck.add(aDiscard);
					break;
				case ARTIFACT:
					artifactDeck.add(aDiscard);
					break;
			}
		}
		selectCards.clear();
	}
	* */
//TODO 2 double requirements
    public Boolean canCompleteQuest(Quest aQuest, List<Card> storedCards) {
        Set<Card> used = new HashSet<>(5);
        int doubleCount = aQuest.getDoubleRequirement() == Symbol.NONE ? 2 : 0;
        int tripleCount = aQuest.getTripleRequirement() == Symbol.NONE ? 3 : 0;
        List<Card> sortedCards = new ArrayList<>(storedCards);
        Collections.sort(sortedCards, new SortBySymbolCount());

        for (Card aStoredCard : sortedCards) {
            if (!(aStoredCard instanceof CreatureCard) || used.contains(aStoredCard)) {
                continue;
            }
            CreatureCard aStoredCreature = ((CreatureCard) aStoredCard);
            if (doubleCount <= 2 && aQuest.getDoubleRequirement() == aStoredCreature.values.get(0)) {
                used.add(aStoredCard);
                doubleCount++;
                if (aStoredCreature.values.size() > 1 &&
                        aQuest.getDoubleRequirement() == aStoredCreature.values.get(1)) {
                    doubleCount++;
                }
                continue;
            }

            if (tripleCount <= 3 && aQuest.getTripleRequirement() == aStoredCreature.values.get(0)) {
                used.add(aStoredCard);
                tripleCount++;
                if (aStoredCreature.values.size() > 1 &&
                        aQuest.getTripleRequirement() == aStoredCreature.values.get(1)) {
                    tripleCount++;
                }
                continue;
            }
        }

        return doubleCount >= 2 && tripleCount >= 3;
    }

    public List<Card> completeQuest(Quest aQuest, List<Card> availableCards) {
        List<Card> results = new ArrayList<>();
        int doubleCount = aQuest.getDoubleRequirement() == Symbol.NONE ? 2 : 0;
        int tripleCount = aQuest.getTripleRequirement() == Symbol.NONE ? 3 : 0;
        List<Card> sortedCards = new ArrayList<>(availableCards);
        Collections.sort(sortedCards, new SortBySymbolCount());
        for (Card aStoredCard : sortedCards) {
            if (!(aStoredCard instanceof CreatureCard) || results.contains(aStoredCard)) {
                continue;
            }
            CreatureCard aStoredCreature = ((CreatureCard) aStoredCard);
            if (doubleCount < 2 && aQuest.getDoubleRequirement() == aStoredCreature.values.get(0)) {
                results.add(aStoredCard);
                doubleCount++;
                if (aStoredCreature.values.size() > 1 &&
                        aQuest.getDoubleRequirement() == aStoredCreature.values.get(1)) {
                    doubleCount++;
                }
                continue;
            }

            if (tripleCount < 3 && aQuest.getTripleRequirement() == aStoredCreature.values.get(0)) {
                results.add(aStoredCard);
                tripleCount++;
                if (aStoredCreature.values.size() > 1 &&
                        aQuest.getTripleRequirement() == aStoredCreature.values.get(1)) {
                    tripleCount++;
                }
                continue;
            }
        }
        if (results.size()>0) {
            board.quests.remove(aQuest);
        }
        return results;
    }

    public Boolean subdue(Road aRoad, Player aPlayer) {
//        if (canSubdue(aRoad, aPlayer.hand)) {
//            return true;
//        }
        return false;
    }
    /*
    private List<List<Card>> canSubdue(Road aRoad, List<Card> aHand) {

    }
*/
    public List<Set<Card>> canSubdueSingle(CreatureCard aCreature, List<Card> aHand){
        List<Set<Card>> fullList = new ArrayList<>();

        List<Symbol> handSymbols = new ArrayList<>();
        //Check hand for first symbol
        for (Card aCard:aHand) {
            if (aCard instanceof CreatureCard && ((CreatureCard)aCard).values.get(0) != Symbol.NONE) {
                CreatureCard handCreature = (CreatureCard) aCard;
                if (aCreature.subduedBy == handCreature.values.get(0)) {
                    //symbol match
                    fullList.add(Collections.singleton(aCard));
                    continue;
                }
                if (handCreature.values.size() > 1 && handCreature.values.get(0) == handCreature.values.get(1)) {
                    //Double symbol as wildcard
                    fullList.add(Collections.singleton(aCard));
                    continue;
                }
                if (handSymbols.contains(handCreature.values.get(0))) {
                    //add these cards later
                }
                else {
                    handSymbols.add(handCreature.values.get(0));
                }
            }
        }
        List<Card> matches = new ArrayList<>();
        Combinations combos;
        Set<Card> comboCards;
        for (Symbol match : handSymbols) {
            for (Card aCard : aHand) {
                if (aCard instanceof CreatureCard &&
                        ((CreatureCard)aCard).values.get(0) == match) {
                    matches.add(aCard);
                }
            }
            if (matches.size()>1) {
                combos = new Combinations(matches.size(), 2);
                for (int[] pairing : combos) {
                    comboCards = new HashSet<>(2);
                    Collections.addAll(comboCards, matches.get(pairing[0]), matches.get(pairing[1]));
                    fullList.add(comboCards);
                }
            }
            matches.clear();
        }
        return fullList;
    }

    public List<Set<Card>> canSubdueDouble(CreatureCard toSubdue, List<Card> aHand) {
        List<Set<Card>> fullList = new ArrayList<>();
        List<List<Card>> singleWildSets = new ArrayList<>();
        List<List<Card>> singleNonMatch = new ArrayList<>();

        for (Card aCard:aHand) {
            if (aCard instanceof CreatureCard && ((CreatureCard)aCard).values.get(0) != Symbol.NONE) {
                CreatureCard handCreature = (CreatureCard) aCard;
                if (handCreature.values.size() > 1 && handCreature.values.get(0) == handCreature.values.get(1)) {
                //double symbol
                    if (toSubdue.subduedBy == handCreature.values.get(0)) {
                        fullList.add(Collections.singleton(aCard));
                    }
                    else {
                        singleWildSets.add(Collections.singletonList(aCard));
                    }
                }
                else if (toSubdue.subduedBy == handCreature.values.get(0)) {
                    //single symbol match
                    singleWildSets.add(Collections.singletonList(aCard));
                }
                else {
                    //single miss
                    //is the symbol already in the list
                    for (List<Card> singleSet : singleNonMatch) {
                        if (((CreatureCard)singleSet.iterator().next()).values.get(0) == handCreature.values.get(0)) {
                            singleSet.add(aCard);
                            continue;
                        }
                    }
                    List<Card> newSingle = new ArrayList<>();
                    newSingle.add(aCard);
                    singleNonMatch.add(newSingle);
                }
            }
        }

        Combinations combos;
        List<Card> comboCards;
        for (List<Card> singles : singleNonMatch) {
            if (singles.size()>=2) {
                combos = new Combinations(singles.size(),2);
                for (int[] pairing : combos) {
                    comboCards = new ArrayList<>(2);
                    Collections.addAll(comboCards, singles.get(pairing[0]), singles.get(pairing[1]));
                    singleWildSets.add(comboCards);
                }
            }
        }
        Set<Card> comboCardSet;
        for (List<Card> singleWilds : singleWildSets){
            if (singleWilds.size()>=2) {
                combos = new Combinations(singleWilds.size(),2);
                for (int[] pairing : combos) {
                    comboCardSet = new HashSet<>(2);
                    Collections.addAll(comboCardSet, singleWilds.get(pairing[0]), singleWilds.get(pairing[1]));
                    fullList.add(comboCardSet);
                }
            }
        }

        return fullList;
    }

	public Player nextPlayer(Player currentPlayer){
		ListIterator<Player> playerIter = players.listIterator();
		while (playerIter.hasNext()){
			if (playerIter.next()==currentPlayer) {
				if (playerIter.hasNext()) {
					return playerIter.next();
				}
			}
		}
		return players.get(0);
	}
}

enum deckTypes {
	QUEST, BAZAAR, ARTIFACT, CREATURE
}

class SortBySymbolCount implements Comparator<Card>
{
    // Used for sorting in ascending order of symbol count
    public int compare(Card a, Card b)
    {
        int aCreature = (a instanceof CreatureCard) && ((CreatureCard)a).values.get(0) != Symbol.NONE ?
                ((CreatureCard)a).values.size() :
                0;
        int bCreature = (b instanceof CreatureCard) && ((CreatureCard)b).values.get(0) != Symbol.NONE ?
                ((CreatureCard)b).values.size() :
                0;

        return bCreature-aCreature;
    }
}
