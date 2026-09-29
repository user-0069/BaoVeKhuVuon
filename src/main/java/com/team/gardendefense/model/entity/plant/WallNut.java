package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public class WallNut extends Plant {
    public WallNut(int col, int row) { 
        super(col, row); 
        this.health = Constants.HP_WALLNUT; this.maxHealth = Constants.HP_WALLNUT;
    }
    @Override
    public void update(GameManager gm) { }
    @Override
    public void draw(Graphics2D g) {
        g.setColor(new Color(139, 69, 19)); g.fillOval(x + 5, y + 5, Constants.TILE_SIZE - 10, Constants.TILE_SIZE - 10);
        g.setColor(Color.BLACK);
        if (health < Constants.HP_WALLNUT/2) g.drawLine(x + 20, y + 20, x + 40, y + 40);
        if (health < Constants.HP_WALLNUT/3) g.drawLine(x + 40, y + 20, x + 20, y + 40);
    }
}
