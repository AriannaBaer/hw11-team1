package polymorphia.strategy;

import polymorphia.characters.Character;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

public interface IStrategy {
    /**
     * Homework 11: a strategy now only CHOOSES the action and hands back a command.
     * Executing it is the command's job (Single Responsibility Principle).
     */
    ICommand getAction(Character myself, IRoom room);
    default boolean isFighter() { return false; }
    default boolean isSeeker() { return false; }
    default boolean isGuard() { return false; }
    default boolean isCoward() { return false; }
    default boolean isGlutton() { return false; }
}
