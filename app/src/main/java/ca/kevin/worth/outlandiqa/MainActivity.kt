package ca.kevin.worth.outlandiqa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mock data for the board grid
        val mockSpaces = mutableListOf<BoardSpace>()
        var idCounter = 1
        for (r in 0..2) {
            for (c in 0..1) {
                mockSpaces.add(BoardSpace(id = idCounter++, row = r, col = c))
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BoardGameScreen(spaces = mockSpaces)
                }
            }
        }
    }
}

/**
 * Jetpack Compose visual scaffolding matching 3 mandatory functional blocks:
 * 1. Board Area (3x2 Grid layout)
 * 2. Hand Area (6-card inventory deck aligned smoothly)
 * 3. Action Area (Control actions for Game State transitions)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardGameScreen(
    spaces: List<BoardSpace>,
    modifier: Modifier = Modifier
) {
    // Scaffold UI States representing real-time interactions
    var selectedSpaceId by remember { mutableStateOf<Int?>(null) }
    var selectedRoadId by remember { mutableStateOf<Int?>(null) }
    var selectedCardIndex by remember { mutableStateOf<Int?>(null) }
    var currentTurn by remember { mutableStateOf(1) }
    var appFeedbackLog by remember { mutableStateOf("Match commenced. Player 1's Turn.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grid Board Game Scaffold", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Live Status indicator
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Turn #$currentTurn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = appFeedbackLog,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f).padding(start = 12.dp)
                    )
                }
            }

            // SECTION 1: BOARD AREA (Containers holding the 6 grid tiles nested inside nested loops)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row Array index loops for Row 0, Row 1, and Row 2 (3x2 Grid)
                    for (r in 0..2) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            for (c in 0..1) {
                                val currentSpace = spaces.find { it.row == r && it.col == c }
                                if (currentSpace != null) {
                                    SpaceGridCard(
                                        space = currentSpace,
                                        isSelected = selectedSpaceId == currentSpace.id,
                                        onClick = {
                                            selectedSpaceId = currentSpace.id
                                            selectedRoadId = null
                                            appFeedbackLog = "Focusing: ${currentSpace.id}"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ROAD CREATURE ATTRIBUTE SPECIAL DISPLAY PORT
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Road Creature Attributes (Bidirectional Guardian info)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Selected road lookup to display creature attributes
                    val activeRoad = selectedRoadId?.let { rid ->
                        spaces.flatMap { it.roads }.find { it.id == rid }
                    }

                    if (activeRoad != null) {
                        Column {
                            val guardian = activeRoad.creature
                            if (guardian != null) {
                                Text(
                                    text = "🛣️ ${activeRoad.name} Guardian: ${guardian.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("⚔️ Attack: ${guardian.values.joinToString("|")}", style = MaterialTheme.typography.bodySmall)
                                    Text("🛡️ Weakness (SubduedBy): ${guardian.subduedBy}", style = MaterialTheme.typography.bodySmall)
                                    Text("💎 Gem: ${if (guardian.gem) "Yes" else "No"}", style = MaterialTheme.typography.bodySmall)
                                    Text("✨ Ability: [Blank]", style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                Text(
                                    text = "🛣️ ${activeRoad.name} Guardian: [Cleared] 💨",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Tap any road in the game to inspect of all its CreatureCard attributes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // SECTION 2: HAND AREA (6 distinct tactical deck items generated horizontally)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Tactical Hand Index (6 Cards)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val mockHand = listOf(
                        "[object Object]",
                        "[object Object]",
                        "[object Object]",
                        "[object Object]",
                        "[object Object]",
                        "[object Object]"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(mockHand) { index, cardTitle ->
                            HandCardItem(
                                title = cardTitle,
                                index = index,
                                isSelected = selectedCardIndex == index,
                                onClick = {
                                    selectedCardIndex = index
                                    appFeedbackLog = "Ready to play card: ${cardTitle}"
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // SECTION 3: ACTION AREA (Standard high action command console layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        selectedSpaceId = null
                        selectedRoadId = null
                        selectedCardIndex = null
                        currentTurn++
                        appFeedbackLog = "Round passed to next challenger"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("End Turn")
                }

                OutlinedButton(
                    onClick = {
                        appFeedbackLog = "Player declared Pass"
                        currentTurn++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pass")
                }
            }
        }
    }
}

@Composable
fun SpaceGridCard(
    space: BoardSpace,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 125.dp, height = 85.dp)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ID: ${space.id}  (${space.row},${space.col})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                //text = space.name.substringBefore(" ("),
                text = "${space.id}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${space.roads.size} Bidirectional Connections",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun HandCardItem(
    title: String,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 95.dp, height = 100.dp)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "#${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                    .align(Alignment.End)
            )
        }
    }
}
