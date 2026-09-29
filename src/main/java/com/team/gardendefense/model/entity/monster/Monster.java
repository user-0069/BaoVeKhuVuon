package com.team.gardendefense.model.entity.monster;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.plant.Plant;

public abstract class Monster {
    public float x, y;
    public int health;
    public Plant attackTarget = null;
    
    public abstract void update(GameManager gm);
    public abstract void draw(Graphics2D g);
    public void takeDamage(int amount) { health -= amount; }
    public boolean isDead() { return health <= 0; }
    public abstract Rectangle getBounds();
    public abstract void applyFreeze();
}
