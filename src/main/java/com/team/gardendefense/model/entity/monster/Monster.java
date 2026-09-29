package com.team.gardendefense.model.entity.monster;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.plant.Plant;
import com.team.gardendefense.model.level.Waypoint;

public abstract class Monster {
    public float x, y;
    public int health;
    public Plant attackTarget = null;
    public Waypoint[] waypoints;
    public int wpIdx = 0;
    
    public abstract void update(GameManager gm);
    public abstract void draw(Graphics2D g);
    public void takeDamage(int amount) { health -= amount; }
    public boolean isDead() { return health <= 0; }
    public abstract Rectangle getBounds();
    public abstract void applyFreeze();

    public void moveAlongWaypoints(float currentSpeed) {
        if (waypoints == null || wpIdx >= waypoints.length) { x += currentSpeed; return; } // Đi thẳng ra khỏi map
        float targetX = waypoints[wpIdx].x;
        float targetY = waypoints[wpIdx].y;
        float dx = targetX - x; float dy = targetY - y;
        float dist = (float)Math.hypot(dx, dy);
        
        if (dist <= currentSpeed) {
            x = targetX; y = targetY; wpIdx++; // Đã đến neo, chuyển mốc
        } else {
            x += (dx / dist) * currentSpeed; y += (dy / dist) * currentSpeed;
        }
    }
}
