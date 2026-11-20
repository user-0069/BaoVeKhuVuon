package com.team.gardendefense.model.entity.monster;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;
import com.team.gardendefense.utils.ImageLoader;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.plant.Plant;
import com.team.gardendefense.model.level.Waypoint;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.view.renderer.Reanim;

public abstract class Monster {
    public float x, y;
    public int health;
    public Plant attackTarget = null;
    public Waypoint[] waypoints;
    public int wpIdx = 1; // Mặc định điểm xuất phát là index 0, nên hướng tới index 1
    
    public boolean facingRight = false;
    public boolean isMiniBoss = false;

    public abstract void update(GameManager gm);
    public Image image;
    protected Reanim animation;
    protected double animationTicks;

    protected void setAnimation(String fileName) {
        animation = Reanim.load(fileName);
    }

    protected void advanceAnimation(double speedMultiplier) {
        animationTicks += speedMultiplier;
    }
    
    public void draw(Graphics2D g) {
        if (animation != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (facingRight) {
                g2.translate(2.0 * (x + Constants.TILE_SIZE / 2.0), 0);
                g2.scale(-1, 1);
            }
            // Scale and offset adjusted so the zombie stays strictly on the dirt road without overflowing into grass,
            // aligned on the exact same ground baseline (Y = 57) as plants.
            animation.draw(g2, x + 10, y - 11, 0.55f,
                    attackTarget == null ? "walk" : "eat", animationTicks, true);
            g2.dispose();
            return;
        }
        if (image != null) {
            int w = Constants.TILE_SIZE - 12;
            int h = Constants.TILE_SIZE - 7;
            int drawX = (int)x + 6;
            int drawY = (int)y + (57 - h);
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
