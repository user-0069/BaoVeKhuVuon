package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.model.entity.projectile.Pea;

public class SnowPea extends Plant {
    private int shootTimer = 0;

    public SnowPea(int col, int row) { 
        super(col, row); 
        this.health = Constants.HP_SNOWPEA; this.maxHealth = Constants.HP_SNOWPEA;
    }
    
    @Override
    public void update(GameManager gm) {
        shootTimer++;
        if (shootTimer >= Constants.RATE_SNOWPEA) {
            Monster target = getClosestZombie(gm);
            if (target != null) {
                gm.projectiles.add(new Pea(
                    x + 32, y + 32, target.x + 32, target.y + 32, 
                    Constants.DMG_SNOWPEA, new Color(0, 191, 255), Constants.SPEED_SNOWPEA_PEA, true
                ));
                shootTimer = 0;
            }
        }
    }
    
    private Monster getClosestZombie(GameManager gm) {
        Monster closest = null; float minDist = Constants.RANGE_SNOWPEA;
        for(Monster z : gm.zombies) {
            float dist = (float) Math.hypot(z.x - x, z.y - y);
            if (dist <= minDist) { minDist = dist; closest = z; }
        }
        return closest;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(135, 206, 250)); g.fillOval(x + 10, y + 10, Constants.TILE_SIZE - 20, Constants.TILE_SIZE - 20);
        g.setColor(Color.WHITE); g.drawOval(x + 10, y + 10, Constants.TILE_SIZE - 20, Constants.TILE_SIZE - 20);
    }
}
