package com.example.fantastiqa.gameState

import android.util.Pair
import com.example.fantastiqa.pieces.RegionName
import com.example.fantastiqa.pieces.TowerName
import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import java.util.Arrays
import java.util.LinkedList
import java.util.Random

class Board {
    @JvmField
    val quests: MutableList<Quest?> = ArrayList<Quest?>(2)
    @JvmField
    var regionsRoads: MutableNetwork<Region, Road?> =
        NetworkBuilder.undirected().expectedEdgeCount(7).expectedNodeCount(6)
            .build<Region, Road?>()

    init {
        val r = Random()

        val regions: MutableList<RegionName?> =
            ArrayList<RegionName?>(Arrays.asList<RegionName>(*RegionName.values()))
        val towers: MutableList<TowerName?> =
            ArrayList<TowerName?>(Arrays.asList<TowerName>(*TowerName.values()))
        towers.addAll(Arrays.asList<TowerName>(*TowerName.values()))

        while (regions.size > 0) {
            regionsRoads.addNode(
                Region(
                    regions.removeAt(r.nextInt(regions.size))!!,
                    towers.removeAt(r.nextInt(towers.size))!!
                )
            )
        }

        val regionsIter = regionsRoads.nodes().iterator()
        if (regionsIter.hasNext()) {
            var regionCount = 0
            var regionMiddle1: Region? = null
            var regionFirst = regionsIter.next()
            val regionVeryFirst = regionFirst
            while (regionsIter.hasNext()) {
                val regionSecond = regionsIter.next()
                if (regionCount == 1) {
                    regionMiddle1 = regionFirst
                }
                if (regionCount == 4) {
                    regionsRoads.addEdge(regionMiddle1!!, regionFirst, Road())
                }
                regionsRoads.addEdge(regionFirst, regionSecond, Road())
                regionFirst = regionSecond
                regionCount++
            }
            regionsRoads.addEdge(regionFirst, regionVeryFirst, Road())
        }
    }

    fun getAdjacentAreas(starting: Region): MutableList<Pair<Road?, Region?>?> {
        val result: MutableList<Pair<Road?, Region?>?> = LinkedList<Pair<Road?, Region?>?>()
        for (adjRegion in regionsRoads.adjacentNodes(starting)) {
            result.add(
                Pair<Road?, Region?>(
                    regionsRoads.edgeConnectingOrNull(starting, adjRegion),
                    adjRegion
                )
            )
        }
        return result
    }

    fun regions(): MutableList<Region?> {
        return ArrayList<Region?>(regionsRoads.nodes())
    }

    fun roads(): MutableList<Road?> {
        return ArrayList<Road?>(regionsRoads.edges())
    }

    fun getTowerMatch(startRegion: Region): Region? {
        for (aRegion in regionsRoads.nodes()) {
            if (aRegion != startRegion && aRegion.tower == startRegion.tower) {
                return aRegion
            }
        }
        return null
    }

    fun getRoad(r1: Region, r2: Region): Road? {
        return regionsRoads.edgeConnectingOrNull(r1, r2)
    }
}
