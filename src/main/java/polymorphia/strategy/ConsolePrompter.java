package polymorphia.strategy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * All of the console reading and writing lives in this one class.
 *
 * Homework 11 asks us to isolate the command-line code, because a test run has nobody sitting
 * there to type an answer. Keeping it behind this class means HumanStrategy never touches
 * System.in directly, and a test can hand it a stand-in that answers from a script instead.
 */
public class ConsolePrompter {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public void print(String text) {
        System.out.print(text);
    }

    public void println(String text) {
        System.out.println(text);
    }

    /**
     * Shows a numbered menu and keeps asking until the player types a number that is on it.
     * Returns the zero-based index of what they picked.
     */
    public int choose(String heading, List<String> choices) {
        println("");
        println(heading);
        println("");
        for (int i = 0; i < choices.size(); i++) {
            println((i + 1) + ": " + choices.get(i));
        }
        while (true) {
            print("\nEnter your option: ");
            String line = readLine();
            if (line == null) {
                // nobody is typing any more (stdin closed), so take the first option and move on
                // instead of asking a question that can never be answered
                println("\nNo more input, going with option 1.");
                return 0;
            }
            try {
                int picked = Integer.parseInt(line.trim());
                if (picked >= 1 && picked <= choices.size()) {
                    return picked - 1;
                }
                println("Please enter a number between 1 and " + choices.size() + ".");
            } catch (NumberFormatException e) {
                println("\"" + line + "\" is not a number. Try again.");
            }
        }
    }

    /**
     * One line from the player, or null when there is nothing left to read. Tests override this
     * so they can answer from a script instead of a keyboard.
     */
    protected String readLine() {
        try {
            return reader.readLine();
        } catch (Exception e) {
            // a broken stream is the same as nobody typing
            return null;
        }
    }
}
