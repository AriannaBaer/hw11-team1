package polymorphia.commands;

import polymorphia.artifacts.Food;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/** Eat a food item lying in the room. The food leaves the room and the health goes up. */
public class EatCommand extends ArtifactCommand {

    public EatCommand(Character myself, IRoom room, Food food) {
        super(CommandType.EAT, myself, room, food);
    }

    @Override
    public boolean execute() {
        return myself.eat((Food) getArtifactFromRoom());
    }
}
