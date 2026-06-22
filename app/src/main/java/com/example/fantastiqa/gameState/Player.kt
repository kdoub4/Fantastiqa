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
    @JvmField val isComputer: Boolean = false,
    @JvmField val isMouser: Boolean = false
) {
    fun getGems(): Int = gems
    fun getVps(): Int = vps
    fun getTrophies(): Int = trophies
    fun getFlyingCarpets(): Int = flyingCarpets
    fun getTents(): Int = tents

    /**
     * Total cards owned by the player.
     */
    fun totalCardCount(): Int = if (isMouser) hand.size + storage.size else hand.size + storage.size + deck.size() + deck.discardSize()

    /**
     * Secondary constructor for initial setup
     */
    constructor(thename: String, isComputer: Boolean = false, isMouser: Boolean = false) : this(
        name = thename,
        deck = createInitialDeck(isComputer),
        isComputer = isComputer,
        isMouser = isMouser
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
     * Returns a new Player with a card added to their discard pile.
     */
    fun gainCard(card: Card): Player {
        return if (isMouser) copy(hand = hand + card) else copy(deck = deck.discard(card))
    }

    /**
     * Returns a new Player with cards removed from their hand.
     */
    fun removeFromHand(cards: List<Card>): Player {
        return copy(hand = hand - cards.toSet())
    }

    /**
     * Returns a new Player with cards moved from hand or storage to discard pile.
     */
    fun discard(cards: List<Card>): Player {
        val set = cards.toSet()
        var nextDeck = deck
        cards.forEach { nextDeck = nextDeck.discard(it) }
        return copy(
            hand = hand - set,
            storage = storage - set,
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
        val playerQuest = quest.copy(isPersonal = true)
        return copy(quests = quests + playerQuest)
    }

    companion object {
        private fun createInitialDeck(isComputer: Boolean): Deck<Card> {
            val deckSetup = mutableListOf<Card>()

            // Standard starter cards from enums
            CreatureCards.entries.forEach { aCard ->
                if (aCard.value2 == Symbol.NONE) {
                    deckSetup.add(
                        CreatureCard(
                            java.util.UUID.randomUUID().toString(),
                            playerCardName(aCard),
                            false,
                            if (aCard.value2 == Symbol.NONE) listOf(aCard.value1) else listOf(
                                aCard.value1,
                                aCard.value2
                            ),
                            aCard.subduedBy,
                            Ability.NONE
                        )
                    )
                }
            }
            // Special starter cards
            deckSetup.add(
                CreatureCard(
                    java.util.UUID.randomUUID().toString(),
                    "Peaceful Dragon",
                    false,
                    listOf(Symbol.NONE),
                    Symbol.NONE,
                    Ability.DRAGON,
                )
            )
            deckSetup.add(
                CreatureCard(
                    java.util.UUID.randomUUID().toString(),
                    "Dog",
                    false,
                    listOf(Symbol.NONE),
                    Symbol.NONE,
                    Ability.GEM
                )
            )

            if (isComputer) deckSetup.add(
                Artifact(
                    java.util.UUID.randomUUID().toString(),
                    "Rogue's Purse",
                    0,
                    Ability.ROGUES_PURSE
                )
            )
            else {
                val startingArtifacts =
                    listOf(Ability.ROGUES_PURSE, Ability.LOOKING_GLASS, Ability.SUMMONING)
                deckSetup.add(
                    when (startingArtifacts.random()) {
                        Ability.LOOKING_GLASS ->
                            Artifact(
                                java.util.UUID.randomUUID().toString(),
                                "LookingGlass",
                                0,
                                Ability.LOOKING_GLASS
                            )

                        Ability.SUMMONING ->
                            Artifact(
                                java.util.UUID.randomUUID().toString(),
                                "BellOfSummoning",
                                0,
                                Ability.SUMMONING
                            )

                        Ability.ROGUES_PURSE ->
                            Artifact(
                                java.util.UUID.randomUUID().toString(),
                                "Rogue's Purse",
                                0,
                                Ability.ROGUES_PURSE
                            )

                        else -> throw IllegalArgumentException("Unknown ability")
                    }
                )
            }
            return Deck(deckSetup).shuffle(true)

        }
        private fun playerCardName(card: CreatureCards): String {
            return when (card) {
                CreatureCards.Knight -> "Spatula"
                CreatureCards.BabyDragon -> "Candle"
                CreatureCards.FenFairy -> "Pail"
                CreatureCards.Witch -> "Broom"
                CreatureCards.Rabbits -> "Cat"
                CreatureCards.Spiders -> "Net"
                CreatureCards.BillyGoat -> "Helmet"
                CreatureCards.Troll -> "Bat"
                CreatureCards.Enchantress -> "Toothbrush"
                else -> throw IllegalArgumentException("Unknown card: $card")
            }
        }
    }
}
