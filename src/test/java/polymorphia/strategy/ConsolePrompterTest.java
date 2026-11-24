package polymorphia.strategy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The real prompter, fed a fake keyboard. */
class ConsolePrompterTest {

    private final InputStream realIn = System.in;
    private final PrintStream realOut = System.out;

    @AfterEach
    void putTheConsoleBack() {
        System.setIn(realIn);
        System.setOut(realOut);
    }

    private ByteArrayOutputStream captureOutputAndType(String typed) {
        System.setIn(new ByteArrayInputStream(typed.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        return captured;
    }

    @Test
    void itReadsAChoiceFromTheKeyboard() {
        ByteArrayOutputStream out = captureOutputAndType("2\n");
        int picked = new ConsolePrompter().choose("Pick one:", List.of("first", "second", "third"));
        assertEquals(1, picked);
        assertTrue(out.toString().contains("second"));
    }

    @Test
    void itKeepsAskingUntilTheAnswerIsOnTheMenu() {
        ByteArrayOutputStream out = captureOutputAndType("nope\n9\n1\n");
        int picked = new ConsolePrompter().choose("Pick one:", List.of("only", "other"));
        assertEquals(0, picked);
        String shown = out.toString();
        assertTrue(shown.contains("is not a number"));
        assertTrue(shown.contains("Please enter a number"));
    }

    @Test
    void printAndPrintlnGoToTheConsole() {
        ByteArrayOutputStream out = captureOutputAndType("");
        ConsolePrompter prompter = new ConsolePrompter();
        prompter.print("no newline");
        prompter.println("with newline");
        assertTrue(out.toString().contains("no newline"));
        assertTrue(out.toString().contains("with newline"));
    }

    @Test
    void readLineSaysNullWhenThereIsNothingLeft() {
        captureOutputAndType("");                 // empty stream: straight to end of input
        ConsolePrompter prompter = new ConsolePrompter();
        assertNull(prompter.readLine());
    }

    @Test
    void runningOutOfInputTakesTheFirstOptionInsteadOfAskingForever() {
        ByteArrayOutputStream out = captureOutputAndType("");
        int picked = new ConsolePrompter().choose("Pick one:", List.of("first", "second"));
        assertEquals(0, picked);
        assertTrue(out.toString().contains("No more input"));
    }
}
