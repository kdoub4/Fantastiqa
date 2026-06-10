package com.example.fantastiqa.redux;

import com.example.fantastiqa.gameState.Board;
import com.example.fantastiqa.gameState.Deck;
import com.example.fantastiqa.gameState.Card;
import com.example.fantastiqa.gameState.Quest;
import com.example.fantastiqa.gameState.Player;
import com.example.fantastiqa.gameState.CreatureCard;
import com.example.fantastiqa.gameState.Artifact;

import java.util.Collections;
import java.util.List;

/**
 * Immutable game state object that serves as the single source of truth.
 * All game state is contained within this object. To update the game,
 * a new GameState must be created with the updated values.
 */
public final class GameState {
    // Board and game components
    public final Board board;
    public final int vpGoal;
    
    // Deck management
    public final Deck<Card> creatureDeck;
    public final Deck<Artifact> artifactDeck;
    public final Deck<CreatureCard> bazaarDeck;
    public final Deck<Quest> questDeck;
    
    // Players and turn management
    public final List<Player> players;
    public final int currentPlayerIndex;  // Index into players list
    
    // Card selection state
    public final List<Card> selectedCards;
    
    // Game state flags
    public final GamePhase gamePhase;
    public final boolean isGameOver;
    
    public enum GamePhase {
        INITIALIZATION,
        MAIN_PHASE,
        PLAYER_TURN,
        CARD_SELECTION,
        GAME_END
    }

    /**
     * Private constructor - use Builder to create instances
     */
    private GameState(
            Board board,
            int vpGoal,
            Deck<Card> creatureDeck,
            Deck<Artifact> artifactDeck,
            Deck<CreatureCard> bazaarDeck,
            Deck<Quest> questDeck,
            List<Player> players,
            int currentPlayerIndex,
            List<Card> selectedCards,
            GamePhase gamePhase,
            boolean isGameOver
    ) {
        this.board = board;
        this.vpGoal = vpGoal;
        this.creatureDeck = creatureDeck;
        this.artifactDeck = artifactDeck;
        this.bazaarDeck = bazaarDeck;
        this.questDeck = questDeck;
        this.players = Collections.unmodifiableList(players);
        this.currentPlayerIndex = currentPlayerIndex;
        this.selectedCards = Collections.unmodifiableList(selectedCards);
        this.gamePhase = gamePhase;
        this.isGameOver = isGameOver;
    }

    /**
     * Get the current active player
     */
    public Player getCurrentPlayer() {
        if (currentPlayerIndex >= 0 && currentPlayerIndex < players.size()) {
            return players.get(currentPlayerIndex);
        }
        return null;
    }

    /**
     * Get the next player in turn order
     */
    public Player getNextPlayer() {
        int nextIndex = (currentPlayerIndex + 1) % players.size();
        return players.get(nextIndex);
    }

    /**
     * Builder pattern for creating GameState instances
     */
    public static class Builder {
        private Board board;
        private int vpGoal = 4;
        private Deck<Card> creatureDeck;
        private Deck<Artifact> artifactDeck;
        private Deck<CreatureCard> bazaarDeck;
        private Deck<Quest> questDeck;
        private List<Player> players;
        private int currentPlayerIndex = 0;
        private List<Card> selectedCards = Collections.emptyList();
        private GamePhase gamePhase = GamePhase.INITIALIZATION;
        private boolean isGameOver = false;

        public Builder board(Board board) {
            this.board = board;
            return this;
        }

        public Builder vpGoal(int vpGoal) {
            this.vpGoal = vpGoal;
            return this;
        }

        public Builder creatureDeck(Deck<Card> creatureDeck) {
            this.creatureDeck = creatureDeck;
            return this;
        }

        public Builder artifactDeck(Deck<Artifact> artifactDeck) {
            this.artifactDeck = artifactDeck;
            return this;
        }

        public Builder bazaarDeck(Deck<CreatureCard> bazaarDeck) {
            this.bazaarDeck = bazaarDeck;
            return this;
        }

        public Builder questDeck(Deck<Quest> questDeck) {
            this.questDeck = questDeck;
            return this;
        }

        public Builder players(List<Player> players) {
            this.players = players;
            return this;
        }

        public Builder currentPlayerIndex(int index) {
            this.currentPlayerIndex = index;
            return this;
        }

        public Builder selectedCards(List<Card> selectedCards) {
            this.selectedCards = selectedCards;
            return this;
        }

        public Builder gamePhase(GamePhase gamePhase) {
            this.gamePhase = gamePhase;
            return this;
        }

        public Builder isGameOver(boolean isGameOver) {
            this.isGameOver = isGameOver;
            return this;
        }

        public GameState build() {
            return new GameState(
                    board,
                    vpGoal,
                    creatureDeck,
                    artifactDeck,
                    bazaarDeck,
                    questDeck,
                    players,
                    currentPlayerIndex,
                    selectedCards,
                    gamePhase,
                    isGameOver
            );
        }
    }

    @Override
    public String toString() {
        return "GameState{" +
                "gamePhase=" + gamePhase +
                ", currentPlayer=" + (getCurrentPlayer() != null ? getCurrentPlayer().name : "null") +
                ", isGameOver=" + isGameOver +
                '}';
    }
}
