package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public class WallNut extends Plant {
    public WallNut(int col, int row) { 
        super(col, row); 
        this.health = 30; // Máu cực trâu
        this.maxHealth = 30;
    }
    
    @Override
    public void update(GameManager gm) { /* Không làm gì cả, chỉ đứng chặn */ }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(139, 69, 19)); // SaddleBrown
        g.fillOval(x + 5, y + 5, Constants.TILE_SIZE - 10, Constants.TILE_SIZE - 10);
        // Vẽ thêm vết nứt nếu máu thấp
        g.setColor(Color.BLACK);
        if (health < 15) g.drawLine(x + 20, y + 20, x + 40, y + 40);
        if (health < 10) g.drawLine(x + 40, y + 20, x + 20, y + 40);
    }
}
