package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;
import com.team.gardendefense.utils.ImageLoader;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public abstract class Plant {
    public int x, y;
    public int health;
    public int maxHealth;
    public Image image;
    public int direction = 1; // 0=Up, 1=Right, 2=Down, 3=Left

    public Plant(int col, int row) {
        this.x = col * Constants.TILE_SIZE;
        this.y = row * Constants.TILE_SIZE;
    }
    public abstract void update(GameManager gm);
    public void draw(Graphics2D g) {
        if (image != null) {
            int cx = x + Constants.TILE_SIZE / 2;
            int cy = y + Constants.TILE_SIZE / 2;
            int size = Constants.TILE_SIZE + 10;
            Graphics2D g2 = (Graphics2D) g.create();
            
            if (direction == 0) { // Up
                g2.rotate(-Math.PI / 2, cx, cy);
                g2.drawImage(image, x - 5, y - 5, size, size, null);
            } else if (direction == 2) { // Down
                g2.rotate(Math.PI / 2, cx, cy);
                g2.drawImage(image, x - 5, y - 5, size, size, null);
            } else if (direction == 3) { // Left (flip)
                g2.drawImage(image, x - 5 + size, y - 5, -size, size, null);
            } else { // Right
                g2.drawImage(image, x - 5, y - 5, size, size, null);
            }
            g2.dispose();
        }
    }
    
    public void takeDamage(int amount) { health -= amount; }
    public boolean isDead() { return health <= 0; }
    public Rectangle getBounds() { return new Rectangle(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE); }
}
