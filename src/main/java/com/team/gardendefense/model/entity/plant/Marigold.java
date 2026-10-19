package com.team.gardendefense.model.entity.plant;

import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.model.entity.coin.CoinType;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.utils.ImageLoader;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Random;

/**
 * Cây Marigold (Hoa Cúc):
 * Tự động tích lũy và nhả xu ngẫu nhiên sau mỗi 24 giây (1440 frames).
 * Tỉ lệ nhả: 85% Xu Bạc ($10), 15% Xu Vàng ($50).
 */
public class Marigold extends Plant {
    private static final Random random = new Random();
    public static final int COIN_PRODUCE_INTERVAL = 60 * 24; // 24 giây = 1440 frames

    private int timer = 0;
    private Image petalsImg;
    private Image headImg;

    public Marigold(int col, int row) {
        super(col, row);
        this.health = 5;
        this.maxHealth = 5;
        this.petalsImg = ImageLoader.getImage("src/main/resources/assets/reanim/Marigold_petals.png");
        this.headImg = ImageLoader.getImage("src/main/resources/assets/reanim/Marigold_head.png");
    }

    @Override
    public void update(GameManager gm) {
        timer++;
        if (timer >= COIN_PRODUCE_INTERVAL) {
            timer = 0;
            produceCoin(gm);
        }
    }

    private void produceCoin(GameManager gm) {
        // 85% Xu Bạc, 15% Xu Vàng
        CoinType type = (random.nextFloat() < 0.15f) ? CoinType.GOLD : CoinType.SILVER;
        gm.spawnCoin(x + Constants.TILE_SIZE / 2.0f, y + Constants.TILE_SIZE / 2.0f, type);
    }

    @Override
    public void draw(Graphics2D g) {
        int cx = x + 4;
        int cy = y + 4;
        int size = Constants.TILE_SIZE;

        // Vẽ hiệu ứng sắp ra xu (sáng dần trong 2 giây trước khi nhả xu)
        if (timer > COIN_PRODUCE_INTERVAL - 120) {
            float glowAlpha = ((timer % 30) / 30f) * 0.4f;
            g.setColor(new Color(255, 230, 100, (int)(glowAlpha * 255)));
            g.fillOval(cx - 5, cy - 5, size + 10, size + 10);
        }

        if (petalsImg != null && headImg != null) {
            g.drawImage(petalsImg, cx, cy, size, size, null);
            g.drawImage(headImg, cx + 8, cy + 8, size - 16, size - 16, null);
        } else {
            // Fallback vẽ bông hoa cúc vàng
            g.setColor(new Color(255, 200, 0));
            g.fillOval(cx, cy, size, size);
            g.setColor(new Color(255, 255, 255));
            g.fillOval(cx + 10, cy + 10, size - 20, size - 20);
        }
    }
}

