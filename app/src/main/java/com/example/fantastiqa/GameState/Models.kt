package com.example.boardgame

/**
 * Open Base Class representing a general game card with basic attributes.
 * Allows inheritance for specialized game components like Creatures.
 */
open class Card(
    val name: String
)

/**
 * CreatureCard class inheriting from Card.
 * Has specific game metrics: gem presence, tactical ability, attack power, and subduedBy vulnerability.
 */
class CreatureCard(
    name: String,
    val gem: Boolean,
    val ability: String = "",
    val attack: Int,
    val subduedBy: Int
) : Card(name, image)

class Artifact (
    name: String,
    val cost: Int,
    val text: String
) : Card(name, image)

data class Quest(
    val name: String,
    val vps: Int,
    val gems: Int,
    val doubleReq: Symbol,
    val tripleReq: Symbol,
    val land: RegionName
) : Card(name, image {
  init {
        require(doubleRequirement != tripleRequirement) { 
            "Double and Triple requirements must be different" 
        }
    }

    fun getRequirements(): List<Symbol> = listOf(doubleRequirement, tripleRequirement)
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
 * Adheres to the requested bidirectional architecture.
 */
data class Space(
    val id: Int,
    val row: Int,
    val col: Int,
    val name: String,
    // Holds the dynamic adjacency list of bidirectional roads attached to this space.
    // MutableList allows dynamic addition during board generation.
    val roads: MutableList<Road> = mutableListOf(),
    // Tracks current state/control during gameplay
    var claimedByPlayerId: String? = null
)

/**
 * Data Structure representing a bidirectional edge (Road) joining Space A and B.
 * Specifying endpointA and endpointB makes it bidirectional.
 * Each road displays all CreatureCard attributes.
 */
data class Road(
    val id: Int,
    val endpointA: Int, // Refers to Space.id for the first vertex
    val endpointB: Int, // Refers to Space.id for the second vertex
    val name: String,
    val creature: CreatureCard, // Every road holds and displays creature card attributes
    var isPaved: Boolean = false,
    var pavedByPlayerId: String? = null
)

enum class Ability {
    NONE, MAGIC_CARPET, TOWER_KEY, PLUS_CARD, DRAGON, GEM
}