package com.team.gardendefense.model.entity.projectile;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;

public abstract class Projectile {
    public float x, y;
    public int damage;
    public boolean active = true;
    public boolean isEnemy = false;
    public boolean isFreeze = false; // Thuộc tính đóng băng

    public abstract void move();
    public Image image;
    public abstract void hit();
    public void draw(Graphics2D g) {
        if (image != null) {
            int w = 24, h = 24;
            g.drawImage(image, (int)x - w/2, (int)y - h/2, w, h, null);
        }
    }
    public abstract Rectangle getBounds();
}
