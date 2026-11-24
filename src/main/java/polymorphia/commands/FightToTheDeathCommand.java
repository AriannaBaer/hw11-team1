package polymorphia.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/**
 * Keep swinging until one of them drops, instead of trading a single round.
 * Used when somebody is standing between a seeker and the treasure.
 */
public class FightToTheDeathCommand extends FightCommand {
    private static final Logger logger = LoggerFactory.getLogger(FightToTheDeathCommand.class);

    public FightToTheDeathCommand(Character myself, IRoom room, Character opponent) {
        super(CommandType.FIGHT_TO_THE_DEATH, myself, room, opponent);
    }

    @Override
    public boolean execute() {
        if (opponent == null) {
            logger.warn("{} has nobody to fight to the death in {}", myself.getName(), room.getName());
            return false;
        }
        while (myself.isAlive() && opponent.isAlive()) {
            myself.fight(opponent);
        }
        return myself.isAlive();
    }
}
