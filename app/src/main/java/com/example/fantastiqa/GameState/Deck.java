package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck <T extends Card> {
	ArrayList<Card> deck= new ArrayList<>();
	private ArrayList<Card> discardPile = new ArrayList<>();
	
	public Deck(ArrayList<T> initDeck) {
		deck.addAll(initDeck);
	}

	public int size() { return deck.size();}
	public int discardSize() { return discardPile.size();}

	public String sizeToString() {
		return "Deck" + (deck.size()) + " Discard" + discardPile.size();
	}

	public T drawOne() {
		return draw(1).get(0);
	}

	public List<T> draw(int amount) {
		List<T> result = new ArrayList<>(amount);
		if (amount == 0) return result;
		if (deck.size()<=0) {
			shuffle(true);
		}
		result.add((T)deck.remove(0));
		result.addAll(draw(amount-1));
		return result;
	}

	public void discard(T aCard) {
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
