package polymorphia.commands;

import polymorphia.artifacts.Armor;
import polymorphia.artifacts.Artifact;
import polymorphia.artifacts.Cloak;
import polymorphia.artifacts.Food;
import polymorphia.artifacts.Potion;
import polymorphia.artifacts.Weapon;
import polymorphia.characters.Character;
import polymorphia.maze.IRoom;

/**
 * The only place a concrete Command is ever constructed.
 *
 * The strategies decide WHAT to do and ask for the matching command; they never name a
 * concrete command class, so adding a new action does not touch a single strategy.
 */
public class CommandFactory {

    public ICommand newSleepCommand(Character myself, IRoom room) {
        return new SleepCommand(myself, room);
    }

    public ICommand newMoveCommand(Character myself, IRoom room, IRoom destination) {
        return new MoveCommand(myself, room, destination);
    }

    public ICommand newEatCommand(Character myself, IRoom room, Food food) {
        return new EatCommand(myself, room, food);
    }

    public ICommand newFightCommand(Character myself, IRoom room, Character opponent) {
        return new FightCommand(myself, room, opponent);
    }

    public ICommand newFightToTheDeathCommand(Character myself, IRoom room, Character opponent) {
        return new FightToTheDeathCommand(myself, room, opponent);
    }

    public ICommand newTakeCommand(Character myself, IRoom room, Artifact artifact) {
        return new TakeCommand(myself, room, artifact);
    }

    public ICommand newWearCommand(Character myself, IRoom room, Armor armor) {
        return new WearCommand(myself, room, armor);
    }

    public ICommand newWieldCommand(Character myself, IRoom room, Weapon weapon) {
        return new WieldCommand(myself, room, weapon);
    }

    public ICommand newDonCommand(Character myself, IRoom room, Cloak cloak) {
        return new DonCommand(myself, room, cloak);
    }

    public ICommand newDrinkCommand(Character myself, IRoom room, Potion potion) {
        return new DrinkCommand(myself, room, potion);
    }

    public ICommand newReincarnateCommand(Character myself, IRoom room, Character reborn) {
        return new ReincarnateCommand(myself, room, reborn);
    }
}
