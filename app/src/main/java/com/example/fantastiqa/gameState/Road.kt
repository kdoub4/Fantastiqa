package com.example.fantastiqa.gameState

class Road : Area {
    @JvmField
    var creature: CreatureCard? = null
    @JvmField
    var gem: Boolean = false

    constructor()

    fun getName(): String? {
        return creature?.name
    }

    constructor(creature: CreatureCard, gem: Boolean) {
        this.creature = creature
        this.gem = gem
    }

    fun getConnectedRegion(starter: Region?): Region? {
        val reg0 = adjacencies.get(0) as Region?
        return if (reg0 === starter) adjacencies.get(1) as Region? else reg0
    }
}
