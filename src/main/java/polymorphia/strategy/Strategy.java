package polymorphia.strategy;

import polymorphia.characters.Character;
import polymorphia.characters.CharacterFactory;
import polymorphia.commands.CommandFactory;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

import java.util.Optional;
import java.util.Set;

/**
 * Shared lookups for the strategies.
 *
 * Homework 11: a strategy's job is now only to pick an action. Every helper below answers a
 * question about the room; none of them change anything. The commandFactory turns the choice
 * into an ICommand so that no strategy ever names a concrete command class.
 */
public abstract class Strategy implements IStrategy {
    protected static final CharacterFactory characterFactory = new CharacterFactory(new StrategyFactory());
    protected static final CommandFactory commandFactory = new CommandFactory();

    Optional<Character> getLivingCreature(IRoom room) {
        return room.getLivingCreatures().stream()
                .filter(Character::isCreature)
                .filter(Character::isVisible)       // a cloak hides you from this
                .findFirst();
    }

    public Optional<Character> getRandomCharacterNotMe(Character myself, IRoom room) {
        return room.getLivingCharacters().stream()
                .filter(character -> !character.equals(myself))
                .filter(Character::isVisible)       // can't fight what you can't see
                .findFirst();
    }

    <T> Set<T> getArtifacts(IRoom room, Class<T> type) {
        return room.getItemsOf(type);
    }

    <T> Optional<T> getArtifact(IRoom room, Class<T> type) {
        return getArtifacts(room, type).stream()
                .findAny();
    }

    <T> boolean hasArtifacts(IRoom room, Class<T> type) {
        return !room.getItemsOf(type).isEmpty();
    }

    Optional<IRoom> getRandomNeighbor(IRoom room) {
        return room.getNeighbors().stream()
                .findAny();
    }

    /** Every strategy falls back to this, so it lives here. */
    protected ICommand moveCommand(Character myself, IRoom room) {
        return commandFactory.newMoveCommand(myself, room, getRandomNeighbor(room).orElse(null));
    }

    protected ICommand sleepCommand(Character myself, IRoom room) {
        return commandFactory.newSleepCommand(myself, room);
    }
}
