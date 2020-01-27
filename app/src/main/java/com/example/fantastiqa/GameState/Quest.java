package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Quest extends Card {
    public int vps;
    public int gems;
    private ArrayList<Symbol> requirements = new ArrayList<>();
    public RegionName land;
    public ArrayList<Card> stored = new ArrayList<>();

    public Quest(String name, int vps, int gems, Symbol doubleRequirement, Symbol tripleRequirement, RegionName land) {
        super(name);
        this.vps = vps;
        this.gems = gems;
        this.land = land;
        assert(doubleRequirement!=tripleRequirement);
        this.requirements.add(doubleRequirement);
        this.requirements.add(tripleRequirement);
    }

    public Symbol getDoubleRequirement() {
        return requirements.get(0);
    }
    public Symbol getTripleRequirement() {
        return requirements.get(1);
    }
    public List<Symbol> getRequirements() {
        return  requirements;
    }


}


