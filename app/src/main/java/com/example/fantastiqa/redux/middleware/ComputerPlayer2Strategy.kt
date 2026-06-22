package com.example.fantastiqa.redux.middleware

import com.example.fantastiqa.gameState.*
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.redux.Action
import com.example.fantastiqa.redux.GameEngine
import com.example.fantastiqa.redux.GameState
import com.example.fantastiqa.redux.actions.*
import java.util.PriorityQueue

/**
 * ComputerPlayer2Strategy implements the Turn Planning Algorithm described in CompPlayer2.txt.
 * It uses an Evaluation Scan (simulation) to plan an entire turn sequence.
 */
class ComputerPlayer2Strategy : ComputerPlayerStrategy {
    private val engine = GameEngine()

    override fun evaluateNextAction(state: GameState): Action? {
        val player = state.currentPlayer ?: return null
        if (!player.isComputer) return null

        // 1. Plan from a completely "clean" state (nothing selected)
        val cleanState = state.copy(
            selectedCards = emptyList(),
            selectedQuest = null,
            selectedRoad = null
        )
        val plan = planEntireTurn(cleanState)
        if (plan.isEmpty()) return null

        // 2. Reconciliation: find the first action in the plan whose *result* is not yet reflected in current state.
        var simState = cleanState
        for (action in plan) {
            val prevState = simState
            simState = engine.reduce(simState, action)

            // If current state already reflects the outcome of this action (including game state changes),
            // move to the next action in the plan.
            if (isAtExpectedState(state, simState)) continue

            // If the LOGIC (board, hand, resources) already reflects the result, skip this action.
            if (isAtLogicState(state, simState)) continue

            // We haven't reached simState yet.
            // Check if we are at the state required to perform this action.
            if (isAtExpectedState(state, prevState)) {
                return action
            }

            // Otherwise, we need to move selection toward prevState.
            return getCorrectionAction(state, prevState)
        }

        return null
    }

    private fun isAtExpectedState(current: GameState, expected: GameState): Boolean {
        // 1. Compare selection
        val cards1 = current.selectedCards.filterNotNull().map { it.id }.toSet()
        val cards2 = expected.selectedCards.filterNotNull().map { it.id }.toSet()
        if (cards1 != cards2) return false
        if (current.selectedQuest?.id != expected.selectedQuest?.id) return false
        if (current.selectedRoad?.id != expected.selectedRoad?.id) return false

        // 2. Compare logic state
        return isAtLogicState(current, expected)
    }

    private fun isAtLogicState(current: GameState, expected: GameState): Boolean {
        val p1 = current.currentPlayer ?: return expected.currentPlayer == null
        val p2 = expected.currentPlayer ?: return false

        // Hand and Storage cards
        if (p1.hand.map { it.id }.toSet() != p2.hand.map { it.id }.toSet()) return false
        if (p1.storage.map { it.id }.toSet() != p2.storage.map { it.id }.toSet()) return false

        // Personal Quest Storage
        if (p1.quests.size != p2.quests.size) return false
        val expectedQuests = p2.quests.associateBy { it.id }
        for (q1 in p1.quests) {
            val q2 = expectedQuests[q1.id] ?: return false
            if (q1.stored.map { it.id }.toSet() != q2.stored.map { it.id }.toSet()) return false
        }

        // Resources, Position and Phase
        if (p1.gems != p2.gems || p1.vps != p2.vps || p1.trophies != p2.trophies) return false
        if (current.playerPositions[p1.name] != expected.playerPositions[p2.name]) return false
        if (current.gamePhase != expected.gamePhase) return false

        // Board Check: compare roads (Subdue moves clear road creatures)
        val r1 = current.board?.roads() ?: emptyList()
        val r2 = expected.board?.roads() ?: emptyList()
        if (r1.size != r2.size) return false
        val roadsWithCreatures1 = r1.filter { it.creature != null }.map { it.id }.toSet()
        val roadsWithCreatures2 = r2.filter { it.creature != null }.map { it.id }.toSet()
        if (roadsWithCreatures1 != roadsWithCreatures2) return false

        return true
    }

