package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import com.team.gardendefense.utils.ImageLoader;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.model.entity.projectile.Pea;

public class Peashooter extends Plant {
    private int shootTimer = 0;
    private boolean pendingShot = false;
    private int shotDelay = 0;

    public Peashooter(int col, int row) { 
        super(col, row); 
        this.health = Constants.HP_PEASHOOTER; this.maxHealth = Constants.HP_PEASHOOTER;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/Peashooter_full.png");
        setAnimation("PeaShooterSingle.reanim");
    }

    @Override
    public void update(GameManager gm) {
        shootTimer++;

        // Delayed projectile spawn perfectly synchronized with the peak mouth spit frame
        if (pendingShot) {
            shotDelay--;
            if (shotDelay <= 0) {
                pendingShot = false;
                spawnPea(gm);
            }
        }

        if (shootTimer >= Constants.RATE_PEASHOOTER) { 
            Monster target = getClosestZombie(gm);
            if (target != null) {
                playAction("shoot");
                pendingShot = true;
                shotDelay = 11; // Launch pea when mouth lunges forward (frame ~11 out of 24)
                shootTimer = 0; 
            }
        }
    }

    private void spawnPea(GameManager gm) {
        float sx = x + 32;
        float sy = y + 20;
        float tx = sx;
        float ty = sy;
        if (direction == 0) { ty -= 100; sy -= 14; }
        else if (direction == 1) { tx += 100; sx += 18; }
        else if (direction == 2) { ty += 100; sy += 14; }
        else if (direction == 3) { tx -= 100; sx -= 18; }

        gm.projectiles.add(new Pea(
            sx, sy, tx, ty, 
            Constants.DMG_PEASHOOTER, new Color(144, 238, 144), Constants.SPEED_PEA, false
        ));
    }
    
    private Monster getClosestZombie(GameManager gm) {
        Monster closest = null; float minDist = Constants.RANGE_PEASHOOTER;
        for(Monster z : gm.zombies) {
            if (z.isDead()) continue;
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
