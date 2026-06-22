package com.example.fantastiqa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Card as M3Card
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.window.DialogProperties
import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.TowerName
import com.example.fantastiqa.redux.*
import com.example.fantastiqa.redux.actions.*
import com.example.fantastiqa.redux.middleware.ComputerPlayer2Strategy
import com.example.fantastiqa.redux.middleware.ComputerPlayerMiddleware
import com.example.fantastiqa.redux.utils.GameInitializer
import androidx.lifecycle.lifecycleScope
import com.example.fantastiqa.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BoardGameScreen()
                }
            }
        }
    }
}

@Composable
fun BoardGameScreen() {
    val activity = androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity
    var playerType by remember { mutableStateOf<GameInitializer.PlayerType?>(null) }

    if (playerType == null) {
        PlayerSelectionDialog(onSelected = { playerType = it })
        return
    }

    val store = remember(playerType) {
        val s = Store(GameInitializer.initializeNewGame(playerType!!))
        ComputerPlayerMiddleware(
            s,
            ComputerPlayer2Strategy(),
            com.example.fantastiqa.redux.middleware.RogueMouserStrategy(),
            activity.lifecycleScope
        )
        s
    }
    val state by store.stateFlow.collectAsState()
    
    Box {
        BoardGameContent(state = state, onAction = { store.dispatch(it) })
        // ... rest of the existing code
        
        if (state.isGameOver) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "GAME OVER",
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White
                        )
                        val winner = state.players.maxByOrNull { it.totalCardCount() }
                        Text(
                            "${winner?.name ?: "Unknown"} Wins!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Yellow
                        )
                        Button(
                            onClick = { /* Restart logic could go here */ },
                            modifier = Modifier.padding(top = 20.dp)
                        ) {
                            Text("New Game")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardGameContent(state: GameState, onAction: (Action) -> Unit) {
    // UI Local selections (keeping some local for non-permanent board/road focus)
    var selectedRegion by remember { mutableStateOf<Region?>(null) }
    var bitterBrewPending by remember { mutableStateOf(false) }

    // Show BITTER_BREW card picker when triggered
    if (bitterBrewPending) {
        val player = state.currentPlayer
        if (player != null) {
            val candidates = (player.hand + player.deck.discardPile)
                .filter { it !in player.storage && it !in state.selectedCards }
                .distinctBy { it.id }
            BitterBrewDialog(
                candidates = candidates,
                onConfirm = { targetCard ->
                    bitterBrewPending = false
                    onAction(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(targetCard)))
                    onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null))
                },
                onDismiss = { bitterBrewPending = false }
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Fantastiqa - Redux Board") }) }
    ) { padding ->
        val isTowerPhase = state.gamePhase == GameState.GamePhase.TOWER
        val isSummoningPhase = state.gamePhase == GameState.GamePhase.SUMMONING
        val isSelectionActive = (isTowerPhase || isSummoningPhase) && state.towerDrawnCards.isNotEmpty()

        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            // Top: Status Bar (stretches all the way across)
            if (isSelectionActive) {
                CardSelectionOverlay(
                    title = if (isSummoningPhase) "Summoning Selection (Pick 1)" else "Tower Selection - ${state.playerPositions[state.currentPlayer?.name]?.tower ?: TowerName.QUEST}",
                    cards = state.towerDrawnCards,
                    minSelections = if (isSummoningPhase) 1 else 0,
                    playerGems = state.currentPlayer?.gems ?: 0,
                    costSelector = if (isSummoningPhase) null else { card ->
                        when (state.playerPositions[state.currentPlayer?.name]?.tower) {
                            TowerName.BAZAAR -> 3
                            TowerName.ARTIFACT -> (card as? ArtifactCard)?.cost ?: (card as? Artifact)?.cost ?: 0
                            else -> 0
                        }
                    },
                    onConfirm = { selected: List<Card> ->
                        if (isSummoningPhase && selected.size > 1) {
                            // Only 1 allowed for summoning, but minSelections should handle it if set to exactly 1? 
                            // Actually user said "selects 1". I should probably enforce exactly 1.
                            onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.RESOLVE_TOWER_DRAW, selected.take(1)))
                        } else {
                            onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.RESOLVE_TOWER_DRAW, selected))
                        }
                    }
                )
            } else {
                GameHeader(state, state.selectedRoad, onAction)
            }

            BoardLayout(
                state = state,
                selectedRegion = selectedRegion,
                selectedRoad = state.selectedRoad,
                onRegionSelect = { region ->
                    if (!isSelectionActive) {
                        val currentRegion = state.playerPositions[state.currentPlayer?.name]
                        val hasRoad = currentRegion?.let { state.board?.getRoad(it, region) } != null

                        if (state.selectedCards.isEmpty()) {
                            onAction(MoveAction(state.currentPlayerIndex, region, MoveType.FLYING_CARPET))
                        } else if (hasRoad) {
                            onAction(MoveAction(state.currentPlayerIndex, region, MoveType.ADJACENT, useAbility = true))
                        }

                        selectedRegion = region
                        onAction(SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, null, null, null))
                        onAction(QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, null, emptyList()))
                    }
                },
                onRoadSelect = { road ->
                    if (!isSelectionActive) {
                        val currentRegion = state.playerPositions[state.currentPlayer?.name]
                        val destination = currentRegion?.let { start ->
                            state.board?.getAdjacentAreas(start)?.find { it.first == road }?.second
                        }

                        if (destination != null && (state.selectedCards.isNotEmpty() || road.creature == null)) {
                            onAction(MoveAction(state.currentPlayerIndex, destination, MoveType.ADJACENT, useAbility = false))
                        } else {
                            onAction(SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, road, null, null))
                            selectedRegion = null
                        }
                    }
                }
            )
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Left Side: The Board
                Column(modifier = Modifier.weight(2.5f).padding(8.dp)) {
                    QuestsSection(
                        quests = state.board?.quests ?: emptyList(),
                        player = state.currentPlayer,
                        selectedQuest = state.selectedQuest,
                        onQuestSelect = { quest ->
                            if (!isSelectionActive) {
                                onAction(QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, quest, emptyList()))
                                selectedRegion = null
                                onAction(SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, null, null, null))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    StorageSection(
                        cards = state.currentPlayer?.storage ?: emptyList(),
                        selectedCards = state.selectedCards.filterNotNull(),
                        onCardSelect = { 
                            if (!isSelectionActive) {
                                onAction(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(it))) 
                            }
                        }
                    )
                }

                // Right Side: Actions
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(8.dp)
                        .background(Color.DarkGray.copy(alpha = 0.05f))
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    ActionControls(
                        state = state,
                        selectedRegion = selectedRegion,
                        selectedCards = state.selectedCards,
                        selectedQuest = state.selectedQuest,
                        towerMenuOpen = state.towerMenuOpen,
                        onTowerMenuToggle = { onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.SET_TOWER_MENU, it)) },
                        onRegionClear = { selectedRegion = null },
                        onCardClear = { /* Handled by Redux */ },
                        onRoadClear = { onAction(SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, null, null, null)) },
                        onAction = { if (!isSelectionActive) onAction(it) },
                        onTowerDraw = {
                            onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.START_TOWER_DRAW, null))
                        },
                        onBitterBrew = { bitterBrewPending = true }
                    )
                }
            }


            // Bottom: Hand Section (stretches all the way across)
            HandSection(
                cards = state.currentPlayer?.hand ?: emptyList(),
                selectedCards = state.selectedCards.filterNotNull(),
                onCardSelect = { 
                    if (!isSelectionActive) {
                        onAction(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(it))) 
                    }
                }
            )
        }
    }
}

