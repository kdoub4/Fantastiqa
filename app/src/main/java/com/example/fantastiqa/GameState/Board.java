package com.example.fantastiqa.GameState;

import android.util.Pair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.Network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Iterator;
import java.util.Set;

public class Board {
    public final List<Quest> quests = new ArrayList<>(2);
    public MutableNetwork<Region,Road> regionsRoads= NetworkBuilder.undirected().expectedEdgeCount(7).expectedNodeCount(6).build();;

    public Board() {
        Random  r = new Random();

        List<RegionName> regions = new ArrayList<>(Arrays.asList(RegionName.values()));
        List<TowerName> towers = new ArrayList<>(Arrays.asList(TowerName.values()));
        towers.addAll(Arrays.asList(TowerName.values()));

		while (regions.size()>0) {
			regionsRoads.addNode(new Region(regions.remove(r.nextInt(regions.size())),
				towers.remove(r.nextInt(towers.size()))));
		}
		
		Iterator<Region> regionsIter = regionsRoads.nodes().iterator();
		int regionCount = 0;
		Region regionMiddle1 = null;
		Region regionFirst = (regionsIter.hasNext() ? regionsIter.next() : null);
		Region regionVeryFirst = regionFirst;
		Region regionSecond = null;
		while (regionsIter.hasNext()) {
			regionSecond = regionsIter.next();
			if (regionCount == 1) {
				regionMiddle1 = regionFirst;
			}
			if (regionCount == 4) {
				regionsRoads.addEdge(regionMiddle1, regionFirst, new Road());
			}
			regionsRoads.addEdge(regionFirst, regionSecond, new Road());
			regionFirst = regionSecond;
			regionCount++;
		}
		regionsRoads.addEdge(regionFirst, regionVeryFirst, new Road());
    }

    public List<Pair<Road, Region>> getAdjacentAreas(Region starting) {
        List<Pair<Road,Region>> result = new LinkedList<>();
        for (Region adjRegion : regionsRoads.adjacentNodes(starting)) {
			result.add(new Pair<>(regionsRoads.edgeConnectingOrNull(starting, adjRegion), adjRegion));
		}
		return result;
    }

    public List<Region> regions(){
		return new ArrayList<>(regionsRoads.nodes());
    }

    public List<Road> roads() {
		return new ArrayList<>(regionsRoads.edges());
    }
    
    public Region getTowerMatch(Region startRegion) {
		for (Region aRegion : regionsRoads.nodes()) {
			if (aRegion != startRegion && aRegion.tower == startRegion.tower) {
				return aRegion;
			}
		}
		return null;
	}
	
	public Road getRoad(Region r1, Region r2) {
		return regionsRoads.edgeConnectingOrNull(r1,r2);
	}
	
}
