package polymorphia.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polymorphia.artifacts.Treasure;
import polymorphia.characters.Character;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SeekerStrategy extends Strategy {
    static Logger logger = LoggerFactory.getLogger(SeekerStrategy.class);

    private final Set<IRoom> visitedRooms = new HashSet<>();

    @Override
    public ICommand getAction(Character myself, IRoom room) {
        visitedRooms.add(room);

        logger.info("{} looking for treasure in room {}", myself.getName(), room.getName());
        if (!hasArtifacts(room, Treasure.class)) {
            logger.info("{} found no treasure in room {}. Moving", myself.getName(), room.getName());
            return commandFactory.newMoveCommand(myself, room, selectNewRoom(room).orElse(null));
        }

        // Anything standing over the treasure has to go, and a single round won't do it
        Optional<Character> possibleGuard = getLivingCreature(room);
        if (possibleGuard.isPresent()) {
            logger.info("{} found guard {} in treasure room {}", myself.getName(), possibleGuard.get(), room.getName());
            return commandFactory.newFightToTheDeathCommand(myself, room, possibleGuard.get());
        }

        Optional<Treasure> treasure = getArtifact(room, Treasure.class);
        if (treasure.isEmpty()) {
            logger.info("{} had the treasure snatched before they could take it in room {}", myself.getName(), room.getName());
            return sleepCommand(myself, room);
        }
        return commandFactory.newTakeCommand(myself, room, treasure.get());
    }

    private Optional<IRoom> selectNewRoom(IRoom room) {
        for (IRoom candidate : room.getNeighbors()) {
            if (!visitedRooms.contains(candidate)) {
                return Optional.of(candidate);
            }
        }
        return getRandomNeighbor(room);
    }

    @Override
    public boolean isSeeker() {
        return true;
    }
}
