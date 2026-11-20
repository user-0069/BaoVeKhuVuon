package com.team.gardendefense.model.entity.plant;

import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.utils.ImageLoader;
import com.team.gardendefense.utils.Constants;
import java.awt.Graphics2D;

public class Chomper extends Plant {
    private int state = 0; // 0 = idle, 1 = biting, 2 = chewing, 3 = swallowing
    private int biteTimer = 0;
    private Monster biteTarget = null;
    private int chewTimer = 0;
    private final int MAX_CHEW_TIME = 420; // ~7 seconds chewing

    public Chomper(int col, int row, GameManager gameManager) {
        super(col, row);
        this.health = 5;
        this.maxHealth = 5;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/Chomper.png");
        setAnimation("Chomper.reanim");
    }

    @Override
    public void update(GameManager gm) {
        if (state == 1) { // Biting: lunging forward with mouth opening
            biteTimer++;
            // At tick 13 (mouth snaps shut), deal damage and consume target
            if (biteTimer == 13) {
                if (biteTarget != null && biteTarget.health > 0) {
                    biteTarget.takeDamage(1800);
                    state = 2; // Transition to chewing
                    chewTimer = MAX_CHEW_TIME;
                }
            }
            if (biteTimer >= 25) {
                if (state == 1) { // Target moved away or died before snap
                    state = 0;
                    biteTarget = null;
                }
            }
            return;
        }

        if (state == 2) { // Chewing: digesting with mouth closed and belly bobbing
            chewTimer--;
            if (chewTimer <= 20) {
                state = 3; // Swallowing
                biteTimer = 0;
            }
            return;
        }

        if (state == 3) { // Swallowing: gulping down and burping before returning to idle
            biteTimer++;
            if (biteTimer >= 20) {
                state = 0; // Return to idle
                biteTarget = null;
            }
            return;
        }

        // Idle state: search for closest active zombie in 3x3 range
        for (Monster zombie : gm.zombies) {
            if (Math.abs(zombie.x - this.x) <= Constants.TILE_SIZE * 1.5 && Math.abs(zombie.y - this.y) <= Constants.TILE_SIZE * 1.5) {
                if (zombie.health > 0) {
                    biteTarget = zombie;
                    state = 1;
                    biteTimer = 0;
                    break;
                }
            }
        }
    }

    @Override
    protected String animationClip() {
        if (state == 1) return "bite";
        if (state == 2) return "chew";
        if (state == 3) return "swallow";
        return "idle";
    }

    @Override
    protected double animationTime() {
        if (state == 1) return biteTimer;
        if (state == 2) return (MAX_CHEW_TIME - chewTimer);
        if (state == 3) return biteTimer;
        return animationTicks;
    }

    @Override
    public void draw(Graphics2D g) {
        if (animation != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            int cx = x + Constants.TILE_SIZE / 2;
            if (direction == 3) {
                g2.translate(2.0 * cx, 0);
                g2.scale(-1, 1);
            }
            animation.draw(g2, x, y - 9, 0.82f, animationClip(), animationTime(), animationTicks, false);
            g2.dispose();
            if (health < maxHealth) {
                g.setColor(java.awt.Color.RED);
                g.fillRect(x, y - 5, Constants.TILE_SIZE, 4);
                g.setColor(java.awt.Color.GREEN);
                g.fillRect(x, y - 5, (int)(Constants.TILE_SIZE * ((float)health / maxHealth)), 4);
            }
            return;
        }
        super.draw(g);
    }
}
