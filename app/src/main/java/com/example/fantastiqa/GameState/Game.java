package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Game {
    public Board board;
    public int VPgoal=1;
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