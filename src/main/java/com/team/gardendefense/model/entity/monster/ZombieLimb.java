package com.team.gardendefense.model.entity.monster;

import com.team.gardendefense.utils.ImageLoader;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

/**
 * Hiệu ứng mảnh cơ thể rơi rụng của Zombie (Cánh tay hoặc Đầu):
 * - Văng ra theo quỹ đạo parabol vật lý có trọng lực và nảy nhẹ khi chạm đất.
 * - Xoay tròn tự nhiên khi bay trong không trung.
 * - Mờ dần từ từ (fade out) sau khi rơi xuống đất rồi tự động biến mất.
 */
public class ZombieLimb {
    public enum LimbType { ARM, HEAD }

    public float x, y;
    public float vx, vy;
    public float rotation = 0;
    public float angularVel;
    public float groundY;
    public int timer;
    public static final int MAX_TIMER = 90; // 1.5 giây ở 60 FPS
    public static final float ZOMBIE_SCALE = 0.55f; // Giữ nguyên đúng tỉ lệ 0.55f của mô hình Zombie gốc
    public boolean facingRight;
    public LimbType type;
    public Image image;
    public boolean onGround = false;

    public ZombieLimb(float x, float y, float vx, float vy, float angularVel, float groundY, LimbType type, boolean facingRight) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.angularVel = angularVel;
        this.groundY = groundY;
        this.type = type;
        this.facingRight = facingRight;
        this.timer = MAX_TIMER;

        if (type == LimbType.ARM) {
            this.image = ImageLoader.getImage("src/main/resources/assets/particles/ZombieArm.png");
        } else {
            this.image = ImageLoader.getImage("src/main/resources/assets/particles/ZombieHead.png");
        }
    }

    public static ZombieLimb createArm(float zombieX, float zombieY, boolean facingRight) {
        float startX = facingRight ? (zombieX + 34) : (zombieX + 16);
        float startY = zombieY + 16;
        float vx = facingRight ? -(1.2f + (float) Math.random() * 1.4f) : (1.2f + (float) Math.random() * 1.4f);
        float vy = -(2.5f + (float) Math.random() * 1.2f);
        float angularVel = facingRight ? -(0.14f + (float) Math.random() * 0.12f) : (0.14f + (float) Math.random() * 0.12f);
        float groundY = zombieY + 43;
        return new ZombieLimb(startX, startY, vx, vy, angularVel, groundY, LimbType.ARM, facingRight);
    }

    public static ZombieLimb createHead(float zombieX, float zombieY, boolean facingRight) {
        float startX = facingRight ? (zombieX + 30) : (zombieX + 18);
        float startY = zombieY + 2;
        float vx = facingRight ? (1.5f + (float) Math.random() * 1.4f) : -(1.5f + (float) Math.random() * 1.4f);
        float vy = -(3.2f + (float) Math.random() * 1.4f);
        float angularVel = facingRight ? (0.18f + (float) Math.random() * 0.14f) : -(0.18f + (float) Math.random() * 0.14f);
        float groundY = zombieY + 40;
        return new ZombieLimb(startX, startY, vx, vy, angularVel, groundY, LimbType.HEAD, facingRight);
    }

    public void update() {
        x += vx;
        y += vy;
        vy += 0.32f; // Trọng lực rơi
        rotation += angularVel;

        if (y >= groundY) {
            y = groundY;
            vy = -vy * 0.42f; // Nảy lên nhẹ
            vx *= 0.72f; // Ma sát sàn
            angularVel *= 0.65f;
            if (Math.abs(vy) < 0.6f) {
                vy = 0;
                onGround = true;
                angularVel = 0;
            }
        }

        if (timer > 0) {
            timer--;
        }
    }

    public boolean isDead() {
        return timer <= 0;
    }

    public void draw(Graphics2D g) {
        if (isDead() || image == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        float alpha = (timer < 30) ? Math.max(0f, timer / 30.0f) : 1.0f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int w = image.getWidth(null);
        int h = image.getHeight(null);
        if (w <= 0 || h <= 0) {
            w = (type == LimbType.HEAD) ? 64 : 26;
            h = (type == LimbType.HEAD) ? 61 : 50;
        }

        AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.rotate(rotation);
        // Thu nhỏ đúng tỉ lệ 0.55f của zombie gốc để đầu và tay không bị quá to
        if (facingRight) {
            transform.scale(-ZOMBIE_SCALE, ZOMBIE_SCALE);
        } else {
            transform.scale(ZOMBIE_SCALE, ZOMBIE_SCALE);
        }
        transform.translate(-w / 2.0, -h / 2.0);

        g2.drawImage(image, transform, null);
        g2.dispose();
    }
}
