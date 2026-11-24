package polymorphia.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polymorphia.FixedDie;
import polymorphia.artifacts.*;
import polymorphia.characters.Character;
import polymorphia.characters.CharacterFactory;
import polymorphia.maze.Room;
import polymorphia.strategy.StrategyFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every command, executed for real. These are the classes the action moved into for
 * Homework 11, so this is where the behaviour is now pinned down.
 */
class CommandsTest {

    private final CharacterFactory characterFactory = new CharacterFactory(new StrategyFactory());
    private final ArtifactFactory artifactFactory = new ArtifactFactory();
    private final CommandFactory commandFactory = new CommandFactory();

    private Room room;
    private Room nextRoom;
    private Character galahad;

    @BeforeEach
    void setUp() {
        room = new Room("Starting");
        nextRoom = new Room("Other");
        room.addNeighbor(nextRoom, true);
        galahad = characterFactory.createKnight("Galahad", 10.0, new FixedDie(4));
        room.addCharacter(galahad);
    }

    @Test
    void sleepingDoesNothingButStillCostsATurn() {
        ICommand sleep = commandFactory.newSleepCommand(galahad, room);
        assertEquals(CommandType.SLEEP, sleep.getType());
        assertTrue(sleep.execute());
        assertEquals("", sleep.optionString());
    }

    @Test
    void movingEntersTheNextRoomAndChargesForIt() {
        ICommand move = commandFactory.newMoveCommand(galahad, room, nextRoom);
        assertEquals(CommandType.MOVE, move.getType());
        assertEquals("Other", move.optionString());

        assertTrue(move.execute());
        assertTrue(nextRoom.containsCharacter(galahad));
        assertEquals(10.0 - Character.HEALTH_LOST_IN_MOVING_ROOMS, galahad.getHealth(), 0.0001);
    }

    @Test
    void movingNowhereFailsInsteadOfThrowing() {
        ICommand move = commandFactory.newMoveCommand(galahad, room, null);
        assertFalse(move.execute());
        assertEquals("nowhere", move.optionString());
    }

    @Test
    void eatingTakesTheFoodOutOfTheRoomAndAddsHealth() {
        Food cupcake = (Food) artifactFactory.createFood("cupcake", 1.5);
        room.addArtifact(cupcake);

        ICommand eat = commandFactory.newEatCommand(galahad, room, cupcake);
        assertEquals(CommandType.EAT, eat.getType());
        assertEquals("cupcake", eat.optionString());

        assertTrue(eat.execute());
        assertEquals(11.5, galahad.getHealth(), 0.0001);
        assertFalse(room.hasArtifact(cupcake));
    }

    @Test
    void fightingHurtsTheOneWhoRollsLower() {
        Character ogre = characterFactory.createCreature("Ogre", 8.0, new FixedDie(1));
        room.addCharacter(ogre);

        ICommand fight = commandFactory.newFightCommand(galahad, room, ogre);
        assertEquals(CommandType.FIGHT, fight.getType());
        assertEquals("Ogre", fight.optionString());

        assertTrue(fight.execute());
        assertTrue(ogre.getHealth() < 8.0);
    }

    @Test
    void fightingNobodyFails() {
        ICommand fight = commandFactory.newFightCommand(galahad, room, null);
        assertFalse(fight.execute());
        assertEquals("nobody", fight.optionString());
    }

    @Test
    void fightingToTheDeathRunsUntilSomebodyDrops() {
        Character ogre = characterFactory.createCreature("Ogre", 3.0, new FixedDie(1));
        room.addCharacter(ogre);

        ICommand duel = commandFactory.newFightToTheDeathCommand(galahad, room, ogre);
        assertEquals(CommandType.FIGHT_TO_THE_DEATH, duel.getType());

        assertTrue(duel.execute());          // Galahad always rolls higher
        assertTrue(ogre.isDead());
        assertTrue(galahad.isAlive());
    }

    @Test
    void fightingToTheDeathWithNoOpponentFails() {
        assertFalse(commandFactory.newFightToTheDeathCommand(galahad, room, null).execute());
    }

