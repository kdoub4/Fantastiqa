# Redux Architecture Refactoring

## Overview

This refactoring implements a **Redux-like unidirectional data flow** pattern to ensure scalability, testability, and maintainability of the Fantastiqa game engine.

### Core Principles

1. **Single Source of Truth**: All game state is contained in an immutable `GameState` object
2. **Unidirectional Data Flow**: UI → Action → Store → GameEngine → New GameState → UI
3. **Pure Functions**: The GameEngine is a pure function with no side effects
4. **Immutability**: Game state is never modified; new states are created
5. **Decoupling**: Core game logic is completely independent of Android/UI frameworks

## Architecture Components

### 1. GameState (Single Source of Truth)

**File**: `redux/GameState.java`

An immutable data class that holds ALL game state:
- Board configuration and piece positions
- Player hands, resources, and progress
- Active quests and turn queue
- Game phase and win conditions

```java
GameState state = new GameState.Builder()
    .board(board)
    .players(players)
    .currentPlayerIndex(0)
    .gamePhase(GameState.GamePhase.PLAYER_TURN)
    .build();
```

**Key Features**:
- Builder pattern for flexible construction
- Unmodifiable lists prevent accidental mutations
- Immutable after creation

---

### 2. Action (Command Pattern)

**Files**: `redux/Action.java`, `redux/actions/*.java`

Actions are immutable commands describing what happened in the game.

**Action Types**:
- `PlayerAction`: Card drawing, resource usage, ability activation
- `CardAction`: Card selection and discarding
- `QuestAction`: Quest completion and card storage
- `SubdueAction`: Creature subdual and road conquest
- `TurnAction`: Game flow and phase progression

```java
// Example: Player draws 5 cards
Action drawCards = new PlayerAction(playerIndex, ActionType.DRAW_CARDS, 5);

// Example: Advance to next player's turn
Action nextTurn = new TurnAction(ActionType.NEXT_TURN);
```

**Key Features**:
- Immutable command objects
- Self-documenting action types
- Payload can contain any required data
- All information needed to update state is contained in the action

---

### 3. GameEngine (Pure Reducer)

**File**: `redux/GameEngine.java`

A pure function that processes actions and returns new game states.

```java
public GameState reduce(GameState currentState, Action action) {
    // Pure function: same inputs → same output
    // No side effects, no mutations
    // Returns NEW state object
}
```

**Key Features**:
- **Pure function**: No I/O, no randomness, no mutations
- **Deterministic**: Same action on same state always produces same result
- **Zero Android dependencies**: Can be tested in plain Java unit tests
- **Composable**: Easy to test individual action handlers
- **Extensible**: New action types just need new handler methods

**Example Handler**:
```java
private GameState handleDrawCards(GameState state, PlayerAction action) {
    Player currentPlayer = state.getCurrentPlayer();
    List<Card> newCards = state.creatureDeck.draw((int) action.getPayload());
    
    // Create new state with updated player hand
    List<Player> updatedPlayers = new ArrayList<>(state.players);
    Player updatedPlayer = new Player(currentPlayer); // copy constructor
    updatedPlayer.hand.addAll(newCards);
    updatedPlayers.set(state.currentPlayerIndex, updatedPlayer);
    
    return new GameState.Builder()
        .board(state.board)
        .vpGoal(state.vpGoal)
        // ... copy all other fields
        .players(updatedPlayers)
        .build();
}
```

---

### 4. Store (State Management)

**File**: `redux/Store.java`

Holds the current `GameState` in a `StateFlow` and processes actions.

```java
Store store = new Store(initialGameState);

// Dispatch an action
store.dispatch(new PlayerAction(0, ActionType.DRAW_CARDS, 5));

// Observe state changes (reactive)
store.getStateFlow().collect { newState ->
    updateUI(newState)
}

// Get current state
GameState state = store.getState();
```

**Key Features**:
- **Single dispatch point**: All state changes go through `dispatch()`
- **StateFlow integration**: Reactive updates for Android UI
- **Listener support**: Subscribe to state changes
- **Pure action processing**: Uses GameEngine to reduce state
- **Immutability enforcement**: Only returns unmodifiable references

---

### 5. Middleware (Extensible Processing)

**Files**: `redux/middleware/*.java`

Middleware intercepts actions for logging, validation, or side effects.

```java
Store store = new Store(initialState);

// Add logging
Middleware logging = new LoggingMiddleware();

// Add validation
Middleware validation = new ValidationMiddleware();

// Chain them
MiddlewarePipeline pipeline = new MiddlewarePipeline()
    .use(logging)
    .use(validation)
    .build();
```

