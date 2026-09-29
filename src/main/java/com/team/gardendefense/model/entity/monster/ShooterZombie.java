package com.team.gardendefense.model.entity.monster;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.plant.Plant;
import com.team.gardendefense.model.entity.projectile.ZombieBone;
import com.team.gardendefense.model.level.Waypoint;

public class ShooterZombie extends Monster {
    private float baseSpeed = Constants.SPEED_ZOMBIE_SHOOTER;
    private float speed = Constants.SPEED_ZOMBIE_SHOOTER;
    private int freezeTimer = 0, shootTimer = 0;
    private int maxHp;

    public ShooterZombie(float startY, Waypoint[] wps) {
        this.x = -Constants.TILE_SIZE; this.y = startY;
        this.waypoints = wps; this.health = Constants.HP_ZOMBIE_SHOOTER; this.maxHp = Constants.HP_ZOMBIE_SHOOTER;
    }

    @Override public void applyFreeze() { freezeTimer = Constants.FREEZE_DURATION; }

    @Override
    public void update(GameManager gm) {
        if (freezeTimer > 0) { freezeTimer--; speed = baseSpeed * Constants.FREEZE_SPEED_MULTI; } 
        else { speed = baseSpeed; }

        Plant closestPlant = null; float minDist = Constants.RANGE_ZOMBIE_SHOOTER;
        for(Plant p : gm.plants) {
            float dist = (float) Math.hypot((p.x + 32) - (x + 32), (p.y + 32) - (y + 32));
            if (dist <= minDist) { minDist = dist; closestPlant = p; }
        }

        if (closestPlant != null) {
            shootTimer++;
            if (shootTimer >= Constants.RATE_ZOMBIE_SHOOTER) {
                gm.projectiles.add(new ZombieBone(
                    x + 32, y + 32, closestPlant.x + 32, closestPlant.y + 32, 
                    Constants.DMG_ZOMBIE_SHOOTER, Constants.SPEED_BONE
                ));
                shootTimer = 0;
            }
            return; 
        }
        moveAlongWaypoints(speed);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(freezeTimer > 0 ? new Color(173, 216, 230) : new Color(255, 140, 0));
        g.fillRect((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20);
        g.setColor(Color.BLACK); g.drawRect((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20);
        g.setColor(Color.BLACK); g.fillOval((int)x + 25, (int)y + 20, 10, 10);
        g.setColor(Color.GREEN); g.fillRect((int)x + 15, (int)y - 5, (Constants.TILE_SIZE - 30) * health / maxHp, 5);
    }
    @Override public Rectangle getBounds() { return new Rectangle((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20); }
}
