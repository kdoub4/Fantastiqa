# GameViewModel Integration Guide

## Overview

**GameViewModel** bridges the Redux Store with Android's UI layer. It:

- ✅ Manages the Redux Store lifecycle
- ✅ Exposes GameState as LiveData for reactive UI updates
- ✅ Provides convenience methods for dispatching actions
- ✅ Handles UI state (loading, errors, navigation)
- ✅ Survives configuration changes (screen rotation, etc.)

## Architecture

```
┌─────────────────────────────────────────────────────┐
│          Android UI Layer           │
│  (Activities, Fragments, Compose)   │
└─────────────────────┬───────────────────────────────┘
               │
               │ observes LiveData
               │ calls methods
               ▼
┌─────────────────────────────────────────────────────┐
│         GameViewModel               │
│  (LiveData<GameState>)              │
│  (Convenience action methods)        │
└─────────────────────┬───────────────────────────────┘
               │
               │ uses
               ▼
┌─────────────────────────────────────────────────────┐
│         Redux Layer                 │
│  Store → GameEngine → GameState    │
│  (Pure logic, no Android deps)      │
└─────────────────────────────────────────────────────┘
```

## Quick Start

### 1. Initialize ViewModel in Activity

```java
public class GameActivity extends AppCompatActivity {
    private GameViewModel gameViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        // Get or create ViewModel
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);
        
        // Initialize game (only on first creation)
        if (gameViewModel.getCurrentGameState() == null) {
            gameViewModel.initializeGame();
        }
    }
}
```

### 2. Observe GameState in Fragment

```java
public class GameBoardFragment extends BaseGameFragment {
    
    @Override
    protected void onGameStateChanged(GameState gameState) {
        // Update UI when state changes
        updateCurrentPlayerDisplay(gameState.getCurrentPlayer());
        updateBoardDisplay(gameState.board);
        updatePlayerResources(gameState.players);
    }
}
```

### 3. Dispatch Actions from UI

```java
// Player draws 5 cards
gameViewModel.playerDrawCards(0, 5);

// Player gains 3 gems
gameViewModel.playerGainGems(0, 3);

// Advance to next turn
gameViewModel.nextTurn();
```

## Complete Example: Card Selection Screen

### XML Layout

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    
    <TextView
        android:id="@+id/prompt_text"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Select 5 cards" />
    
    <RecyclerView
        android:id="@+id/cards_recycler"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
    
    <Button
        android:id="@+id/confirm_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Confirm Selection" />
</LinearLayout>
```

### Fragment Implementation

```java
public class CardSelectionFragment extends BaseGameFragment {
    
    private RecyclerView cardsRecycler;
    private CardAdapter cardAdapter;
    private List<Card> selectedCards = new ArrayList<>();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_selection, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        cardsRecycler = view.findViewById(R.id.cards_recycler);
        Button confirmButton = view.findViewById(R.id.confirm_button);
        
        // Set up RecyclerView with card adapter
        cardAdapter = new CardAdapter(card -> onCardSelected(card));
        cardsRecycler.setAdapter(cardAdapter);
        
        // Confirm button dispatches SELECT_CARDS action
        confirmButton.setOnClickListener(v -> onConfirmSelection());
    }
    
    @Override
    protected void onGameStateChanged(GameState gameState) {
        // Display available cards
        if (gameState.selectedCards != null) {
            cardAdapter.setCards(gameState.selectedCards);
        }
    }
    
    private void onCardSelected(Card card) {
        selectedCards.add(card);
    }
    
    private void onConfirmSelection() {
        // Dispatch SELECT_CARDS action through ViewModel
        int currentPlayerIndex = gameViewModel.getCurrentGameState().currentPlayerIndex;
        gameViewModel.selectCards(currentPlayerIndex, selectedCards);
    }
    
    @Override
    protected void onUIStateChanged(GameViewModel.UIState uiState) {
        if (uiState == GameViewModel.UIState.CARD_SELECTION) {
            // Enable selection UI
        } else {
            // Disable selection UI, maybe navigate away
        }
    }
}
```

## All Available ViewModel Methods

### Getters (LiveData)

```java
// Get game state as LiveData
LiveData<GameState> state = gameViewModel.getGameState();

// Get UI state
LiveData<GameViewModel.UIState> uiState = gameViewModel.getUIState();

// Get error messages
LiveData<String> errors = gameViewModel.getErrorMessage();

