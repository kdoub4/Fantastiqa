package com.example.fantastiqa.pieces

import com.example.fantastiqa.gameState.Ability
import com.example.fantastiqa.gameState.Symbol

enum class CreatureCards(
        val isGem: Boolean,
        val ability: Ability,
        subdueBy: Symbol,
        vararg value: Symbol
    ) {
        Knight(true, Ability.NONE, Symbol.WAND, Symbol.SWORD),
        BabyDragon(true, Ability.NONE, Symbol.SWORD, Symbol.FIRE),
        FenFairy(false, Ability.PLUS_CARD, Symbol.FIRE, Symbol.WATER),
        Witch(false, Ability.MAGIC_CARPET, Symbol.WATER, Symbol.BROOM),
        Rabbits(false, Ability.TOWER_KEY, Symbol.BROOM, Symbol.TOOTH),
        Spiders(true, Ability.NONE, Symbol.TOOTH, Symbol.NET),
        BillyGoat(true, Ability.NONE, Symbol.NET, Symbol.HELMET),
        Troll(true, Ability.NONE, Symbol.HELMET, Symbol.BAT),
        Enchantress(false, Ability.DRAGON, Symbol.BAT, Symbol.WAND),
        Gryphon(true, Ability.MAGIC_CARPET, Symbol.WAND, Symbol.SWORD, Symbol.SWORD),
        Dragon(true, Ability.TOWER_KEY, Symbol.SWORD, Symbol.FIRE, Symbol.FIRE),
        WaterNymphs(false, Ability.PLUS_CARD, Symbol.FIRE, Symbol.WATER, Symbol.WATER),
        WarlocksPet(false, Ability.MAGIC_CARPET, Symbol.WATER, Symbol.BROOM, Symbol.BROOM),
        VampireBats(false, Ability.TOWER_KEY, Symbol.BROOM, Symbol.TOOTH, Symbol.TOOTH),
        GiantSpider(true, Ability.PLUS_CARD, Symbol.TOOTH, Symbol.NET, Symbol.NET),
        Satyr(true, Ability.PLUS_CARD, Symbol.NET, Symbol.HELMET, Symbol.HELMET),
        Bear(true, Ability.TOWER_KEY, Symbol.HELMET, Symbol.BAT, Symbol.BAT),
        Fairies(false, Ability.MAGIC_CARPET, Symbol.BAT, Symbol.WAND, Symbol.WAND);

        val value1: Symbol
        val value2: Symbol

        val subduedBy: Symbol

        init {
            this.value1 = value[0]
            if (value.size > 1) {
                this.value2 = value[1]
            } else {
                this.value2 = Symbol.NONE
            }
            this.subduedBy = subdueBy
        }
    }
