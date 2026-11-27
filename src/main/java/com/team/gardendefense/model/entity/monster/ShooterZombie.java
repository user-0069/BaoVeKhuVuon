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
import com.team.gardendefense.system.AudioManager;

public class ShooterZombie extends Monster {
    private float baseSpeed = Constants.SPEED_ZOMBIE_SHOOTER;
    private float speed = Constants.SPEED_ZOMBIE_SHOOTER;
    private int shootTimer = 0;

    public ShooterZombie(Waypoint[] wps) {
        this.waypoints = wps;
        if (wps != null && wps.length > 0) {
            this.x = wps[0].col * Constants.TILE_SIZE;
            this.y = wps[0].row * Constants.TILE_SIZE;
        } else { this.x = -Constants.TILE_SIZE; this.y = 0; }

        this.health = Constants.HP_ZOMBIE_SHOOTER; this.maxHp = Constants.HP_ZOMBIE_SHOOTER;
        this.isMiniBoss = true;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/Zombie_full.png");
        setAnimation("Zombie.reanim");
    }

    @Override
    public void update(GameManager gm) {
        if (health <= 0) {
            handleDeathSequence(gm);
            return;
        }

        // Rơi cánh tay khi zombie còn 1 nửa máu
        if (!armDropped && health <= maxHp / 2) {
            armDropped = true;
            lostArm = true;
            if (gm != null) {
                gm.spawnZombieLimb(ZombieLimb.createArm(x, y, facingRight));
            }
        }

        if (freezeTimer > 0) {
            freezeTimer--;
            if (freezeTimer <= Constants.FREEZE_DURATION) {
                completelyFrozen = false;
            }
            if (completelyFrozen) {
                speed = 0;
                return; // Đóng băng cứng: không di chuyển, không bắn xương, không cắn cây
            } else {
                speed = baseSpeed * Constants.FREEZE_SPEED_MULTI;
            }
        } else {
            speed = baseSpeed;
            completelyFrozen = false;
        }
        advanceAnimation(speed / baseSpeed);

        if (attackTarget != null) {
            if (attackTarget.isDead() || (gm != null && !gm.plants.contains(attackTarget))) {
                attackTarget = null;
            } else {
                // Đứng lại cắn nếu đụng trúng cây
                shootTimer++;
                if (shootTimer >= Constants.RATE_ZOMBIE_SHOOTER) {
                    attackTarget.takeDamage(Constants.DMG_ZOMBIE_SHOOTER);
                    AudioManager.getInstance().playZombieChompSound();
                    shootTimer = 0;
                }
                return; // Dừng lại không đi tiếp, không bắn xa
            }
        }

        Plant closestPlant = null; float minDist = Constants.RANGE_ZOMBIE_SHOOTER;
        for(Plant p : gm.plants) {
            if (p.isDead()) continue;
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
                AudioManager.getInstance().playZombieThrowSound();
                shootTimer = 0;
            }
        }
        moveAlongWaypoints(speed);
    }

    @Override public Rectangle getBounds() { return new Rectangle((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20); }
}
