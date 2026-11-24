package polymorphia.strategy;

import polymorphia.Die;
import polymorphia.artifacts.Armor;
import polymorphia.artifacts.Food;
import polymorphia.artifacts.Weapon;
import polymorphia.characters.Character;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

import java.util.Optional;

public class FighterStrategy extends Strategy {

    @Override
    public ICommand getAction(Character myself, IRoom room) {
        Optional<Armor> armor = getArtifact(room, Armor.class);
        if (armor.isPresent()) {
            return commandFactory.newWearCommand(myself, room, armor.get());
        }
        Optional<Weapon> weapon = getArtifact(room, Weapon.class);
        if (weapon.isPresent()) {
            return commandFactory.newWieldCommand(myself, room, weapon.get());
        }
        Optional<Character> creature = getLivingCreature(room);
        if (creature.isPresent()) {
            return commandFactory.newFightCommand(myself, room, creature.get());
        }
        Optional<Die> die = getArtifact(room, Die.class);
        if (die.isPresent()) {
            return commandFactory.newTakeCommand(myself, room, (polymorphia.artifacts.Artifact) die.get());
        }
        Optional<Food> food = getArtifact(room, Food.class);
        if (food.isPresent()) {
            return commandFactory.newEatCommand(myself, room, food.get());
        }
        return moveCommand(myself, room);
    }

    @Override
    public boolean isFighter() {
        return true;
    }
}
