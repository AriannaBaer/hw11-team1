package polymorphia.strategy;

import polymorphia.Die;
import polymorphia.artifacts.Armor;
import polymorphia.artifacts.Artifact;
import polymorphia.artifacts.Cloak;
import polymorphia.artifacts.Food;
import polymorphia.artifacts.Potion;
import polymorphia.artifacts.Treasure;
import polymorphia.artifacts.TreasureMap;
import polymorphia.artifacts.Weapon;
import polymorphia.characters.Character;
import polymorphia.commands.CommandType;
import polymorphia.commands.ICommand;
import polymorphia.maze.IRoom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lets a person play one of the adventurers.
 *
 * The menu is built from what is actually in the room, so you are never offered something you
 * cannot do. Move and Sleep are always there. If a choice has only one possible target -- one
 * food item, one creature -- we skip the second menu and just do it.
 */
public class HumanStrategy extends Strategy {

    private final ConsolePrompter prompter;

    public HumanStrategy() {
        this(new ConsolePrompter());
    }

    /** Lets a test supply a scripted prompter instead of the real console. */
    public HumanStrategy(ConsolePrompter prompter) {
        this.prompter = prompter;
    }

    @Override
    public ICommand getAction(Character myself, IRoom room) {
        List<ICommand> options = buildOptions(myself, room);
        showRoom(myself, room);

        CommandType chosenType = getTopLevelChoice(options);
        prompter.println("Your chosen option is " + chosenType);

        List<ICommand> matching = options.stream()
                .filter(command -> command.getType() == chosenType)
                .toList();

        if (matching.size() == 1) {
            return matching.getFirst();         // only one target, don't ask twice
        }
        List<String> labels = matching.stream().map(ICommand::optionString).toList();
        int picked = prompter.choose(secondLevelHeading(chosenType), labels);
        return matching.get(picked);
    }

    /** Everything this character could legally do in this room, right now. */
    private List<ICommand> buildOptions(Character myself, IRoom room) {
        List<ICommand> options = new ArrayList<>();

        for (Character other : room.getLivingCharacters()) {
            if (!other.equals(myself)) {
                options.add(commandFactory.newFightCommand(myself, room, other));
            }
        }
        for (Food food : room.getItemsOf(Food.class)) {
            options.add(commandFactory.newEatCommand(myself, room, food));
        }
        for (Armor armor : room.getItemsOf(Armor.class)) {
            options.add(commandFactory.newWearCommand(myself, room, armor));
        }
        for (Weapon weapon : room.getItemsOf(Weapon.class)) {
            options.add(commandFactory.newWieldCommand(myself, room, weapon));
        }
        for (Cloak cloak : room.getItemsOf(Cloak.class)) {
            options.add(commandFactory.newDonCommand(myself, room, cloak));
        }
        for (Potion potion : room.getItemsOf(Potion.class)) {
            options.add(commandFactory.newDrinkCommand(myself, room, potion));
        }
        for (Treasure treasure : room.getItemsOf(Treasure.class)) {
            options.add(commandFactory.newTakeCommand(myself, room, treasure));
        }
        for (TreasureMap map : room.getItemsOf(TreasureMap.class)) {
            options.add(commandFactory.newTakeCommand(myself, room, map));
        }
        for (Die die : room.getItemsOf(Die.class)) {
            options.add(commandFactory.newTakeCommand(myself, room, (Artifact) die));
        }
        // Move and Sleep are always available
        for (IRoom neighbor : room.getNeighbors()) {
            options.add(commandFactory.newMoveCommand(myself, room, neighbor));
        }
        options.add(commandFactory.newSleepCommand(myself, room));
        return options;
    }

    private void showRoom(Character myself, IRoom room) {
        prompter.println("");
        prompter.println(myself.getName() + ", you are in room " + room.getName() + ":");
        for (Character other : room.getLivingCharacters()) {
            if (!other.equals(myself)) {
                prompter.println("  " + other.getNameAndHealth());
            }
        }
        for (Artifact artifact : room.getAllArtifacts()) {
            prompter.println("  " + artifact.getName());
        }
    }

    /** The first menu: the kinds of thing you can do, each listed once. */
    private CommandType getTopLevelChoice(List<ICommand> options) {
        Map<CommandType, CommandType> distinct = new LinkedHashMap<>();
        for (ICommand option : options) {
            distinct.put(option.getType(), option.getType());
        }
        List<CommandType> types = new ArrayList<>(distinct.keySet());
        List<String> labels = types.stream().map(Enum::name).toList();
        return types.get(prompter.choose("What would you like to do?", labels));
    }

    private String secondLevelHeading(CommandType type) {
        return switch (type) {
            case FIGHT, FIGHT_TO_THE_DEATH -> "Select opponent:";
            case EAT -> "Select what to eat:";
            case MOVE -> "Select where to go:";
            case WEAR -> "Select armor to wear:";
            case WIELD -> "Select a weapon:";
            case DON -> "Select a cloak:";
            case DRINK -> "Select a potion:";
            case TAKE -> "Select what to take:";
            default -> "Select one:";
        };
    }
}
