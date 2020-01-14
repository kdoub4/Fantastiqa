package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    List<Quest> quests = new ArrayList<Quest>();
    public List<Card> deck = new ArrayList<Card>();
    List<Card> discard = new ArrayList<Card>();
    List<Card> publicQuest = new ArrayList<>();
    public List<Card> hand = new ArrayList<>();
    private int trophies = 0;
    private int flyingCarpets = 3;
    private int tents = 3;
    public String name;

    public Player(String thename) {
        name= thename;
        for (CreatureCards aCard: CreatureCards.values()
             ) {
            if (aCard.getValue2()==Symbol.NONE) {
                deck.add(new CreatureCard(aCard.name(), Symbol.NONE, false, Ability.NONE, aCard.getValue1()));
            }
        }
        deck.add(new CreatureCard("Peaceful Dragon", Symbol.NONE,false, Ability.DRAGON, Symbol.NONE));
        deck.add(new CreatureCard("Dog", Symbol.NONE,false, Ability.GEM, Symbol.NONE));

        shuffleDeck();
    }

    @Override
    public String toString() {
        return name;
    }

    private void shuffleDeck() {
        deck.addAll(discard);
        Collections.shuffle(deck);
    }

    public void drawCards(int amount) {
        while (amount>0) {
            if (deck.size()==0) {
                shuffleDeck();
                if(deck.size()==0) {
                    return;
                }
            }
            hand.add(deck.remove(0));
            amount--;
        }
    }

    public int getFlyingCarpets() {
        return flyingCarpets;
    }

    public void setFlyingCarpets(int flyingCarpets) {
        this.flyingCarpets = flyingCarpets;
    }

    public int getTents() {
        return tents;
    }

    public void setTents(int tents) {
        this.tents = tents;
    }

    public int getTrophies() {
        return trophies;
    }

    public void setTrophies(int trophies) {
        this.trophies = trophies;
    }
}
