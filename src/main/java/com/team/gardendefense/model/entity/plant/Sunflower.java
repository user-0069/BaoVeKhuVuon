package com.team.gardendefense.model.entity.plant;
import java.awt.Graphics2D;
import com.team.gardendefense.utils.ImageLoader;
import java.awt.Image;
import java.awt.image.BufferedImage;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;

public class Sunflower extends Plant {
    private static final int ANIMATION_FRAME_DURATION = 4;
    private static final int FRAME_WIDTH = 460;
    private static final int FRAME_HEIGHT = 520;
    private static final int[][] FRAME_BOUNDS = {
        {79, 410, 419, 446},
        {526, 423, 421, 477},
        {988, 489, 412, 388},
        {1390, 432, 460, 456},
        {1840, 412, 418, 469},
        {2290, 434, 365, 422},
        {101, 1070, 373, 417},
        {553, 1070, 374, 417},
        {988, 1090, 400, 396},
        {1431, 1115, 419, 371},
        {1840, 1073, 440, 413},
        {2270, 1073, 415, 413}
    };
    private static final int[][] SUN_BOUNDS = {
        {2399, 280, 148, 150},
        {213, 904, 149, 149},
        {655, 899, 163, 150},
        {1145, 938, 83, 91},
        {1587, 1005, 87, 55}
    };
    private static final BufferedImage SPRITE_SHEET = loadSpriteSheet();
    private static final Image[] PRODUCTION_FRAMES = loadProductionFrames();
    private static final Image[] PRODUCTION_SUNS = loadProductionSuns();
    private static final int[] SUN_FRAME_INDEXES = {5, 6, 7, 8, 9};

    private int sunTimer = 0;
    private int sunPopTimer = 0;
    private static final int SUN_POP_DURATION = 35;

    public Sunflower(int col, int row) { 
        super(col, row); 
        this.health = Constants.HP_SUNFLOWER; this.maxHealth = Constants.HP_SUNFLOWER;
        this.image = ImageLoader.getImage("src/main/resources/assets/images/Sunflower_full.png");
        setAnimation("SunFlower.reanim");
    }

    @Override
    public void update(GameManager gm) {
        sunTimer++;
        if (sunPopTimer > 0) {
            sunPopTimer--;
        }
        if (sunTimer >= Constants.RATE_SUNFLOWER) {
            gm.sun += Constants.AMOUNT_SUNFLOWER;
            sunTimer = 0;
            sunPopTimer = SUN_POP_DURATION;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Draw continuous warm solar glow charging up before producing sun (last 50 ticks)
        if (sunTimer >= Constants.RATE_SUNFLOWER - 50) {
            float progress = (sunTimer - (Constants.RATE_SUNFLOWER - 50)) / 50.0f;
            float pulse = (float) Math.sin(progress * Math.PI);
            int cx = x + Constants.TILE_SIZE / 2;
            int cy = y + Constants.TILE_SIZE / 2 - 4;
            int glowSize = (int)(Constants.TILE_SIZE * (0.85f + 0.25f * pulse));
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new java.awt.Color(255, 220, 50, (int)(pulse * 105)));
            g2.fillOval(cx - glowSize / 2, cy - glowSize / 2, glowSize, glowSize);
            g2.dispose();
        }

        // Draw animated sunflower skeleton smoothly at all times
        drawPlantImage(g, Constants.TILE_SIZE, true);

        // Draw sun token smoothly emerging and floating up above the flower
        if (sunPopTimer > 0) {
            float p = 1.0f - (sunPopTimer / (float) SUN_POP_DURATION);
            float floatY = (y + 14) - p * 24; // Smoothly rises from inside head to above head
            int sunSize = (int)(18 + Math.sin(p * Math.PI) * 4);
            int sunX = x + (Constants.TILE_SIZE - sunSize) / 2;
            int sunY = (int) floatY;

            Graphics2D g2 = (Graphics2D) g.create();
            float alpha = p < 0.8f ? 1.0f : (1.0f - p) / 0.2f;
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));

            // Sun aura
            g2.setColor(new java.awt.Color(255, 235, 100, 160));
            g2.fillOval(sunX - 3, sunY - 3, sunSize + 6, sunSize + 6);

            if (PRODUCTION_SUNS.length > 0) {
                g2.drawImage(PRODUCTION_SUNS[0], sunX, sunY, sunSize, sunSize, null);
            } else {
                g2.setColor(new java.awt.Color(255, 215, 0));
                g2.fillOval(sunX, sunY, sunSize, sunSize);
            }
            g2.dispose();
        }
    }

    private static BufferedImage loadSpriteSheet() {
        Image image = ImageLoader.getImage("src/main/resources/assets/images/sunfloweranimation2.png");
        return image instanceof BufferedImage ? (BufferedImage) image : null;
    }

    private static Image[] loadProductionFrames() {
        if (SPRITE_SHEET == null) return new Image[0];

        Image[] frames = new Image[FRAME_BOUNDS.length];
        for (int i = 0; i < FRAME_BOUNDS.length; i++) {
            int[] bounds = FRAME_BOUNDS[i];
            frames[i] = createPlantFrame(bounds[0], bounds[1], bounds[2], bounds[3]);
        }
        return frames;
    }

    private static Image[] loadProductionSuns() {
        if (SPRITE_SHEET == null) return new Image[0];
        Image[] suns = new Image[SUN_BOUNDS.length];
        for (int i = 0; i < SUN_BOUNDS.length; i++) {
            int[] bounds = SUN_BOUNDS[i];
            suns[i] = createTransparentCrop(bounds[0], bounds[1], bounds[2], bounds[3]);
        }
        return suns;
    }

    private static BufferedImage createPlantFrame(int x, int y, int width, int height) {
        BufferedImage crop = createTransparentCrop(x, y, width, height);
        BufferedImage frame = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = frame.createGraphics();
        try {
            g.drawImage(crop, (FRAME_WIDTH - width) / 2, FRAME_HEIGHT - height, null);
        } finally {
            g.dispose();
        }
        return frame;
    }

    private static BufferedImage createTransparentCrop(int x, int y, int width, int height) {
        BufferedImage crop = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int pixel = SPRITE_SHEET.getRGB(x + col, y + row);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                int brightness = (red + green + blue) / 3;
                int colorRange = Math.max(red, Math.max(green, blue)) - Math.min(red, Math.min(green, blue));
                if (colorRange <= 18 && brightness >= 100 && brightness <= 245) {
                    pixel = 0;
                }
                crop.setRGB(col, row, pixel);
            }
        }
        return crop;
    }

    private static boolean hasProductionAnimation() {
        return PRODUCTION_FRAMES.length == FRAME_BOUNDS.length;
    }
}
