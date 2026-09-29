package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.model.entity.projectile.Pea;

public class Peashooter extends Plant {
    private int shootTimer = 0;

    public Peashooter(int col, int row) { 
        super(col, row); 
        this.health = Constants.HP_PEASHOOTER; this.maxHealth = Constants.HP_PEASHOOTER;
    }
    
    @Override
    public void update(GameManager gm) {
        shootTimer++;
        if (shootTimer >= Constants.RATE_PEASHOOTER) { 
            Monster target = getClosestZombie(gm);
            if (target != null) {
                gm.projectiles.add(new Pea(
                    x + 32, y + 32, target.x + 32, target.y + 32, 
                    Constants.DMG_PEASHOOTER, new Color(144, 238, 144), Constants.SPEED_PEA, false
                ));
                shootTimer = 0; 
            }
        }
    }
    
    private Monster getClosestZombie(GameManager gm) {
        Monster closest = null; float minDist = Constants.RANGE_PEASHOOTER;
        for(Monster z : gm.zombies) {
            float dist = (float) Math.hypot(z.x - x, z.y - y);
            if (dist <= minDist) { minDist = dist; closest = z; }
        }
        return closest;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(34, 139, 34)); g.fillOval(x + 10, y + 10, Constants.TILE_SIZE - 20, Constants.TILE_SIZE - 20);
    }
}
