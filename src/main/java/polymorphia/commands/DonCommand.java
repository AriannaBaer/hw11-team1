package polymorphia.commands;

import polymorphia.artifacts.Cloak;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/** Put on a cloak: wrap the character in a CloakedCharacter and swap it into the room. */
public class DonCommand extends ArtifactCommand {

    public DonCommand(Character myself, IRoom room, Cloak cloak) {
        super(CommandType.DON, myself, room, cloak);
    }

    @Override
    public boolean execute() {
        Cloak cloak = (Cloak) getArtifactFromRoom();
        room.replace(myself, characterFactory.newCloakedCharacter(myself, cloak));
        return true;
    }
}
