package com.example.fantastiqa.GameState;

import java.util.ArrayList;
import java.util.List;

public abstract class Area {
    private int Id;

    public List<Area> adjacenies = new ArrayList<>();
    public Area(){}


    public abstract String getName();
}
