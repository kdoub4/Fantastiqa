package com.example.fantastiqa.GameState;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Board {
    public List<Area> locations = new ArrayList<>(6);
    public List<Road> roads = new ArrayList<>(7);
    public List<Quest> quests = new ArrayList<>(2);

    public Board() {
        Random  r = new Random();

        List<RegionName> regions = new ArrayList<>(Arrays.asList(RegionName.values()));
        List<TowerName> towers = new ArrayList<>(Arrays.asList(TowerName.values()));
        towers.addAll(Arrays.asList(TowerName.values()));
        while (regions.size()>0) {
            locations.add(new Area(regions.remove(r.nextInt(regions.size())), towers.remove(r.nextInt(towers.size()))));
        }
    }


}
