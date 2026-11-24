package polymorphia.strategy;

import polymorphia.characters.Character;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

public class SleepStrategy extends Strategy {

    @Override
    public ICommand getAction(Character myself, IRoom room) {
        return sleepCommand(myself, room);      // this character sleeps through everything
    }
}
