package polymorphia.strategy;

import polymorphia.characters.Character;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

import java.util.Optional;

public class CreatureStrategy extends Strategy {
    // Creatures fight adventurers or other creatures

    @Override
    public ICommand getAction(Character myself, IRoom currentRoom) {
        Optional<Character> character = getRandomCharacterNotMe(myself, currentRoom);
        if (character.isPresent()) {
            return commandFactory.newFightCommand(myself, currentRoom, character.get());
        }
        return moveCommand(myself, currentRoom);
    }
}
