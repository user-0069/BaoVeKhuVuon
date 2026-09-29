package com.team.gardendefense.model.entity.projectile;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Projectile {
    public float x, y;
    public int damage;
    public boolean active = true;
    public boolean isEnemy = false;
    public boolean isFreeze = false; // Thuộc tính đóng băng

    public abstract void move();
    public abstract void hit();
    public abstract void draw(Graphics2D g);
    public abstract Rectangle getBounds();
}
