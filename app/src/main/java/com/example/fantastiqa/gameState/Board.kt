package com.example.fantastiqa.gameState

import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.pieces.TowerName
import java.util.*

/**
 * Immutable Board representation.
 */
data class Board(
    @JvmField val quests: List<BoardQuest?> = listOf(null, null),
    @JvmField val adjacencies: Map<Region, Map<Region, Road>> = emptyMap()
) {

    /**
     * Returns a new board with the quest at [index] replaced.
     */
    fun withQuest(index: Int, newQuest: BoardQuest?): Board {
        val newQuests = quests.toMutableList()
        newQuests[index] = newQuest
        return copy(quests = newQuests)
    }

    /**
     * Returns a new board with the road between [r1] and [r2] replaced.
     */
    fun withRoad(r1: Region, r2: Region, newRoad: Road): Board {
        val newAdjacencies = adjacencies.toMutableMap()
        
        val r1Map = newAdjacencies[r1]?.toMutableMap() ?: mutableMapOf()
        r1Map[r2] = newRoad
        newAdjacencies[r1] = r1Map

        val r2Map = newAdjacencies[r2]?.toMutableMap() ?: mutableMapOf()
        r2Map[r1] = newRoad
        newAdjacencies[r2] = r2Map

        return copy(adjacencies = newAdjacencies)
    }

    fun getAdjacentAreas(starting: Region): List<Pair<Road, Region>> {
        return adjacencies[starting]?.map { (adjRegion, road) ->
            Pair(road, adjRegion)
        } ?: emptyList()
    }

    fun regions(): List<Region> = adjacencies.keys.toList()

    fun roads(): List<Road> {
        val identitySet = Collections.newSetFromMap(IdentityHashMap<Road, Boolean>())
        adjacencies.values.forEach { innerMap ->
            identitySet.addAll(innerMap.values)
        }
        return identitySet.toList()
    }

    fun getTowerMatch(startRegion: Region): Region? {
        return adjacencies.keys.find { it != startRegion && it.tower == startRegion.tower }
    }

    fun getRoad(r1: Region, r2: Region): Road? = adjacencies[r1]?.get(r2)

    companion object {
        /**
         * Factory method to create an initial board.
         */
        @JvmStatic
        fun createInitialBoard(): Board {
            val r = Random()
            val regionsNames = RegionName.values().toMutableList()
            val towers = (TowerName.values().toList() + TowerName.values().toList()).toMutableList()

            val regionNodes = mutableListOf<Region>()
            while (regionsNames.isNotEmpty()) {
                val region = Region(
                    regionsNames.removeAt(r.nextInt(regionsNames.size)),
                    towers.removeAt(r.nextInt(towers.size))
                )
                regionNodes.add(region)
            }

            var tempBoard = Board(adjacencies = regionNodes.associateWith { emptyMap<Region, Road>() })

            if (regionNodes.isNotEmpty()) {
                var regionMiddle1: Region? = null
                var regionFirst = regionNodes[0]
                val regionVeryFirst = regionFirst

                for (i in 1 until regionNodes.size) {
                    val regionSecond = regionNodes[i]
                    if (i == 2) regionMiddle1 = regionNodes[1]
                    
                    if (i == 5 && regionMiddle1 != null) {
                        tempBoard = tempBoard.addRoad(regionMiddle1, regionFirst, Road())
                    }
                    
                    tempBoard = tempBoard.addRoad(regionFirst, regionSecond, Road())
                    regionFirst = regionSecond
                }
                tempBoard = tempBoard.addRoad(regionFirst, regionVeryFirst, Road())
            }
            return tempBoard
        }

        private fun Board.addRoad(r1: Region, r2: Region, road: Road): Board {
            val newAdjacencies = adjacencies.toMutableMap()
            
            val r1Map = newAdjacencies[r1]?.toMutableMap() ?: mutableMapOf()
            r1Map[r2] = road
            newAdjacencies[r1] = r1Map

            val r2Map = newAdjacencies[r2]?.toMutableMap() ?: mutableMapOf()
            r2Map[r1] = road
            newAdjacencies[r2] = r2Map

            return copy(adjacencies = newAdjacencies)
        }
    }
}
