package com.example.fantastiqa.gameState

import com.example.fantastiqa.pieces.*

/**
 * Open Base Class representing a general game card with basic attributes.
 * Allows inheritance for specialized game components like Creatures.
 */
sealed class Card(
    @JvmField val id: String,
    @JvmField val name: String
)

data class Quest(
    val _id: String,
    val _name: String,
    @JvmField val title: String,
    @JvmField val vps: Int,
    @JvmField val gems: Int,
    @JvmField val doubleReq: Symbol,
    @JvmField val tripleReq: Symbol,
    @JvmField val land: RegionName,
    @JvmField val stored: List<Card> = emptyList()
) : Card(_id, _name) {
    constructor(name: String, vps: Int, gems: Int, doubleReq: Symbol, tripleReq: Symbol, land: RegionName) :
            this(name, name, name, vps, gems, doubleReq, tripleReq, land)

    init {
        require(doubleReq != tripleReq || doubleReq == Symbol.NONE) {
            "Double and Triple requirements must be different"
        }
    }

    fun getDoubleRequirement(): Symbol = doubleReq
    fun getTripleRequirement(): Symbol = tripleReq

    fun getRequirements(): List<Symbol> {
        val list = mutableListOf<Symbol>()
        if (doubleReq != Symbol.NONE) {
            repeat(2) { list.add(doubleReq) }
        }
        if (tripleReq != Symbol.NONE) {
            repeat(3) { list.add(tripleReq) }
        }
        return list
    }

    fun canStoreCard(card: Card): Boolean {
        if (card !is CreatureCard) return false
        val reqs = getRequirements()
        val fulfilled = getFulfilledIndices()
        
        if (fulfilled.size >= reqs.size) return false

        // Check if card matches any unmet requirement
        for (i in reqs.indices) {
            if (i !in fulfilled && card.values.contains(reqs[i])) {
                return true
            }
        }
        return false
    }

    fun getFulfilledIndices(): Set<Int> {
        val reqs = getRequirements()
        val fulfilled = mutableSetOf<Int>()
        val providedSymbols = stored.filterIsInstance<CreatureCard>().flatMap { it.values }.toMutableList()

        for (i in reqs.indices) {
            val req = reqs[i]
            if (providedSymbols.remove(req)) {
                fulfilled.add(i)
            }
        }
        return fulfilled
    }

    fun withIncrementedVp(): Quest = copy(vps = vps + 1)
}

data class PlayerQuest(
    val quest: Quest,
    val stored: List<Card> = emptyList()
)

class ArtifactCard(id: String, name: String, val cost: Int) : Card(id, name)
class PlayerCard(id: String, name: String) : Card(id, name)
class EventCard(id: String, name: String) : Card(id, name)

data class CreatureCard(
    val _id: String,
    val _name: String,
    @JvmField val gem: Boolean,
    @JvmField val values: List<Symbol>,
    @JvmField val subduedBy: Symbol,
    @JvmField val ability: Ability
) : Card(_id, _name) {
    constructor(name: String, subduedBy: Symbol, gem: Boolean, ability: Ability, value1: Symbol) :
            this(name, name, gem, listOf(value1), subduedBy, ability)

    constructor(name: String, subduedBy: Symbol, gem: Boolean, ability: Ability, value1: Symbol, value2: Symbol) :
            this(name, name, gem, listOf(value1, value2), subduedBy, ability)
}

data class Artifact(val _id: String, val _name: String, @JvmField val cost: Int, @JvmField val ability: Ability = Ability.NONE) : Card(_id, _name)