    @Test
    void takingTreasureMapAndDieAllGoThroughOneCommand() {
        Artifact treasure = artifactFactory.createTreasure();
        room.addArtifact(treasure);
        ICommand take = commandFactory.newTakeCommand(galahad, room, treasure);
        assertEquals(CommandType.TAKE, take.getType());
        assertTrue(take.execute());
        assertTrue(galahad.hasTreasure());
        assertFalse(room.hasArtifact(treasure));

        Artifact map = artifactFactory.createTreasureMap("map to the gold");
        room.addArtifact(map);
        assertTrue(commandFactory.newTakeCommand(galahad, room, map).execute());
        assertFalse(room.hasArtifact(map));

        Artifact die = artifactFactory.createRandomDie(20);
        room.addArtifact(die);
        assertTrue(commandFactory.newTakeCommand(galahad, room, die).execute());
        assertFalse(room.hasArtifact(die));
    }

    @Test
    void wearingArmorSwapsInAnArmoredCharacter() {
        Armor iron = (Armor) artifactFactory.createArmor("iron", 2.0, 0.3);
        room.addArtifact(iron);

        ICommand wear = commandFactory.newWearCommand(galahad, room, iron);
        assertEquals(CommandType.WEAR, wear.getType());
        assertEquals("iron", wear.optionString());

        assertTrue(wear.execute());
        assertFalse(room.hasArtifact(iron));
        assertNotSame(galahad, room.getCharacterWithName("Galahad").orElseThrow());
    }

    @Test
    void wieldingAWeaponSwapsInAWeaponizedCharacter() {
        Weapon sword = (Weapon) artifactFactory.createWeapon("sword", 1.0);
        room.addArtifact(sword);

        ICommand wield = commandFactory.newWieldCommand(galahad, room, sword);
        assertEquals(CommandType.WIELD, wield.getType());
        assertTrue(wield.execute());
        assertFalse(room.hasArtifact(sword));
    }

    @Test
    void donningACloakSwapsInACloakedCharacter() {
        Cloak cloak = (Cloak) artifactFactory.createCloak("invisibility", 1.0);
        room.addArtifact(cloak);

        ICommand don = commandFactory.newDonCommand(galahad, room, cloak);
        assertEquals(CommandType.DON, don.getType());
        assertTrue(don.execute());
        assertFalse(room.hasArtifact(cloak));
        assertFalse(room.getCharacterWithName("Galahad").orElseThrow().isVisible());
    }

    @Test
    void drinkingAPotionSwapsInAReincarnatableCharacter() {
        Potion elixir = (Potion) artifactFactory.createPotion("elixir", 3.5);
        room.addArtifact(elixir);

        ICommand drink = commandFactory.newDrinkCommand(galahad, room, elixir);
        assertEquals(CommandType.DRINK, drink.getType());
        assertTrue(drink.execute());
        assertFalse(room.hasArtifact(elixir));
    }

    @Test
    void reincarnatingPutsTheRebornCharacterInTheRoom() {
        Character reborn = characterFactory.createKnight("Galahad", 5.0, new FixedDie(3));
        ICommand command = commandFactory.newReincarnateCommand(galahad, room, reborn);
        assertEquals(CommandType.REINCARNATE, command.getType());
        assertEquals("Galahad", command.optionString());

        assertTrue(command.execute());
        assertSame(reborn, room.getCharacterWithName("Galahad").orElseThrow());
    }

    @Test
    void reincarnatingWithNobodyToComeBackAsFails() {
        ICommand command = commandFactory.newReincarnateCommand(galahad, room, null);
        assertFalse(command.execute());
        assertEquals("", command.optionString());
    }

    @Test
    void executeCommandChargesTheTurnWhetherItWorkedOrNot() {
        double before = galahad.getHealth();
        commandFactory.newSleepCommand(galahad, room).executeCommand();
        assertEquals(before - Character.HEALTH_LOST_IN_AGING_EACH_TURN, galahad.getHealth(), 0.0001);

        double afterSleep = galahad.getHealth();
        commandFactory.newMoveCommand(galahad, room, null).executeCommand();   // fails
        assertEquals(afterSleep - Character.HEALTH_LOST_IN_AGING_EACH_TURN, galahad.getHealth(), 0.0001);
    }
}
