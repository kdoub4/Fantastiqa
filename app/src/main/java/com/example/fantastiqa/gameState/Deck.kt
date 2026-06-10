package com.example.fantastiqa.gameState

import java.util.*

/**
 * Immutable Deck implementation.
 * Drawing a card returns a new Deck instance without the drawn card.
 */
data class Deck<T : Card> @JvmOverloads constructor(
    @JvmField val cards: List<T> = emptyList(),
    @JvmField val discardPile: List<T> = emptyList()
) {
    @JvmName("getSize")
    fun size(): Int = cards.size
    
    @JvmName("getDiscardSize")
    fun discardSize(): Int = discardPile.size

    fun sizeToString(): String = "Deck${cards.size} Discard${discardPile.size}"

    fun drawOne(): Pair<T?, Deck<T>> {
        val result = draw(1)
        return Pair(result.first.getOrNull(0), result.second)
    }

    fun draw(amount: Int): Pair<List<T>, Deck<T>> {
        if (amount <= 0) return Pair(emptyList(), this)

        if (cards.isEmpty() && discardPile.isEmpty()) return Pair(emptyList(), this)

        val currentDeck = cards.ifEmpty {
            cards + discardPile.shuffled()
        }

        val nextDiscard = if (cards.isEmpty()) emptyList() else discardPile

        val drawn = currentDeck.take(amount)
        val remaining = currentDeck.drop(amount)

        val newDeck = Deck(remaining, nextDiscard)
        
        if (drawn.size < amount && newDeck.discardPile.isNotEmpty()) {
            val (additionalDrawn, finalDeck) = newDeck.draw(amount - drawn.size)
            return Pair(drawn + additionalDrawn, finalDeck)
        }

        return Pair(drawn, newDeck)
    }

    fun discard(card: T): Deck<T> {
        return copy(discardPile = discardPile + card)
    }

    @JvmOverloads
    fun shuffle(includeDiscard: Boolean = false): Deck<T> {
        val newCards = if (includeDiscard) (cards + discardPile).shuffled() else cards.shuffled()
        val newDiscard = if (includeDiscard) emptyList() else discardPile
        return Deck(newCards, newDiscard)
    }

    fun remove(card: T): Deck<T> {
        return copy(
            cards = cards - card,
            discardPile = discardPile - card
        )
    }

    fun putOnBottom(newCards: List<T>): Deck<T> {
        return copy(cards = cards + newCards)
    }
}
