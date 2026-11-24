package polymorphia.strategy;

import polymorphia.artifacts.Cloak;
import polymorphia.artifacts.Food;
import polymorphia.artifacts.TreasureMap;
import polymorphia.characters.Character;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

import java.util.Optional;

public class CowardStrategy extends Strategy {

    @Override
    public ICommand getAction(Character myself, IRoom room) {
        // hiding beats running, so grab a cloak before anything else
        Optional<Cloak> cloak = getArtifact(room, Cloak.class);
        if (cloak.isPresent()) {
            return commandFactory.newDonCommand(myself, room, cloak.get());
        }
        if (!room.getLivingCreatures().isEmpty()) {
            return moveCommand(myself, room);       // run away first, always
        }
        Optional<TreasureMap> treasureMap = getArtifact(room, TreasureMap.class);
        if (treasureMap.isPresent()) {
            return commandFactory.newTakeCommand(myself, room, treasureMap.get());
        }
        Optional<Food> food = getArtifact(room, Food.class);
        if (food.isPresent()) {
            return commandFactory.newEatCommand(myself, room, food.get());
        }
        return moveCommand(myself, room);
    }

    @Override
    public boolean isCoward() {
        return true;
    }
}