**Built-in Middleware**:
- **LoggingMiddleware**: Logs all actions and state changes for debugging
- **ValidationMiddleware**: Validates actions against game rules before reducing

**Custom Middleware Example**:
```java
public class AIMiddleware implements Middleware {
    @Override
    public void process(Store store, Next next, Action action) {
        // Pre-process
        if (action instanceof PlayerAction) {
            PlayerAction playerAction = (PlayerAction) action;
            // Could add AI decision-making here
        }
        
        // Pass to next middleware
        next.dispatch(action);
        
        // Post-process (could trigger AI moves)
    }
}
```

---

### 6. GameInitializer (Setup)

**File**: `redux/utils/GameInitializer.java`

Factory for creating a fully initialized game state.

```java
GameState initialState = GameInitializer.initializeNewGame();
Store store = new Store(initialState);
```

---

### 7. TimeTravel (Debugging)

**File**: `redux/utils/TimeTravel.java`

Debug utility for inspecting game history and state at any point.

```java
TimeTravel timeTravel = new TimeTravel();

// Record each action and resulting state
timeTravel.record(action, newState);

// Inspect history
GameState stateAtTurn5 = timeTravel.getStateAt(5);
Action actionThatLedHere = timeTravel.getActionAt(4);
```

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer (Android)                    │
│                    (Observes StateFlow)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │ 1. User Action
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  store.dispatch(action)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │ 2. Action flows through
                           │    middleware pipeline
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Middleware Pipeline                             │
│  (Logging, Validation, Custom Processing)                   │
└──────────────────────────┬──────────────────────────────────┘
                           │ 3. After middleware
                           ▼
┌─────────────────────────────────────────────────────────────┐
│            GameEngine.reduce(state, action)                 │
│         (Pure function, no side effects)                     │
│                                                              │
│  Pure Kotlin/Java logic                                      │
│  ├─ No Android dependencies                                  │
│  ├─ No I/O operations                                        │
│  ├─ Fully testable                                           │
└──────────────────────────┬─────────��────────────────────────┘
                           │ 4. New immutable state
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Current GameState (Immutable)                   │
│                                                              │
│  ├─ Board configuration                                      │
│  ├─ Player states                                            │
│  ├─ Deck contents                                            │
│  ├─ Game phase                                               │
│  └─ All game data                                            │
└──────────────────────────┬──────────────────────────────────┘
                           │ 5. StateFlow emits
                           │    new state
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    UI Recomposes                             │
│                 (Observes new state)                         │
└─────────────────────────────────────────────────────────────┘
```

---

## Benefits of This Architecture

### 1. Testability
✅ **GameEngine is 100% testable** - pure functions, no Android dependencies

```java
@Test
public void testDrawCards() {
    GameState state = initialState;
    Action drawCards = new PlayerAction(0, ActionType.DRAW_CARDS, 5);
    
    GameState newState = gameEngine.reduce(state, drawCards);
    
    assertEquals(5 + initialHandSize, newState.getCurrentPlayer().hand.size());
}
```

### 2. Predictability
✅ **State changes are deterministic** - no randomness, same input = same output

### 3. Debuggability
✅ **Every action is recorded** - inspect state at any point in time
✅ **Time-travel debugging** - replay the game from any state

### 4. Scalability
✅ **New features are isolated** - add new action types without affecting existing code
✅ **Middleware for cross-cutting concerns** - logging, validation, async operations

### 5. Separation of Concerns
✅ **Core logic decoupled from UI** - game engine doesn't know about Android
✅ **Easy to swap UI layers** - could use Compose, XML, or even a web client

### 6. Maintainability
✅ **Data flow is explicit** - follow actions through middleware to engine to new state
✅ **Immutability prevents bugs** - no hidden state mutations
✅ **Self-documenting actions** - action names describe what happened

---

## Migration Guide

### Step 1: Initialize the Store

```java
// In your main activity or application setup
GameState initialState = GameInitializer.initializeNewGame();
Store gameStore = new Store(initialState);
```

### Step 2: Observe State Changes

```java
// In your Compose UI
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.gameState.collectAsState()
    
    GameBoard(
        board = state.board,
        players = state.players,
        currentPlayer = state.getCurrentPlayer()
    )
}
```

### Step 3: Dispatch Actions Instead of Calling Methods

**Before** (Old way - mutates state):
```java
player.hand.addAll(deck.draw(5));
player.gems += 3;
```

**After** (New way - creates new state):
```java
store.dispatch(new PlayerAction(playerIndex, ActionType.DRAW_CARDS, 5));
store.dispatch(new PlayerAction(playerIndex, ActionType.GAIN_GEMS, 3));
```

### Step 4: Create Action Handlers in GameEngine

```java
private GameState handleGainGems(GameState state, PlayerAction action) {
    int gemsToGain = (int) action.getPayload();
    
    // Create updated player
    Player currentPlayer = state.getCurrentPlayer();
    Player updatedPlayer = new Player(currentPlayer);
    updatedPlayer.gems += gemsToGain;
    
    // Create updated player list
    List<Player> updatedPlayers = new ArrayList<>(state.players);
    updatedPlayers.set(state.currentPlayerIndex, updatedPlayer);
    
    // Return new state
    return new GameState.Builder()
        .board(state.board)
        .vpGoal(state.vpGoal)
        .creatureDeck(state.creatureDeck)
        .artifactDeck(state.artifactDeck)
        .bazaarDeck(state.bazaarDeck)
        .questDeck(state.questDeck)
        .players(updatedPlayers)
        .currentPlayerIndex(state.currentPlayerIndex)
        .selectedCards(state.selectedCards)
        .gamePhase(state.gamePhase)
        .isGameOver(state.isGameOver)
        .build();
}
```

---

## File Structure

```
app/src/main/java/com/example/fantastiqa/
├── redux/
│   ├── Action.java                 # Base action class
│   ├── GameState.java              # Immutable state container
│   ├── GameEngine.java             # Pure reducer function
│   ├── Store.java                  # State management & dispatch
│   ├── MiddlewarePipeline.java      # Middleware orchestration
│   ├── actions/
│   │   ├── PlayerAction.java       # Player-related actions
│   │   ├── CardAction.java         # Card manipulation actions
│   │   ├── QuestAction.java        # Quest-related actions
│   │   ├── SubdueAction.java       # Combat/subdual actions
│   │   └── TurnAction.java         # Turn/phase actions
│   ├── middleware/
│   │   ├── Middleware.java         # Middleware interface
│   │   ├── LoggingMiddleware.java  # Action/state logging
│   │   └── ValidationMiddleware.java # Action validation
│   └── utils/
│       ├── GameInitializer.java    # Game setup factory
│       └── TimeTravel.java         # Debug utility
└── GameState/
    └── ... (existing game classes)
