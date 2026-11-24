package polymorphia.commands;

import polymorphia.artifacts.Potion;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/** Drink a potion: wrap the character so that their next death is not the last one. */
public class DrinkCommand extends ArtifactCommand {

    public DrinkCommand(Character myself, IRoom room, Potion potion) {
        super(CommandType.DRINK, myself, room, potion);
    }

    @Override
    public boolean execute() {
        Potion potion = (Potion) getArtifactFromRoom();
        room.replace(myself, characterFactory.newReincarnatableMeCharacter(myself, potion));
        return true;
    }
}
