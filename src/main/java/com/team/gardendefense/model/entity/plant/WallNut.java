package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import com.team.gardendefense.utils.ImageLoader;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public class WallNut extends Plant {
    public WallNut(int col, int row) { 
        super(col, row); 
        this.health = Constants.HP_WALLNUT; this.maxHealth = Constants.HP_WALLNUT;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/WallNut_full.png");
    }
    @Override
    public void update(GameManager gm) { }
    
}
