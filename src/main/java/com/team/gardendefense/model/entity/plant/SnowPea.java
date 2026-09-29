package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.model.entity.projectile.Pea;

public class SnowPea extends Plant {
    private int shootTimer = 0;
    private float range = 250f;

    public SnowPea(int col, int row) { 
        super(col, row); 
        this.health = 5; this.maxHealth = 5;
    }
    
    @Override
    public void update(GameManager gm) {
        shootTimer++;
        if (shootTimer >= 60) {
            Monster target = getClosestZombie(gm);
            if (target != null) {
                // Đạn tuyết màu xanh lơ, isFreeze = true
                gm.projectiles.add(new Pea(x + 32, y + 32, target.x + 32, target.y + 32, 1, new Color(0, 191, 255), 8f, true));
                shootTimer = 0;
            }
        }
    }
    
    private Monster getClosestZombie(GameManager gm) {
        Monster closest = null; float minDist = range;
        for(Monster z : gm.zombies) {
            float dist = (float) Math.hypot(z.x - x, z.y - y);
            if (dist <= minDist) { minDist = dist; closest = z; }
        }
        return closest;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(135, 206, 250)); // Light Sky Blue
        g.fillOval(x + 10, y + 10, Constants.TILE_SIZE - 20, Constants.TILE_SIZE - 20);
        g.setColor(Color.WHITE); g.drawOval(x + 10, y + 10, Constants.TILE_SIZE - 20, Constants.TILE_SIZE - 20);
    }
}
