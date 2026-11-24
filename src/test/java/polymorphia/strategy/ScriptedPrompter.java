package polymorphia.strategy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A ConsolePrompter that answers from a script instead of from a person.
 *
 * This is the reason all the console code was pushed into ConsolePrompter in the first place:
 * with this stand-in, HumanStrategy can be tested without anybody sitting at a keyboard.
 */
public class ScriptedPrompter extends ConsolePrompter {

    private final Deque<String> answers = new ArrayDeque<>();
    private final List<String> output = new ArrayList<>();

    public ScriptedPrompter(String... scriptedAnswers) {
        for (String answer : scriptedAnswers) {
            answers.add(answer);
        }
    }

    @Override
    protected String readLine() {
        // null means "the script ran out", which the prompter treats as nobody typing
        return answers.isEmpty() ? null : answers.removeFirst();
    }

    @Override
    public void print(String text) {
        output.add(text);
    }

    @Override
    public void println(String text) {
        output.add(text);
    }

    /** Everything the strategy tried to show the player. */
    public String transcript() {
        return String.join("\n", output);
    }

    public boolean everythingWasUsed() {
        return answers.isEmpty();
    }
}
