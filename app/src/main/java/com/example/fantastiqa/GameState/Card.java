package com.example.fantastiqa.GameState;

public class Card {
    public String name;

    public Card(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }
}