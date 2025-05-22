package game.model.rooms;

import game.model.Player;
import game.model.Position;
import game.model.traps.ActiveTrapState;
import game.model.traps.HiddenTrapState;
import game.model.traps.Trap;

import java.util.Random;

/**
 * A room containing a trap that can harm the player.
 */
public class TrapRoom extends Room {
    private Trap trap;
    
    /**
     * Creates a new trap room.
     * 
     * @param position The room's position
     */
    public TrapRoom(Position position) {
        super(RoomType.TRAP, position);
        this.trap = generateRandomTrap();
        this.description = "A room that feels somehow off. " + 
                          (trap.getState() instanceof HiddenTrapState ? 
                           "There's an uneasy feeling in the air." :
                           "You can see a dangerous trap here.");
    }
    
    /**
     * Generates a random trap.
     * 
     * @return The generated trap
     */
    private Trap generateRandomTrap() {
        String[] trapTypes = {
            "Spike Pit:A pit filled with sharp spikes:20",
            "Poison Dart:A trap that shoots poisonous darts:15",
            "Fire Geyser:A geyser that erupts with fire:25",
            "Crushing Ceiling:A ceiling that lowers to crush intruders:30",
            "Acid Spray:A trap that sprays corrosive acid:18"
        };
        
        Random random = new Random();
        String[] trapData = trapTypes[random.nextInt(trapTypes.length)].split(":");
        
        String name = trapData[0];
        String description = trapData[1];
        int damage = Integer.parseInt(trapData[2]);
        
        Trap trap = new Trap(name, description, damage);
        
        // 70% chance the trap is hidden
        if (random.nextDouble() < 0.7) {
            trap.setHidden();
        } else {
            trap.setActive();
        }
        
        return trap;
    }
    
    @Override
    public String onEnter(Player player) {
        markVisited();
        
        StringBuilder result = new StringBuilder();
        result.append("You enter ").append(description).append("\n");
        
        // Check if the player triggers the trap
        if (trap.isActive() || (trap.isHidden() && Math.random() < 0.7)) {
            if (trap.isHidden()) {
                trap.setActive();
                result.append("You've triggered a hidden trap! ");
            } else {
                result.append("You've triggered the visible trap! ");
            }

            // ВСЕГДА срабатывает
            player.setTrapped(true);

            String[][] riddles = {
                    {"What gets wetter as it dries?", "towel"},
                    {"I’m tall when I’m young and short when I’m old. What am I?", "candle"},
                    {"What has hands but can’t clap?", "clock"},
                    {"The more you take, the more you leave behind. What am I?", "footsteps"},
                    {"What has to be broken before you can use it?", "egg"}
            };
            Random r = new Random();
            int index = r.nextInt(riddles.length);
            player.setTrapPuzzle(riddles[index][0], riddles[index][1]);
            result.append("\nYou are trapped! Solve the puzzle: ").append(player.getTrapRiddle());

            boolean playerAlive = player.takeDamage(trap.getDamage());
            result.append("\nThe ").append(trap.getName())
                    .append(" deals ").append(trap.getDamage())
                    .append(" damage to you!");

            if (!playerAlive) {
                result.append("\nYou have been killed by the trap!");
            }
        }

        
        return result.toString();
    }
    
    @Override
    public String onInteract(Player player, String action) {
        if (action.equalsIgnoreCase("examine") || action.equalsIgnoreCase("look") || 
            action.equalsIgnoreCase("inspect")) {
            
            // Higher chance to spot a hidden trap
            if (trap.isHidden() && Math.random() < 0.6) {
                trap.reveal();
                return "You carefully examine the room and discover a hidden " + trap.getName() + 
                       "! " + trap.getDescription();
            } else if (trap.isHidden()) {
                return "You examine the room carefully but don't notice anything unusual.";
            } else if (trap.isActive()) {
                return "You examine the " + trap.getName() + ". " + trap.getDescription();
            } else { // Disabled
                return "You examine the disabled " + trap.getName() + ". " + trap.getDescription();
            }
        } else if (action.equalsIgnoreCase("disarm") || action.equalsIgnoreCase("disable")) {
            if (trap.isDisabled()) {
                return "The trap is already disabled.";
            }
            
            if (trap.isHidden()) {
                return "You can't disarm a trap you haven't found yet. Try examining the room first.";
            }
            
            // 50% chance to disarm if active
            if (trap.isActive() && Math.random() < 0.5) {
                trap.disable();
                return "You successfully disarm the " + trap.getName() + "!";
            } else if (trap.isActive()) {
                // Trap triggers when disarm fails
                boolean playerAlive = player.takeDamage(trap.getDamage());
                String result = "You fail to disarm the trap and trigger it! The " + 
                               trap.getName() + " deals " + trap.getDamage() + " damage to you!";
                
                if (!playerAlive) {
                    result += "\nYou have been killed by the trap!";
                }
                
                return result;
            }
        } else if (action.equalsIgnoreCase("avoid") || action.equalsIgnoreCase("jump over") ||
                   action.equalsIgnoreCase("bypass")) {
            if (trap.isHidden()) {
                // Can't avoid what you don't know exists
                boolean playerAlive = player.takeDamage(trap.getDamage());
                trap.setActive(); // Now it's revealed
                String result = "As you move through the room, you trigger a hidden " + 
                                trap.getName() + "! It deals " + trap.getDamage() + " damage to you!";
                
                if (!playerAlive) {
                    result += "\nYou have been killed by the trap!";
                }
                
                return result;
            } else if (trap.isActive() && Math.random() < 0.7) { // 70% chance to avoid known trap
                return "You carefully avoid the " + trap.getName() + " and pass safely.";
            } else if (trap.isActive()) {
                boolean playerAlive = player.takeDamage(trap.getDamage() / 2); // Take reduced damage
                String result = "You try to avoid the " + trap.getName() + 
                                " but partially trigger it! You take " + (trap.getDamage() / 2) + " damage!";
                
                if (!playerAlive) {
                    result += "\nYou have been killed by the trap!";
                }
                
                return result;
            } else { // Disabled
                return "You easily pass by the disabled " + trap.getName() + ".";
            }
        }
        
        return "Try 'examine', 'disarm', or 'avoid' to interact with the room.";
    }
    
    /**
     * Gets the trap in this room.
     * 
     * @return The trap
     */
    public Trap getTrap() {
        return trap;
    }
}