@Composable
fun CardSelectionOverlay(
    title: String,
    cards: List<Card>,
    minSelections: Int = 0,
    playerGems: Int = 0,
    costSelector: ((Card) -> Int)? = null,
    onConfirm: (List<Card>) -> Unit
) {
    var selectedCards by remember { mutableStateOf(setOf<Card>()) }
    var currentGems by remember { mutableStateOf(playerGems) }

    M3Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (costSelector != null) {
                        Text("Gems: $currentGems", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(16.dp))
                    }
                    Button(
                        onClick = { onConfirm(selectedCards.toList()) },
                        enabled = selectedCards.size >= minSelections,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("Confirm (${selectedCards.size})")
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(cards) { card ->
                    val cost = costSelector?.invoke(card) ?: 0
                    val isSelected = selectedCards.contains(card)
                    val canAfford = isSelected || currentGems >= cost

                    Box(modifier = Modifier.width(90.dp)) {
                        HandCard(
                            card = card,
                            isSelected = isSelected,
                            modifier = Modifier.fillMaxSize(),
                            onClick = {
                                if (isSelected) {
                                    selectedCards = selectedCards - card
                                    currentGems += cost
                                } else if (canAfford) {
                                    selectedCards = selectedCards + card
                                    currentGems -= cost
                                }
                            }
                        )
                        
                        if (!canAfford && !isSelected) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Gray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestsSection(quests: List<Quest?>, player: Player? = null, selectedQuest: Quest? = null, onQuestSelect: (Quest) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Board Quests", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            quests.forEach { quest ->
                if (quest != null) {
                    QuestCard(
                        quest = quest, 
                        isSelected = selectedQuest == quest,
                        modifier = Modifier.weight(1f).height(110.dp),
                        onClick = { onQuestSelect(quest) }
                    )
                } else {
                    M3Card(modifier = Modifier.weight(1f).height(110.dp)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Empty", color = Color.Gray)
                        }
                    }
                }
            }
        }
        
        if (player != null) {
            Spacer(Modifier.height(12.dp))
            Text("Your Quests", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (player.quests.isEmpty()) {
                    Text("No quests", fontSize = 10.sp, color = Color.Gray)
                } else {
                    player.quests.forEach { quest ->
                        if (quest is Quest) {
                            QuestCard(
                                quest = quest,
                                isSelected = selectedQuest == quest,
                                modifier = Modifier.height(110.dp).weight(1f).border(2.dp, if (selectedQuest == quest) Color.Red else Color.Black, RoundedCornerShape(8.dp)),
                                onClick = { onQuestSelect(quest) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* DetailsSection removed */

@Composable
fun BoardLayout(
    state: GameState,
    selectedRegion: Region?,
    selectedRoad: Road?,
    onRegionSelect: (Region) -> Unit,
    onRoadSelect: (Road) -> Unit
) {
    val regions = state.board?.regions() ?: emptyList()
    // Mapping to 3x2 grid: Top (0,1,2), Bottom (5,4,3) to match hexagon loop
    val topRow = regions.filterIndexed { i, _ -> i in 0..2 }
    val bottomRow = listOfNotNull(regions.getOrNull(5), regions.getOrNull(4), regions.getOrNull(3))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Row Regions and horizontal roads
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            topRow.forEachIndexed { index, region ->
                RegionCell(
                    region = region,
                    state = state,
                    isSelected = selectedRegion == region,
                    modifier = Modifier.weight(1f),
                    onClick = { onRegionSelect(region) }
                )
                if (index < topRow.size - 1) {
                    val road = state.board?.getRoad(region, topRow[index + 1])
                    val isSelected = selectedRoad == road || state.selectedRoads.any { it.id == road?.id }
                    RoadCell(road, isVertical = false, modifier = Modifier.weight(0.6f), isSelected = isSelected) {
                        road?.let { onRoadSelect(it) }
                    }
                }
            }
        }

        // Vertical roads between rows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            topRow.forEachIndexed { index, topRegion ->
                val bottomRegion = bottomRow.getOrNull(index)
                if (bottomRegion != null) {
                    val road = state.board?.getRoad(topRegion, bottomRegion)
                    val isSelected = selectedRoad == road || state.selectedRoads.any { it.id == road?.id }
                    RoadCell(road, isVertical = true, modifier = Modifier.weight(1f), isSelected = isSelected) {
                        road?.let { onRoadSelect(it) }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                // Add spacer to align with horizontal roads
                if (index < topRow.size - 1) {
                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }
        }

        // Bottom Row Regions and horizontal roads
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomRow.forEachIndexed { index, region ->
                RegionCell(
                    region = region,
                    state = state,
                    isSelected = selectedRegion == region,
                    modifier = Modifier.weight(1f),
                    onClick = { onRegionSelect(region) }
                )
                if (index < bottomRow.size - 1) {
                    val road = state.board?.getRoad(region, bottomRow[index + 1])
                    val isSelected = selectedRoad == road || state.selectedRoads.any { it.id == road?.id }
                    RoadCell(road, isVertical = false, modifier = Modifier.weight(0.5f), isSelected = isSelected) {
                        road?.let { onRoadSelect(it) }
                    }
                }
            }
        }
    }
}

@Composable
fun RoadCell(road: Road?, isVertical: Boolean, modifier: Modifier = Modifier, isSelected: Boolean = false, onClick: () -> Unit) {
    val sizeModifier = if (isVertical) {
        Modifier.height(55.dp).width(60.dp)
    } else {
        Modifier.height(55.dp)
    }

    M3Card(
        modifier = modifier.then(sizeModifier).padding(2.dp).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current
        ) { onClick() }
            .border(2.dp, if (isSelected) Color.Red else Color.Transparent, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(2.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (road?.creature != null) {
                Text(
                    text = road.creature.name,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    road.creature.values.forEach { symbol ->
                        Image(
                            painter = painterResource(id = getSymbolDrawable(symbol)),
                            contentDescription = symbol.name,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Subdued By and Gem on next row
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = getSymbolDrawable(road.creature.subduedBy)),
                            contentDescription = "Subdued by ${road.creature.subduedBy.name}",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (road.gem) {
                        Image(
                            painter = painterResource(id = R.drawable.diamond),
                            contentDescription = "Gem",
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            } else {
                Text("Road", fontSize = 8.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RegionCell(region: Region, state: GameState, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val currentPlayerAtRegion = state.playerPositions[state.currentPlayer?.name] == region

    M3Card(
        modifier = modifier.aspectRatio(1.6f).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current
        ) { onClick() }
            .border(2.dp, if (isSelected) Color.Blue else Color.Transparent, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = if (currentPlayerAtRegion) Color(0xFFBBDEFB) else Color.White)
    ) {
        Column(Modifier.padding(4.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(region.name.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(region.tower.name, fontSize = 10.sp, color = Color.Gray)
            
            Spacer(Modifier.weight(1f))
            
            // Show all players at this region
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                state.players.forEachIndexed { index, player ->
                    if (state.playerPositions[player.name] == region) {
                        val playerColor = when (index) {
                            0 -> Color.Red
                            1 -> Color.Green
                            else -> Color.Gray
                        }
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Player ${index + 1}",
                            tint = playerColor,
                            modifier = Modifier.size(24.dp) // 15.dp * 1.6 = 24.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActionControls(
    state: GameState,
    selectedRegion: Region?,
    selectedCards: List<Card?>,
    selectedQuest: Quest?,
    towerMenuOpen: Boolean,
    onTowerMenuToggle: (Boolean) -> Unit,
    onRegionClear: () -> Unit,
    onCardClear: () -> Unit,
    onRoadClear: () -> Unit,
    onAction: (Action) -> Unit,
    onTowerDraw: () -> Unit,
    onBitterBrew: () -> Unit
) {
    Spacer(Modifier.height(8.dp))

    val currentRegion = state.playerPositions[state.currentPlayer?.name]
    val towerDestination = currentRegion?.let { state.board?.getTowerMatch(it) }

    val phase = state.gamePhase
    val isSubduePhase = phase == GameState.GamePhase.SUBDUE
    val isTowerPhase = phase == GameState.GamePhase.TOWER
    val isQuestPhase = phase == GameState.GamePhase.QUEST
    val isWardrobePhase = phase == GameState.GamePhase.WARDROBE
    val isTurnActionPhase = isSubduePhase || isTowerPhase || isQuestPhase || isWardrobePhase
    val canFreeAction = phase == GameState.GamePhase.OPEN || phase == GameState.GamePhase.DISCARD_OPEN

    if (!towerMenuOpen) {
        if (!isTurnActionPhase) {
            Button(
                onClick = { onTowerMenuToggle(true) },
                enabled = phase == GameState.GamePhase.OPEN,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Tower") }
        }

        val playerInQuestRegion = currentRegion?.name == selectedQuest?.land
        val isPlayerQuest = state.currentPlayer?.quests?.any { it.id == selectedQuest?.id } == true
        val isBoardQuest = state.board?.quests?.any { it?.id == selectedQuest?.id } == true

        val questComplete = if (isPlayerQuest) {
            (selectedQuest as? PlayerQuest)?.let { it.getFulfilledIndices().size == it.getRequirements().size } == true
        } else if (isBoardQuest) {
            selectedQuest?.let { quest ->
                val reqs = quest.getRequirements()
                val providedSymbols = selectedCards.filterIsInstance<CreatureCard>().flatMap { it.values }.toMutableList()
                reqs.all { req -> providedSymbols.remove(req) }
            } == true
        } else false

        if (!isTurnActionPhase) {
            Button(
                onClick = {
                    onAction(QuestAction(QuestAction.ActionType.COMPLETE_QUEST, state.currentPlayerIndex, selectedQuest!!, emptyList()))
                },
                enabled = phase == GameState.GamePhase.OPEN && playerInQuestRegion && questComplete,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Quest") }
        }

        val isBitterBrew = selectedCards.size == 1 &&
            (selectedCards[0] as? CreatureCard)?.ability == Ability.BITTER_BREW
        val isLookingGlassCombo = selectedCards.size == 2 &&
                selectedCards.any { (it as? Artifact)?.ability == Ability.LOOKING_GLASS } &&
                selectedCards.any { it is CreatureCard }

        Button(
            onClick = {
                if (isBitterBrew) onBitterBrew()
                else onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null))
            },
            enabled = canFreeAction && (selectedCards.size == 1 || isLookingGlassCombo),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Ability") }

        Button(
            onClick = { onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.STORE_FOR_BOARD_QUEST, null)) },
            enabled = canFreeAction && selectedCards.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Storage") }

        Button(
            onClick = {
                onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.DISCARD_FROM_HAND, state.selectedCards.filterNotNull()))
            },
            enabled = canFreeAction && selectedCards.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Discard") }
    } else {
        // Tower Sub-menu
        Button(
            onClick = {
                towerDestination?.let {
                    onAction(MoveAction(state.currentPlayerIndex, it, MoveType.TOWER_KEY))
                    onRegionClear()
                    onCardClear()
                    onRoadClear()
                    onTowerMenuToggle(false)
                }
            },
            enabled = phase == GameState.GamePhase.OPEN && towerDestination != null && (state.currentPlayer?.gems ?: 0) >= 2,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Teleport") }

        val gems = state.currentPlayer?.gems ?: 0
        val releasableCards = state.currentPlayer?.hand?.filter { it is CreatureCard && it.name != "Peaceful Dragon" } ?: emptyList()
        val canRelease = selectedCards.isNotEmpty() && selectedCards.size <= minOf(3, gems) && selectedCards.all { it in releasableCards }
        
        Button(
            onClick = {
                // Costs 1 gem per card
                onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.RELEASE_CARDS, state.selectedCards.filterNotNull()))
                onTowerMenuToggle(false)
            },
            enabled = canFreeAction && canRelease,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Release") }

        Button(
            onClick = { 
                onTowerDraw()
                onTowerMenuToggle(false)
            },
            enabled = currentRegion?.tower != null && (phase == GameState.GamePhase.OPEN || phase == GameState.GamePhase.TOWER),
            modifier = Modifier.fillMaxWidth()
        ) { 
            val plusCount = state.selectedCards.count { it is CreatureCard && it.ability == Ability.PLUS_CARD }
            Text(if (plusCount > 0) "Draw (+$plusCount)" else "Draw") 
        }

        Button(
            onClick = { onTowerMenuToggle(false) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) { Text("Exit") }
    }

    Spacer(Modifier.height(16.dp))

    Button(
        onClick = {
            onRegionClear()
            onCardClear()
            onRoadClear()
            if (isSubduePhase) {
                onAction(TurnAction(TurnAction.ActionType.DONE_ADVENTURING))
            } else {
                onAction(TurnAction(TurnAction.ActionType.NEXT_TURN))
            }
        },
        enabled = phase == GameState.GamePhase.OPEN || phase == GameState.GamePhase.DISCARD_OPEN || isSubduePhase,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSubduePhase) Color(0xFF2196F3) else Color(0xFF4CAF50)),
        modifier = Modifier.fillMaxWidth()
    ) { 
        Text(if (isSubduePhase) "Done Adventuring" else "End Turn") 
    }
}

@Composable
fun PlayerSelectionDialog(onSelected: (GameInitializer.PlayerType) -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Choose Player 2") },
        text = {
            Column {
                Button(
                    onClick = { onSelected(GameInitializer.PlayerType.HUMAN) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { Text("Human Player") }
                Button(
                    onClick = { onSelected(GameInitializer.PlayerType.COMPUTER) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { Text("Standard AI (Computer)") }
                Button(
                    onClick = { onSelected(GameInitializer.PlayerType.MOUSER) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) { Text("Rogue Mouser AI") }
            }
        },
        confirmButton = { },
        dismissButton = { }
    )
}

/** Dialog that lets the current player pick a card from hand+discard to give to the opponent. */
@Composable
fun BitterBrewDialog(candidates: List<Card>, onConfirm: (Card) -> Unit, onDismiss: () -> Unit) {
    var picked by remember { mutableStateOf<Card?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.9f),
        title = { Text("Bitter Brew — choose a card to give away") },
        text = {
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(candidates) { card ->
                    HandCard(
                        card = card,
                        isSelected = picked == card,
                        modifier = Modifier.width(90.dp),
                        onClick = { picked = if (picked == card) null else card }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { picked?.let(onConfirm) }, enabled = picked != null) {
                Text("Give Away")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = false, device = "spec:parent=pixel_9,orientation=portrait")
@Composable
fun BoardGameScreenPreview() {
    BoardGameContent(
        state = GameInitializer.initializeNewGame(),
        onAction = {}
    )
}
