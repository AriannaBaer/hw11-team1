package polymorphia.strategy;

import polymorphia.artifacts.Food;
import polymorphia.artifacts.Potion;
import polymorphia.characters.Character;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

import java.util.Optional;

public class GluttonStrategy extends Strategy {

    @Override
    public ICommand getAction(Character myself, IRoom room) {
        Optional<Potion> potion = getArtifact(room, Potion.class);
        if (potion.isPresent()) {
            return commandFactory.newDrinkCommand(myself, room, potion.get());
        }
        Optional<Food> food = getArtifact(room, Food.class);
        if (food.isPresent()) {
            return commandFactory.newEatCommand(myself, room, food.get());
        }
        return moveCommand(myself, room);
    }

    @Override
    public boolean isGlutton() {
        return true;
    }
}
