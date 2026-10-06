package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import com.team.gardendefense.utils.ImageLoader;
import java.awt.Color;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public class Sunflower extends Plant {
    private int sunTimer = 0;

    public Sunflower(int col, int row) { 
        super(col, row); 
        this.health = Constants.HP_SUNFLOWER; this.maxHealth = Constants.HP_SUNFLOWER;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/Sunflower_full.png");
    }

    @Override
    public void update(GameManager gm) {
        sunTimer++;
        if (sunTimer >= Constants.RATE_SUNFLOWER) {
            gm.sun += Constants.AMOUNT_SUNFLOWER;
            sunTimer = 0;
        }
    }

    
}
