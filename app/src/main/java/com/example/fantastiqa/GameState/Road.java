package com.example.fantastiqa.GameState;

public class Road extends Area{
    public CreatureCard creature;
    public boolean gem;

    public Road(){

    }

    @Override
    public String getName() {
        return creature.name;
    }

    public Road(CreatureCard creature, boolean gem) {
        this.creature = creature;
        this.gem = gem;
    }

    public Region getConnectedRegion(Region starter){
        Region reg0 = (Region)adjacenies.get(0);
        return reg0 == starter ?
                (Region)adjacenies.get(1) :
                reg0;
    }

}
