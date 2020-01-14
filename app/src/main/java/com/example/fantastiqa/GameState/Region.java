package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.List;

public class Region extends Area {
    public RegionName name;
    public List<Player> players = new ArrayList<Player>();
    public TowerName tower;

    public Region(){

    }
    public Region(RegionName newland, TowerName newtower){
        name = newland;
        tower = newtower;
    }

    @Override
    public String getName(){
        return name.toString();
    }

}