// Get current state (immediate, not LiveData)
GameState currentState = gameViewModel.getCurrentGameState();
```

### Player Actions

```java
// Drawing and resources
gameViewModel.playerDrawCards(playerIndex, count);
gameViewModel.playerGainGems(playerIndex, gemCount);
gameViewModel.playerLoseGems(playerIndex, gemCount);
gameViewModel.playerGainTrophies(playerIndex, trophyCount);
gameViewModel.playerLoseTrophies(playerIndex, trophyCount);

// Abilities
gameViewModel.playerUseFlyingCarpet(playerIndex);
gameViewModel.playerUseShuffle(playerIndex);
gameViewModel.playerUseTent(playerIndex);
```

### Card Actions

```java
// Card manipulation
gameViewModel.selectCards(playerIndex, selectedCards);
gameViewModel.discardCards(playerIndex, cardsToDiscard);
gameViewModel.returnUnselectedCards(playerIndex, cardsToReturn);
```

### Quest Actions

```java
// Quest management
gameViewModel.completeQuest(playerIndex, quest, requiredCards);
gameViewModel.storeCardForQuest(playerIndex, quest, card);
gameViewModel.drawQuest(playerIndex);
```

### Combat/Subdue Actions

```java
// Combat
gameViewModel.subdueCreature(playerIndex, road, creature, playedCards);
gameViewModel.conquestRoad(playerIndex, road);
gameViewModel.placeCreatureOnRoad(playerIndex, road, creature);
```

### Turn/Phase Actions

```java
// Turn management
gameViewModel.nextTurn();
gameViewModel.advancePhase(newPhase);
gameViewModel.startPlayerTurn(playerIndex);
gameViewModel.endGame();
```

## UI State Handling

### UIState Enum

```java
public enum UIState {
    IDLE,                    // Normal gameplay
    LOADING,                 // Loading game
    CARD_SELECTION,          // Waiting for card selection
    QUEST_COMPLETION,        // Completing a quest
    SUBDUING_CREATURE,       // Subduing a creature
    SHOWING_ERROR,           // Error dialog shown
    GAME_OVER                // Game has ended
}
```

### Responding to UI State

```java
@Override
protected void onUIStateChanged(GameViewModel.UIState uiState) {
    switch (uiState) {
        case IDLE:
            // Hide loading, enable normal gameplay
            loadingIndicator.setVisibility(View.GONE);
            gameBoard.setEnabled(true);
            break;
            
        case LOADING:
            // Show loading spinner
            loadingIndicator.setVisibility(View.VISIBLE);
            gameBoard.setEnabled(false);
            break;
            
        case CARD_SELECTION:
            // Show card selection UI
            startCardSelectionMode();
            break;
            
        case QUEST_COMPLETION:
            // Show quest completion dialog
            showQuestCompletionDialog();
            break;
            
        case SUBDUING_CREATURE:
            // Show creature subdue UI
            showSubdueUI();
            break;
            
        case GAME_OVER:
            // Navigate to game over screen
            navigateToGameOverScreen();
            break;
            
        case SHOWING_ERROR:
            // Error already shown in onErrorMessage
            break;
    }
}
```

## Best Practices

✅ **Do:**
- Use GameViewModel for all game state access
- Dispatch actions instead of modifying state directly
- Observe LiveData for UI updates
- Handle configuration changes gracefully
- Keep ViewModel dependencies minimal

❌ **Don't:**
- Access Store directly from UI
- Hold references to GameState outside of LiveData observation
- Modify game state from UI code
- Create multiple GameViewModels
- Forget to call actions for turn progression

## Testing

### Unit Testing with ViewModel

```java
public class GameViewModelTest {
    
    private GameViewModel viewModel;
    
    @Before
    public void setUp() {
        viewModel = new GameViewModel();
        viewModel.initializeGame();
    }
    
    @Test
    public void testPlayerDrawCards() {
        GameState state = viewModel.getCurrentGameState();
        int initialHandSize = state.getCurrentPlayer().hand.size();
        
        viewModel.playerDrawCards(0, 5);
        
        GameState newState = viewModel.getCurrentGameState();
        assertEquals(initialHandSize + 5, newState.getCurrentPlayer().hand.size());
    }
}
```

## Related Documentation

- **REDUX_ARCHITECTURE.md** - Core Redux pattern
- **REDUX_IMPLEMENTATION_SUMMARY.md** - All Redux components
- **QuickStart.java** - Redux initialization examples
