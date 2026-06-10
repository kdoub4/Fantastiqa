package com.example.fantastiqa.gameState

import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.pieces.TowerName

/**
 * Immutable Region representation.
 * Player positions are now tracked in GameState, not here.
 */
class Region(@JvmField var name: RegionName, @JvmField var tower: TowerName) : Area() {

    @JvmField var players: List<Player> = emptyList()

    fun getPlayersString(): String {
        return players.joinToString(" ") { it.name }
    }

    fun getName(): String {
        return name.toString()
    }

    override fun toString(): String {
        return name.toString() + "\n" + tower.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Region) return false
        return name == other.name && tower == other.tower
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + tower.hashCode()
        return result
    }
}
