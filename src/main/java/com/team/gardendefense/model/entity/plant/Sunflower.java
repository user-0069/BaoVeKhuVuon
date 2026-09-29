package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public class Sunflower extends Plant {
    private int sunTimer = 0;

    public Sunflower(int col, int row) { 
        super(col, row); 
        this.health = Constants.HP_SUNFLOWER; this.maxHealth = Constants.HP_SUNFLOWER;
    }
    
    @Override
    public void update(GameManager gm) {
        sunTimer++;
        if (sunTimer >= Constants.RATE_SUNFLOWER) {
            gm.sun += Constants.AMOUNT_SUNFLOWER;
            sunTimer = 0;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(34, 139, 34)); g.fillRect(x + 28, y + 20, 8, Constants.TILE_SIZE - 20);
        g.setColor(Color.YELLOW); g.fillOval(x + 10, y + 5, Constants.TILE_SIZE - 20, Constants.TILE_SIZE - 20);
        g.setColor(new Color(139, 69, 19)); g.fillOval(x + 20, y + 15, Constants.TILE_SIZE - 40, Constants.TILE_SIZE - 40);
    }
}
