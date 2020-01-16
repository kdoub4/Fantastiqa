package com.example.fantastiqa.GameState;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    public Board board;
    public List<CreatureCard> creatureDeck = new ArrayList<>();
    public List<Aritifact> artifactDeck = new ArrayList<>();
    public List<CreatureCard> bazaarDeck = new ArrayList<>();
    public List<Quest> questDeck = new ArrayList<>();
    public List<Player> players = new ArrayList<>();

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

        for (int i =0;i<13;i+=2){
            CreatureCard roadCreature = creatureDeck.remove(0);
            Road aRoad = (Road)board.locations.get(i);
            aRoad.creature= roadCreature;
            aRoad.gem = roadCreature.gem;
        }

        players.add(new Player("P1"));
        players.add(new Player("P2"));
        for (Player someone: players) {
            ((Region)board.locations.get(1)).players.add(someone);
            someone.drawCards(5);
            someone.quests.add(questDeck.remove(random.nextInt(questDeck.size())));
        }

        Log.v("DeckSize", Integer.toString(creatureDeck.size()));
        for (CreatureCard aCard: creatureDeck)
            Log.v(aCard.name, aCard.values.toString());
    }
}
