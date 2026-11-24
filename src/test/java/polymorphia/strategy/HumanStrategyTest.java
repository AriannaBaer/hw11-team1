package polymorphia.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import polymorphia.FixedDie;
import polymorphia.artifacts.*;
import polymorphia.characters.Character;
import polymorphia.characters.CharacterFactory;
import polymorphia.commands.CommandType;
import polymorphia.commands.ICommand;
import polymorphia.maze.Room;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The human player's menu, driven by a scripted prompter.
 *
 * The menu is built fresh from the room every turn, so what gets offered changes as the room
 * changes. Options are listed in the order HumanStrategy builds them: FIGHT, EAT, WEAR, WIELD,
 * DON, DRINK, TAKE, then MOVE and SLEEP, which are always last and always present.
 */
class HumanStrategyTest {

    private final CharacterFactory characterFactory = new CharacterFactory(new StrategyFactory());
    private final ArtifactFactory artifactFactory = new ArtifactFactory();

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
    void anEmptyRoomStillOffersMoveAndSleep() {
        ScriptedPrompter prompter = new ScriptedPrompter("1");     // MOVE
        ICommand chosen = new HumanStrategy(prompter).getAction(galahad, room);

        assertEquals(CommandType.MOVE, chosen.getType());
        assertTrue(prompter.transcript().contains("MOVE"));
        assertTrue(prompter.transcript().contains("SLEEP"));
    }

    @Test
    void theOnlyTargetIsChosenWithoutASecondMenu() {
        room.addArtifact(artifactFactory.createFood("cupcake", 1.0));

        // menu is FIGHT? no -> EAT, MOVE, SLEEP. Pick 1 = EAT, and there is only one cupcake
        ScriptedPrompter prompter = new ScriptedPrompter("1");
        ICommand chosen = new HumanStrategy(prompter).getAction(galahad, room);

        assertEquals(CommandType.EAT, chosen.getType());
        assertEquals("cupcake", chosen.optionString());
        assertTrue(prompter.everythingWasUsed(), "should not have asked a second question");
    }

    @Test
    void twoTargetsMeansASecondMenu() {
        Character ogre = characterFactory.createCreature("Ogre", 5.0, new FixedDie(1));
        Character dragon = characterFactory.createCreature("Dragon", 5.0, new FixedDie(1));
        room.addCharacter(ogre);
        room.addCharacter(dragon);

        // 1 = FIGHT, then 2 = the second opponent listed
        ScriptedPrompter prompter = new ScriptedPrompter("1", "2");
        ICommand chosen = new HumanStrategy(prompter).getAction(galahad, room);

        assertEquals(CommandType.FIGHT, chosen.getType());
        assertTrue(prompter.everythingWasUsed());
        assertTrue(prompter.transcript().contains("Select opponent:"));
    }

    @Test
    void everyKindOfThingInTheRoomShowsUpOnTheMenu() {
        room.addCharacter(characterFactory.createCreature("Ogre", 5.0, new FixedDie(1)));
        room.addArtifact(artifactFactory.createFood("cupcake", 1.0));
        room.addArtifact(artifactFactory.createArmor("iron", 2.0, 0.3));
        room.addArtifact(artifactFactory.createWeapon("sword", 1.0));
        room.addArtifact(artifactFactory.createCloak("invisibility", 1.0));
        room.addArtifact(artifactFactory.createPotion("elixir", 3.5));
        room.addArtifact(artifactFactory.createTreasure());

        ScriptedPrompter prompter = new ScriptedPrompter("1");
        new HumanStrategy(prompter).getAction(galahad, room);

        String menu = prompter.transcript();
        for (String expected : new String[]{"FIGHT", "EAT", "WEAR", "WIELD", "DON", "DRINK", "TAKE", "MOVE", "SLEEP"}) {
            assertTrue(menu.contains(expected), "menu should offer " + expected);
        }
    }

    @Test
    void aNumberThatIsNotOnTheMenuJustAsksAgain() {
        ScriptedPrompter prompter = new ScriptedPrompter("99", "1");
        ICommand chosen = new HumanStrategy(prompter).getAction(galahad, room);

        assertEquals(CommandType.MOVE, chosen.getType());
        assertTrue(prompter.transcript().contains("Please enter a number"));
    }

    @Test
    void somethingThatIsNotANumberJustAsksAgain() {
        ScriptedPrompter prompter = new ScriptedPrompter("banana", "2");
        ICommand chosen = new HumanStrategy(prompter).getAction(galahad, room);

        assertEquals(CommandType.SLEEP, chosen.getType());
        assertTrue(prompter.transcript().contains("is not a number"));
    }

    @Test
    void theRoomIsDescribedBeforeTheMenu() {
        room.addCharacter(characterFactory.createCreature("Ogre", 5.0, new FixedDie(1)));
        room.addArtifact(artifactFactory.createFood("cupcake", 1.0));

        ScriptedPrompter prompter = new ScriptedPrompter("1", "1");
        new HumanStrategy(prompter).getAction(galahad, room);

        String shown = prompter.transcript();
        assertTrue(shown.contains("Starting"));
        assertTrue(shown.contains("Ogre"));
        assertTrue(shown.contains("cupcake"));
    }

    @Test
    void theRealConstructorWiresUpARealPrompter() {
        assertNotNull(new HumanStrategy());
    }
}
