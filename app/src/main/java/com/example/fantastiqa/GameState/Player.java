package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public List<Card> quests = new ArrayList<>();
    public Deck deck;
    public List<Card> publicQuest = new ArrayList<>();
    public List<Card> hand = new ArrayList<>();
    private int trophies = 0;
    private int flyingCarpets = 3;
    private int tents = 3;
    public String name;
    private int gems = 3;
    private playerListener mListener = null;

    public Player(String thename) {
        name= thename;
		ArrayList<Card> deckSetup = new ArrayList<>();
        for (CreatureCards aCard: CreatureCards.values()
             ) {
            if (aCard.getValue2()==Symbol.NONE) {
                deckSetup.add(new CreatureCard(aCard.name(), Symbol.NONE, false, Ability.NONE, aCard.getValue1()));
            }
        }
        deckSetup.add(new CreatureCard("Peaceful Dragon", Symbol.NONE,false, Ability.DRAGON, Symbol.NONE));
        deckSetup.add(new CreatureCard("Dog", Symbol.NONE,false, Ability.GEM, Symbol.NONE));
		deck = new Deck(deckSetup);
    }
    
    //TODO event framework
    public interface playerListener {
		public void onGemChange(int newGems, int oldGems);
	}
	
	public void setPlayerListener(playerListener aListener) {
		mListener = aListener;
	}

    @Override
    public String toString() {
        return name;
    }

    public void drawCards(int amount) {
		hand.addAll(deck.draw(amount));
    }
	
	public int getGems() {
		return gems;
	}
	
	public void setGems(int gems) {
		if (mListener != null ) 
			mListener.onGemChange(gems, this.gems);
		this.gems = gems;
	}
	
	public void changeGems(int gems) {
		setGems(this.gems + gems);
	}
	
    public int getFlyingCarpets() {
        return flyingCarpets;
    }

    public void setFlyingCarpets(int flyingCarpets) {
        this.flyingCarpets = flyingCarpets;
    }
    
    public void useFlyingCarpet() {
		this.flyingCarpets--;
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

    public void subdue(Symbol toSubdue) {
        for (Card aCard : hand
        ) {
            if (aCard instanceof CreatureCard) {
                CreatureCard handCreature = (CreatureCard) aCard;
                //Toast.makeText(MainActivity.this, handCreature.values.get(0).toString(), Toast.LENGTH_SHORT).show();
                if (toSubdue == handCreature.values.get(0)) {
                    hand.remove(handCreature);
                    break;
                }
            }
            //TODO other methods of subdue
        }
    }
    
    public List<Card> handContains(Ability ability) {
		List<Card> results = new ArrayList<>();
		for (Card aHandCard : hand) {
			if (aHandCard instanceof CreatureCard && ((CreatureCard)aHandCard).ability == ability) {
				results.add(aHandCard);
			}
		}
		return results;
	} 
}
