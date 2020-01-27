package com.example.fantastiqa.GameState;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

public class GameTest {

    @Test
    public void canCompleteQuest_2symbol_2cardmatch() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.TOOTH, Symbol.NONE, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("Rabbits");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));

        assert(aGame.canCompleteQuest(aQuest, cards));
    }
    @Test
    public void canCompleteQuest_2symbol_1cardmatch() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.TOOTH, Symbol.NONE, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("Rabbits");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        aCard = CreatureCards.valueOf("Witch");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));

        assertFalse(aGame.canCompleteQuest(aQuest, cards));
    }

    @Test
    public void canCompleteQuest_2symbol_0cardmatch() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.TOOTH, Symbol.NONE, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("Witch");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));

        assertFalse(aGame.canCompleteQuest(aQuest, cards));
    }

    @Test
    public void canCompleteQuest_3symbol_3cardmatch() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.NONE, Symbol.TOOTH, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("Rabbits");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));

        assert(aGame.canCompleteQuest(aQuest, cards));
    }

    @Test
    public void canCompleteQuest_3symbol_1cardmatch() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.NONE, Symbol.TOOTH, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("Rabbits");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        aCard = CreatureCards.valueOf("Knight");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));

        assertFalse(aGame.canCompleteQuest(aQuest, cards));
    }

    @Test
    public void canCompleteQuest_3symbol_2doublematch() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.NONE, Symbol.TOOTH, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("VampireBats");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));

        assert(aGame.canCompleteQuest(aQuest, cards));
    }

    @Test
    public void canCompleteQuest_5symbol_doubledoublesingleothers() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.BROOM, Symbol.TOOTH, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("Knight");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        cards.add(new CreatureCard("Dog", Symbol.NONE, false, Ability.NONE, Symbol.NONE));
        aCard = CreatureCards.valueOf("WarlocksPet");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
        aCard = CreatureCards.valueOf("Satyr");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
        aCard = CreatureCards.valueOf("VampireBats");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));

        assert(aGame.canCompleteQuest(aQuest, cards));
    }

    @Test
    public void completeQuest_2symbol_2singleothers() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.BROOM, Symbol.NONE, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("Witch");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        ArrayList<Card> expected = new ArrayList<>(cards.size());
        expected.addAll(cards);
        cards.add(new CreatureCard("Dog", Symbol.NONE, false, Ability.NONE, Symbol.NONE));
        aCard = CreatureCards.valueOf("Satyr");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
        aCard = CreatureCards.valueOf("VampireBats");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
        cards.add(0,new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));

        assertEquals(expected, aGame.completeQuest(aQuest, cards));
    }


}