package com.example.fantastiqa.GameState;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;

public class Game {
    public Board board;
    public List<CreatureCard> creatureDeck = new ArrayList<>();
    public List<Aritifact> artifactDeck = new ArrayList<>();
    public List<CreatureCard> bazaarDeck = new ArrayList<>();
    public List<Quest> questDeck = new ArrayList<>();
    public List<Player> players = new ArrayList<>();
	public List<Card> selectCards = new ArrayList<>(5);
//	public deckType selectType;
	
    public Game() {
        Random random = new Random();
        //Create creatureDeck
        //TopDeck
        ArrayList<CreatureCard> tempDeck = new ArrayList<>();
        for (CreatureCards aCard: CreatureCards.values()) {
            if (aCard.getValue2()== Symbol.NONE) {
                for (int j = 0; j < 2; j++) {
                    tempDeck.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
                }
            }
        }
        while (!tempDeck.isEmpty()) {
            creatureDeck.add(tempDeck.remove(random.nextInt(tempDeck.size())));
        }

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
                    bazaarDeck.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), false, aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
                }
            }
        }
        Collections.shuffle(bazaarDeck);
        while (!tempDeck.isEmpty()) {
            creatureDeck.add(tempDeck.remove(random.nextInt(tempDeck.size())));
        }

        //QuestDeck
        questDeck.add(new Quest("A", 1,3, new ArrayList<Symbol>(){{
            add(Symbol.FIRE);
            add(Symbol.FIRE);
        }
        }, RegionName.WETLANDS));

        questDeck.add(new Quest("B", 1,3, new ArrayList<Symbol>(){{
            add(Symbol.WATER);
            add(Symbol.WATER);
        }
        }, RegionName.FIELDS));

        questDeck.add(new Quest("C", 1,3, new ArrayList<Symbol>(){{
            add(Symbol.BAT);
            add(Symbol.BAT);
        }
        }, RegionName.HIGHLANDS));

        questDeck.add(new Quest("D", 1,3, new ArrayList<Symbol>(){{
            add(Symbol.BROOM);
            add(Symbol.BROOM);
        }
        }, RegionName.FIELDS));

        questDeck.add(new Quest("E", 1,3, new ArrayList<Symbol>(){{
            add(Symbol.NET);
            add(Symbol.NET);
        }
        }, RegionName.HILLS));

        questDeck.add(new Quest("F", 1,3, new ArrayList<Symbol>(){{
            add(Symbol.HELMET);
            add(Symbol.HELMET);
        }
        }, RegionName.TUNDRA));

        questDeck.add(new Quest("G", 1,3, new ArrayList<Symbol>(){{
            add(Symbol.SWORD);
            add(Symbol.SWORD);
        }
        }, RegionName.HILLS));

        questDeck.add(new Quest("H", 1,3, new ArrayList<Symbol>(){{
            add(Symbol.TOOTH);
            add(Symbol.TOOTH);
        }
        }, RegionName.TUNDRA));

        questDeck.add(new Quest("I", 1,3, new ArrayList<Symbol>(){{
            add(Symbol.WAND);
            add(Symbol.WAND);
        }
        }, RegionName.WETLANDS));

        board = new Board();
        for (int i=0;i<2;i++) {
            board.quests.add(questDeck.remove(random.nextInt(questDeck.size())));
        }

        //for (int i =0;i<13;i+=2){
		for (Road aRoad : board.roads()) {
            CreatureCard roadCreature = creatureDeck.remove(0);
            aRoad.creature= roadCreature;
            aRoad.gem = roadCreature.gem;
        }

        players.add(new Player("P1"));
        players.add(new Player("P2"));
        for (Player someone: players) {
            ((Region)board.regions().get(1)).players.add(someone);
            someone.drawCards(5);
            someone.quests.add(questDeck.remove(random.nextInt(questDeck.size())));
        }

        Log.v("DeckSize", Integer.toString(creatureDeck.size()));
        for (CreatureCard aCard: bazaarDeck)
            Log.v(aCard.name, aCard.values.toString());
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
