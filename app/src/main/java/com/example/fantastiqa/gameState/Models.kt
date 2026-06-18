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

sealed class Quest(
    id: String,
    name: String,
    open val title: String,
    open val vps: Int,
    open val gems: Int,
    open val doubleReq: Symbol,
    open val tripleReq: Symbol,
    open val land: RegionName
) : Card(id, name) {

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
}

data class BoardQuest(
    val _id: String,
    val _name: String,
    override val title: String,
    override val vps: Int,
    override val gems: Int,
    override val doubleReq: Symbol,
    override val tripleReq: Symbol,
    override val land: RegionName
) : Quest(_id, _name, title, vps, gems, doubleReq, tripleReq, land)

data class PlayerQuest(
    val _id: String,
    val _name: String,
    override val title: String,
    override val vps: Int,
    override val gems: Int,
    override val doubleReq: Symbol,
    override val tripleReq: Symbol,
    override val land: RegionName,
    val stored: List<CreatureCard> = emptyList()
) : Quest(_id, _name, title, vps, gems, doubleReq, tripleReq, land) {

    fun getFulfilledIndices(): Set<Int> {
        val reqs = getRequirements()
        val fulfilled = mutableSetOf<Int>()
        val providedSymbols = stored.flatMap { it.values }.toMutableList()

        for (i in reqs.indices) {
            val req = reqs[i]
            if (providedSymbols.remove(req)) {
                fulfilled.add(i)
            }
        }
        return fulfilled
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
}

interface HasValues { val values: List<Symbol> }

class ArtifactCard(id: String, name: String, val cost: Int) : Card(id, name)
class PlayerCard(id: String, name: String) : Card(id, name)
class EventCard(id: String, name: String) : Card(id, name)

data class CreatureCard(
    val _id: String,
    val _name: String,
    @JvmField val gem: Boolean,
    override val values: List<Symbol>,
    @JvmField val subduedBy: Symbol,
    @JvmField val ability: Ability
) : Card(_id, _name), HasValues {
    constructor(name: String, subduedBy: Symbol, gem: Boolean, ability: Ability, value1: Symbol) :
            this(name, name, gem, listOf(value1), subduedBy, ability)

    constructor(name: String, subduedBy: Symbol, gem: Boolean, ability: Ability, value1: Symbol, value2: Symbol) :
            this(name, name, gem, listOf(value1, value2), subduedBy, ability)
}

data class Artifact(val _id: String, val _name: String, @JvmField val cost: Int, @JvmField val ability: Ability = Ability.NONE) : Card(_id, _name)
