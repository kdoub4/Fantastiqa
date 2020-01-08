package com.example.fantastiqa.GameState;

public class Aritifact extends Card {
    public int cost;
    public String text;

    public Aritifact(String name, int cost, String text) {
        super(name);
        this.cost = cost;
        this.text = text;
    }
}
