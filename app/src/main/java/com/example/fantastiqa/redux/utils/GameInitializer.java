package com.example.fantastiqa.redux.utils;

import com.example.fantastiqa.redux.GameState;
import com.example.fantastiqa.GameState.Board;
import com.example.fantastiqa.GameState.Deck;
import com.example.fantastiqa.GameState.Card;
import com.example.fantastiqa.GameState.Quest;
import com.example.fantastiqa.GameState.CreatureCard;
import com.example.fantastiqa.GameState.Artifact;
import com.example.fantastiqa.GameState.Player;
import com.example.fantastiqa.GameState.RegionName;
import com.example.fantastiqa.GameState.TowerName;
import com.example.fantastiqa.GameState.Region;
import com.example.fantastiqa.GameState.Road;
import com.example.fantastiqa.GameState.Symbol;
import com.example.fantastiqa.GameState.CreatureCards;
import com.example.fantastiqa.GameState.Ability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for initializing a new game state.
 * This is used to set up the initial GameState that will be used by the Store.
 */
public class GameInitializer {
    
    /**
     * Create a new game with initialized state
     */
    public static GameState initializeNewGame() {
        // Create board with all regions and roads
        Board board = new Board();
        
        // Create and shuffle decks
        Deck<Card> creatureDeck = initializeCreatureDeck();
        Deck<CreatureCard> bazaarDeck = initializeBazaarDeck();
        Deck<Quest> questDeck = initializeQuestDeck();
        Deck<Artifact> artifactDeck = new Deck<>(new ArrayList<>());  // TODO: Populate with artifacts
        
        // Set up quests on board
        List<Card> initialQuests = questDeck.draw(2);
        for (Card questCard : initialQuests) {
            board.quests.add((Quest) questCard);
        }
        
        // Place creatures on roads
        for (Road road : board.roads()) {
            CreatureCard roadCreature = (CreatureCard) creatureDeck.drawOne();
            road.creature = roadCreature;
            road.gem = roadCreature.gem;
        }
        
        // Create players
        List<Player> players = new ArrayList<>();
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        players.add(player1);
        players.add(player2);
        
        // Initialize players
        Region startingRegion = (Region) board.regions().get(1);
        startingRegion.players.add(player1);
        startingRegion.players.add(player2);
        
        player1.drawCards(5);
        player2.drawCards(5);
        
        player1.quests.add(questDeck.drawOne());
        player2.quests.add(questDeck.drawOne());
        
        // Build and return initial game state
        return new GameState.Builder()
                .board(board)
                .vpGoal(4)
                .creatureDeck(creatureDeck)
                .artifactDeck(artifactDeck)
                .bazaarDeck(bazaarDeck)
                .questDeck(questDeck)
                .players(players)
                .currentPlayerIndex(0)
                .selectedCards(new ArrayList<>())
                .gamePhase(GameState.GamePhase.PLAYER_TURN)
                .isGameOver(false)
                .build();
    }
    
    /**
     * Initialize the creature deck
     */
    private static Deck<Card> initializeCreatureDeck() {
        ArrayList<Card> tempDeck = new ArrayList<>();
        ArrayList<Card> tempTop = new ArrayList<>();
        
        // Single symbol creatures (appear twice in top deck)
        for (CreatureCards aCard : CreatureCards.values()) {
            if (aCard.getValue2() == Symbol.NONE) {
                for (int j = 0; j < 2; j++) {
                    tempDeck.add(new CreatureCard(
                            aCard.name(),
                            aCard.getSubduedBy(),
                            aCard.isGem(),
                            aCard.isGem() ? Ability.NONE : aCard.getAbility(),
                            aCard.getValue1()
                    ));
                }
            }
        }
        
        tempTop.addAll(tempDeck);
        Collections.shuffle(tempTop);
        tempDeck.clear();
        
        // Double symbol creatures (bottom deck)
        for (CreatureCards aCard : CreatureCards.values()) {
            if (aCard.getValue2() != Symbol.NONE) {
                tempDeck.add(new CreatureCard(
                        aCard.name(),
                        aCard.getSubduedBy(),
                        aCard.isGem(),
                        aCard.isGem() ? Ability.NONE : aCard.getAbility(),
                        aCard.getValue1(),
                        aCard.getValue2()
                ));
                tempDeck.add(new CreatureCard(
                        aCard.name(),
                        aCard.getSubduedBy(),
                        aCard.isGem(),
                        aCard.isGem() ? Ability.NONE : aCard.getAbility(),
                        aCard.getValue1(),
                        aCard.getValue2()
                ));
            }
        }
        
        Collections.shuffle(tempDeck);
        tempTop.addAll(tempDeck);
        
        return new Deck<>(tempTop);
    }
    
    /**
     * Initialize the bazaar deck
     */
    private static Deck<CreatureCard> initializeBazaarDeck() {
        ArrayList<CreatureCard> tempBazaar = new ArrayList<>();
        
        for (CreatureCards aCard : CreatureCards.values()) {
            if (aCard.getValue2() != Symbol.NONE) {
                for (int i = 0; i < 3; i++) {
                    tempBazaar.add(new CreatureCard(
                            aCard.name(),
                            aCard.getSubduedBy(),
                            false,
                            aCard.getAbility(),
                            aCard.getValue1(),
                            aCard.getValue2()
                    ));
                }
            }
        }
        
        Deck<CreatureCard> bazaarDeck = new Deck<>(tempBazaar);
        bazaarDeck.shuffle(true);
        return bazaarDeck;
    }
    
    /**
     * Initialize the quest deck
     */
    private static Deck<Quest> initializeQuestDeck() {
        ArrayList<Quest> tempQuest = new ArrayList<>();
        
        tempQuest.add(new Quest("FIRE", 1, 3, Symbol.FIRE, Symbol.NONE, RegionName.WETLANDS));
        tempQuest.add(new Quest("WATER", 1, 3, Symbol.WATER, Symbol.NONE, RegionName.FIELDS));
        tempQuest.add(new Quest("BAT", 1, 3, Symbol.BAT, Symbol.NONE, RegionName.HIGHLANDS));
        tempQuest.add(new Quest("BROOM", 1, 3, Symbol.BROOM, Symbol.NONE, RegionName.FIELDS));
        tempQuest.add(new Quest("NET", 1, 3, Symbol.NET, Symbol.NONE, RegionName.HILLS));
        tempQuest.add(new Quest("HELMET", 1, 3, Symbol.HELMET, Symbol.NONE, RegionName.TUNDRA));
        tempQuest.add(new Quest("SWORD", 1, 3, Symbol.SWORD, Symbol.NONE, RegionName.HILLS));
        tempQuest.add(new Quest("TOOTH", 1, 3, Symbol.TOOTH, Symbol.NONE, RegionName.TUNDRA));
        tempQuest.add(new Quest("WAND", 1, 3, Symbol.WAND, Symbol.NONE, RegionName.WETLANDS));
        
        Deck<Quest> questDeck = new Deck<>(tempQuest);
        questDeck.shuffle(true);
        return questDeck;
    }
}
