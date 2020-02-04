package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.List;

public class Region extends Area {
    public RegionName name;
    public List<Player> players = new ArrayList<Player>();
    public TowerName tower;

    public Region(RegionName newland, TowerName newtower){
        name = newland;
        tower = newtower;
    }

    @Override
    public String getName(){
        return name.toString();
    }
    
    @Override
    public String toString() {
		return name.toString() + "\n" + tower.toString() + "\n" + getPlayersString();
	}
	
    public String getPlayersString(){
		String results="";
		for (Player aPlayer : players) {
			results +=aPlayer.toString() + " ";
		}
		return results;
	}

}

