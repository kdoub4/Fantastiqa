package com.example.fantastiqa.redux.middleware

import com.example.fantastiqa.redux.GameState
import com.example.fantastiqa.redux.Action

interface ComputerPlayerStrategy {
    fun evaluateNextAction(state: GameState): Action?
}
