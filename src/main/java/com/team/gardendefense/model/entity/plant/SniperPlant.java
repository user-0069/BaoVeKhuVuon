package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.model.entity.projectile.Pea;

public class SniperPlant extends Plant {
    private int shootTimer = 0;
    private float range = 500f; // Tầm bắn xa

    public SniperPlant(int col, int row) { super(col, row); this.health = 5; this.maxHealth = 5; }
    
    @Override
    public void update(GameManager gm) {
        shootTimer++;
        if (shootTimer >= 120) { // Tốc độ bắn chậm (2 giây)
            Monster target = getClosestZombie(gm);
            if (target != null) {
                // Sát thương x2, đạn nhanh hơn, màu tím
                gm.projectiles.add(new Pea(
                    x + 32, y + 32, 
                    target.x + 32, target.y + 32, 
                    2, new Color(138, 43, 226), 12f, false 
                ));
                shootTimer = 0;
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
        g.setColor(new Color(138, 43, 226)); // Màu tím
        g.fillOval(x + 15, y + 15, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 30);
        g.setColor(Color.WHITE);
        g.drawOval(x + 25, y + 25, 14, 14); // Tâm ngắm
    }
}
