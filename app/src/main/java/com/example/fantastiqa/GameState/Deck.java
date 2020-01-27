package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck <Card> {
	ArrayList<Card> deck= new ArrayList<>();
	private ArrayList<Card> discardPile = new ArrayList<>();
	
	public Deck(ArrayList<Card> initDeck) {
		deck.addAll(initDeck);
	}

	public String sizeToString() {
		return "Deck" + (deck.size()) + " Discard" + discardPile.size();
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
		discardPile.add(aCard);
	}

	public void shuffle(Boolean includeDiscard) {
		if (includeDiscard) {
			deck.addAll(discardPile);
			discardPile.clear();
		}
		Collections.shuffle(deck);
	}
}
