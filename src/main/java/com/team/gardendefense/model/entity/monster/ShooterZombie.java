package com.team.gardendefense.model.entity.monster;
import java.awt.Graphics2D;
import com.team.gardendefense.utils.ImageLoader;
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

    public ShooterZombie(Waypoint[] wps) {
        this.waypoints = wps;
        if (wps != null && wps.length > 0) {
            this.x = wps[0].col * Constants.TILE_SIZE;
            this.y = wps[0].row * Constants.TILE_SIZE;
        } else { this.x = -Constants.TILE_SIZE; this.y = 0; }

        this.health = Constants.HP_ZOMBIE_SHOOTER; this.maxHp = Constants.HP_ZOMBIE_SHOOTER;
        this.isMiniBoss = true;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/Zombie_full.png");
    }

    @Override public void applyFreeze() { freezeTimer = Constants.FREEZE_DURATION; }

    @Override
    public void update(GameManager gm) {
        if (freezeTimer > 0) { freezeTimer--; speed = baseSpeed * Constants.FREEZE_SPEED_MULTI; } 
        else { speed = baseSpeed; }

        if (attackTarget != null) {
            if (attackTarget.isDead()) { attackTarget = null; }
            else {
                // Đứng lại cắn nếu đụng trúng cây
                shootTimer++;
                if (shootTimer >= Constants.RATE_ZOMBIE_SHOOTER) {
                    attackTarget.takeDamage(Constants.DMG_ZOMBIE_SHOOTER);
                    shootTimer = 0;
                }
                return; // Dừng lại không đi tiếp, không bắn xa
            }
        }

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
        }
        moveAlongWaypoints(speed);
    }

    @Override public Rectangle getBounds() { return new Rectangle((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20); }
}
