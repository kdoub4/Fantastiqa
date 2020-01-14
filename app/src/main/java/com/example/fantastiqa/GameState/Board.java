package com.example.fantastiqa.GameState;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Board {
    public List<Area> locations = Arrays.asList(new Road(), new Region(), new Road(), new Region(), new Road(), new Region(),
            new Road(), new Region(), new Road(), new Region(), new Road(), new Region(), new Road());
    public List<Quest> quests = new ArrayList<>(2);

    public Board() {
        Random  r = new Random();


        List<RegionName> regions = new ArrayList<>(Arrays.asList(RegionName.values()));
        List<TowerName> towers = new ArrayList<>(Arrays.asList(TowerName.values()));
        towers.addAll(Arrays.asList(TowerName.values()));
        for (int i=0; i<=12; i++) {
            Area anArea = locations.get(i);
            if (anArea instanceof Region) {
                Region aRegion = (Region) anArea;
                aRegion.name = regions.remove(r.nextInt(regions.size()));
                aRegion.tower = towers.remove(r.nextInt(towers.size()));
            }
            //middle road
            if (i == 12) {
                anArea.adjacenies.add(locations.get(3));
                anArea.adjacenies.add(locations.get(9));
            } else {
                //previous
                anArea.adjacenies.add(i == 11 ? locations.get(0) : locations.get(i + 1));
                //next
                anArea.adjacenies.add(i == 0 ? locations.get(11) : locations.get(i - 1));
                //middle regions
                if (i == 3 || i == 9) {
                    anArea.adjacenies.add(locations.get(12));
                }
            }
        }
    }

    public List<Pair<Road, Region>> getAdjacentAreas(Region starting) {
        List<Pair<Road,Region>> result = new LinkedList<>();
        for (Area adjRoad: starting.adjacenies
             ) {
            result.add(new Pair<>((Road)adjRoad, ((Road)adjRoad).getConnectedRegion(starting)));
        }
        return result;
    }

    public List<Region> regions(){
        List<Region> results = new ArrayList<>();
        for (Area aArea: locations
             ) {
            if (aArea instanceof Region){
                results.add((Region)aArea);
            }
        }
        return results;
    }

    public List<Road> roads() {
        List<Road> results = new ArrayList<>();
        for (Area aArea: locations
             ) {
            if (aArea instanceof Road){
                results.add((Road)aArea);
            }
        }
        return results;
    }

}
