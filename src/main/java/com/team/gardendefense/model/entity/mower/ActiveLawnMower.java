package com.team.gardendefense.model.entity.mower;

import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.monster.Monster;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.utils.ImageLoader;
import com.team.gardendefense.view.renderer.Reanim;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Xe Cắt Cỏ càn quét (Active LawnMower):
 * - Chạy từ bên phải qua bên trái trong vòng đúng 3.5 giây (210 ticks ở 60 FPS).
 * - Tiêu diệt toàn bộ zombie trên hàng ngang được chọn.
 * - Có hoạt ảnh khung xương (Reanim) cuộn bánh, rung động cơ, khói xả và mảnh cỏ văng.
 */
public class ActiveLawnMower {
    public float x, y;
    public int row;
    public float startX;
    public float endX;
    public float speed;
    public int elapsedTicks = 0;
    public static final int TOTAL_TICKS = 210; // 3.5 giây * 60 FPS = 210 ticks
    public boolean finished = false;

    private Image mowerImg;
    private Image bodyImg;
    private Image engineImg;
    private Image wheelImg;

    // Hiệu ứng hạt bụi khói xả và mảnh cỏ bay
    private static class Particle {
        float px, py, vx, vy;
        Color color;
        int life, maxLife;
        float size;

        Particle(float x, float y, float vx, float vy, Color color, int maxLife, float size) {
            this.px = x; this.py = y; this.vx = vx; this.vy = vy;
            this.color = color; this.life = maxLife; this.maxLife = maxLife;
            this.size = size;
        }
    }

    private final List<Particle> particles = new CopyOnWriteArrayList<>();

    public ActiveLawnMower(int row, float startX, float endX) {
        this.row = row;
        this.startX = startX;
        this.endX = endX;
        this.x = startX;
        this.y = row * Constants.TILE_SIZE + (Constants.TILE_SIZE - 56) / 2.0f;
        this.speed = (startX - endX) / (float) TOTAL_TICKS;

        this.mowerImg = ImageLoader.getImage("src/main/resources/assets/images/icon_roofcleaner.png");
        if (this.mowerImg == null) {
            this.mowerImg = ImageLoader.getImage("assets/images/icon_roofcleaner.png");
        }
        this.bodyImg = ImageLoader.getImage("src/main/resources/assets/reanim/LawnMower_body.png");
        this.engineImg = ImageLoader.getImage("src/main/resources/assets/reanim/LawnMower_engine.png");
        this.wheelImg = ImageLoader.getImage("src/main/resources/assets/reanim/LawnMower_wheel1.png");
    }

    public void update(GameManager gm) {
        if (finished) return;

        x -= speed;
        elapsedTicks++;

        // Sinh hạt khói xả ở phía sau xe (bên phải vì xe chạy sang trái)
        if (elapsedTicks % 2 == 0) {
            float tailX = x + 58;
            float tailY = y + 26 + (float) (Math.random() * 8 - 4);
            // Khói xả màu xám
            particles.add(new Particle(tailX, tailY, (float) (Math.random() * 2.0 + 1.0), (float) (Math.random() * 1.5 - 0.75),
                    new Color(90, 90, 90, 180), 24, (float) (Math.random() * 6 + 5)));
            // Mảnh cỏ xanh văng ra từ lưỡi cắt phía trước
            particles.add(new Particle(x + 5, y + 42, (float) (Math.random() * 3.0 - 1.5), (float) -(Math.random() * 3.0 + 1.0),
                    new Color(45, 180, 50, 220), 18, (float) (Math.random() * 4 + 3)));
        }

        // Cập nhật các hạt
        for (Particle p : particles) {
            p.px += p.vx;
            p.py += p.vy;
            p.life--;
        }
        particles.removeIf(p -> p.life <= 0);

        // Tiêu diệt toàn bộ zombie trên cùng hàng ngang chạm phải xe cắt cỏ
        Rectangle mowerHitbox = new Rectangle((int) x - 15, (int) y - 10, 85, 70);
        for (Monster z : gm.zombies) {
            if (z.isDead()) continue;
            int zRow = (int) ((z.y + Constants.TILE_SIZE / 2) / Constants.TILE_SIZE);
            if (zRow == row || Math.abs(z.y - y) < Constants.TILE_SIZE * 0.75f) {
                Rectangle zBounds = z.getBounds();
                if (zBounds.intersects(mowerHitbox) || (z.x >= x - 35 && z.x <= x + 85)) {
                    // Tiêu diệt zombie ngay lập tức
                    z.takeDamage(99999);
                    com.team.gardendefense.system.AudioManager.getInstance().playMowerKillSound();
                    
                    // Nổ tung các mảnh văng zombie (đỏ, tím)
                    for (int i = 0; i < 12; i++) {
                        particles.add(new Particle(z.x + 20, z.y + 25,
                                (float) (Math.random() * 6.0 - 3.0), (float) -(Math.random() * 4.0 + 1.5),
                                (i % 2 == 0) ? new Color(180, 20, 20, 240) : new Color(80, 120, 70, 220),
                                28, (float) (Math.random() * 6 + 4)));
                    }
                }
            }
        }

        if (elapsedTicks >= TOTAL_TICKS || x <= endX) {
            finished = true;
        }
    }

    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Vẽ vệt cỏ bị san phẳng phía sau xe cắt cỏ
        int trackStartX = (int) Math.min(startX, x + 60);
        int trackEndX = (int) startX;
        if (trackEndX > trackStartX) {
            g2.setColor(new Color(30, 90, 25, 60));
            g2.fillRect(trackStartX, (int) y + 20, trackEndX - trackStartX, 24);
            g2.setColor(new Color(60, 130, 45, 90));
            g2.drawLine(trackStartX, (int) y + 20, trackEndX, (int) y + 20);
            g2.drawLine(trackStartX, (int) y + 44, trackEndX, (int) y + 44);
        }

