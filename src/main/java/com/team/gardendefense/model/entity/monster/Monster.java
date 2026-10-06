package com.team.gardendefense.model.entity.monster;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;
import com.team.gardendefense.utils.ImageLoader;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.plant.Plant;
import com.team.gardendefense.model.level.Waypoint;
import com.team.gardendefense.utils.Constants;

public abstract class Monster {
    public float x, y;
    public int health;
    public Plant attackTarget = null;
    public Waypoint[] waypoints;
    public int wpIdx = 1; // Mặc định điểm xuất phát là index 0, nên hướng tới index 1
    
    public boolean facingRight = false;

    public abstract void update(GameManager gm);
    public Image image;
    
    public void draw(Graphics2D g) {
        if (image != null) {
            int w = Constants.TILE_SIZE + 10;
            int h = Constants.TILE_SIZE + 10;
            int drawX = (int)x - 5;
            int drawY = (int)y - 5;
            // Base image faces left. If moving right, flip horizontally
            if (facingRight) {
                g.drawImage(image, drawX + w, drawY, -w, h, null);
            } else {
                g.drawImage(image, drawX, drawY, w, h, null);
            }
        }
    }
    public void takeDamage(int amount) { health -= amount; }
    public boolean isDead() { return health <= 0; }
    public abstract Rectangle getBounds();
    public abstract void applyFreeze();

    public void moveAlongWaypoints(float currentSpeed) {
        if (waypoints == null || wpIdx >= waypoints.length) { 
            x += currentSpeed; 
            facingRight = true;
            return; 
        }
        
        float targetX = waypoints[wpIdx].col * Constants.TILE_SIZE;
        float targetY = waypoints[wpIdx].row * Constants.TILE_SIZE;
        float dx = targetX - x; float dy = targetY - y;
        
        if (dx > 0) facingRight = true;
        else if (dx < 0) facingRight = false;
        
        float dist = (float)Math.hypot(dx, dy);
        
        if (dist <= currentSpeed) {
            x = targetX; y = targetY; wpIdx++; 
        } else {
            x += (dx / dist) * currentSpeed; y += (dy / dist) * currentSpeed;
        }
    }
}