```

---

## Testing

### Unit Testing GameEngine

```java
public class GameEngineTest {
    private GameEngine engine;
    private GameState initialState;
    
    @Before
    public void setUp() {
        engine = new GameEngine();
        initialState = GameInitializer.initializeNewGame();
    }
    
    @Test
    public void testPlayerDrawsCards() {
        Action action = new PlayerAction(0, ActionType.DRAW_CARDS, 5);
        GameState newState = engine.reduce(initialState, action);
        
        // Verify new state has updated hand
        assertEquals(5 + 5, newState.getCurrentPlayer().hand.size());
        // Verify original state unchanged
        assertEquals(5, initialState.getCurrentPlayer().hand.size());
    }
    
    @Test
    public void testNextTurn() {
        Action action = new TurnAction(ActionType.NEXT_TURN);
        GameState newState = engine.reduce(initialState, action);
        
        assertEquals(1, newState.currentPlayerIndex);
    }
}
```

### Integration Testing with Store

```java
public class StoreTest {
    private Store store;
    
    @Before
    public void setUp() {
        GameState initialState = GameInitializer.initializeNewGame();
        store = new Store(initialState);
    }
    
    @Test
    public void testDispatchUpdatesState() {
        GameState oldState = store.getState();
        store.dispatch(new PlayerAction(0, ActionType.DRAW_CARDS, 5));
        GameState newState = store.getState();
        
        assertNotEquals(oldState, newState);
    }
}
```

---

## Next Steps

1. **Implement remaining action handlers** in GameEngine
2. **Create ViewModel** that wraps Store for Android UI
3. **Add Compose bindings** to observe StateFlow
4. **Migrate existing screens** to use Redux dispatch
5. **Add comprehensive test coverage** for GameEngine
6. **Document custom middleware** patterns

---

## References

- Redux Pattern: https://redux.js.org/
- Command Pattern: https://refactoring.guru/design-patterns/command
- Unidirectional Data Flow: https://redux.js.org/understanding/history-and-design/prior-art
- Immutable Data Structures: https://en.wikipedia.org/wiki/Persistent_data_structure
