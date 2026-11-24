package polymorphia.strategy;

import polymorphia.artifacts.Treasure;
import polymorphia.characters.Character;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

import java.util.Optional;

public class GuardStrategy extends Strategy {

    @Override
    public ICommand getAction(Character myself, IRoom currentRoom) {
        if (hasArtifacts(currentRoom, Treasure.class)) {
            // stand guard over the treasure and do nothing else
            return sleepCommand(myself, currentRoom);
        }
        // Else, behave like a normal creature
        Optional<Character> character = getRandomCharacterNotMe(myself, currentRoom);
        if (character.isPresent()) {
            return commandFactory.newFightCommand(myself, currentRoom, character.get());
        }
        return moveCommand(myself, currentRoom);
    }

    @Override
    public boolean isGuard() {
        return true;
    }
}
