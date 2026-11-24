package polymorphia.maze;

import org.junit.jupiter.api.Test;
import polymorphia.FixedDie;
import polymorphia.artifacts.Artifact;
import polymorphia.artifacts.ArtifactFactory;
import polymorphia.artifacts.Food;
import polymorphia.characters.Character;
import polymorphia.characters.CharacterFactory;
import polymorphia.strategy.StrategyFactory;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The parts of the maze API that the game itself never happens to use, plus the builder shapes
 * the other tests do not build. Lives in polymorphia.maze because RoomFactory's createPantry and
 * createPortal are package-private.
 */
class MazeApiTest {

    private final CharacterFactory characterFactory = new CharacterFactory(new StrategyFactory());
    private final ArtifactFactory artifactFactory = new ArtifactFactory();

    // ---------- builder shapes ----------

    @Test
    void aStraightLineConnectsEachRoomToTheNextOneOnly() {
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .createStraightLineRooms("One", "Two", "Three")
                .build();

        assertEquals(3, maze.size());
        assertTrue(maze.getRoom("One").hasNeighbor(maze.getRoom("Two")));
        assertTrue(maze.getRoom("Two").hasNeighbor(maze.getRoom("Three")));
        // connections are two-way unless the builder is told otherwise
        assertTrue(maze.getRoom("Three").hasNeighbor(maze.getRoom("Two")));
    }

    @Test
    void aRingOfRoomsIsBuiltFromTheNamesGiven() {
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .createRingOfConnectedRooms("North", "East", "South", "West")
                .build();

        assertEquals(4, maze.size());
        assertTrue(maze.getRoom("North").hasNeighbor(maze.getRoom("East")));
    }

    @Test
    void fullyConnectedMeansEveryRoomReachesEveryOtherRoom() {
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .createFullyConnectedRooms("A", "B", "C")
                .build();

        assertEquals(3, maze.size());
        for (String name : List.of("A", "B", "C")) {
            assertTrue(maze.getRoom(name).numberOfNeighbors() > 0, name + " should be connected");
        }
    }

    @Test
    void aGridIsNamedByRowAndColumn() {
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .createAGridOfRooms(2, 2, "Cell")
                .build();

        assertEquals(4, maze.size());
        assertNotNull(maze.getRoom("Cell0-0"));
        assertNotNull(maze.getRoom("Cell1-1"));
    }

    @Test
    void roomsCanBeCreatedFromAListOfNamesWithNoConnections() {
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .createRooms(List.of("Alpha", "Beta"))
                .build();

        // createRooms only makes the rooms; wiring them up is somebody else's job
        assertEquals(2, maze.size());
        assertEquals("Alpha", maze.getRoom("Alpha").getName());
        assertEquals("Beta", maze.getRoom("Beta").getName());
    }

    // ---------- looking things up ----------

    @Test
    void askingForARoomThatIsNotThereIsAnError() {
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .createRooms(List.of("Only"))
                .build();

        IllegalArgumentException thrown =
                assertThrows(IllegalArgumentException.class, () -> maze.getRoom("Nowhere"));
        assertTrue(thrown.getMessage().contains("Nowhere"));
    }

    @Test
    void removingAnArtifactNoRoomHasIsAnError() {
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .createRooms(List.of("Only"))
                .build();
        Artifact stray = artifactFactory.createTreasure("Not in the maze");

        assertThrows(IllegalArgumentException.class, () -> maze.removeArtifact(stray));
    }

    @Test
    void theMazeGroupsItsLivingCharactersByRoom() {
        Character bilbo = characterFactory.createKnight("Bilbo");
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .create2x2Grid()
                .addCharacters(List.of(bilbo))
                .build();

        var byRoom = maze.getLivingCharactersByRoom();

        assertTrue(byRoom.values().stream().anyMatch(set -> set.contains(bilbo)));
    }

