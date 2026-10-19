package com.team.gardendefense.model.entity.coin;

import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.system.WalletManager;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.utils.ImageLoader;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.Random;

/**
 * Thực thể Coin/Diamond rớt trên sân:
 * 1. Spawn: Nảy parabol từ xác quái.
 * 2. Idle: Tồn tại 6 giây (360 frames); 2 giây cuối nhấp nháy mờ dần.
 * 3. Interaction: Hover hoặc Click để nhặt.
 * 4. Collect Motion: Bay mượt mà (Lerp) và thu nhỏ dần về biểu tượng Ví tiền ở góc màn hình.
 */
public class Coin {
    public enum State { SPAWNING, IDLE, COLLECTING }

    private static final Random random = new Random();

    public float x, y;
    public CoinType type;
    public State state = State.SPAWNING;

    // Vật lý nảy parabol
    private float vx, vy;
    private float groundY;
    private boolean hasBounced = false;

    // Thời gian tồn tại (60 FPS: 360 frames = 6s, 240 frames = 4s)
    private int lifeTimer = 0;
    public static final int TOTAL_LIFETIME = 360;
    public static final int FADE_START_FRAME = 240;

    // Thu nhỏ và bay về UI
    private float targetUIX, targetUIY;
    private float scale = 1.0f;
    private boolean dead = false;

    // Kích thước hiển thị cơ bản
    private int baseSize = 36;
    private Image sprite;

    public Coin(float startX, float startY, CoinType type) {
        this.x = startX;
        this.y = startY;
        this.type = type;
        this.groundY = startY + random.nextInt(25) - 10;
        
        // Vận tốc nảy parabol
        this.vx = (random.nextFloat() * 4.0f) - 2.0f; // Văng sang trái/phải ngẫu nhiên
        this.vy = -7.0f - random.nextFloat() * 2.5f;   // Bắn vọt lên trên

        if (type == CoinType.DIAMOND) {
            baseSize = 44; // Kim cương to hơn một chút
        }

        this.sprite = ImageLoader.getImage(type.getImagePath());
    }

    public void update(GameManager gm) {
        if (dead) return;

        if (state == State.SPAWNING) {
            x += vx;
            y += vy;
            vy += 0.45f; // Trọng lực

            if (y >= groundY) {
                y = groundY;
                if (!hasBounced) {
                    vy = -3.0f; // Nảy nhẹ lần 2
                    vx *= 0.5f;
                    hasBounced = true;
                } else {
                    vx = 0;
                    vy = 0;
                    state = State.IDLE;
                }
            }
        } else if (state == State.IDLE) {
            lifeTimer++;

            // Kiểm tra chuột Hover qua để nhặt tự động
            if (isHovered(gm.mouseX, gm.mouseY)) {
                collect(gm);
            }

            if (lifeTimer >= TOTAL_LIFETIME) {
                dead = true; // Despawn
            }
        } else if (state == State.COLLECTING) {
            // Tọa độ mục tiêu là icon ví tiền ở góc phải trên
            targetUIX = gm.getScreenWidth() - 100;
            targetUIY = 40;

            // Bay mượt mà theo Lerp (nội suy phi tuyến)
            float dx = targetUIX - x;
            float dy = targetUIY - y;
            float dist = (float) Math.hypot(dx, dy);

            float speed = Math.max(12.0f, dist * 0.16f);
            if (dist <= speed || dist < 15.0f) {
                // Đã chạm vào ví tiền!
                WalletManager.getInstance().addCoins(type.getValue(), type, targetUIX, targetUIY);
                dead = true;
            } else {
                x += (dx / dist) * speed;
                y += (dy / dist) * speed;
                // Thu nhỏ dần kích thước khi tiếp cận đích
                scale = Math.max(0.25f, dist / 250.0f);
            }
        }
    }

    /**
     * Kích hoạt nhặt đồng xu.
     */
    public void collect(GameManager gm) {
        if (state == State.COLLECTING || dead) return;
        state = State.COLLECTING;
    }

    public boolean isHovered(int mX, int mY) {
        int radius = baseSize / 2 + 6;
        float dist = (float) Math.hypot(mX - (x + baseSize / 2), mY - (y + baseSize / 2));
        return dist <= radius;
    }

    public boolean isDead() {
        return dead;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, baseSize, baseSize);
    }

    public void draw(Graphics2D g) {
        if (dead) return;

        Graphics2D g2 = (Graphics2D) g.create();

        // 2 giây cuối: nhấp nháy báo hiệu sắp biến mất
        if (state == State.IDLE && lifeTimer >= FADE_START_FRAME) {
            int blink = (lifeTimer / 6) % 2;
            float alpha = (blink == 0) ? 0.35f : 1.0f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        int drawSize = Math.max(8, (int) (baseSize * scale));
        int drawX = (int) (x + (baseSize - drawSize) / 2.0f);
        int drawY = (int) (y + (baseSize - drawSize) / 2.0f);

        // Vẽ bóng đổ dưới đất
        if (state == State.IDLE || state == State.SPAWNING) {
            g2.setColor(new Color(0, 0, 0, 70));
            int shadowW = drawSize;
            int shadowH = drawSize / 3;
            g2.fillOval((int) x, (int) (groundY + baseSize - shadowH / 2), shadowW, shadowH);
        }

        if (sprite != null) {
            g2.drawImage(sprite, drawX, drawY, drawSize, drawSize, null);
        } else {
            // Fallback vẽ hình tròn màu vàng/bạc nếu chưa load được ảnh
            if (type == CoinType.SILVER) g2.setColor(new Color(192, 192, 192));
            else if (type == CoinType.GOLD) g2.setColor(new Color(255, 215, 0));
            else g2.setColor(new Color(100, 220, 255));
            g2.fillOval(drawX, drawY, drawSize, drawSize);
            g2.setColor(Color.WHITE);
            g2.drawOval(drawX, drawY, drawSize, drawSize);
        }

        g2.dispose();
    }
}

