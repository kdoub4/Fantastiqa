package com.example.fantastiqa.redux.actions

import com.example.fantastiqa.gameState.Region
import com.example.fantastiqa.redux.Action

/**
 * Atomic action for player movement.
 */
class MoveAction(
    val playerIndex: Int,
    val destination: Region,
    val moveType: MoveType,
    val useAbility: Boolean = false
) : Action() {
    override val type: String = "MOVE_PLAYER_${moveType.name}${if (useAbility) "_ABILITY" else ""}"
}

enum class MoveType {
    ADJACENT,       // Normal move to connected region
    FLYING_CARPET,  // Move anywhere (costs 1 carpet)
    TOWER_KEY       // Move between matching tower types
}
