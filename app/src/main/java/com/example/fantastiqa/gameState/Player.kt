package com.example.fantastiqa.gameState

import com.example.fantastiqa.pieces.CreatureCards
import com.example.fantastiqa.pieces.SymbolCard

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
    @JvmField val quests: List<PlayerQuest> = emptyList(),
    @JvmField val storage: List<Card> = emptyList(),
    @JvmField val deck: Deck<Card> = Deck(),
    @JvmField val isComputer: Boolean = false
) {
    fun getGems(): Int = gems
    fun getVps(): Int = vps
    fun getTrophies(): Int = trophies
    fun getFlyingCarpets(): Int = flyingCarpets
    fun getTents(): Int = tents

    /**
     * Total cards owned by the player.
     */
    fun totalCardCount(): Int = hand.size + storage.size + deck.size() + deck.discardSize()

    /**
     * Secondary constructor for initial setup
     */
    constructor(thename: String, isComputer: Boolean = false) : this(
        name = thename,
        deck = createInitialDeck(),
        isComputer = isComputer
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
     * Returns a new Player with a card added to their hand.
     */
    fun gainCard(card: Card): Player {
        return copy(hand = hand + card)
    }

    /**
     * Returns a new Player with cards removed from their hand.
     */
    fun removeFromHand(cards: List<Card>): Player {
        return copy(hand = hand - cards.toSet())
    }

    /**
     * Returns a new Player with cards moved from hand to discard pile.
     */
    fun discardFromHand(cards: List<Card>): Player {
        val nextHand = hand - cards.toSet()
        var nextDeck = deck
        cards.forEach { nextDeck = nextDeck.discard(it) }
        return copy(
            hand = nextHand,
            deck = nextDeck
        )
    }

    /**
     * Returns a new Player with one less flying carpet.
     */
    fun useFlyingCarpet(): Player {
        return copy(flyingCarpets = (flyingCarpets - 1).coerceAtLeast(0))
    }

    /**
     * Returns a new Player with updated gems.
     */
    fun withGems(newGems: Int): Player {
        return copy(gems = newGems)
    }

    /**
     * Returns a new Player with updated trophies.
     */
    fun withTrophies(newTrophies: Int): Player {
        return copy(trophies = newTrophies)
    }

    /**
     * Helper to check if player has enough symbols of a certain type
     */
    fun countSymbols(symbol: Symbol): Int {
        return hand.filterIsInstance<CreatureCard>()
            .flatMap { it.values }
            .count { it == symbol }
    }

    fun handContains(ability: Ability): List<Card> {
        return hand.filter { it is CreatureCard && it.ability == ability }
    }

    fun storeForBoardQuest(cards: List<Card>): Player {
        val validCards = cards.filter { hand.contains(it) }
        if (validCards.isEmpty()) return this
        return copy(
            hand = hand - validCards.toSet(),
            storage = storage + validCards
        )
    }

    fun drawQuest(quest: Quest): Player {
        val playerQuest = if (quest is PlayerQuest) quest else PlayerQuest(
            _id = quest.id,
            _name = quest.name,
            title = quest.title,
            vps = quest.vps,
            gems = quest.gems,
            doubleReq = quest.doubleReq,
            tripleReq = quest.tripleReq,
            land = quest.land
        )
        return copy(quests = quests + playerQuest)
    }

    companion object {
        private fun createInitialDeck(): Deck<Card> {
            val deckSetup = mutableListOf<Card>()
            
            // Standard starter cards from enums
            CreatureCards.entries.forEach { aCard ->
                if (aCard.name == "Knight") {
                    repeat(2) {
                        deckSetup.add(CreatureCard(java.util.UUID.randomUUID().toString(),"Knight",  false, listOf(Symbol.SWORD), Symbol.WAND, Ability.NONE))
                    }
                }
            }
            deckSetup.add(CreatureCard(java.util.UUID.randomUUID().toString(),"Peaceful Dragon",  false, listOf(Symbol.NONE), Symbol.NONE, Ability.NONE))
            deckSetup.add(CreatureCard(java.util.UUID.randomUUID().toString(),"Dog",  false, listOf(Symbol.NONE), Symbol.NONE, Ability.NONE))
            deckSetup.add(Artifact(java.util.UUID.randomUUID().toString(), "LookingGlass", 0, Ability.LOOKING_GLASS))

            return Deck<Card>(deckSetup).shuffle(true)
        }
    }
}
