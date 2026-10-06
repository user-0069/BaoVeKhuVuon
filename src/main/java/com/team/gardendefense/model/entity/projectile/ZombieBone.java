package com.team.gardendefense.model.entity.projectile;
import java.awt.Graphics2D;
import com.team.gardendefense.utils.ImageLoader;
import java.awt.Color;
import java.awt.Rectangle;

public class ZombieBone extends Projectile {
    private float vx, vy;

    public ZombieBone(float startX, float startY, float targetX, float targetY, int damage, float speed) {
        this.x = startX; this.y = startY;
        this.damage = damage;
        this.isEnemy = true; // Đây là đạn của quái
        
        float dx = targetX - startX;
        float dy = targetY - startY;
        float distance = (float) Math.hypot(dx, dy);
        if (distance == 0) distance = 1;
        this.vx = (dx / distance) * speed;
        this.vy = (dy / distance) * speed;
        this.image = ImageLoader.getImage("src/main/resources/assets/reanim/Zombie_hand_rock2.png");
    }
    
    @Override
    public void move() { x += vx; y += vy; }
    @Override
    public void hit() { active = false; }
    
    @Override
    public Rectangle getBounds() { return new Rectangle((int)x - 6, (int)y - 4, 12, 8); }
}