    private fun getCorrectionAction(state: GameState, target: GameState): Action {
        val currentCards = state.selectedCards.filterNotNull().toSet()
        val targetCards = target.selectedCards.filterNotNull().toSet()

        // Unselect unwanted cards first (prioritize hand cleanup)
        val unwanted = currentCards.filter { c -> targetCards.none { it.id == c.id } }
        if (unwanted.isNotEmpty()) return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(unwanted.first()))

        // Select missing cards
        val missing = targetCards.filter { t -> currentCards.none { it.id == t.id } }
        if (missing.isNotEmpty()) return CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(missing.first()))

        // Then handle Quest selection
        if (state.selectedQuest?.id != target.selectedQuest?.id) {
            if (state.selectedQuest != null) return QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, state.selectedQuest, emptyList())
            if (target.selectedQuest != null) return QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, target.selectedQuest, emptyList())
        }

        // Finally handle Road selection
        if (state.selectedRoad?.id != target.selectedRoad?.id) {
            if (state.selectedRoad != null) return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, state.selectedRoad, null, null)
            if (target.selectedRoad != null) return SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, target.selectedRoad, null, null)
        }

        return TurnAction(TurnAction.ActionType.ADVANCE_PHASE)
    }

    fun planEntireTurn(initialState: GameState): List<Action> {
        val player = initialState.currentPlayer ?: return emptyList()
        if (!player.isComputer) return emptyList()

        val plannedActions = mutableListOf<Action>()
        var currentState = initialState

        if (initialState.gamePhase == GameState.GamePhase.OPEN) {
            val publicQuests = initialState.board?.quests?.filterNotNull()?.sortedBy { it.id } ?: emptyList()
            for (quest in publicQuests) {
                val actions = tryPlanQuest(initialState, quest)
                if (actions.isNotEmpty()) plannedActions.addAll(actions)
            }

            val personalQuests = player.quests.sortedBy { it.id }
            for (quest in personalQuests) {
                val actions = tryPlanQuest(initialState, quest)
                if (actions.isNotEmpty()) plannedActions.addAll(actions)
            }
        } else {
            return handleNonOpenPhases(initialState)
        }


        val specialAbilities = planSpecialAbilities(currentState)
        if (specialAbilities.isNotEmpty()) {
            plannedActions.addAll(specialAbilities)
            currentState = simulateActions(currentState, specialAbilities)
        }

        // * If we have at least 17 cards in our deck then do 5. before 4.
        if (player.totalCardCount() >= 17) {
            val hoarding = planResourceHoarding(currentState)
            plannedActions.addAll(hoarding)
            currentState = simulateActions(currentState, hoarding)
        }

        val subdueChainActions = planSubdueChain(currentState)
        if (subdueChainActions.isNotEmpty()) {
            plannedActions.addAll(subdueChainActions)
            currentState = simulateActions(currentState, subdueChainActions)
        }

        if (plannedActions.none { isMajorAction(it) }) {
            val towerVisit = planTowerVisit(currentState)
            if (towerVisit.isNotEmpty()) {
                plannedActions.addAll(towerVisit)
                currentState = simulateActions(currentState, towerVisit)
            }
        }
//redundant?
        val finalHoarding = planResourceHoarding(currentState)
        plannedActions.addAll(finalHoarding)
        currentState = simulateActions(currentState, finalHoarding)

        plannedActions.addAll(planResidualAndPurge(currentState))
        return plannedActions
    }

    private fun handleNonOpenPhases(state: GameState): List<Action> {
        return when (state.gamePhase) {
            GameState.GamePhase.SUBDUE -> {
                val player = state.currentPlayer ?: return emptyList()
                val currentRegion = state.playerPositions[player.name] ?: return listOf(TurnAction(TurnAction.ActionType.DONE_ADVENTURING))
                val board = state.board ?: return listOf(TurnAction(TurnAction.ActionType.DONE_ADVENTURING))
                val chain = findMaxSubdueChain(currentRegion, player.hand, board)
                if (chain.isNotEmpty()) translateChainToActions(state, currentRegion, chain.take(1))
                else listOf(TurnAction(TurnAction.ActionType.DONE_ADVENTURING))
            }
            GameState.GamePhase.TOWER, GameState.GamePhase.SUMMONING -> {
                val options = state.towerDrawnCards
                if (options.isNotEmpty()) {
                    val dragon = options.find { (it as? CreatureCard)?.ability == Ability.DRAGON }
                    val choice = dragon ?: options.sortedByDescending { (it as? CreatureCard)?.values?.size ?: 0 }.first()
                    listOf(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.RESOLVE_TOWER_DRAW, listOf(choice)))
                } else listOf(TurnAction(TurnAction.ActionType.ADVANCE_PHASE))
            }
            GameState.GamePhase.QUEST -> listOf(TurnAction(TurnAction.ActionType.ADVANCE_PHASE))
            GameState.GamePhase.DISCARD_OPEN -> planResidualAndPurge(state)
            else -> emptyList()
        }
    }

    private fun isMajorAction(action: Action): Boolean {
        return when (action) {
            is QuestAction -> action.actionType == QuestAction.ActionType.COMPLETE_QUEST
            is MoveAction -> action.moveType == MoveType.ADJACENT && !action.useAbility
            is PlayerAction -> action.actionType == PlayerAction.ActionType.START_TOWER_DRAW
            else -> false
        }
    }

    private fun tryPlanQuest(state: GameState, quest: Quest): List<Action> {
        val player = state.currentPlayer ?: return emptyList()
        val currentRegion = state.playerPositions[player.name] ?: return emptyList()

        if (currentRegion.name == quest.land) {
            val availablePool = if (quest.isPersonal) player.hand + quest.stored else player.hand + player.storage
            val combo = findFulfillmentCombo(quest, availablePool)
            if (combo != null) {
                val actions = mutableListOf<Action>()
                val selectCombo = combo.filter { it in player.hand || it in player.storage }
                if (selectCombo.isNotEmpty()) actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, selectCombo.sortedBy { it.id }))
                actions.add(QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, quest, emptyList()))
                actions.add(QuestAction(QuestAction.ActionType.COMPLETE_QUEST, state.currentPlayerIndex, quest, emptyList()))
                return actions
            }
        }

        val availablePool = if (quest.isPersonal) player.hand + quest.stored else player.hand + player.storage
        val combo = findFulfillmentCombo(quest, availablePool)

        if (combo != null) {
            val board = state.board ?: return emptyList()
            val hand = player.hand.sortedBy { it.id }.toMutableList()
            hand.removeAll(combo)
            val path = findShortestPath(currentRegion, quest.land, board, hand, player.flyingCarpets > 0, player.gems >= 2)
            if (path != null) {
                if (path.all { it.isFree() }) {
                    val actions = mutableListOf<Action>()
                    var tempState = state
                    for (step in path) {
                        val stepActions = translateStepToActions(tempState, step)
                        actions.addAll(stepActions)
                        tempState = simulateActions(tempState, stepActions)
                    }
                    val updatedPlayer = tempState.currentPlayer ?: return actions
                    val updatedPool = if (quest.isPersonal) updatedPlayer.hand + quest.stored else updatedPlayer.hand + updatedPlayer.storage
                    findFulfillmentCombo(quest, updatedPool)?.let { finalCombo ->
                        val selectCombo = finalCombo.filter { it in updatedPlayer.hand || it in updatedPlayer.storage }
                        if (selectCombo.isNotEmpty()) actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, selectCombo.sortedBy { it.id }))
                        actions.add(QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, quest, emptyList()))
                        actions.add(QuestAction(QuestAction.ActionType.COMPLETE_QUEST, state.currentPlayerIndex, quest, emptyList()))
                    }
                    return actions
                }

                val actions = mutableListOf<Action>()
                var tempState = state
                for (step in path) {
                    val stepActions = translateStepToActions(tempState, step)
                    actions.addAll(stepActions)
                    tempState = simulateActions(tempState, stepActions)
                    if (!step.isFree()) break
                }
                return actions
            }
        }
        return emptyList()
    }

    private fun planSpecialAbilities(state: GameState): List<Action> {
        val player = state.currentPlayer ?: return emptyList()
        val actions = mutableListOf<Action>()
        val abilities = listOf(Ability.SUMMONING, Ability.LOOKING_GLASS, Ability.GEM, Ability.ROGUES_PURSE)

        for (ability in abilities) {
            player.handContains(ability).sortedBy { it.id }.forEach { card ->
                if (ability == Ability.LOOKING_GLASS) {
                    player.hand.filter { it is CreatureCard && it != card }.maxByOrNull { (it as CreatureCard).values.size }?.let { target ->
                        actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(card, target)))
                        actions.add(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null))
                    }
                } else {
                    actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(card)))
                    actions.add(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null))
                }
            }
        }
        return actions
    }

    private fun planSubdueChain(state: GameState): List<Action> {
        val player = state.currentPlayer ?: return emptyList()
        val currentRegion = state.playerPositions[player.name] ?: return emptyList()
        val board = state.board ?: return emptyList()

        val chain = findMaxSubdueChain(currentRegion, player.hand, board)
        if (chain.isNotEmpty()) return translateChainToActions(state, currentRegion, chain)

        if (state.gamePhase == GameState.GamePhase.OPEN) {
            val adjacentAreas = board.getAdjacentAreas(currentRegion).sortedBy { it.second.name }
            for ((road, destination) in adjacentAreas) {
                val moveSteps = mutableListOf<PathStep>()
                if (player.flyingCarpets > 0) moveSteps.add(PathStep(destination, MoveType.FLYING_CARPET))
                player.handContains(Ability.WITCH_BROOM).forEach { _ -> moveSteps.add(PathStep(destination, MoveType.ADJACENT, true)) }

                for (step in moveSteps) {
                    val stepActions = translateStepToActions(state, step)
                    val stateAfterMove = simulateActions(state, stepActions)
                    val chainAfterMove = findMaxSubdueChain(destination, stateAfterMove.currentPlayer?.hand ?: emptyList(), stateAfterMove.board!!)
                    if (chainAfterMove.isNotEmpty()) return stepActions + translateChainToActions(stateAfterMove, destination, chainAfterMove)
                }
            }
        }
        return emptyList()
    }

    private fun translateChainToActions(state: GameState, startRegion: Region, chain: List<Road>): List<Action> {
        val actions = mutableListOf<Action>()
        var currentState = state
        var currentPos = startRegion
        for (road in chain) {
            val p = currentState.currentPlayer ?: break
            val creature = road.creature ?: break
            val combo = engine.canSubdue(creature, p.hand).firstOrNull() ?: break
            val stepActions = mutableListOf<Action>()
            stepActions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, combo.toList().sortedBy { it.id }))
            stepActions.add(SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, road, null, null))
            val dest = state.board?.getAdjacentAreas(currentPos)?.find { it.first.id == road.id }?.second ?: break
            stepActions.add(MoveAction(state.currentPlayerIndex, dest, MoveType.ADJACENT, false))
            actions.addAll(stepActions)
            currentState = simulateActions(currentState, stepActions)
            currentPos = dest
        }
        return actions
    }

    private fun planTowerVisit(state: GameState): List<Action> {
        val player = state.currentPlayer ?: return emptyList()
        val region = state.playerPositions[player.name] ?: return emptyList()
        if (region.tower != null) return listOf(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.START_TOWER_DRAW, null))
        return emptyList()
    }

    private fun planResourceHoarding(state: GameState): List<Action> {
        val player = state.currentPlayer ?: return emptyList()
        val actions = mutableListOf<Action>()
        val board = state.board ?: return emptyList()
        val hand = player.hand.sortedBy { it.id }.toMutableList()
        val currentStorage = player.storage.toMutableList()
        val publicQuests = board.quests.filterNotNull().sortedBy { it.id }
        val allQuests = (publicQuests + player.quests).sortedBy { it.id }

        var currentlySelectedQuest: Quest? = null

        for (card in hand.toList()) {
            val matchingQuest = allQuests.find { it.canStoreCard(card) } ?: continue

            fun selectQuest(q: Quest) {
                if (currentlySelectedQuest?.id != q.id) {
                    if (currentlySelectedQuest != null) actions.add(QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, currentlySelectedQuest!!, emptyList()))
                    actions.add(QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, q, emptyList()))
                    currentlySelectedQuest = q
                }
            }

            if (matchingQuest.isPersonal) {
                actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(card)))
                selectQuest(matchingQuest)
                actions.add(QuestAction(QuestAction.ActionType.STORE_CARD_FOR_QUEST, state.currentPlayerIndex, matchingQuest, listOf(card)))
                hand.remove(card)
            } else if (currentStorage.size < 5) {
                actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(card)))
                // selectQuest(matchingQuest) // Board quest doesn't strictly need selection for storage, but user said "unselect once done"
                actions.add(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.STORE_FOR_BOARD_QUEST, null))
                hand.remove(card)
                currentStorage.add(card)
            } else {
                val staleCard = currentStorage.find { sCard -> publicQuests.none { it.canStoreCard(sCard) } }
                if (staleCard != null) {
                    if (currentlySelectedQuest != null) {
                        actions.add(QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, currentlySelectedQuest!!, emptyList()))
                        currentlySelectedQuest = null
                    }
                    actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(staleCard)))
                    actions.add(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.DISCARD_FROM_HAND, listOf(staleCard)))
                    currentStorage.remove(staleCard)
                    actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(card)))
                    // selectQuest(matchingQuest)
                    actions.add(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.STORE_FOR_BOARD_QUEST, null))
                    hand.remove(card)
                    currentStorage.add(card)
                }
            }
        }
        if (currentlySelectedQuest != null) actions.add(QuestAction(QuestAction.ActionType.SELECT_QUEST, state.currentPlayerIndex, currentlySelectedQuest!!, emptyList()))
        return actions
    }

    private fun planResidualAndPurge(state: GameState): List<Action> {
        val player = state.currentPlayer ?: return emptyList()
        val actions = mutableListOf<Action>()
        var hand = player.hand.toMutableList()
        val abilitiesToFire = listOf(Ability.DRAGON, Ability.GEM, Ability.SUMMONING, Ability.ROGUES_PURSE)

        for (ability in abilitiesToFire) {
            val cards = hand.filter { (it as? CreatureCard)?.ability == ability || (it as? Artifact)?.ability == ability }.sortedBy { it.id }
            for (card in cards) {
                actions.add(
                    CardAction(
                        CardAction.ActionType.SELECT_CARDS,
                        state.currentPlayerIndex,
                        listOf(card)
                    )
                )
                actions.add(
                    PlayerAction(
                        state.currentPlayerIndex,
                        PlayerAction.ActionType.USE_ABILITY,
                        null
                    )
                )
                hand.remove(card)
            }
        }
        if (hand.isNotEmpty()) {
            actions.addAll(planResourceHoarding(state))
            actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, hand.sortedBy { it.id }))
            actions.add(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.DISCARD_FROM_HAND, hand))
        }
        actions.add(TurnAction(TurnAction.ActionType.NEXT_TURN))
        return actions
    }

    private fun simulateActions(state: GameState, actions: List<Action>) = actions.fold(state) { s, a -> engine.reduce(s, a) }

    private fun findFulfillmentCombo(quest: Quest, cards: List<Card>): List<Card>? {
        val reqs = quest.getRequirements().toMutableList()
        val pool = cards.filterIsInstance<CreatureCard>().sortedBy { it.id }.toMutableList()
        val combo = mutableListOf<Card>()
        while (reqs.isNotEmpty()) {
            val card = pool.find { it.values.contains(reqs[0]) } ?: return null
            combo.add(card)
            pool.remove(card)
            card.values.forEach { reqs.remove(it) }
        }
        return combo
    }

    private fun findShortestPath(start: Region, target: RegionName, board: Board, hand: List<Card>, canUseToken: Boolean, canTeleport: Boolean): List<PathStep>? {
        val pq = PriorityQueue<Node>().apply { add(Node(start, emptyList(), hand, false, !canUseToken)) }
        val visited = mutableMapOf<Int, Int>()
        while (pq.isNotEmpty()) {
            val node = pq.poll()!!
            if (node.region.name == target) return node.path
            val hash = arrayOf(node.region, node.turnActionUsed, node.carpetTokenUsed, node.currentHand.size).contentHashCode()
            if (visited.getOrDefault(hash, Int.MAX_VALUE) <= node.path.size) continue
            visited[hash] = node.path.size
            for ((road, dest) in board.getAdjacentAreas(node.region)) {
                node.currentHand.find { it is CreatureCard && it.ability == Ability.WITCH_BROOM }?.let {
                    pq.add(Node(dest, node.path + PathStep(dest, MoveType.ADJACENT, true), node.currentHand - it, node.turnActionUsed, node.carpetTokenUsed))
                }
                if (!node.carpetTokenUsed) pq.add(Node(dest, node.path + PathStep(dest, MoveType.FLYING_CARPET), node.currentHand, node.turnActionUsed, true))
                if (!node.turnActionUsed && road.creature?.let { engine.canSubdue(it, node.currentHand).isNotEmpty() } == true)
                    pq.add(Node(dest, node.path + PathStep(dest, MoveType.ADJACENT, false), node.currentHand, true, node.carpetTokenUsed))
            }
            if (node.region.tower != null) board.getTowerMatch(node.region)?.let { dest ->
                node.currentHand.find { it is CreatureCard && it.ability == Ability.TOWER_KEY }?.let {
                    pq.add(Node(dest, node.path + PathStep(dest, MoveType.TOWER_KEY, true), node.currentHand - it, node.turnActionUsed, node.carpetTokenUsed))
                }
                if (!node.turnActionUsed && canTeleport) pq.add(Node(dest, node.path + PathStep(dest, MoveType.TOWER_KEY, false), node.currentHand, true, node.carpetTokenUsed))
            }
        }
        return null
    }

    private fun translateStepToActions(state: GameState, step: PathStep): List<Action> {
        val p = state.currentPlayer ?: return emptyList()
        val actions = mutableListOf<Action>()
        when (step.moveType) {
            MoveType.ADJACENT -> if (step.useAbility) {
                p.hand.find { it is CreatureCard && it.ability == Ability.WITCH_BROOM }?.let {
                    actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(it)))
                    actions.add(MoveAction(state.currentPlayerIndex, step.destination, MoveType.ADJACENT, true))
                }
            } else state.board?.getRoad(state.playerPositions[p.name]!!, step.destination)?.let { road ->
                road.creature?.let { engine.canSubdue(it, p.hand).firstOrNull()?.let { combo ->
                    actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, combo.toList().sortedBy { it.id }))
                    actions.add(SubdueAction(SubdueAction.ActionType.SELECT_ROAD, state.currentPlayerIndex, road, null, null))
                    actions.add(MoveAction(state.currentPlayerIndex, step.destination, MoveType.ADJACENT, false))
                }}
            }
            MoveType.FLYING_CARPET -> actions.add(MoveAction(state.currentPlayerIndex, step.destination, MoveType.FLYING_CARPET))
            MoveType.TOWER_KEY -> if (step.useAbility) {
                p.hand.find { it is CreatureCard && it.ability == Ability.TOWER_KEY }?.let {
                    actions.add(CardAction(CardAction.ActionType.SELECT_CARDS, state.currentPlayerIndex, listOf(it)))
                    actions.add(PlayerAction(state.currentPlayerIndex, PlayerAction.ActionType.USE_ABILITY, null))
                    actions.add(MoveAction(state.currentPlayerIndex, step.destination, MoveType.TOWER_KEY, true))
                }
            } else actions.add(MoveAction(state.currentPlayerIndex, step.destination, MoveType.TOWER_KEY))
        }
        return actions
    }

    private fun findMaxSubdueChain(start: Region, hand: List<Card>, board: Board): List<Road> {
        fun find(curr: Region, currHand: List<Card>, vis: Set<Region>): List<Road> {
            var best = emptyList<Road>()
            val adj = board.getAdjacentAreas(curr).sortedBy { it.second.name }
            for ((road, dest) in adj) {
                if (dest in vis) continue
                road.creature?.let { c ->
                    val handSorted = currHand.sortedBy { it.id }
                    engine.canSubdue(c, handSorted).firstOrNull()?.let { combo ->
                        val sub = find(dest, currHand - combo.toList(), vis + dest)
                        if (sub.size + 1 > best.size) best = listOf(road) + sub
                    }
                }
            }
            return best
        }
        return find(start, hand, setOf(start))
    }

    private data class PathStep(val destination: Region, val moveType: MoveType, val useAbility: Boolean = false) {
        fun isFree(): Boolean = when (moveType) {
            MoveType.ADJACENT -> useAbility
            MoveType.TOWER_KEY -> useAbility
            MoveType.FLYING_CARPET -> true
        }
    }
    private data class Node(val region: Region, val path: List<PathStep>, val currentHand: List<Card>, val turnActionUsed: Boolean, val carpetTokenUsed: Boolean) : Comparable<Node> {
        override fun compareTo(other: Node): Int = if (turnActionUsed != other.turnActionUsed) (if (turnActionUsed) 1 else -1) else path.size.compareTo(other.path.size)
    }
}