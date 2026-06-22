package com.example.fantastiqa.gameState

import com.example.fantastiqa.pieces.*

/**
 * Open Base Class representing a general game card with basic attributes.
 */
sealed class Card {
    abstract val id: String
    abstract val name: String
}

/**
 * ponytail: merged Quest, BoardQuest, and PlayerQuest into a single class.
 * uses isPersonal flag to distinguish board vs player state.
 */
data class Quest(
    override val id: String,
    override val name: String,
    val title: String,
    val vps: Int,
    val gems: Int,
    val doubleReq: Symbol,
    val tripleReq: Symbol,
    val land: RegionName,
    val isPersonal: Boolean = false,
    val stored: List<CreatureCard> = emptyList()
) : Card() {

    init {
        // ponytail: invariant check - board quests never hold stored cards.
        if (!isPersonal) require(stored.isEmpty()) { "Board quests cannot have stored cards" }
    }

    fun getRequirements(): List<Symbol> = buildList {
        if (doubleReq != Symbol.NONE) repeat(2) { add(doubleReq) }
        if (tripleReq != Symbol.NONE) repeat(3) { add(tripleReq) }
    }

    fun getFulfilledIndices(): Set<Int> {
        if (!isPersonal) return emptySet()
        val fulfilled = mutableSetOf<Int>()
        val providedSymbols = stored.flatMap { it.values }.toMutableList()
        val requirements = getRequirements()
        requirements.forEachIndexed { i, req ->
            if (providedSymbols.remove(req)) fulfilled.add(i)
        }
        return fulfilled
    }

    fun canAccept(card: Card): Boolean {
        if (card !is CreatureCard) return false
        val reqs = getRequirements()
        if (!isPersonal) return reqs.any { it in card.values }
        
        val fulfilled = getFulfilledIndices()
        if (fulfilled.size >= reqs.size) return false
        return reqs.indices.any { i -> i !in fulfilled && card.values.contains(reqs[i]) }
    }

    // Compatibility aliases
    fun matchReq(card: Card) = canAccept(card)
    fun canStoreCard(card: Card) = canAccept(card)
}

// Typealiases for compatibility with existing code
typealias BoardQuest = Quest
typealias PlayerQuest = Quest

interface HasValues { val values: List<Symbol> }

class ArtifactCard(override val id: String, override val name: String, val cost: Int) : Card()
class PlayerCard(override val id: String, override val name: String) : Card()
class EventCard(override val id: String, override val name: String) : Card()

data class CreatureCard(
    override val id: String,
    override val name: String,
    @JvmField val gem: Boolean,
    override val values: List<Symbol>,
    @JvmField val subduedBy: Symbol,
    @JvmField val ability: Ability
) : Card(), HasValues {
    constructor(name: String, subduedBy: Symbol, gem: Boolean, ability: Ability, vararg value: Symbol) :
            this(name, name, gem, value.toList(), subduedBy, ability)
}

data class Artifact(override val id: String, override val name: String, @JvmField val cost: Int, @JvmField val ability: Ability = Ability.NONE) : Card()
