package com.example.fantastiqa.GameState;

import java.util.ArrayList;

public class CreatureCard extends Card {
    boolean gem;
    public Ability ability;
    public ArrayList<Symbol> values = new ArrayList<Symbol>();
    public Symbol subduedBy;

    public CreatureCard(String name, Symbol subduedBy, boolean gem, Ability ability, Symbol ... value) {
        super(name);
        this.gem = gem;
        this.ability = ability;
        for (Symbol aValue : value
             ) {
            this.values.add(aValue);
        }
        this.subduedBy = subduedBy;
    }
}

enum CreatureCards {
    Knight(true,Ability.NONE, Symbol.FIRE, Symbol.SWORD),
    BabyDragon(true, Ability.NONE, Symbol.WATER, Symbol.FIRE),
    FenFairy(false, Ability.PLUS_CARD, Symbol.BROOM, Symbol.WATER),
    Witch(false,Ability.MAGIC_CARPET, Symbol.TOOTH, Symbol.BROOM),
    Rabbits(false, Ability.TOWER_KEY, Symbol.NET, Symbol.TOOTH),
    Spiders(true, Ability.NONE, Symbol.HELMET, Symbol.NET),
    BillyGoat(true, Ability.NONE, Symbol.BAT, Symbol.HELMET),
    Troll(true, Ability.NONE,Symbol.WAND, Symbol.BAT),
    Enchantress(false, Ability.DRAGON,Symbol.SWORD, Symbol.WAND),
    Gryphon(true,Ability.MAGIC_CARPET, Symbol.FIRE, Symbol.SWORD, Symbol.SWORD),
    Dragon(true, Ability.TOWER_KEY, Symbol.WATER, Symbol.FIRE, Symbol.FIRE),
    WaterNymphs(false, Ability.PLUS_CARD, Symbol.BROOM, Symbol.WATER, Symbol.WATER),
    WarlocksPet(false,Ability.MAGIC_CARPET, Symbol.TOOTH, Symbol.BROOM, Symbol.BROOM),
    VampireBats(false, Ability.TOWER_KEY, Symbol.NET, Symbol.TOOTH, Symbol.TOOTH),
    GiantSpider(true, Ability.PLUS_CARD, Symbol.HELMET, Symbol.NET, Symbol.NET),
    Satyr(true, Ability.PLUS_CARD, Symbol.BAT, Symbol.HELMET, Symbol.HELMET),
    Bear(true, Ability.TOWER_KEY, Symbol.WAND, Symbol.BAT, Symbol.BAT),
    Fairies(false, Ability.MAGIC_CARPET, Symbol.SWORD, Symbol.WAND, Symbol.WAND)
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

    public Symbol getSubduedBy() {
        return subduedBy;
    }

    private final Symbol subduedBy;

    CreatureCards(boolean gem, Ability ability, Symbol subdueBy, Symbol ... value) {
        this.gem = gem;
        this.ability = ability;
        this.value1 = value[0];
        if (value.length>1) {
            this.value2 = value[1];
        } else {
            this.value2 = Symbol.NONE;
        }
        this.subduedBy = subdueBy;
    }

}

