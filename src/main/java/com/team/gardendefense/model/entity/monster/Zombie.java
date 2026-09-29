package com.team.gardendefense.model.entity.monster;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Rectangle;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public class Zombie extends Monster {
    private float baseSpeed = 1.0f;
    private float speed = 1.0f;
    private int freezeTimer = 0;
    private int attackTimer = 0;
    
    private float targetX1 = 10 * Constants.TILE_SIZE; 
    private float targetY1 = 9 * Constants.TILE_SIZE;

    public Zombie() {
        this.x = -Constants.TILE_SIZE;
        this.y = 5 * Constants.TILE_SIZE;
        this.health = 5; // Tăng xíu máu
    }

    @Override
    public void applyFreeze() {
        freezeTimer = 180; // Đóng băng 3 giây (3 * 60 FPS)
    }

    @Override
    public void update(GameManager gm) {
        // Cập nhật trạng thái đóng băng
        if (freezeTimer > 0) {
            freezeTimer--;
            speed = baseSpeed * 0.4f; // Chậm đi nhiều
        } else {
            speed = baseSpeed;
        }

        // Cập nhật trạng thái tấn công
        if (attackTarget != null) {
            if (attackTarget.isDead()) {
                attackTarget = null; // Cây đã chết, đi tiếp
            } else {
                // Đứng yên cắn cây
                attackTimer++;
                if (attackTimer >= 60) { // Cắn 1 phát mỗi giây
                    attackTarget.takeDamage(1);
                    attackTimer = 0;
                }
                return; // Dừng việc di chuyển
            }
        }

        // Di chuyển
        if (y == 5 * Constants.TILE_SIZE && x < targetX1) {
            x += speed; if (x > targetX1) x = targetX1; 
        } 
        else if (x == targetX1 && y < targetY1) {
            y += speed; if (y > targetY1) y = targetY1; 
        } 
        else if (y == targetY1) {
            x += speed; 
        }
        else { x += speed; }
    }

    @Override
    public void draw(Graphics2D g) {
        if (freezeTimer > 0) g.setColor(new Color(173, 216, 230)); // Xanh lơ nhạt nếu bị đóng băng
        else g.setColor(Color.RED);
        
        g.fillRect((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20);
        g.setColor(Color.BLACK);
        g.drawRect((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20);
        
        // Máu
        g.setColor(Color.GREEN);
        g.fillRect((int)x + 15, (int)y - 5, (Constants.TILE_SIZE - 30) * health / 5, 5);
    }
    
    @Override
    public Rectangle getBounds() { return new Rectangle((int)x + 15, (int)y + 10, Constants.TILE_SIZE - 30, Constants.TILE_SIZE - 20); }
}