    @Test
    void aSingleRoomGroupsItsOwnCharactersUnderItself() {
        // Room does not override getLivingCharactersByRoom, so this is IRoom's default
        Room room = new Room("Solo");
        Character bilbo = characterFactory.createKnight("Bilbo");
        room.addCharacter(bilbo);

        var byRoom = room.getLivingCharactersByRoom();

        assertEquals(1, byRoom.size());
        assertEquals(Set.of(bilbo), byRoom.get(room));
    }

    @Test
    void theMazeAnswersByNameAndHandsBackEverythingInIt() {
        // Maze does not override containsCharacterWithName or getAllArtifacts, so these are
        // IRoom's defaults rather than Room's versions
        Character bilbo = characterFactory.createKnight("Bilbo");
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .create2x2Grid()
                .addCharacters(List.of(bilbo))
                .addArtifacts(List.of(artifactFactory.createTreasure("Crown")))
                .build();

        assertTrue(maze.containsCharacterWithName("Bilbo"));
        assertFalse(maze.containsCharacterWithName("Smaug"));
        assertEquals(1, maze.getAllArtifacts().size());
    }

    // ---------- moving people around ----------

    @Test
    void enteringTheMazeTakesYouOutOfTheRoomYouCameFrom() {
        Character bilbo = characterFactory.createKnight("Bilbo");
        Room elsewhere = new Room("Elsewhere");
        elsewhere.addCharacter(bilbo);

        Maze maze = Maze.getNewBuilder(new RoomFactory()).create2x2Grid().build();
        maze.enter(bilbo, elsewhere);

        assertFalse(elsewhere.containsCharacter(bilbo));
        assertTrue(maze.containsCharacter(bilbo));
    }

    @Test
    void replacingSomebodySwapsThemInWhicheverRoomTheyAreStandingIn() {
        Character bilbo = characterFactory.createKnight("Bilbo", 10.0, new FixedDie(3));
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .create2x2Grid()
                .addCharacters(List.of(bilbo))
                .build();

        Character frodo = characterFactory.createKnight("Frodo", 10.0, new FixedDie(3));
        maze.replace(bilbo, frodo);

        assertFalse(maze.containsCharacter(bilbo));
        assertTrue(maze.containsCharacter(frodo));
    }

    @Test
    void theMazeCanTakeSomebodyOutOfWhicheverRoomTheyAreStandingIn() {
        Character bilbo = characterFactory.createKnight("Bilbo");
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .create2x2Grid()
                .addCharacters(List.of(bilbo))
                .build();
        assertTrue(maze.containsCharacter(bilbo));

        maze.removeCharacter(bilbo);

        assertFalse(maze.containsCharacter(bilbo));
    }

    @Test
    void aMazeCanBeGivenANeighborWithoutSayingWhetherItIsTwoWay() {
        // Maze does not override the one-argument addNeighbor, so this is IMutableRoom's default
        Maze maze = Maze.getNewBuilder(new RoomFactory())
                .createRooms(List.of("Inside"))
                .build();
        Room outside = new Room("Outside");

        maze.addNeighbor(outside);

        assertTrue(maze.getRoom("Inside").hasNeighbor(outside));
    }

    // ---------- rooms ----------

    @Test
    void aRoomCanSayWhetherItHoldsAKindOfArtifact() {
        Room room = new Room("Larder");
        assertFalse(room.contains(Food.class));

        room.addArtifact(artifactFactory.createFood("Cupcake"));

        assertTrue(room.contains(Food.class));
    }

    @Test
    void theRoomFactoryMakesPantriesAndPortalsWithNamesOffItsOwnList() {
        RoomFactory roomFactory = new RoomFactory();

        Room pantry = roomFactory.createPantry();
        Room portal = roomFactory.createPortal();

        assertInstanceOf(Pantry.class, pantry);
        assertInstanceOf(Portal.class, portal);
        assertFalse(pantry.getName().isBlank());
        assertFalse(portal.getName().isBlank());
        // a pantry conjures food the moment somebody looks for it
        assertFalse(pantry.getItemsOf(Food.class).isEmpty());
    }
}
