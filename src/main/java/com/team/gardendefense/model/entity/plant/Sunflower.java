package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public class Sunflower extends Plant {
    private int sunTimer = 0;

    public Sunflower(int col, int row) { 
        super(col, row); 
        this.health = 5; 
        this.maxHealth = 5;
    }
    
    @Override
    public void update(GameManager gm) {
        sunTimer++;
        if (sunTimer >= 300) { // Mỗi 5 giây (300 frames) tạo ra 25 Sun
            gm.sun += 25;
            sunTimer = 0;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Cuống xanh
        g.setColor(new Color(34, 139, 34));
        g.fillRect(x + 28, y + 20, 8, Constants.TILE_SIZE - 20);
        // Hoa vàng
        g.setColor(Color.YELLOW);
        g.fillOval(x + 10, y + 5, Constants.TILE_SIZE - 20, Constants.TILE_SIZE - 20);
        // Nhụy nâu
        g.setColor(new Color(139, 69, 19)); 
        g.fillOval(x + 20, y + 15, Constants.TILE_SIZE - 40, Constants.TILE_SIZE - 40);
    }
}
