package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.model.entity.projectile.Pea;

public class Peashooter extends Plant {
    private int shootTimer = 0;
    private float range = 250f; // Tầm bắn ngắn

    public Peashooter(int col, int row) { super(col, row); this.health = 5; this.maxHealth = 5; }
    
    @Override
    public void update(GameManager gm) {
        shootTimer++;
        if (shootTimer >= 60) { // Bắn mỗi 1 giây
            Monster target = getClosestZombie(gm);
            if (target != null) {
                // Định hướng: x+32, y+32 là tâm của cây; target.x+32 là tâm zombie
                gm.projectiles.add(new Pea(
                    x + 32, y + 32, 
                    target.x + 32, target.y + 32, 
                    1, new Color(144, 238, 144), 8f, false
                ));
                shootTimer = 0; // Reset
            }
        }
    }
    
    private Monster getClosestZombie(GameManager gm) {
        Monster closest = null;
        float minDist = range;
        for(Monster z : gm.zombies) {
            float dist = (float) Math.hypot(z.x - x, z.y - y);
            if (dist <= minDist) {
                minDist = dist;
                closest = z;
            }
        }
        return closest;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(34, 139, 34)); // Xanh lá
        g.fillOval(x + 10, y + 10, Constants.TILE_SIZE - 20, Constants.TILE_SIZE - 20);
    }
}
