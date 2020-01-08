package com.example.fantastiqa.GameState;

import java.util.ArrayList;

public class Area {
    public RegionName land;
    public ArrayList<Player> players = new ArrayList<Player>();
    public TowerName tower;

    Area(RegionName newland, TowerName newtower){
        land = newland;
        tower = newtower;
    }
}
