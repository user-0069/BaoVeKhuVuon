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
        if (this.boostTimer > 0) {
            this.boostTimer += 60 * 180; // Cộng dồn thêm 3 phút
            if (this.boostTimer > 60 * 600) this.boostTimer = 60 * 600; // Tối đa 10 phút
        } else {
            this.boostTimer = 60 * 180; // 3 phút (10800 ticks)
        }
    }

    public int getBoostTimer() {
        return boostTimer;
    }

    public int getRemainingSeconds() {
        return boostTimer / 60;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public boolean isBoosted() {
        return boostTimer > 0;
    }

    @Override
    public float getCollectionRadius() {
        return isBoosted() ? 52.0f : collectionRadius;
    }

    @Override
    public boolean isBusy() {
        return targetCoin != null;
    }

    public void draw(Graphics2D g) {
        draw(g, -1, -1);
    }

    public void draw(Graphics2D g, int mouseX, int mouseY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        int drawX = (int) x;
        int drawY = (int) y;
        boolean hover = (mouseX >= drawX && mouseX <= drawX + width && mouseY >= drawY && mouseY <= drawY + height);

        // Hiệu ứng hào quang khi đang ăn Chocolate Boost
        if (isBoosted()) {
            float pulse = (float) Math.sin(System.currentTimeMillis() / 120.0);
            int glowPad = (int) (8 + 4 * pulse);

            // Lớp hào quang vàng cam rực rỡ
            g2.setColor(new Color(255, 170, 0, (int) (75 + 30 * pulse)));
            g2.fillOval(drawX - glowPad, drawY - glowPad, width + glowPad * 2, height + glowPad * 2);

            g2.setColor(new Color(255, 230, 80, (int) (130 + 35 * pulse)));
            g2.setStroke(new java.awt.BasicStroke(1.8f));
            g2.drawOval(drawX - glowPad, drawY - glowPad, width + glowPad * 2, height + glowPad * 2);

            // Tia chớp tốc độ đồ họa phía sau (Polygon tia sét sắc nét)
            int trailX = facingRight ? drawX - 10 : drawX + width + 4;
            int[] sparkX = {trailX + 3, trailX + 7, trailX + 5, trailX + 8, trailX + 2, trailX + 4, trailX};
            int[] sparkY = {drawY + 16, drawY + 16, drawY + 22, drawY + 22, drawY + 29, drawY + 23, drawY + 23};
            g2.setColor(new Color(255, 235, 60, (int) (180 + 70 * pulse)));
            g2.fillPolygon(sparkX, sparkY, 7);

            // Hiển thị thanh đếm ngược thời gian nhỏ gọn trên đầu Pet (không dùng emoji để tránh lỗi font)
            int totalSec = getRemainingSeconds();
            int mins = totalSec / 60;
            int secs = totalSec % 60;
            String timerStr = String.format("SPEED %d:%02d", mins, secs);

            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
            int tw = g2.getFontMetrics().stringWidth(timerStr);
            int tx = drawX + (width - tw) / 2;
            int ty = drawY - 8;

            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRoundRect(tx - 6, ty - 12, tw + 12, 16, 6, 6);
            g2.setColor(new Color(255, 215, 0));
            g2.drawRoundRect(tx - 6, ty - 12, tw + 12, 16, 6, 6);
            g2.setColor(new Color(255, 240, 120));
            g2.drawString(timerStr, tx, ty);
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

        // Khung viền và bong bóng gợi ý khi rê chuột vào Pet
        if (hover) {
            g2.setColor(new Color(255, 235, 120, 200));
            g2.setStroke(new java.awt.BasicStroke(1.5f));
            g2.drawRoundRect(drawX - 2, drawY - 2, width + 4, height + 4, 8, 8);

            String tip = "Cho ăn Kẹo [Phím 2]";
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
            int tw = g2.getFontMetrics().stringWidth(tip);
            int bx = drawX + (width - tw) / 2;
            int by = isBoosted() ? drawY - 28 : drawY - 10;

            g2.setColor(new Color(35, 20, 10, 210));
            g2.fillRoundRect(bx - 4, by - 12, tw + 8, 16, 6, 6);
            g2.setColor(new Color(255, 200, 80));
            g2.drawRoundRect(bx - 4, by - 12, tw + 8, 16, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString(tip, bx, by);
        }

        g2.dispose();
    }
}

