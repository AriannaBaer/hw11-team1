package polymorphia.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/**
 * Cash in a potion that has already been drunk. The character comes back undecorated,
 * so the spent wrapper is swapped out of the room for the character underneath it.
 */
public class ReincarnateCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger(ReincarnateCommand.class);

    private final Character reborn;

    public ReincarnateCommand(Character myself, IRoom room, Character reborn) {
        super(CommandType.REINCARNATE, myself, room);
        this.reborn = reborn;
    }

    @Override
    public boolean execute() {
        if (reborn == null) {
            logger.warn("{} has nothing to come back as", myself.getName());
            return false;
        }
        logger.info("{} is reincarnated", myself.getName());
        room.replace(myself, reborn);
        return true;
    }

    @Override
    public String optionString() {
        return reborn == null ? "" : reborn.getName();
    }
}
