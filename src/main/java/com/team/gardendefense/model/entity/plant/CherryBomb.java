package com.team.gardendefense.model.entity.plant;

import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.utils.ImageLoader;
import com.team.gardendefense.utils.Constants;
import java.awt.Graphics2D;

public class CherryBomb extends Plant {
    private int fuseTimer = 0;
    private final int EXPLODE_TIME = 45; // ~0.75s fuse before explosion
    private int blastTimer = 0;
    private final int BLAST_DURATION = 24; // ~0.4s blast display
    private boolean hasExploded = false;

    public CherryBomb(int col, int row, GameManager gameManager) {
        super(col, row);
        this.health = Constants.HP_CHERRYBOMB;
        this.maxHealth = Constants.HP_CHERRYBOMB;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/CherryBomb.png");
        setAnimation("CherryBomb.reanim");
    }

    @Override
    public void update(GameManager gm) {
        if (!hasExploded) {
            fuseTimer++;
            if (fuseTimer >= EXPLODE_TIME) {
                explode(gm);
            }
        } else {
            blastTimer++;
            if (blastTimer >= BLAST_DURATION) {
                this.health = 0; // Destroy plant after blast animation finishes
            }
        }
    }

    private void explode(GameManager gm) {
        hasExploded = true;
        // Kill all zombies in 3x3 area
        for (Monster zombie : gm.zombies) {
            if (Math.abs(zombie.x - this.x) <= Constants.TILE_SIZE * 1.5 && Math.abs(zombie.y - this.y) <= Constants.TILE_SIZE * 1.5) {
                zombie.takeDamage(1800);
            }
        }
    }

    @Override
    protected String animationClip() {
        return fuseTimer >= 22 ? "explode" : "idle";
    }

    @Override
    protected double animationTime() {
        if (fuseTimer >= 22) {
            return fuseTimer - 22;
        }
        return animationTicks;
    }

    @Override
    public void draw(Graphics2D g) {
        if (!hasExploded) {
            if (animation != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                animation.draw(g2, x, y + 5, 0.80f, animationClip(), animationTime(), animationTicks, false);
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
        } else {
            // Draw explosion blast effect (fiery expanding shockwave, bright flash, and boom)
            float progress = blastTimer / (float) BLAST_DURATION;
            int cx = x + Constants.TILE_SIZE / 2;
            int cy = y + Constants.TILE_SIZE / 2;
            int maxRadius = (int)(Constants.TILE_SIZE * 1.8f);
            int currentRadius = (int)(maxRadius * (0.35f + 0.65f * progress));

            Graphics2D g2 = (Graphics2D) g.create();
            float alpha = Math.max(0, 1.0f - progress);
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));

            // Outer fiery ring
            g2.setColor(new java.awt.Color(255, 69, 0, (int)(180 * alpha)));
            g2.fillOval(cx - currentRadius, cy - currentRadius, currentRadius * 2, currentRadius * 2);

            // Inner bright core
            int innerRadius = (int)(currentRadius * 0.65f);
            g2.setColor(new java.awt.Color(255, 215, 0, (int)(230 * alpha)));
            g2.fillOval(cx - innerRadius, cy - innerRadius, innerRadius * 2, innerRadius * 2);

            // Boom text/sprite if available
            java.awt.Image boom = ImageLoader.getImage("src/main/resources/assets/images/Boom.png");
            if (boom != null) {
                int boomSize = (int)(Constants.TILE_SIZE * 2.2f);
                g2.drawImage(boom, cx - boomSize / 2, cy - boomSize / 2, boomSize, boomSize, null);
            }
            g2.dispose();
        }
    }
}