        // 2. Vẽ các hạt khói và cỏ bay
        for (Particle p : particles) {
            float alpha = (float) p.life / p.maxLife;
            Color c = p.color;
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * alpha)));
            g2.fillOval((int) p.px, (int) p.py, (int) p.size, (int) p.size);
        }

        // 3. Rung giật động cơ khi đang chạy
        int engineBob = (int) (Math.sin(elapsedTicks * 1.2) * 2.2);

        // 4. Vẽ xe cắt cỏ hướng sang trái (quay đầu sang trái từ phải sang)
        int drawX = (int) x;
        int drawY = (int) y + engineBob;
        int drawW = 68;
        int drawH = 52;

        // Bóng đổ
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(drawX + 4, drawY + drawH - 12, drawW - 8, 14);

        if (mowerImg != null) {
            Graphics2D gMower = (Graphics2D) g2.create();
            // Lật ngang ảnh để xe quay đầu sang trái (chạy từ phải qua trái)
            gMower.translate(drawX + drawW, drawY);
            gMower.scale(-1, 1);
            gMower.drawImage(mowerImg, 0, 0, drawW, drawH, null);
            gMower.dispose();
        } else if (bodyImg != null) {
            // Fallback vẽ bằng các sprite ảnh sẵn có lật ngang sang trái
            if (wheelImg != null) {
                // Bánh trước
                g2.drawImage(wheelImg, drawX + 4, drawY + 28, 22, 22, null);
                // Bánh sau
                g2.drawImage(wheelImg, drawX + 38, drawY + 28, 22, 22, null);
            }
            // Thân xe lật sang trái
            g2.drawImage(bodyImg, drawX + 64, drawY, -64, 48, null);
            if (engineImg != null) {
                g2.drawImage(engineImg, drawX + 50, drawY + 6, -30, 28, null);
            }
        } else {
            // Fallback đồ họa vector xe cắt cỏ
            g2.setColor(new Color(210, 35, 35));
            g2.fillRoundRect(drawX, drawY + 12, 54, 30, 8, 8);
            g2.setColor(new Color(70, 70, 70));
            g2.fillOval(drawX + 4, drawY + 32, 18, 18);
            g2.fillOval(drawX + 32, drawY + 32, 18, 18);
            g2.setColor(new Color(180, 180, 180));
            g2.fillRect(drawX + 40, drawY + 4, 10, 16);
        }

        // 5. Hiệu ứng tia lửa và lưỡi dao quay tít phía trước xe
        g2.setColor(new Color(255, 235, 60));
        g2.fillOval(drawX - 6, drawY + 28 + engineBob, 4, 4);
        g2.setColor(new Color(255, 160, 40));
        g2.fillOval(drawX - 2, drawY + 32 + engineBob, 3, 3);

        g2.dispose();
    }
}
