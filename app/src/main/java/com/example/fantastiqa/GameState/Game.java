package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game {
    public Board board;
    public Deck creatureDeck;
    public Deck artifactDeck;
    public Deck bazaarDeck;
    public Deck questDeck;
    public List<Player> players = new ArrayList<>();
	public List<Card> selectCards = new ArrayList<>(5);
//	public deckType selectType;
	
    public Game() {
        Random random = new Random();
        //Create creatureDeck
        //TopDeck
        ArrayList<Card> tempDeck = new ArrayList<>();
        ArrayList<Card> tempTop = new ArrayList<>();
        ArrayList<Card> tempBazaar = new ArrayList<>();

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
        bazaarDeck = new Deck(tempBazaar);
        bazaarDeck.shuffle(true);

        Collections.shuffle(tempDeck);
        tempTop.addAll(tempDeck);
        creatureDeck = new Deck(tempTop);

        //QuestDeck
        tempDeck.clear();
        tempDeck.add(new Quest("FIRE", 1, 3, new ArrayList<Symbol>() {
            {
            add(Symbol.FIRE);
            add(Symbol.FIRE);
        }
        }, RegionName.WETLANDS));

        tempDeck.add(new Quest("WATER", 1, 3, new ArrayList<Symbol>() {
            {
            add(Symbol.WATER);
            add(Symbol.WATER);
        }
        }, RegionName.FIELDS));

        tempDeck.add(new Quest("BAT", 1, 3, new ArrayList<Symbol>() {
            {
            add(Symbol.BAT);
            add(Symbol.BAT);
        }
        }, RegionName.HIGHLANDS));

        tempDeck.add(new Quest("BROOM", 1, 3, new ArrayList<Symbol>() {
            {
            add(Symbol.BROOM);
            add(Symbol.BROOM);
        }
        }, RegionName.FIELDS));

        tempDeck.add(new Quest("NET", 1, 3, new ArrayList<Symbol>() {
            {
            add(Symbol.NET);
            add(Symbol.NET);
        }
        }, RegionName.HILLS));

        tempDeck.add(new Quest("HELMET", 1, 3, new ArrayList<Symbol>() {
            {
            add(Symbol.HELMET);
            add(Symbol.HELMET);
        }
        }, RegionName.TUNDRA));

        tempDeck.add(new Quest("SWORD", 1, 3, new ArrayList<Symbol>() {
            {
            add(Symbol.SWORD);
            add(Symbol.SWORD);
        }
        }, RegionName.HILLS));

        tempDeck.add(new Quest("TOOTH", 1, 3, new ArrayList<Symbol>() {
            {
            add(Symbol.TOOTH);
            add(Symbol.TOOTH);
        }
        }, RegionName.TUNDRA));

        tempDeck.add(new Quest("WAND", 1, 3, new ArrayList<Symbol>() {
            {
            add(Symbol.WAND);
            add(Symbol.WAND);
        }
        }, RegionName.WETLANDS));

        questDeck = new Deck(tempDeck);
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
}

enum deckTypes {
	QUEST, BAZAAR, ARTIFACT, CREATURE
}
