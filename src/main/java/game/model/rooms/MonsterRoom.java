package game.model.rooms;

import game.model.Artifact;
import game.model.Player;
import game.model.Position;
import game.model.combat.CombatStrategy;
import game.model.combat.EnemyCombatStrategy;
import game.model.entities.Enemy;
import game.model.entities.EnemyState;
import game.model.entities.SleepingState;

import java.util.Random;

/**
 * A room containing a monster the player can fight.
 */
public class MonsterRoom extends Room {
    private Enemy enemy;
    private boolean defeated;
    private Artifact reward;
    
    /**
     * Creates a new monster room.
     * 
     * @param position The room's position
     */
    public MonsterRoom(Position position) {
        super(RoomType.MONSTER, position);
        this.enemy = generateRandomEnemy();
        this.description = "A dark room with the presence of danger. " + enemy.getDescription();
        this.defeated = false;
        this.reward = generateReward();
    }
    
    /**
     * Generates a random enemy.
     * 
     * @return The generated enemy
     */
    private Enemy generateRandomEnemy() {
        String[] enemyTypes = {
            "Goblin:A small, green creature with a wicked grin:15:8:3:2:10",
            "Skeleton:A reanimated skeleton clutching a rusty sword:25:10:2:3:15",
            "Orc:A bulky, green-skinned brute with tusks:35:12:5:5:20",
            "Troll:A towering creature with thick, regenerating skin:50:15:8:10:30",
            "Ghost:A translucent specter that floats eerily:20:8:1:15:25",
            "Giant Spider:A hairy arachnid the size of a wolf:30:12:4:4:20"
        };
        
        Random random = new Random();
        String[] enemyData = enemyTypes[random.nextInt(enemyTypes.length)].split(":");
        
        String name = enemyData[0];
        String description = enemyData[1];
        int health = Integer.parseInt(enemyData[2]);
        int attack = Integer.parseInt(enemyData[3]);
        int defense = Integer.parseInt(enemyData[4]);
        int speed = Integer.parseInt(enemyData[5]);
        int experience = Integer.parseInt(enemyData[6]);
        
        // Create the enemy with a random strategy
        CombatStrategy strategy = new EnemyCombatStrategy();
        Enemy enemy = new Enemy(name, description, health, attack, defense, speed, experience);
        
        // Start in sleeping state by default
        EnemyState initialState = new SleepingState(enemy);
        enemy.setState(initialState);
        enemy.setCombatStrategy(strategy);
        
        return enemy;
    }
    
    /**
     * Generates a reward for defeating the enemy.
     * 
     * @return An artifact reward
     */
    private Artifact generateReward() {
        String[] possibleRewards = {
            "Monster Fang:A sharp fang taken from a defeated monster:0:3:0",
            "Monster Hide:Tough hide that improves defense:0:0:3",
            "Monster Heart:A still-beating heart that increases health:10:0:0",
            "Monster Claw:A razor-sharp claw useful in combat:0:5:0",
            "Monster Eye:A magical eye that enhances perception:5:2:2"
        };
        
        Random random = new Random();
        String[] rewardData = possibleRewards[random.nextInt(possibleRewards.length)].split(":");
        
        return new Artifact(
            rewardData[0],
            rewardData[1],
            Integer.parseInt(rewardData[2]),
            Integer.parseInt(rewardData[3]),
            Integer.parseInt(rewardData[4])
        );
    }
    
    @Override
    public String onEnter(Player player) {
        markVisited();
        
        if (defeated) {
            return "You enter a room where you defeated a " + enemy.getName() + ". " + 
                  "The creature's remains still litter the floor.";
        } else {
            enemy.playerDetected(); // This will change state if the enemy is sleeping
            return "You enter a monster room! " + description + " The " + enemy.getName() + 
                  " is " + enemy.getState().getDescription() + "!";
        }
    }
    
    @Override
    public String onInteract(Player player, String action) {
        if (defeated) {
            return "The " + enemy.getName() + " has already been defeated.";
        }
        
        if (action.equalsIgnoreCase("attack") || action.equalsIgnoreCase("fight")) {
            // Start a combat sequence
            enemy.playerDetected(); // Make sure the enemy is aware of the player
            
            StringBuilder combatLog = new StringBuilder();
            combatLog.append("You engage the ").append(enemy.getName()).append(" in combat!\n");
            
            boolean playerTurn = player.getPosition().getY() % 2 == 0; // Random-ish determination of who goes first
            
            while (player.getHealth() > 0 && enemy.getHealth() > 0) {
                if (playerTurn) {
                    // Player's turn
                    int playerDamage = player.attack();
                    boolean enemyAlive = enemy.takeDamage(playerDamage);
                    combatLog.append("You attack for ").append(playerDamage).append(" damage!\n");
                    
                    if (!enemyAlive) {
                        break;
                    }
                } else {
                    // Enemy's turn
                    int enemyDamage = enemy.attack();
                    boolean playerAlive = player.takeDamage(enemyDamage);
                    combatLog.append("The ").append(enemy.getName())
                            .append(" attacks for ").append(enemyDamage).append(" damage!\n");
                    
                    if (!playerAlive) {
                        break;
                    }
                }
                
                playerTurn = !playerTurn;
            }
            
            if (player.getHealth() <= 0) {
                return combatLog.toString() + "You have been defeated by the " + enemy.getName() + "!";
            } else {
                defeated = true;
                player.gainExperience(enemy.getExperienceValue());
                player.addArtifact(reward);
                return combatLog.toString() + "You defeated the " + enemy.getName() + "!\n" +
                       "You gained " + enemy.getExperienceValue() + " experience!\n" +
                       "You found: " + reward;
            }
        } else if (action.equalsIgnoreCase("examine") || action.equalsIgnoreCase("look")) {
            return "You examine the " + enemy.getName() + ". " + enemy.getDescription() + " It is " + 
                   enemy.getState().getDescription() + ".";
        } else if (action.equalsIgnoreCase("sneak") || action.equalsIgnoreCase("hide")) {
            if (enemy.getState().canAvoid()) {
                return "You successfully sneak past the " + enemy.getName() + " without alerting it.";
            } else {
                enemy.playerDetected();
                return "The " + enemy.getName() + " spots you trying to sneak by! It prepares to attack!";
            }
        }
        
        return "Try 'attack', 'examine', or 'sneak' to interact with the monster.";
    }
    
    /**
     * Gets the enemy in this room.
     * 
     * @return The enemy
     */
    public Enemy getEnemy() {
        return enemy;
    }
    
    /**
     * Checks if the enemy has been defeated.
     * 
     * @return true if defeated, false otherwise
     */
    public boolean isDefeated() {
        return defeated;
    }
}
