package polymorphia.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/** One round of combat with a chosen opponent. */
public class FightCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger(FightCommand.class);

    protected final Character opponent;

    public FightCommand(Character myself, IRoom room, Character opponent) {
        this(CommandType.FIGHT, myself, room, opponent);
    }

    protected FightCommand(CommandType type, Character myself, IRoom room, Character opponent) {
        super(type, myself, room);
        this.opponent = opponent;
    }

    @Override
    public boolean execute() {
        if (opponent == null) {
            logger.warn("{} has nobody to fight in {}", myself.getName(), room.getName());
            return false;
        }
        return myself.fight(opponent);
    }

    @Override
    public String optionString() {
        return opponent == null ? "nobody" : opponent.getName();
    }
}
