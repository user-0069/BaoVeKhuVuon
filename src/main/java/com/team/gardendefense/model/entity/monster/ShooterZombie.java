package com.team.gardendefense.model.entity.monster;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.plant.Plant;
import com.team.gardendefense.model.entity.projectile.ZombieBone;

public class ShooterZombie extends Monster {
    private float baseSpeed = 0.8f; // Đi chậm hơn Zombie thường một chút
    private float speed = 0.8f;
    private int freezeTimer = 0;
    
    private int shootTimer = 0;
    private float range = 250f; // Tầm nhìn quét cây
    
    private float targetX1 = 10 * Constants.TILE_SIZE; 
    private float targetY1 = 9 * Constants.TILE_SIZE;

    public ShooterZombie() {
        this.x = -Constants.TILE_SIZE;
        this.y = 5 * Constants.TILE_SIZE;
        this.health = 3; // Máu giấy hơn
    }

    @Override
    public void applyFreeze() {
        freezeTimer = 180;
    }

    @Override
    public void update(GameManager gm) {
        if (freezeTimer > 0) { freezeTimer--; speed = baseSpeed * 0.4f; } 
        else { speed = baseSpeed; }

        // Tìm cây gần nhất trong tầm bắn
        Plant closestPlant = null;
        float minDist = range;
        for(Plant p : gm.plants) {
            float dist = (float) Math.hypot((p.x + 32) - (x + 32), (p.y + 32) - (y + 32));
            if (dist <= minDist) {
                minDist = dist;
                closestPlant = p;
            }
        }

        // Nếu có cây trong tầm -> Đứng lại bắn
        if (closestPlant != null) {
            shootTimer++;
            if (shootTimer >= 100) { // Tốc độ xả đạn
                gm.projectiles.add(new ZombieBone(
                    x + 32, y + 32, 
                    closestPlant.x + 32, closestPlant.y + 32, 
                    1, 5f // Sát thương 1, tốc độ đạn 5
                ));
                shootTimer = 0;
            }
            return; // Đứng yên không di chuyển khi đang có mục tiêu
        }

        // Di chuyển bình thường nếu không có mục tiêu (Không gọi cắn cận chiến vì đây là xạ thủ)
        if (y == 5 * Constants.TILE_SIZE && x < targetX1) {
            x += speed; if (x > targetX1) x = targetX1; 
        } 
        else if (x == targetX1 && y < targetY1) {
            y += speed; if (y > targetY1) y = targetY1; 
        } 
        else if (y == targetY1) { x += speed; }
        else { x += speed; }
    }

    @Override
    public void draw(Graphics2D g) {
        if (freezeTimer > 0) g.setColor(new Color(173, 216, 230)); 
        else g.setColor(new Color(255, 140, 0)); // Màu Cam (Orange) cho xạ thủ
        
        g.fillRect((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20);
        g.setColor(Color.BLACK);
        g.drawRect((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20);
        
        // Vẽ thêm một ống nhòm/nòng súng nhỏ
        g.setColor(Color.BLACK);
        g.fillOval((int)x + 25, (int)y + 20, 10, 10);
        
        // Máu
        g.setColor(Color.GREEN);
        g.fillRect((int)x + 15, (int)y - 5, (Constants.TILE_SIZE - 30) * health / 3, 5);
    }
    
    @Override
    public Rectangle getBounds() { return new Rectangle((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20); }
}
