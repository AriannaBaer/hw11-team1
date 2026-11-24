package polymorphia.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polymorphia.FixedDie;
import polymorphia.artifacts.ArtifactFactory;
import polymorphia.characters.Character;
import polymorphia.characters.CharacterFactory;
import polymorphia.commands.CommandType;
import polymorphia.commands.ICommand;
import polymorphia.maze.Room;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Each strategy now only picks. These check that the right pick comes back for the
 * situation, without any of them actually changing the world.
 */
class StrategyChoiceTest {

    private final CharacterFactory characterFactory = new CharacterFactory(new StrategyFactory());
    private final ArtifactFactory artifactFactory = new ArtifactFactory();
    private final StrategyFactory strategyFactory = new StrategyFactory();

    private Room room;
    private Character me;

    @BeforeEach
    void setUp() {
        room = new Room("Starting");
        Room other = new Room("Other");
        room.addNeighbor(other, true);
        me = characterFactory.createKnight("Galahad", 10.0, new FixedDie(4));
        room.addCharacter(me);
    }

    private CommandType choiceOf(IStrategy strategy) {
        ICommand command = strategy.getAction(me, room);
        assertNotNull(command);
        return command.getType();
    }

    @Test
    void aFighterGearsUpBeforeFightingAndFightsBeforeEating() {
        room.addArtifact(artifactFactory.createFood("cupcake", 1.0));
        room.addCharacter(characterFactory.createCreature("Ogre", 5.0, new FixedDie(1)));
        assertEquals(CommandType.FIGHT, choiceOf(strategyFactory.newFighterStrategy()));

        room.addArtifact(artifactFactory.createWeapon("sword", 1.0));
        assertEquals(CommandType.WIELD, choiceOf(strategyFactory.newFighterStrategy()));

        room.addArtifact(artifactFactory.createArmor("iron", 2.0, 0.3));
        assertEquals(CommandType.WEAR, choiceOf(strategyFactory.newFighterStrategy()));
    }

    @Test
    void aFighterInAnEmptyRoomMoves() {
        assertEquals(CommandType.MOVE, choiceOf(strategyFactory.newFighterStrategy()));
    }

    @Test
    void aFighterEatsWhenThereIsNothingElseToDo() {
        room.addArtifact(artifactFactory.createFood("cupcake", 1.0));
        assertEquals(CommandType.EAT, choiceOf(strategyFactory.newFighterStrategy()));
    }

    @Test
    void aCowardHidesFirstThenRunsFromCreatures() {
        room.addCharacter(characterFactory.createCreature("Ogre", 5.0, new FixedDie(1)));
        assertEquals(CommandType.MOVE, choiceOf(strategyFactory.newCowardStrategy()));

        room.addArtifact(artifactFactory.createCloak("invisibility", 1.0));
        assertEquals(CommandType.DON, choiceOf(strategyFactory.newCowardStrategy()));
    }

    @Test
    void aCowardAloneTakesAMapOrEats() {
        room.addArtifact(artifactFactory.createFood("cupcake", 1.0));
        assertEquals(CommandType.EAT, choiceOf(strategyFactory.newCowardStrategy()));

        room.addArtifact(artifactFactory.createTreasureMap("map"));
        assertEquals(CommandType.TAKE, choiceOf(strategyFactory.newCowardStrategy()));
    }

    @Test
    void aGluttonDrinksPotionsBeforeEating() {
        room.addArtifact(artifactFactory.createFood("cupcake", 1.0));
        assertEquals(CommandType.EAT, choiceOf(strategyFactory.newGluttonStrategy()));

        room.addArtifact(artifactFactory.createPotion("elixir", 3.5));
        assertEquals(CommandType.DRINK, choiceOf(strategyFactory.newGluttonStrategy()));
    }

    @Test
    void aGluttonInAnEmptyRoomMoves() {
        assertEquals(CommandType.MOVE, choiceOf(strategyFactory.newGluttonStrategy()));
    }

    @Test
    void aCreatureFightsWhoeverIsThereOtherwiseMoves() {
        assertEquals(CommandType.MOVE, choiceOf(strategyFactory.newCreatureStrategy()));

        room.addCharacter(characterFactory.createCreature("Ogre", 5.0, new FixedDie(1)));
        assertEquals(CommandType.FIGHT, choiceOf(strategyFactory.newCreatureStrategy()));
    }

    @Test
    void aGuardStandsOverTreasureAndOtherwiseActsLikeACreature() {
        room.addArtifact(artifactFactory.createTreasure());
        assertEquals(CommandType.SLEEP, choiceOf(strategyFactory.newGuardStrategy()));

        Room plainRoom = new Room("NoTreasure");
        plainRoom.addNeighbor(new Room("Beyond"), true);
        plainRoom.addCharacter(me);
        assertEquals(CommandType.MOVE, strategyFactory.newGuardStrategy().getAction(me, plainRoom).getType());

        plainRoom.addCharacter(characterFactory.createCreature("Ogre", 5.0, new FixedDie(1)));
        assertEquals(CommandType.FIGHT, strategyFactory.newGuardStrategy().getAction(me, plainRoom).getType());
    }

    @Test
    void aSeekerMovesUntilItFindsTreasureThenTakesIt() {
        assertEquals(CommandType.MOVE, choiceOf(strategyFactory.newSeekerStrategy()));

        room.addArtifact(artifactFactory.createTreasure());
        assertEquals(CommandType.TAKE, choiceOf(strategyFactory.newSeekerStrategy()));
    }

    @Test
    void aSeekerFightsAGuardToTheDeathForTheTreasure() {
        room.addArtifact(artifactFactory.createTreasure());
        room.addCharacter(characterFactory.createCreature("Guard", 5.0, new FixedDie(1)));
        assertEquals(CommandType.FIGHT_TO_THE_DEATH, choiceOf(strategyFactory.newSeekerStrategy()));
    }

    @Test
    void aSleeperAlwaysSleeps() {
        assertEquals(CommandType.SLEEP, choiceOf(new SleepStrategy()));
    }

    @Test
    void theStrategyFlagsSayWhoIsWho() {
        assertTrue(strategyFactory.newFighterStrategy().isFighter());
        assertTrue(strategyFactory.newCowardStrategy().isCoward());
        assertTrue(strategyFactory.newGluttonStrategy().isGlutton());
        assertTrue(strategyFactory.newGuardStrategy().isGuard());
        assertTrue(strategyFactory.newSeekerStrategy().isSeeker());
        assertFalse(strategyFactory.newCreatureStrategy().isFighter());
    }

    @Test
    void anInvisibleCharacterIsNotAValidTarget() {
        Character hidden = characterFactory.newCloakedCharacter(
                characterFactory.createCoward("Sneaky"),
                (polymorphia.artifacts.Cloak) artifactFactory.createCloak("invisibility", 1.0));
        Room empty = new Room("Quiet");
        empty.addNeighbor(new Room("Beyond"), true);
        empty.addCharacter(me);
        empty.addCharacter(hidden);

        // nobody visible to fight, so the creature strategy walks instead
        assertEquals(CommandType.MOVE, strategyFactory.newCreatureStrategy().getAction(me, empty).getType());
    }
}
