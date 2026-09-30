package com.team.gardendefense.model.entity.monster;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.level.Waypoint;

public class Zombie extends Monster {
    private float baseSpeed = Constants.SPEED_ZOMBIE_RED;
    private float speed = Constants.SPEED_ZOMBIE_RED;
    private int freezeTimer = 0;
    private int attackTimer = 0;
    private int maxHp;

    public Zombie(Waypoint[] wps) {
        this.waypoints = wps;
        if (wps != null && wps.length > 0) {
            this.x = wps[0].col * Constants.TILE_SIZE;
            this.y = wps[0].row * Constants.TILE_SIZE;
        } else { this.x = -Constants.TILE_SIZE; this.y = 0; }
        
        this.health = Constants.HP_ZOMBIE_RED; this.maxHp = Constants.HP_ZOMBIE_RED;
    }

    @Override public void applyFreeze() { freezeTimer = Constants.FREEZE_DURATION; }

    @Override
    public void update(GameManager gm) {
        if (freezeTimer > 0) { freezeTimer--; speed = baseSpeed * Constants.FREEZE_SPEED_MULTI; } 
        else { speed = baseSpeed; }

        if (attackTarget != null) {
            if (attackTarget.isDead()) { attackTarget = null; } 
            else {
                attackTimer++;
                if (attackTimer >= Constants.RATE_ZOMBIE_RED_ATK) { 
                    attackTarget.takeDamage(Constants.DMG_ZOMBIE_RED); 
                    attackTimer = 0; 
                }
                return;
            }
        }
        moveAlongWaypoints(speed); 
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(freezeTimer > 0 ? new Color(173, 216, 230) : Color.RED);
        g.fillRect((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20);
        g.setColor(Color.BLACK); g.drawRect((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20);
        g.setColor(Color.GREEN); g.fillRect((int)x + 15, (int)y - 5, (Constants.TILE_SIZE - 30) * health / maxHp, 5);
    }
    @Override public Rectangle getBounds() { return new Rectangle((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20); }
}
