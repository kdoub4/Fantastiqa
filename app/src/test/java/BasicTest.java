package com.example.fantastiqaTest;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashSet;

import com.example.fantastiqa.GameState.Board;
import com.example.fantastiqa.GameState.TowerName;
import com.example.fantastiqa.GameState.Region;

public class BasicTest {
	
	Board theBoard = new Board();
	
	@Test
	public void getTowerMatch_Quest() {
		
		Region start = null;
		for (Region aRegion : theBoard.regionsRoads.nodes()) {
			if (aRegion.tower == TowerName.QUEST) {
				start = aRegion;
				break;
			}
		}
		if (start == null) assert(false);
		Region result = theBoard.getTowerMatch(start);
		assert(result!=start && result.tower==TowerName.QUEST);
	}
	
	@Test
	public void confirmUniqueRegions() {
		//6 unique names
		HashSet<String> regionNames = new HashSet();
		for (Region aRegion : theBoard.regionsRoads.nodes()) {
			assert(regionNames.add(aRegion.name.toString()));
		}
		assert(regionNames.size()==6);
	}
	
	@Test
	public void confirmTowerSemiUnique() {
		//3 unique towers 2 each
		HashSet<String> towerNames1 = new HashSet();
		HashSet<String> towerNames2 = new HashSet();
		for (Region aRegion : theBoard.regionsRoads.nodes()) {
			if (!towerNames1.add(aRegion.tower.toString()))
				assert(towerNames2.add(aRegion.tower.toString()));
		}
		for (String aName1 : towerNames1) {
			assert(towerNames2.contains(aName1));
		}
		assert(towerNames1.size()==3 && towerNames2.size()==3);
	}
	
	@Test
	public void boardBuild_7Uniqueroads() {
		HashSet<Object> roads = new HashSet();
		for (Object aRoad : theBoard.regionsRoads.edges()) {
			assert(roads.add(aRoad));
		}
		assert(roads.size()==7);
	}
}
