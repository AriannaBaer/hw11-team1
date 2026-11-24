package polymorphia;

import ooad.gameobserver.intf.EventType;
import ooad.gameobserver.intf.IMaze;
import org.junit.jupiter.api.Test;
import polymorphia.artifacts.Artifact;
import polymorphia.artifacts.ArtifactBag;
import polymorphia.artifacts.ArtifactFactory;
import polymorphia.artifacts.Food;
import polymorphia.characters.Character;
import polymorphia.characters.CharacterFactory;
import polymorphia.characters.Creature;
import polymorphia.maze.Maze;
import polymorphia.maze.RoomFactory;
import polymorphia.observers.TestObserver;
import polymorphia.strategy.FighterStrategy;
import polymorphia.strategy.StrategyFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Odds and ends of public API that the game never calls on its own: the viewer adapter, the
 * convenience constructors, the artifact bag's lookups, and the strategy flags that only have
 * default implementations.
 */
class CoreApiTest {

    private final CharacterFactory characterFactory = new CharacterFactory(new StrategyFactory());
    private final ArtifactFactory artifactFactory = new ArtifactFactory();

    private Maze smallMazeWithSomebodyInIt() {
        return Maze.getNewBuilder(new RoomFactory())
                .create2x2Grid()
                .addCharacters(List.of(characterFactory.createKnight("Bilbo")))
                .addArtifacts(List.of(artifactFactory.createFood("Cupcake")))
                .build();
    }

    // ---------- the adapter the maze viewer talks to ----------

    @Test
    void theAdapterDescribesEveryRoomInTheMaze() {
        Maze maze = smallMazeWithSomebodyInIt();
        MazeAdapter adapter = new MazeAdapter(maze);

        assertEquals(4, adapter.getRoomNames().size());

        String someRoom = adapter.getRoomNames().iterator().next();
        assertNotNull(adapter.getNeighborsOf(someRoom));
        assertNotNull(adapter.getArtifacts(someRoom));
        assertNotNull(adapter.getCharacters(someRoom));
    }

    @Test
    void theAdapterPrintsEachRoomWithItsNeighboursCharactersAndArtifacts() {
        MazeAdapter adapter = new MazeAdapter(smallMazeWithSomebodyInIt());

        String printed = adapter.toString();

        assertTrue(printed.contains("Neighbors:"));
        assertTrue(printed.contains("characters:"));
        assertTrue(printed.contains("artifacts:"));
        assertTrue(printed.contains("Bilbo"));
    }

    // ---------- the observable game's subject side ----------

    @Test
    void observersCanBeAttachedForCertainEventsAndDetachedAgain() {
        ObservablePolymorphia game = new ObservablePolymorphia(smallMazeWithSomebodyInIt());
        TestObserver observer = new TestObserver();

        game.attach(observer, List.of(EventType.FINISHED_TURN));
        game.detach(observer);

        // the point is that both calls go through without blowing up; the game still works after
        assertNotNull(game.getMaze());
    }

    @Test
    void theGameHandsTheViewerAnAdapterRatherThanTheMazeItself() {
        ObservablePolymorphia game = new ObservablePolymorphia(smallMazeWithSomebodyInIt());

        IMaze asTheViewerSeesIt = game.getMaze();

        assertInstanceOf(MazeAdapter.class, asTheViewerSeesIt);
        assertEquals(4, asTheViewerSeesIt.getRoomNames().size());
    }

    // ---------- artifacts ----------

    @Test
    void aPlainArtifactIsNotTreasureNorAMapNorADie() {
        Artifact plain = artifactFactory.createFood("Cupcake");

        assertFalse(plain.isTreasure());
        assertFalse(plain.isTreasureMap());
        assertFalse(plain.isDie());
    }

    @Test
    void aRandomDieKnowsItIsADie() {
        assertTrue(new RandomDie(6).isDie());
    }

