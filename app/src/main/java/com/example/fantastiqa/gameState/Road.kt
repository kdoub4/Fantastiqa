package com.example.fantastiqa.gameState

import java.util.UUID

/**
 * Immutable Road representation.
 */
data class Road(
    @JvmField val creature: CreatureCard? = null,
    @JvmField val gem: Boolean = false,
    @JvmField val id: String = UUID.randomUUID().toString()
) : Area() {
    
    fun getName(): String? = creature?._name

    fun getConnectedRegion(starter: Region?, board: Board): Region? {
        val adjacent = board.getAdjacentAreas(starter!!)
        // Logic depends on finding the other side of this road in the board
        return adjacent.find { it?.first == this }?.second as? Region
    }
}
