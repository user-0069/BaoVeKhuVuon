package com.team.gardendefense.model.entity.pet;

import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.coin.Coin;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.utils.ImageLoader;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

/**
 * Linh vật hỗ trợ: Ốc sên Stinky (Pet)
 * - Tự động di chuyển tìm Coin rơi trên sân và nhặt giúp người chơi.
 * - Có thể ăn Chocolate để tăng 250% tốc độ di chuyển trong 3 phút.
 */
public class PetSnail implements IAutoCollector {
    public float x, y;
    private float baseSpeed = 1.6f;
    private float currentSpeed;
    private boolean facingRight = true;

    // Chocolate Boost
    private int boostTimer = 0; // Frames (60 fps * 180s = 10800 frames)

    // Bán kính nhặt
    private float collectionRadius = 38.0f;
    private Coin targetCoin = null;

    // Kích thước vẽ
    private int width = 50;
    private int height = 40;

    private Image shellImg;
    private Image bodyImg;

    public PetSnail(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.currentSpeed = baseSpeed;
        this.shellImg = ImageLoader.getImage("src/main/resources/assets/reanim/Stinky_shell.png");
        this.bodyImg = ImageLoader.getImage("src/main/resources/assets/reanim/Stinky_body.png");
    }

    public void update(GameManager gm) {
        // Cập nhật thời gian boost
        if (boostTimer > 0) {
            boostTimer--;
            currentSpeed = baseSpeed * 2.5f;
        } else {
            currentSpeed = baseSpeed;
        }

        scanAndCollect(gm);

        if (targetCoin != null) {
            float tx = targetCoin.x;
            float ty = targetCoin.y;

            float dx = tx - x;
            float dy = ty - y;
            float dist = (float) Math.hypot(dx, dy);

            facingRight = (dx >= 0);

            if (dist <= collectionRadius) {
                targetCoin.collect(gm);
                targetCoin = null;
            } else {
                x += (dx / dist) * currentSpeed;
                y += (dy / dist) * currentSpeed;
            }
        } else {
            // Đi tuần tra nhẹ nhàng quanh mép dưới
            if (facingRight) {
                x += currentSpeed * 0.5f;
                if (x > gm.getMapWidth() - width - 20) facingRight = false;
            } else {
                x -= currentSpeed * 0.5f;
                if (x < 30) facingRight = true;
            }
        }
    }

    @Override
    public void scanAndCollect(GameManager gm) {
        // Nếu target cũ đã chết hoặc đang bay về ví thì bỏ chọn
        if (targetCoin != null && (targetCoin.isDead() || targetCoin.state == Coin.State.COLLECTING)) {
            targetCoin = null;
        }

        if (targetCoin != null) return;

        // Tìm đồng xu gần nhất
        float minDistance = Float.MAX_VALUE;
        Coin nearest = null;

        for (Coin c : gm.coins) {
            if (c.isDead() || c.state == Coin.State.COLLECTING) continue;
            float dist = (float) Math.hypot(c.x - x, c.y - y);
            if (dist < minDistance) {
                minDistance = dist;
                nearest = c;
            }
        }

        targetCoin = nearest;
    }

    public void feedChocolate() {
        this.boostTimer = 60 * 180; // 3 phút
    }

    public boolean isBoosted() {
        return boostTimer > 0;
    }

    @Override
    public float getCollectionRadius() {
        return collectionRadius;
    }

    @Override
    public boolean isBusy() {
        return targetCoin != null;
    }

    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();

        int drawX = (int) x;
        int drawY = (int) y;

        // Hiệu ứng hào quang khi đang ăn Chocolate Boost
        if (isBoosted()) {
            g2.setColor(new Color(255, 180, 0, 90));
            g2.fillOval(drawX - 6, drawY - 6, width + 12, height + 12);
        }

        // Bóng đổ
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(drawX + 5, drawY + height - 10, width - 10, 12);

        // Vẽ thân & vỏ ốc sên
        if (bodyImg != null && shellImg != null) {
            if (facingRight) {
                g2.drawImage(bodyImg, drawX + width, drawY, -width, height, null);
                g2.drawImage(shellImg, drawX + width, drawY, -width, height, null);
            } else {
                g2.drawImage(bodyImg, drawX, drawY, width, height, null);
                g2.drawImage(shellImg, drawX, drawY, width, height, null);
            }
        } else {
            // Fallback vẽ hoạt họa ốc sên
            g2.setColor(new Color(220, 190, 140));
            g2.fillOval(drawX, drawY + 10, width, height - 10);
            g2.setColor(new Color(160, 100, 50));
            g2.fillOval(drawX + 10, drawY, width - 15, height - 5);
        }

        g2.dispose();
    }
}

