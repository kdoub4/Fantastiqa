package ca.kevin.worth.outlandiqa.Model

import ca.kevin.worth.outlandiqa.CreatureCard

enum class CreatureCards(
    val gem: Boolean,
    val ability: Ability,
    val subduedBy: Symbol,
    vararg val values: Symbol
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

    fun create() = CreatureCard(
        name = this.name,
        subduedBy = this.subduedBy,
        gem = this.gem,
        ability = this.ability,
        values = this.values.toList()
    )
}


