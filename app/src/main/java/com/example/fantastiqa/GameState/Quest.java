package com.example.fantastiqa.GameState;

import java.util.ArrayList;

public class Quest extends Card {
    public int vps;
    public int gems;
    public ArrayList<Symbol> requirements = new ArrayList<Symbol>();
    public RegionName land;
    public ArrayList<Card> stored = new ArrayList<>();

    public Quest(String name, int vps, int gems, ArrayList<Symbol> requirements, RegionName land) {
        super(name);
        this.vps = vps;
        this.gems = gems;
        this.requirements = requirements;
        this.land = land;
    }


}


