package com.example.fantastiqa.GameState;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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

        assert(aGame.canCompleteQuest(aQuest, cards, aGame.players.get(0), RegionName.FIELDS ));
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

        assertFalse(aGame.canCompleteQuest(aQuest, cards, aGame.players.get(0), RegionName.FIELDS ));
    }

    @Test
    public void canCompleteQuest_2symbol_0cardmatch() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.TOOTH, Symbol.NONE, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("Witch");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1()));

        assertFalse(aGame.canCompleteQuest(aQuest, cards, aGame.players.get(0), RegionName.FIELDS ));
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

        assert(aGame.canCompleteQuest(aQuest, cards, aGame.players.get(0), RegionName.FIELDS ));
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

        assertFalse(aGame.canCompleteQuest(aQuest, cards, aGame.players.get(0), RegionName.FIELDS ));
    }

    @Test
    public void canCompleteQuest_3symbol_2doublematch() {
        Game aGame = new Game();
        Quest aQuest = new Quest("test", 1, 3, Symbol.NONE, Symbol.TOOTH, RegionName.FIELDS);
        ArrayList<Card> cards = new ArrayList<>();
        CreatureCards aCard = CreatureCards.valueOf("VampireBats");
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));
        cards.add(new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1(), aCard.getValue2()));

        assert(aGame.canCompleteQuest(aQuest, cards, aGame.players.get(0), RegionName.FIELDS ));
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

        assert(aGame.canCompleteQuest(aQuest, cards, aGame.players.get(0), RegionName.FIELDS ));
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

    @Test
    public void canSubdue_1symbol_1match() {
        List<Set<Card>> expected = new ArrayList<>();

        Card witch =new CreatureCard("Witch", Symbol.WATER, false, Ability.NONE, Symbol.BROOM);
        CreatureCards aCard = CreatureCards.valueOf("FenFairy");
        Card fenfairy = new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1());

        expected.add(Collections.singleton(fenfairy));

        Game aGame = new Game();
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(fenfairy);

        assertTrue(CollectionUtils.isEqualCollection(
                aGame.canSubdueSingle((CreatureCard)witch, cards).get(0),
                expected.get(0)));
    }

    @Test
    public void canSubdue_1symbol_2cardwild() {
        List<Set<Card>> expected = new ArrayList<>();

        CreatureCards aCard = CreatureCards.valueOf("FenFairy");

        Card fenfairy = new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1());
        Card fenfairy2 = new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1());

        aCard = CreatureCards.valueOf("Troll");
        Card troll = new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1());

        Set<Card> expected1 = new HashSet<>();
        Collections.addAll(expected1, fenfairy, fenfairy2);
        expected.add(expected1);

        Game aGame = new Game();
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(fenfairy);
        cards.add(fenfairy2);

        assertTrue(CollectionUtils.isEqualCollection(
                aGame.canSubdueSingle((CreatureCard)troll, cards).get(0),
                expected.get(0)));
    }

    @Test
    public void canSubdue_0match() {
        Card knight =new CreatureCard("Witch", Symbol.WAND, false, Ability.NONE, Symbol.SWORD);
        CreatureCards aCard = CreatureCards.valueOf("FenFairy");
        Card fenfairy = new CreatureCard(aCard.name(), aCard.getSubduedBy(), aCard.isGem(), aCard.isGem() ? Ability.NONE : aCard.getAbility(), aCard.getValue1());


        Game aGame = new Game();
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(fenfairy);
        assertEquals(aGame.canSubdueSingle((CreatureCard)knight, cards).size(),0);
    }

    @Test
    public void canSubdue_1symbol_missWildSingleDoubleWildWild () {
        Game aGame = new Game();
        //starting hand
        ArrayList<Card> cards = new ArrayList<>();
        Card troll = newCard("Troll");
        cards.add(troll);
        Card billygoat3 = newCard("BillyGoat");
        cards.add(billygoat3);
        Card fenfairy = newCard("FenFairy");
        cards.add(fenfairy);
        Card giantspider = newCard("GiantSpider");
        cards.add(giantspider);
        Card billygoat = newCard("BillyGoat");
        cards.add(billygoat);
        Card billygoat2 = newCard("BillyGoat");
        cards.add(billygoat2);

        Card roadCreature = newCard("Witch");

        //expecting list<set<fenfairy>set<giantspider>3sets of billygoat
        List<Set<Card>> expected = new ArrayList<>();
        expected.add(Collections.singleton(fenfairy));
        expected.add(Collections.singleton(giantspider));
        Set<Card> expected3 = new HashSet<>();
        Collections.addAll(expected3,billygoat3,billygoat);
        Set<Card> expected4 = new HashSet<>();
        Collections.addAll(expected4,billygoat3,billygoat2);
        Set<Card> expected5 = new HashSet<>();
        Collections.addAll(expected5,billygoat,billygoat2);

        Collections.addAll(expected, expected3, expected4, expected5);
        ListIterator<Set<Card>> expectedIter = expected.listIterator();
        for (Set<Card> result : aGame.canSubdueSingle((CreatureCard)roadCreature   , cards)) {
            assertTrue(CollectionUtils.isEqualCollection(result,
                    expectedIter.next()));

        }
    }

    private Card newCard(String cardName) {
        CreatureCards aCardType = CreatureCards.valueOf(cardName);
        Card nCard = new CreatureCard(aCardType.name(), aCardType.getSubduedBy(), aCardType.isGem(),
                aCardType.isGem() ? Ability.NONE : aCardType.getAbility(), aCardType.getValue1(), aCardType.getValue2());
        return nCard;
    }

}