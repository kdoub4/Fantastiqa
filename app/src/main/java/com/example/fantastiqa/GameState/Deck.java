package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
	ArrayList<Card> deck= new ArrayList<>();
	ArrayList<Card> discard = new ArrayList<>();
	
	public Deck(ArrayList<Card> initDeck) {
		deck.addAll(initDeck);
	}

	public String sizeToString() {
		return "Deck" + (deck.size()) + " Discard" + discard.size();
	}

	public Card drawOne() {
		return draw(1).get(0);
	}

	public List<Card> draw(int amount) {
		List<Card> result = new ArrayList<>(amount);
		if (amount == 0) return result;
		if (deck.size()<=0) {
			shuffle(true);
		}
		result.add(deck.remove(0));
		result.addAll(draw(amount-1));
		return result;
	}

	public void discard(Card aCard) {
		discard.add(aCard);
	}
	
	public void shuffle(Boolean includeDiscard) {
		if (includeDiscard) {
			deck.addAll(discard);
			discard.clear();
		}
		Collections.shuffle(deck);
	}
}
