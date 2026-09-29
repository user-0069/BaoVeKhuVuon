package com.team.gardendefense.model.entity.projectile;
import java.awt.Graphics2D;
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
    }
    
    @Override
    public void move() { x += vx; y += vy; }
    @Override
    public void hit() { active = false; }
    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.WHITE); 
        g.fillRoundRect((int)x - 6, (int)y - 4, 12, 8, 4, 4); // Cục xương/đá nhỏ
        g.setColor(Color.BLACK); g.drawRoundRect((int)x - 6, (int)y - 4, 12, 8, 4, 4);
    }
    @Override
    public Rectangle getBounds() { return new Rectangle((int)x - 6, (int)y - 4, 12, 8); }
}
