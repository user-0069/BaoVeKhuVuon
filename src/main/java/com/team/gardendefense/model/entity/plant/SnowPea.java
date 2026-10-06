package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import com.team.gardendefense.utils.ImageLoader;
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
        this.image = ImageLoader.getImage("src/main/resources/assets/images/SnowPea_full.png");
    }
    
    @Override
    public void update(GameManager gm) {
        shootTimer++;
        if (shootTimer >= Constants.RATE_SNOWPEA) {
            Monster target = getClosestZombie(gm);
            if (target != null) {
                float tx = x + 32; float ty = y + 32;
                if (direction == 0) ty -= 100;
                else if (direction == 1) tx += 100;
                else if (direction == 2) ty += 100;
                else if (direction == 3) tx -= 100;
                
                gm.projectiles.add(new Pea(
                    x + 32, y + 32, tx, ty, 
                    Constants.DMG_SNOWPEA, new Color(0, 191, 255), Constants.SPEED_SNOWPEA_PEA, true
                ));
                shootTimer = 0;
            }
        }
    }
    
    private Monster getClosestZombie(GameManager gm) {
        Monster closest = null; float minDist = Constants.RANGE_SNOWPEA;
        for(Monster z : gm.zombies) {
            float dx = z.x - x; float dy = z.y - y;
            boolean inLine = false; float dist = 0;
            if (direction == 0 && dy < 0 && Math.abs(dx) < 30) { inLine = true; dist = -dy; }
            else if (direction == 1 && dx > 0 && Math.abs(dy) < 30) { inLine = true; dist = dx; }
            else if (direction == 2 && dy > 0 && Math.abs(dx) < 30) { inLine = true; dist = dy; }
            else if (direction == 3 && dx < 0 && Math.abs(dy) < 30) { inLine = true; dist = -dx; }
            
            if (inLine && dist <= minDist) { minDist = dist; closest = z; }
        }
        return closest;
    }

    
}
