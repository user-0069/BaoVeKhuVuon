package com.team.gardendefense.model.map;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.utils.ImageLoader;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

public class Grid {
    public int[][] map;
    private int levelId;
    private Image backgroundImage;
    private Image foregroundImage;

    public Grid(int[][] mapData) { this(mapData, 0); }

    public Grid(int[][] mapData, int levelId) {
        this.map = mapData;
        this.levelId = levelId;
        loadBackground();
    }

    private void loadBackground() {
        if (levelId == 1) {
            backgroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap1.png");
            foregroundImage = null;
        } else if (levelId == 2) {
            backgroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap2.png");
            foregroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap2_overlay.png");
        } else if (levelId == 3) {
            backgroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap3.png");
            foregroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap3_overlay.png");
        } else if (levelId == 4) {
            backgroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap4.png");
            foregroundImage = null;
        } else if (levelId == 5) {
            backgroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap5.png");
            foregroundImage = null;
        } else if (levelId == 6) {
            backgroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap6.png");
            foregroundImage = null;
        } else if (levelId == 7) {
            backgroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap7.png");
            foregroundImage = null;
        } else if (levelId == 8) {
            backgroundImage = ImageLoader.getImage("src/main/resources/assets/images/bgmap8.png");
            foregroundImage = null;
        } else {
            backgroundImage = null;
            foregroundImage = null;
        }
    }

    public void drawForeground(Graphics2D g) {
        if (map == null || foregroundImage == null) return;
        int totalW = map[0].length * Constants.TILE_SIZE;
        int totalH = map.length * Constants.TILE_SIZE;
        g.drawImage(foregroundImage, 0, 0, totalW, totalH, null);
    }

    public void draw(Graphics2D g) {
        if (map == null) return;
        int totalW = map[0].length * Constants.TILE_SIZE;
        int totalH = map.length * Constants.TILE_SIZE;

        // Vẽ ảnh background nếu có, nếu không fallback màu xanh lá
        if (backgroundImage != null) {
            int verticalOffset = levelId == 5 || levelId == 6 ? 16
                    : levelId == 7 ? 12
                    : levelId == 8 ? 8 : 0;
            g.drawImage(backgroundImage, 0, verticalOffset, totalW, totalH + verticalOffset, null);
            return;
        }

        g.setColor(new Color(60, 170, 60));
        g.fillRect(0, 0, totalW, totalH);

        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length; c++) {
                int x = c * Constants.TILE_SIZE; int y = r * Constants.TILE_SIZE;
                if (map[r][c] == 1) {
                    g.setColor(new Color(210, 180, 140));
                    g.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                } else if (map[r][c] == 2) {
                    g.setColor(new Color(60, 170, 60));
                    g.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                    g.setColor(new Color(238, 232, 170));
                    g.fillRect(x + 8, y + 8, Constants.TILE_SIZE - 16, Constants.TILE_SIZE - 16);
                    g.setColor(new Color(139, 137, 100));
                    g.drawRect(x + 8, y + 8, Constants.TILE_SIZE - 16, Constants.TILE_SIZE - 16);
                }
            }
        }
    }
}
