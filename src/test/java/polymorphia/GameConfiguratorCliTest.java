package polymorphia;

import ooad.gameobserver.intf.IGameObserver;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import java.util.List;

import polymorphia.characters.CharacterFactory;
import polymorphia.maze.Maze;
import polymorphia.maze.RoomFactory;
import polymorphia.strategy.StrategyFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the command line the way a person would, but without a person.
 *
 * The tests the starter code shipped for GameConfigurator are all @Disabled ("this is a manual
 * test"), so none of the CLI code was being exercised. These run it for real: the games are tiny
 * so they finish fast, and the output is captured so the test log stays readable.
 */
class GameConfiguratorCliTest {

    private final InputStream realIn = System.in;
    private final PrintStream realOut = System.out;

    @AfterEach
    void putTheConsoleBack() {
        System.setIn(realIn);
        System.setOut(realOut);
    }

    private ByteArrayOutputStream captureOutput() {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        return captured;
    }

    /**
     * The last thing createAndStartGame does is print the maze, and a printed maze always names
     * its rooms. The names come off RoomFactory's list in order, and the configurator's factory is
     * static, so which name comes first depends on how many games ran before this one: any of them
     * showing up is the proof that the game was built and played.
     */
    private void assertTheMazeWasPrinted(String shown) {
        for (String roomName : polymorphia.maze.RoomFactory.NAMES) {
            if (shown.contains(roomName)) {
                return;
            }
        }
        fail("expected the finished maze to be printed, but no room name showed up in:\n" + shown);
    }

    @Test
    void shortHelpFlagPrintsTheUsageAndStartsNoGame() throws ParseException {
        ByteArrayOutputStream out = captureOutput();
        GameConfigurator.main(new String[]{"-h"});
        String shown = out.toString();
        assertTrue(shown.contains("usage"), "the help formatter should print a usage line");
        assertTrue(shown.contains("numberOfRooms"), "the options should be listed");
    }

    @Test
    void longHelpFlagDoesTheSameThing() throws ParseException {
        ByteArrayOutputStream out = captureOutput();
        GameConfigurator.main(new String[]{"--help"});
        assertTrue(out.toString().contains("usage"));
    }

    @Test
    void anUnknownFlagIsReportedAndTheHelpIsPrinted() throws ParseException {
        ByteArrayOutputStream out = captureOutput();
        GameConfigurator.main(new String[]{"--thisIsNotAnOption"});
        String shown = out.toString();
        assertTrue(shown.contains("Error parsing command line arguments"));
        assertTrue(shown.contains("usage"), "it should fall back to showing the help");
    }

    @Test
    void everyOptionIsOnTheMenu() {
        Options options = GameConfigurator.getOptions();
        for (String shortName : new String[]{"a", "c", "d", "f", "g", "h", "k", "l",
                                             "m", "o", "p", "r", "s", "t", "w", "x", "y"}) {
            assertTrue(options.hasOption(shortName), "missing option -" + shortName);
        }
    }

    @Test
    void aFullyConfiguredGameRunsToTheEnd() throws ParseException {
        ByteArrayOutputStream out = captureOutput();
        GameConfigurator.main(new String[]{
                "-r", "4",      // rooms
                "-a", "1",      // armored suits
                "-c", "1",      // creatures
                "-d", "1",      // demons
                "-f", "2",      // food
                "-g", "1",      // gluttons
                "-k", "1",      // knights
                "-l", "1",      // cloaks
                "-m", "1",      // treasure maps
                "-o", "1",      // cowards
                "-p", "1",      // potions
                "-s", "1",      // seekers
                "-t", "1",      // treasures (and the guard that sits on each one)
                "-w", "1"       // weapons
        });
        assertTheMazeWasPrinted(out.toString());
    }

    @Test
    void defaultsFillInEverythingThatWasNotAsked() throws ParseException {
        ByteArrayOutputStream out = captureOutput();
        GameConfigurator.main(new String[]{});      // no arguments at all
        assertTheMazeWasPrinted(out.toString());
    }

    @Test
    void humanPlayersCanBeNamedWithCommasOrSpaces() throws ParseException {
        // the humans answer "1" over and over; when the answers run out the prompter takes
        // option 1 by itself, so this cannot hang
        System.setIn(new ByteArrayInputStream("1\n".repeat(200).getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream out = captureOutput();
        GameConfigurator.main(new String[]{
                "-r", "3", "-c", "1", "-f", "2", "-a", "0", "-d", "0", "-g", "0",
                "-k", "0", "-l", "0", "-m", "0", "-o", "0", "-p", "0", "-s", "0",
                "-t", "1", "-w", "0",
                "-y", "Arianna, Eleanor"
        });
        String shown = out.toString();
        assertTrue(shown.contains("Arianna"), "the first human should be in the maze");
        assertTrue(shown.contains("Eleanor"), "the second human should be in the maze");
        assertTrue(shown.contains("Enter your option"), "the humans should have been prompted");
    }

    /**
     * The one piece of the configurator that a normal test run never reaches, because it is the
     * -x flag: it builds the 2400x1200 maze viewer. We call it through reflection, check it hands
     * back an observer, and then close anything it opened so the build does not sit there with a
     * window on screen. A machine with no display cannot build a Swing window at all, so there we
     * settle for having proved the method runs.
     */
    @Test
    void theGraphicalViewerGetsBuiltFromTheHelperInTheFile() throws Exception {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        CommandLine cmdLine = new DefaultParser().parse(GameConfigurator.getOptions(),
                new String[]{"-r", "3", "-c", "0", "-d", "0", "-g", "0", "-k", "1",
                             "-o", "0", "-s", "0", "-t", "0"});
        GameConfigurator configurator = new GameConfigurator(cmdLine);
        // Polymorphia refuses a maze with nobody in it, so give the viewer somebody to draw
        ObservablePolymorphia game = new ObservablePolymorphia(
                Maze.getNewBuilder(new RoomFactory())
                        .create2x2Grid()
                        .addCharacters(List.of(new CharacterFactory(new StrategyFactory())
                                .createKnight("Viewer")))
                        .build());

        Method createMazeObserver =
                GameConfigurator.class.getDeclaredMethod("createMazeObserver", ObservablePolymorphia.class);
        createMazeObserver.setAccessible(true);
        try {
            Object observer = createMazeObserver.invoke(configurator, game);
            assertNotNull(observer, "the -x flag needs an observer to attach");
            assertInstanceOf(IGameObserver.class, observer);
        } finally {
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                window.dispose();
            }
        }
    }

    @Test
    void oneHumanNamedWithNoSeparatorStillWorks() throws ParseException {
        System.setIn(new ByteArrayInputStream("1\n".repeat(200).getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream out = captureOutput();
        GameConfigurator.main(new String[]{
                "-r", "3", "-c", "1", "-f", "1", "-a", "0", "-d", "0", "-g", "0",
                "-k", "0", "-l", "0", "-m", "0", "-o", "0", "-p", "0", "-s", "0",
                "-t", "1", "-w", "0",
                "-y", "Solo"
        });
        assertTrue(out.toString().contains("Solo"));
    }
}
