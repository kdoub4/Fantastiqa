package com.example.fantastiqa.GameState;

public class Artifact extends Card {
    public int cost;
    public String text;

    public Artifact(String name, int cost, String text) {
        super(name);
        this.cost = cost;
        this.text = text;
    }
}
