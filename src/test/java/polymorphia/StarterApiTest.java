package polymorphia;

import org.junit.jupiter.api.Test;
import polymorphia.artifacts.Artifact;
import polymorphia.artifacts.ArtifactBag;
import polymorphia.artifacts.ArtifactFactory;
import polymorphia.characters.Character;
import polymorphia.characters.CharacterFactory;
import polymorphia.maze.IMutableRoom;
import polymorphia.maze.Maze;
import polymorphia.maze.Room;
import polymorphia.maze.RoomFactory;
import polymorphia.strategy.StrategyFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A few public methods the starter code hands us that the game itself never ends up calling:
 * Character.pickUpDie, Character.takeArtifact, ArtifactBag.removeOneItemOfType and
 * Maze.getRoomContainingCharacterWithName. Nothing in the maze reaches them, so without these
 * tests they are the only methods left showing up uncovered. They are real API, so they get
 * checked here rather than deleted.
 */
class StarterApiTest {

    private final CharacterFactory characterFactory = new CharacterFactory(new StrategyFactory());
    private final ArtifactFactory artifactFactory = new ArtifactFactory();

    @Test
    void pickingUpADieSwapsYourDieAndTakesItOutOfTheRoom() {
        Room room = new Room("Dice Room");
        Character galahad = characterFactory.createKnight("Galahad", 10.0, new FixedDie(2));
        room.addCharacter(galahad);

        FixedDie betterDie = new FixedDie(6);
        room.addArtifact(betterDie);

        galahad.pickUpDie(room, betterDie);

        // a character prints its own die, which is the simplest way to see the swap took
        assertTrue(galahad.toString().contains("FixedDie{6}"), "the new die should be in hand");
        assertFalse(room.getAllArtifacts().contains(betterDie), "the die should be out of the room");
    }

    @Test
    void takingAnArtifactRemovesItFromTheRoom() {
        Room room = new Room("Treasure Room");
        Character galahad = characterFactory.createKnight("Galahad", 10.0, new FixedDie(4));
        room.addCharacter(galahad);

        Artifact treasure = artifactFactory.createTreasure("Crown");
        room.addArtifact(treasure);

        galahad.takeArtifact(room, treasure);

        assertFalse(room.getAllArtifacts().contains(treasure));
    }

    @Test
    void removingOneItemOfATypeHandsItBackAndEmptiesTheBag() {
        ArtifactBag bag = new ArtifactBag();
        Artifact sword = artifactFactory.createWeapon("Sword", 3.0);
        bag.add(sword);

        Optional<Artifact> pulled = bag.removeOneItemOfType(Artifact.class);

        assertTrue(pulled.isPresent());
        assertSame(sword, pulled.get());
        assertFalse(bag.contains(sword), "it should be gone from the bag now");
    }

    @Test
    void removingOneItemOfATypeThatIsNotThereGivesBackNothing() {
        ArtifactBag bag = new ArtifactBag();
        assertTrue(bag.removeOneItemOfType(FixedDie.class).isEmpty());
    }

    @Test
    void theMazeCanFindWhichRoomSomebodyIsStandingIn() {
        Character bilbo = characterFactory.createKnight("Bilbo");
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .create2x2Grid()
                .addCharacters(java.util.List.of(bilbo))
                .build();

        Optional<IMutableRoom> found = maze.getRoomContainingCharacterWithName("Bilbo");

        assertTrue(found.isPresent(), "Bilbo was added to the maze, so some room has him");
        assertTrue(found.get().containsCharacter(bilbo));
    }

    @Test
    void lookingForSomebodyWhoIsNotInTheMazeFindsNoRoom() {
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .create2x2Grid()
                .build();

        assertTrue(maze.getRoomContainingCharacterWithName("Nobody").isEmpty());
    }
}
