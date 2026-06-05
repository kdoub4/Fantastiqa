package ca.kevin.worth.outlandiqa

import ca.kevin.worth.outlandiqa.Model.Ability
import ca.kevin.worth.outlandiqa.Model.Symbol
import kotlin.random.Random

/**
 * Initializes the 6-space grid board organized in a 3x2 grid,
 * instantiates the 8 CreatureCards as specified, and creates 7 bidirectional roads.
 * Adjacency lists are constructed such that each Space object
 * holds reference references to all bidirectional roads that touch it.
 */
fun initializeBoard(): List<BoardSpace> {
    // 1. Instantiating the 6 spaces organized in a beautiful 3x2 layout
    val spaces = listOf(
        BoardSpace(id = 1, row = 0, col = 0),
        BoardSpace(id = 2, row = 0, col = 1),
        BoardSpace(id = 3, row = 1, col = 0),
        BoardSpace(id = 4, row = 1, col = 1),
        BoardSpace(id = 5, row = 2, col = 0),
        BoardSpace(id = 6, row = 2, col = 1)
    )

    val spaceMap = spaces.associateBy { it.id }

    // 2. Generating the 8 Creature Cards
    val animals = listOf(
        "Firefox", "Lunar Hawk", "Shadow Panther", "Golden Cobra",
        "Glacier Bear", "Stone Golem", "Coral Turtle", "Swift Cheetah"
    )

    // Using symbols from ca.kevin.worth.outlandiqa.Model.Symbol
    val symbols = Symbol.values().filter { it != Symbol.NONE }

    val creatureCards = List(8) { index ->
        val name = animals[index]
        val gem = Random.nextBoolean()
        val ability = Ability.NONE

        // Rule: attack values, weakness.
        val attackSymbol = symbols[index % symbols.size]
        val weaknessSymbol = symbols[(index + 1) % symbols.size]

        CreatureCard(
            name = name,
            gem = gem,
            ability = ability,
            values = listOf(attackSymbol),
            subduedBy = weaknessSymbol
        )
    }

    // 3. Creating the 7 bidirectional roads conforming to 3x2 structures
    // Each road holds a distinct CreatureCard showing all detailed attributes
    val roads = listOf(
        // Horizontal roads (Col 0 ↔ Col 1)
        Road(id = 1, endpointA = 1, endpointB = 2, name = "H-Road 1", creature = creatureCards[0]),
        Road(id = 2, endpointA = 3, endpointB = 4, name = "H-Road 2", creature = creatureCards[1]),
        Road(id = 3, endpointA = 5, endpointB = 6, name = "H-Road 3", creature = creatureCards[2]),

        // Vertical roads (Row to Row+1)
        Road(id = 4, endpointA = 1, endpointB = 3, name = "V-Road 4", creature = creatureCards[3]),
        Road(id = 5, endpointA = 3, endpointB = 5, name = "V-Road 5", creature = creatureCards[4]),
        Road(id = 6, endpointA = 2, endpointB = 4, name = "V-Road 6", creature = creatureCards[5]),
        Road(id = 7, endpointA = 4, endpointB = 6, name = "V-Road 7", creature = creatureCards[6])
    )

    // 4. Adjacency List Assignment: Adding bidirectional roads to associated spaces
    for (road in roads) {
        spaceMap[road.endpointA]?.roads?.add(road)
        spaceMap[road.endpointB]?.roads?.add(road)
    }

    return spaces
}
