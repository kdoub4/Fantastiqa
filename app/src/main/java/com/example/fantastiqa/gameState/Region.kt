package com.example.fantastiqa.gameState

import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.pieces.TowerName

class Region(@JvmField var name: RegionName, @JvmField var tower: TowerName) : Area() {
    @JvmField
    var players: MutableList<Player> = ArrayList<Player>()

    fun getName(): String {
        return name.toString()
    }

    override fun toString(): String {
        return name.toString() + "\n" + tower.toString() + "\n" + this.playersString
    }

    val playersString: String
        get() {
            var results = ""
            for (aPlayer in players) {
                results += aPlayer.toString() + " "
            }
            return results
        }
}

