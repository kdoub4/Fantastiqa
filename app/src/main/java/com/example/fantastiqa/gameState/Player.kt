package com.example.fantastiqa.gameState

import com.example.fantastiqa.pieces.CreatureCards

/**
 * Immutable Player representation for Redux.
 * All state changes return a new Player instance.
 */
data class Player(
    @JvmField val name: String,
    @JvmField val gems: Int = 3,
    @JvmField val vps: Int = 0,
    @JvmField val trophies: Int = 0,
    @JvmField val flyingCarpets: Int = 3,
    @JvmField val tents: Int = 3,
    @JvmField val hand: List<Card> = emptyList(),
    @JvmField val quests: List<Card> = emptyList(),
    @JvmField val storage: List<Card> = emptyList(),
    @JvmField val deck: Deck<Card> = Deck()
) {
    fun getGems(): Int = gems
    fun getVps(): Int = vps
    fun getTrophies(): Int = trophies
    fun getFlyingCarpets(): Int = flyingCarpets
    fun getTents(): Int = tents

    /**
     * Secondary constructor for initial setup
     */
    constructor(thename: String) : this(
        name = thename,
        deck = createInitialDeck()
    )

    override fun toString(): String = name

    /**
     * Returns a new Player with cards drawn from their deck.
     */
    fun drawCards(amount: Int): Player {
        val (drawn, nextDeck) = deck.draw(amount)
        return copy(
            hand = hand + drawn,
            deck = nextDeck
        )
    }

    /**
     * Returns a new Player with updated gem count.
     */
    fun withGems(newGems: Int): Player = copy(gems = newGems)

    /**
     * Returns a new Player with updated trophies.
     */
    fun withTrophies(newTrophies: Int): Player = copy(trophies = newTrophies)

    /**
     * Returns a new Player after using a flying carpet.
     */
    fun useFlyingCarpet(): Player {
        if (flyingCarpets <= 0) return this
        return copy(flyingCarpets = flyingCarpets - 1)
    }

    /**
     * Returns a new Player after gaining a card (added to discard pile).
     */
    fun gainCard(card: Card): Player {
        return copy(deck = deck.discard(card))
    }

    /**
     * Returns a new Player after discarding cards.
     * Checks both hand and storage.
     */
    fun discardFromHand(cards: List<Card>): Player {
        val cardsToDiscard = cards.filter { hand.contains(it) || storage.contains(it) }
        
        if (cardsToDiscard.isEmpty()) return this
        
        var nextDeck = deck
        cardsToDiscard.forEach { nextDeck = nextDeck.discard(it) }
        
        val handIds = hand.map { it.id }.toSet()
        val storageIds = storage.map { it.id }.toSet()
        val discardIds = cardsToDiscard.map { it.id }.toSet()

        return copy(
            hand = hand.filter { it.id !in discardIds },
            storage = storage.filter { it.id !in discardIds },
            deck = nextDeck
        )
    }

    /**
     * Returns a new Player after removing cards from hand permanently.
     */
    fun removeFromHand(cards: List<Card>): Player {
        val fromHand = cards.filter { hand.contains(it) }
        if (fromHand.isEmpty()) return this
        return copy(
            hand = hand - fromHand.toSet()
        )
    }

    /**
     * Helper to check for specific abilities in hand.
     */
    fun handContains(ability: Ability): List<Card> {
        return hand.filter { it is CreatureCard && it.ability == ability }
    }

    fun subdue(road: Road?): Player {
        if (road == null) return this
        for (card in hand) {
            if (card is CreatureCard && card.subduedBy == road.creature?.values?.get(0)) {

                return discardFromHand(listOf(card))
            }
        }
        return this
    }

    fun storeCards(cards: List<Card>): Player {
        val validCards = cards.filter { hand.contains(it) }
        if (validCards.isEmpty()) return this
        return copy(
            hand = hand - validCards.toSet(),
            storage = storage + validCards
        )
    }

    fun drawQuest(quest: Quest): Player {
        return copy(quests = quests + quest)
    }

    companion object {
        private fun createInitialDeck(): Deck<Card> {
            val deckSetup = mutableListOf<Card>()
            
            // Standard starter cards from enums
            CreatureCards.values().forEach { aCard ->
                if (aCard.value2 == Symbol.NONE) {
                    deckSetup.add(CreatureCard(
                        aCard.name,
                        Symbol.NONE,
                        false,
                        Ability.NONE,
                        aCard.value1
                    ))
                }
            }
            
            // Special starter cards
            deckSetup.add(CreatureCard("Peaceful Dragon", Symbol.NONE, false, Ability.DRAGON, Symbol.NONE))
            deckSetup.add(CreatureCard("Dog", Symbol.NONE, false, Ability.GEM, Symbol.NONE))
            
            return Deck(deckSetup).shuffle(true)
        }
    }
}
