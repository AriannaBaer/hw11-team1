package polymorphia.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/**
 * Walk into a neighbouring room. The strategy picks which room; this just does the walking
 * and charges the character for the weight they are carrying.
 */
public class MoveCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger(MoveCommand.class);

    private final IRoom destination;

    public MoveCommand(Character myself, IRoom room, IRoom destination) {
        super(CommandType.MOVE, myself, room);
        this.destination = destination;
    }

    @Override
    public boolean execute() {
        if (destination == null) {
            logger.warn("No neighbor to move to from {}", room.getName());
            return false;
        }
        destination.enter(myself, room);
        myself.loseHealth(myself.getMovingCost());
        return true;
    }

    @Override
    public String optionString() {
        return destination == null ? "nowhere" : destination.getName();
    }
}
