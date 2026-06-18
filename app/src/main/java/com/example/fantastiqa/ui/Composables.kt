package com.example.fantastiqa.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Card as M3Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fantastiqa.R
import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.redux.Action
import com.example.fantastiqa.redux.GameState
import com.example.fantastiqa.redux.actions.PlayerAction
import com.example.fantastiqa.redux.actions.QuestAction

@Composable
fun GameHeader(state: GameState, selectedRoad: Road? = null, onAction: (Action) -> Unit = {}) {
    val player = state.currentPlayer ?: return
    M3Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedRoad?.creature != null) {
                        val creature = selectedRoad.creature
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = creature._name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.width(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                creature.values.forEach { symbol ->
                                    Image(
                                        painter = painterResource(id = getSymbolDrawable(symbol)),
                                        contentDescription = symbol.name,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "Current Player: ${player.name}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Phase: ${state.gamePhase}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatItem(label = "Turn", value = state.turnCount.toString())
                    StatItem(label = "Deck", value = player.deck.size().toString())
                    StatItem(label = "Discard", value = player.deck.discardSize().toString())
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Gems", value = player.gems.toString())
                StatItem(label = "VPs", value = "${player.vps}/${state.vpGoal}")
                StatItem(label = "Trophies", value = player.trophies.toString())
                StatItem(label = "Carpets", value = player.flyingCarpets.toString())
                StatItem(
                    label = "Tents", 
                    value = player.tents.toString(),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current
                    ) {
                        onAction(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_TENT, null))
                    }
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun HandSection(cards: List<Card>, selectedCards: List<Card>, onCardSelect: (Card) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Your Hand (${cards.size})", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp))
        Spacer(modifier = Modifier.height(2.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 4.dp)
        ) {
            items(cards) { card ->
                HandCard(
                    card = card,
                    isSelected = selectedCards.contains(card),
                    modifier = Modifier.width(80.dp),
                    onClick = { onCardSelect(card) }
                )
            }
        }
    }
}

@Composable
fun StorageSection(cards: List<Card>, selectedCards: List<Card>, onCardSelect: (Card) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Storage (${cards.size})", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp))
        Spacer(modifier = Modifier.height(2.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 4.dp)
        ) {
            items(cards) { card ->
                HandCard(
                    card = card,
                    isSelected = selectedCards.contains(card),
                    modifier = Modifier.width(40.dp),
                    showAbility = false,
                    onClick = { onCardSelect(card) }
                )
            }
        }
    }
}

@Composable
fun QuestCard(quest: Quest, modifier: Modifier = Modifier, isSelected: Boolean = false, onClick: (() -> Unit)? = null) {
    val playerQuest = quest as? PlayerQuest
    val fulfilled = playerQuest?.getFulfilledIndices() ?: emptySet()
    val requirements = quest.getRequirements()
    
    M3Card(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                enabled = onClick != null
            ) { onClick?.invoke() }
            .border(
                if (isSelected) 3.dp else 1.dp,
                if (isSelected) Color.Red else Color.LightGray,
                RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFEBEE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = quest.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            // Target Region Icon/Text
            Text(text = quest.land.name, fontSize = 8.sp, fontWeight = FontWeight.Medium)
            
            // Requirements Row
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                requirements.forEachIndexed { index, symbol ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 0.5.dp)
                            .then(
                                if (fulfilled.contains(index)) {
                                    Modifier.border(1.5.dp, Color.Blue, RoundedCornerShape(10.dp))
                                } else Modifier
                            )
                            .padding(1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = getSymbolDrawable(symbol)),
                            contentDescription = "Requirement",
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }

            // Rewards Section
            Row(
                modifier = Modifier.padding(top = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (quest.vps > 0) {
                    repeat(quest.vps) {
                        Image(
                            painter = painterResource(id = R.drawable.cup),
                            contentDescription = "VP",
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
                if (quest.gems > 0) {
                    repeat(quest.gems) {
                        Image(
                            painter = painterResource(id = R.drawable.diamond),
                            contentDescription = "Gem",
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
            
            // Stored Cards Names (Mini)
            if (playerQuest?.stored?.isNotEmpty() == true) {
                Text(
                    text = playerQuest.stored.joinToString(", ") { it.name },
                    fontSize = 6.sp,
                    maxLines = 1,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun HandCard(card: Card, isSelected: Boolean, modifier: Modifier = Modifier, showAbility: Boolean = true, onClick: () -> Unit) {
    val commonModifier = modifier
        .aspectRatio(1.15f) // Narrower (80dp vs 90dp) and Shorter (due to fixed aspect ratio)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current
        ) { onClick() }

    if (card is Quest) {
        QuestCard(quest = card, isSelected = isSelected, modifier = commonModifier, onClick = onClick)
        return
    }

    M3Card(
        modifier = commonModifier
            .border(3.dp, if (isSelected) Color.Red else Color.Transparent, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFEBEE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(2.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.name,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                if (card is CreatureCard) {
                    Row(
                        modifier = Modifier.padding(top = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Value symbols
                        card.values.forEach { symbol ->
                            Image(
                                painter = painterResource(id = getSymbolDrawable(symbol)),
                                contentDescription = symbol.name,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else if (card is Artifact) {
                    // Show cost for Artifacts
                    Row(
                        modifier = Modifier.padding(top = 1.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Cost: ${card.cost}", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        Image(
                            painter = painterResource(id = R.drawable.diamond),
                            contentDescription = "Gem Cost",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Ability Icon at the bottom right
            val ability = when (card) {
                is CreatureCard -> card.ability
                is Artifact -> card.ability
                else -> Ability.NONE
            }

            if (showAbility && ability != Ability.NONE) {
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp)
                ) {
                    AbilityIcon(ability)
                }
            }
        }
    }
}

@Composable
fun AbilityIcon(ability: Ability) {
    when (ability) {
        Ability.TOWER_KEY -> Image(painter = painterResource(id = R.drawable.baseline_vpn_key_black_18dp), contentDescription = "Tower Key", modifier = Modifier.size(25.dp))
        Ability.PLUS_CARD -> Image(painter = painterResource(id = R.drawable.baseline_library_add_black_18dp), contentDescription = "Plus Card", modifier = Modifier.size(25.dp))
        Ability.MAGIC_CARPET -> Image(painter = painterResource(id = R.drawable.flyingc), contentDescription = "Flying Carpet", modifier = Modifier.size(25.dp))
        Ability.GEM -> Image(painter = painterResource(id = R.drawable.diamond), contentDescription = "Gem", modifier = Modifier.size(25.dp))
        Ability.DRAGON -> Image(painter = painterResource(id = R.drawable.stars), contentDescription = "Dragon", modifier = Modifier.size(25.dp))
        Ability.LOOKING_GLASS -> Image(painter = painterResource(id = R.drawable.halloween), contentDescription = "Looking Glass", modifier = Modifier.size(25.dp))
        else -> {}
    }
}

fun getCardDrawable(card: Card): Int {
    return when (card) {
        is CreatureCard -> {
            // Mapping based on creature name.
            when (card.name) {
                "Knight" -> R.drawable.ic_launcher_background
                "BabyDragon" -> R.drawable.fire
                "Witch" -> R.drawable.broom
                "Dog" -> R.drawable.ic_launcher_background
                "Peaceful Dragon" -> R.drawable.stars
                else -> R.drawable.stars
            }
        }
        is ArtifactCard, is Artifact -> R.drawable.artifact
        is Quest -> R.drawable.quest
        else -> R.drawable.stars
    }
}

fun getSymbolDrawable(symbol: Symbol): Int {
    return when (symbol) {
        Symbol.SWORD -> R.drawable.sword
        Symbol.WAND -> R.drawable.wand
        Symbol.BAT -> R.drawable.club
        Symbol.HELMET -> R.drawable.helmet
        Symbol.NET -> R.drawable.net
        Symbol.TOOTH -> R.drawable.tooth
        Symbol.BROOM -> R.drawable.broom
        Symbol.WATER -> R.drawable.water
        Symbol.FIRE -> R.drawable.fire
        else -> R.drawable.ic_launcher_background
    }
}

@Composable
fun LogConsole(logs: List<String>) {
    Text("Game Logs", fontWeight = FontWeight.Bold)
    Box(Modifier.fillMaxSize().background(Color.Black).padding(4.dp)) {
        LazyColumn {
            items(logs.reversed()) { log ->
                Text("> $log", color = Color.Green, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
        }
    }
}
