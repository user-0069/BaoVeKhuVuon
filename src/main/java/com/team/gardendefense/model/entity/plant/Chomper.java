package com.team.gardendefense.model.entity.plant;

import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.utils.ImageLoader;
import com.team.gardendefense.utils.Constants;
import java.awt.Graphics2D;

public class Chomper extends Plant {
    private int attackCooldown = 0;
    private final int MAX_COOLDOWN = 600; // ~10 seconds chewing at 60fps

    public Chomper(int col, int row, GameManager gameManager) {
        super(col, row);
        this.health = 5;
        this.maxHealth = 5;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/Chomper.png");
    }

    @Override
    public void update(GameManager gm) {
        if (attackCooldown > 0) {
            attackCooldown--;
            this.image = ImageLoader.getImage("src/main/resources/assets/images/ChomperDigest.png");
            if (attackCooldown == 0) {
                this.image = ImageLoader.getImage("src/main/resources/assets/images/Chomper.png");
            }
            return; // Can't attack while chewing
        }

        // Find a zombie in 3x3 area to eat
        for (Monster zombie : gm.zombies) {
            if (Math.abs(zombie.x - this.x) <= Constants.TILE_SIZE * 1.5 && Math.abs(zombie.y - this.y) <= Constants.TILE_SIZE * 1.5) {
                if (zombie.health > 0) {
                    zombie.takeDamage(1800); // Eat the zombie
                    attackCooldown = MAX_COOLDOWN; // Start chewing
                    break; // Only eat one zombie at a time
                }
            }
        }
    }
}
