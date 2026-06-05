package ca.kevin.worth.outlandiqa

import ca.kevin.worth.outlandiqa.Model.Ability
import ca.kevin.worth.outlandiqa.Model.RegionName
import ca.kevin.worth.outlandiqa.Model.Symbol
import ca.kevin.worth.outlandiqa.Model.TowerName
import ca.kevin.worth.outlandiqa.Model.Player

/**
 * Open Base Class representing a general game card with basic attributes.
 * Allows inheritance for specialized game components like Creatures.
 */
interface Card {
    val name: String
}

class CreatureCard(
    override val name: String,
    val gem: Boolean,
    val ability: Ability = Ability.NONE,
    val values: List<Symbol> = emptyList(),
    val subduedBy: Symbol = Symbol.NONE
) : Card

class Artifact (
    override val name: String,
    val cost: Int,
    val text: String
) : Card

data class Quest(
    override val name : String,
    val vps: Int,
    val gems: Int,
    val doubleReq: Symbol,
    val tripleReq: Symbol,
    val land: RegionName
) : Card {
    init {
        require(doubleReq != tripleReq) {
            "Double and Triple requirements must be different"
        }
    }

    fun getRequirements(): List<Symbol> = listOf(doubleReq, tripleReq)
}

sealed class QuestInstance {
    data class BoardQuest(val definition: Quest) : QuestInstance()

    data class PlayerQuest(
        val definition: Quest,
        val stored: MutableList<Card> = mutableListOf()
    ) : QuestInstance()
}

/**
 * Data Structure representing a single physical tile/space on the 3x2 board grid.
 */
data class BoardSpace(
    val id: Int,
    val row: Int,
    val col: Int,
    val roads: MutableList<Road> = mutableListOf()
)

/**
 * Data Structure representing a bidirectional edge (Road) joining Space A and B.
 */
data class Road(
    val id: Int,
    val endpointA: Int,
    val endpointB: Int,
    val name: String,
    val creature: CreatureCard? = null
)

/**
 * Region class using composition (holding a BoardSpace) instead of inheritance.
 */
class Region(
    val name: RegionName,
    val tower: TowerName,
    val space: BoardSpace
) {
    var players: MutableList<Player> = mutableListOf()

    fun getName(): String {
        return name.toString()
    }

    override fun toString(): String {
        return "${name}\n${tower}\n$playersString"
    }

    val playersString: String
        get() = players.joinToString(" ") { it.toString() }
}

data class Tower(
    val name: TowerName
)