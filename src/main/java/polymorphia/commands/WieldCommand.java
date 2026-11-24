package polymorphia.commands;

import polymorphia.artifacts.Weapon;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/** Pick up a weapon: wrap the character in a WeaponizedCharacter and swap it into the room. */
public class WieldCommand extends ArtifactCommand {

    public WieldCommand(Character myself, IRoom room, Weapon weapon) {
        super(CommandType.WIELD, myself, room, weapon);
    }

    @Override
    public boolean execute() {
        Weapon weapon = (Weapon) getArtifactFromRoom();
        room.replace(myself, characterFactory.newWeaponizedCharacter(myself, weapon));
        return true;
    }
}
