package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public abstract class Plant {
    public int x, y;
    public int health;
    public int maxHealth;
    
    public Plant(int col, int row) {
        this.x = col * Constants.TILE_SIZE;
        this.y = row * Constants.TILE_SIZE;
    }
    public abstract void update(GameManager gm);
    public abstract void draw(Graphics2D g);
    
    public void takeDamage(int amount) { health -= amount; }
    public boolean isDead() { return health <= 0; }
    public Rectangle getBounds() { return new Rectangle(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE); }
}
