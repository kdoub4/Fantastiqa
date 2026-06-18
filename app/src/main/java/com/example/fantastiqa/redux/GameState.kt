package com.example.fantastiqa.redux

import com.example.fantastiqa.gameState.Artifact
import com.example.fantastiqa.gameState.Board
import com.example.fantastiqa.gameState.BoardQuest
import com.example.fantastiqa.gameState.Card
import com.example.fantastiqa.gameState.CreatureCard
import com.example.fantastiqa.gameState.Deck
import com.example.fantastiqa.gameState.Player
import com.example.fantastiqa.gameState.Quest
import com.example.fantastiqa.gameState.Region
import com.example.fantastiqa.gameState.Road

/**
 * Immutable game state object that serves as the single source of truth.
 * All game state is contained within this object. To update the game,
 * a new GameState must be created with the updated values.
 */
data class GameState(
    // Board and game components
    val board: Board?,
    val vpGoal: Int = 4,
    
    // Deck management
    val creatureDeck: Deck<Card>? = null,
    val artifactDeck: Deck<Artifact>? = null,
    val bazaarDeck: Deck<CreatureCard>? = null,
    val questDeck: Deck<BoardQuest>? = null,
    
    // Players and turn management
    @JvmField val players: List<Player> = emptyList(),
    @JvmField val currentPlayerIndex: Int = 0,
    @JvmField val playerPositions: Map<String, Region> = emptyMap(),
    
    // Turn counter
    val turnCount: Int = 1,
    
    // Card selection state
    val selectedCards: List<Card?> = emptyList(),
    val selectedQuest: Quest? = null,
    val selectedRoad: Road? = null,
    val selectedRoads: List<Road> = emptyList(),
    val towerDrawnCards: List<Card> = emptyList(),
    val towerMenuOpen: Boolean = false,
    val isFreeTowerAction: Boolean = false,
    
    // Game state flags
    @JvmField val gamePhase: GamePhase? = GamePhase.INITIALIZATION,
    @JvmField val isGameOver: Boolean = false
) {
    enum class GamePhase {
        INITIALIZATION,
        START,
        OPEN,
        SUBDUE,
        TOWER,
        QUEST,
        WARDROBE,
        DISCARD_OPEN,
        DRAW,
        NEXT_TURN,
        MAIN_PHASE,
        PLAYER_TURN,
        CARD_SELECTION,
        GAME_END
    }

    /**
     * Get the current active player
     */
    val currentPlayer: Player?
        get() = if (currentPlayerIndex in players.indices) players[currentPlayerIndex] else null

    /**
     * Get the next player in turn order
     */
    val nextPlayer: Player?
        get() {
            if (players.isEmpty()) return null
            val nextIndex = (currentPlayerIndex + 1) % players.size
            return players[nextIndex]
        }

    override fun toString(): String {
        return "GameState(" +
                "gamePhase=$gamePhase, " +
                "currentPlayer=${currentPlayer?.name ?: "null"}, " +
                "isGameOver=$isGameOver" +
                ")"
    }
}
