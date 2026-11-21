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
    public int freezeTimer = 0;
    public boolean completelyFrozen = false;

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
        } else if (image != null) {
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

        if (freezeTimer > 0) {
            drawFreezeEffect(g);
        }
    }

    protected void drawFreezeEffect(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int cx = (int) x + Constants.TILE_SIZE / 2;
        int cy = (int) y + Constants.TILE_SIZE / 2;
        int bw = Constants.TILE_SIZE - 8;
        int bh = Constants.TILE_SIZE + 6;
        int bx = cx - bw / 2;
        int by = cy - bh / 2 - 2;

        if (completelyFrozen) {
            // Khối băng pha lê 3D bọc kín zombie
            g2.setColor(new java.awt.Color(160, 220, 255, 115));
            g2.fillRoundRect(bx, by, bw, bh, 14, 14);

            g2.setColor(new java.awt.Color(230, 250, 255, 220));
            g2.drawRoundRect(bx, by, bw, bh, 14, 14);

            // Các vệt phản quang pha lê & tia nứt băng
            g2.setColor(new java.awt.Color(255, 255, 255, 170));
            g2.drawLine(bx + 6, by + 6, bx + 18, by + 18);
            g2.drawLine(bx + 8, by + 6, bx + 24, by + 12);
            g2.drawLine(bx + bw - 10, by + 8, bx + bw - 20, by + 22);

            // Bông tuyết lấp lánh trên đỉnh khối băng
            g2.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 16));
            g2.drawString("❄", cx - 8, by - 3);
        } else {
            // Lớp sương lạnh buốt & hào quang xanh nhạt khi bị làm chậm
            g2.setColor(new java.awt.Color(64, 164, 255, 60));
            g2.fillRoundRect(bx + 4, by + 4, bw - 8, bh - 8, 12, 12);

            g2.setColor(new java.awt.Color(135, 206, 250, 180));
            g2.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 12));
            g2.drawString("❄", bx + 2, by + 14);
        }
        g2.dispose();
    }

    public void takeDamage(int amount) { health -= amount; }
    public boolean isDead() { return health <= 0; }
    public abstract Rectangle getBounds();

    public void applyFreeze() {
        applyFreeze(Constants.FREEZE_DURATION, false);
    }

    public void applyFreeze(int duration, boolean fullImmobilize) {
        this.freezeTimer = Math.max(this.freezeTimer, duration);
        if (fullImmobilize) {
            this.completelyFrozen = true;
        }
    }

    public boolean isFrozen() {
        return freezeTimer > 0;
    }

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
