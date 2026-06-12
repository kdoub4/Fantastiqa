package com.example.fantastiqa;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashSet;

import com.example.fantastiqa.gameState.Board;
import com.example.fantastiqa.pieces.TowerName;
import com.example.fantastiqa.gameState.Region;

public class BoardTest {
	
	Board theBoard = Board.createInitialBoard();
	
	@Test
	public void getTowerMatch_Quest() {
		
		Region start = null;
		for (Region aRegion : theBoard.regions()) {
			if (aRegion.tower == TowerName.QUEST) {
				start = aRegion;
				break;
			}
		}
		assertNotNull(start);
		Region result = theBoard.getTowerMatch(start);
		assertNotSame(start, result);
		assertEquals(TowerName.QUEST, result.tower);
	}
	
	@Test
	public void confirmUniqueRegions() {
		//6 unique names
		HashSet<String> regionNames = new HashSet<>();
		for (Region aRegion : theBoard.regions()) {
			assertTrue(regionNames.add(aRegion.name.toString()));
		}
		assertEquals(6, regionNames.size());
	}
	
	@Test
	public void confirmTowerSemiUnique() {
		//3 unique towers 2 each
		HashSet<String> towerNames1 = new HashSet<>();
		HashSet<String> towerNames2 = new HashSet<>();
		for (Region aRegion : theBoard.regions()) {
			if (!towerNames1.add(aRegion.tower.toString()))
				assertTrue(towerNames2.add(aRegion.tower.toString()));
		}
		for (String aName1 : towerNames1) {
			assertTrue(towerNames2.contains(aName1));
		}
		assertEquals(3, towerNames1.size());
		assertEquals(3, towerNames2.size());
	}
	
	@Test
	public void boardBuild_7Uniqueroads() {
		HashSet<Object> roads = new HashSet<>();
		for (Object aRoad : theBoard.roads()) {
			assertTrue(roads.add(aRoad));
		}
		assertEquals(7, roads.size());
	}
}
