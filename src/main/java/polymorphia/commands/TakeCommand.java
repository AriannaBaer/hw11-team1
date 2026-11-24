package polymorphia.commands;

import polymorphia.Die;
import polymorphia.artifacts.Artifact;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/**
 * Pick something up and keep it: treasure, a treasure map, or a better die.
 * One command covers all three because the only thing that differs is what the
 * character does with it once it is out of the room.
 */
public class TakeCommand extends ArtifactCommand {

    public TakeCommand(Character myself, IRoom room, Artifact artifact) {
        super(CommandType.TAKE, myself, room, artifact);
    }

    @Override
    public boolean execute() {
        Artifact taken = getArtifactFromRoom();
        if (taken instanceof Die die) {
            myself.setDie(die);
        } else if (taken.isTreasure()) {
            myself.takeTreasure(taken);
        } else if (taken.isTreasureMap()) {
            myself.takeMap(taken);
        }
        return true;
    }
}