    @Test
    void theBagAnswersByTypeAndPrintsWhatIsInIt() {
        ArtifactBag bag = new ArtifactBag();
        assertFalse(bag.contains(Food.class));

        Artifact cupcake = artifactFactory.createFood("Cupcake");
        bag.add(cupcake);

        assertTrue(bag.contains(Food.class));
        assertEquals(1, bag.getArtifacts(Food.class).size());
        assertTrue(bag.toString().contains("Cupcake"));
    }

    @Test
    void treasuresCanBeMadeInBulk() {
        List<Artifact> treasures = artifactFactory.createTreasures(3);

        assertEquals(3, treasures.size());
        assertTrue(treasures.stream().allMatch(Artifact::isTreasure));
    }

    // ---------- characters ----------

    @Test
    void theShortCharacterConstructorsFillInADieAndAStrategy() {
        // Character is abstract, so this is the smallest thing that can stand in for one
        Character justAName = new Character("Ghost", 5.0) {
        };

        assertEquals("Ghost", justAName.getName());
        assertEquals(5.0, justAName.getHealth(), 0.0001);
        assertTrue(justAName.isAlive());
        // the two-argument constructor hands down to the one that takes a die
        assertTrue(justAName.getRoll() >= 1);
    }

    @Test
    void theShortCreatureConstructorsFillInHealthAndADie() {
        Creature defaults = new Creature("Ogre");
        Creature withHealth = new Creature("Troll", 7.0);
        Creature withDie = new Creature("Goblin", 4.0, new FixedDie(5));

        assertEquals(3.0, defaults.getHealth(), 0.0001);
        assertEquals(7.0, withHealth.getHealth(), 0.0001);
        assertEquals(5, withDie.getRoll());
        assertTrue(defaults.isCreature());
    }

    @Test
    void theFactoryCanMakeCharactersWithWhateverStrategyItFeelsLike() {
        Character one = characterFactory.createCharacter();
        assertNotNull(one.getName());

        List<Character> several = characterFactory.createCharacters(5);
        assertEquals(5, several.size());
        assertTrue(several.stream().allMatch(Character::isAlive));
    }

    @Test
    void aDecoratedCharacterPassesANewDieThroughToTheRealCharacter() {
        Character galahad = characterFactory.createKnight("Galahad", 10.0, new FixedDie(2));
        Character armored = characterFactory.newArmoredCharacter(
                galahad, (polymorphia.artifacts.Armor) artifactFactory.createArmor("Plate", 1.0, 0.1));

        armored.setDie(new FixedDie(6));

        // the die belongs to the character underneath, not to the wrapper
        assertEquals(6, galahad.getRoll());
        assertEquals(6, armored.getRoll());
    }

    @Test
    void aDecoratedCharacterPassesANewStrategyThroughToTheRealCharacter() {
        Character galahad = characterFactory.createKnight("Galahad", 10.0, new FixedDie(2));
        Character armored = characterFactory.newArmoredCharacter(
                galahad, (polymorphia.artifacts.Armor) artifactFactory.createArmor("Plate", 1.0, 0.1));

        armored.setStrategy(new polymorphia.strategy.SleepStrategy());

        // the strategy is set on the character underneath, so both of them sleep now
        polymorphia.maze.Room room = new polymorphia.maze.Room("Quiet");
        room.addCharacter(galahad);
        assertEquals(polymorphia.commands.CommandType.SLEEP, armored.getAction(room).getType());
        assertEquals(polymorphia.commands.CommandType.SLEEP, galahad.getAction(room).getType());
    }

    // ---------- strategy flags ----------

    @Test
    void aStrategyThatIsNeitherSeekerNorGuardSaysSo() {
        FighterStrategy fighter = new FighterStrategy();

        // FighterStrategy overrides isFighter but leaves these to IStrategy's defaults
        assertFalse(fighter.isSeeker());
        assertFalse(fighter.isGuard());
        assertTrue(fighter.isFighter());
    }
}
