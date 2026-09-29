package com.team.gardendefense.model.entity.projectile;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;

public class Pea extends Projectile {
    private float vx, vy;
    private Color color;

    public Pea(float startX, float startY, float targetX, float targetY, int damage, Color color, float speed, boolean isFreeze) {
        this.x = startX; this.y = startY;
        this.damage = damage; this.color = color;
        this.isFreeze = isFreeze;
        
        float dx = targetX - startX;
        float dy = targetY - startY;
        float distance = (float) Math.sqrt(dx*dx + dy*dy);
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
        g.setColor(color); g.fillOval((int)x - 8, (int)y - 8, 16, 16); 
        g.setColor(Color.WHITE); g.drawOval((int)x - 8, (int)y - 8, 16, 16);
    }
    @Override
    public Rectangle getBounds() { return new Rectangle((int)x - 8, (int)y - 8, 16, 16); }
}
