package game.model.combat;

import game.model.Player;
import game.model.entities.Entity;

import java.util.Random;

/**
 * A combat strategy used by enemies, combining different approaches.
 * Part of the Strategy pattern implementation.
 */
public class EnemyCombatStrategy implements CombatStrategy {
    private Random random = new Random();
    private int strategyType; // 0 = balanced, 1 = aggressive, 2 = defensive, 3 = magic
    
    /**
     * Creates a new enemy combat strategy with a random approach.
     */
    public EnemyCombatStrategy() {
        // Randomly select a strategy type
        this.strategyType = random.nextInt(4);
    }
    
    @Override
    public int calculateAttack(Player player) {
        // Not used for enemies
        return 0;
    }
    
    @Override
    public int calculateDefense(Player player) {
        // Not used for enemies
        return 0;
    }
    
    @Override
    public int calculateAttack(Entity entity) {
        int baseAttack = entity.getAttack();
        double modifier;
        
        switch (strategyType) {
            case 0: // Balanced
                modifier = 0.9 + (random.nextDouble() * 0.2); // 90-110%
                break;
            case 1: // Aggressive
                modifier = 1.2 + (random.nextDouble() * 0.3); // 120-150%
                break;
            case 2: // Defensive
                modifier = 0.7 + (random.nextDouble() * 0.2); // 70-90%
                break;
            case 3: // Magic
                modifier = 0.5 + (random.nextDouble() * 1.5); // 50-200%
                // Critical hit chance for magic
                if (random.nextDouble() < 0.15) {
                    modifier *= 1.5;
                }
                break;
            default:
                modifier = 1.0;
        }
        
        return (int)(baseAttack * modifier);
    }
    
    @Override
    public int calculateDefense(Entity entity) {
        int baseDefense = entity.getDefense();
        double modifier;
        
        switch (strategyType) {
            case 0: // Balanced
                modifier = 0.9 + (random.nextDouble() * 0.2); // 90-110%
                break;
            case 1: // Aggressive
                modifier = 0.6 + (random.nextDouble() * 0.2); // 60-80%
                break;
            case 2: // Defensive
                modifier = 1.2 + (random.nextDouble() * 0.3); // 120-150%
                break;
            case 3: // Magic
                modifier = 0.8 + (random.nextDouble() * 0.4); // 80-120%
                // Magic barrier chance
                if (random.nextDouble() < 0.1) {
                    modifier *= 1.5;
                }
                break;
            default:
                modifier = 1.0;
        }
        
        return (int)(baseDefense * modifier);
    }
    
    /**
     * Gets a description of the strategy type.
     * 
     * @return The strategy description
     */
    public String getStrategyDescription() {
        switch (strategyType) {
            case 0:
                return "balanced";
            case 1:
                return "aggressive";
            case 2:
                return "defensive";
            case 3:
                return "magical";
            default:
                return "unknown";
        }
    }
}
