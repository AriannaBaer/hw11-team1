package polymorphia;

import org.junit.jupiter.api.Test;
import polymorphia.characters.Character;
import polymorphia.characters.CharacterFactory;
import polymorphia.maze.Maze;
import polymorphia.maze.RoomFactory;
import polymorphia.strategy.StrategyFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The game ending badly.
 *
 * Every other test ends with an adventurer still standing or with the treasure found, so the
 * "the creatures won" branch of getFinalResults had never run, and the two private helpers it
 * uses - getCreatureNames and getLivingCreatures - were the last uncovered methods in the
 * project. This puts one doomed knight in a locked room with a much bigger ogre.
 */
class CreaturesWinTest {

    private final CharacterFactory characterFactory = new CharacterFactory(new StrategyFactory());

    @Test
    void whenEveryAdventurerDiesTheCreaturesAreLeftStanding() {
        // a fixed die of 1 against a fixed die of 6 loses every exchange, and with one room and
        // no neighbors there is nowhere to run to and nothing else to do but fight
        Character doomed = characterFactory.createKnight("Doomed", 1.5, new FixedDie(1));
        Character ogre = characterFactory.createCreature("Ogre", 50.0, new FixedDie(6));

        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .createRooms(List.of("Arena"))
                .addToRoom("Arena", doomed)
                .addToRoom("Arena", ogre)
                .build();

        Polymorphia game = new Polymorphia(maze);
        game.play();

        assertTrue(game.isOver());
        assertFalse(game.hasLivingAdventurers(), "the knight should not have survived that");
        assertTrue(game.hasLivingCreatures(), "the ogre should still be standing");
        // getWinners is simply whoever is left standing, so here it is the ogre
        assertEquals(1, game.getWinners().size());
        assertTrue(game.getWinners().contains(ogre));
    }
}
