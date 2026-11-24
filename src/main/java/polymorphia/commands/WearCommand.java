package polymorphia.commands;

import polymorphia.artifacts.Armor;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/** Put on a suit of armor: wrap the character in an ArmoredCharacter and swap it into the room. */
public class WearCommand extends ArtifactCommand {

    public WearCommand(Character myself, IRoom room, Armor armor) {
        super(CommandType.WEAR, myself, room, armor);
    }

    @Override
    public boolean execute() {
        Armor armor = (Armor) getArtifactFromRoom();
        room.replace(myself, characterFactory.newArmoredCharacter(myself, armor));
        return true;
    }
}
