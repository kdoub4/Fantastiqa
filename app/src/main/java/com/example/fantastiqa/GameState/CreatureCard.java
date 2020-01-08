package com.example.fantastiqa.GameState;

import java.util.ArrayList;

public class CreatureCard extends Card {
    boolean gem;
    public Ability ability;
    public ArrayList<Symbol> values = new ArrayList<Symbol>();

    public CreatureCard(String name, boolean gem, Ability ability, Symbol ... value) {
        super(name);
        this.gem = gem;
        this.ability = ability;
        for (Symbol aValue : value
             ) {
            this.values.add(aValue);
        }

    }
}

enum CreatureCards {
    Knight(true,Ability.NONE, Symbol.SWORD),
    BabyDragon(true, Ability.NONE, Symbol.FIRE),
    FenFairy(false, Ability.PLUS_CARD, Symbol.WATER),
    Witch(false,Ability.MAGIC_CARPET, Symbol.BROOM),
    Rabbits(false, Ability.TOWER_KEY, Symbol.TOOTH),
    Spiders(true, Ability.NONE, Symbol.NET),
    BillyGoat(true, Ability.NONE, Symbol.HELMET),
    Troll(true, Ability.NONE, Symbol.BAT),
    Enchantress(false, Ability.DRAGON, Symbol.WAND),
    Gryphon(true,Ability.MAGIC_CARPET, Symbol.SWORD, Symbol.SWORD),
    Dragon(true, Ability.TOWER_KEY, Symbol.FIRE, Symbol.FIRE),
    WaterNymphs(false, Ability.PLUS_CARD, Symbol.WATER, Symbol.WATER),
    WarlocksPet(false,Ability.MAGIC_CARPET, Symbol.BROOM, Symbol.BROOM),
    VampireBats(false, Ability.TOWER_KEY, Symbol.TOOTH, Symbol.TOOTH),
    GiantSpider(true, Ability.PLUS_CARD, Symbol.NET, Symbol.NET),
    Satyr(true, Ability.PLUS_CARD, Symbol.HELMET, Symbol.HELMET),
    Bear(true, Ability.TOWER_KEY, Symbol.BAT, Symbol.BAT),
    Fairies(false, Ability.MAGIC_CARPET, Symbol.WAND, Symbol.WAND)
    ;

    public boolean isGem() {
        return gem;
    }

    public Ability getAbility() {
        return ability;
    }

    public Symbol getValue1() {
        return value1;
    }

    public Symbol getValue2() {
        return value2;
    }

    private final boolean gem;
    private final Ability ability;
    private final Symbol value1;
    private final Symbol value2;

    CreatureCards(boolean gem, Ability ability, Symbol ... value) {
        this.gem = gem;
        this.ability = ability;
        this.value1 = value[0];
        if (value.length>1) {
            this.value2 = value[1];
        } else {
            this.value2 = Symbol.NONE;
        }
    }

}

