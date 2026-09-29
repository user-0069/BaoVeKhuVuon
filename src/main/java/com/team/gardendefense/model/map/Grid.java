package com.team.gardendefense.model.map;
import com.team.gardendefense.utils.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

public class Grid {
    public int[][] map; 
    public Grid(int[][] mapData) { this.map = mapData; }

    public void draw(Graphics2D g) {
        if(map == null) return;
        for(int r = 0; r < map.length; r++) {
            for(int c = 0; c < map[r].length; c++) {
                int x = c * Constants.TILE_SIZE; int y = r * Constants.TILE_SIZE;
                if (map[r][c] == 1) { 
                    g.setColor(new Color(210, 180, 140)); g.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                } else if (map[r][c] == 2) { 
                    g.setColor(new Color(60, 170, 60)); g.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                    g.setColor(new Color(238, 232, 170)); g.fillRect(x + 8, y + 8, Constants.TILE_SIZE - 16, Constants.TILE_SIZE - 16);
                    g.setColor(new Color(139, 137, 100)); g.drawRect(x + 8, y + 8, Constants.TILE_SIZE - 16, Constants.TILE_SIZE - 16);
                } else { 
                    g.setColor(new Color(60, 170, 60)); g.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                }
                g.setColor(new Color(0, 50, 0, 15)); g.drawRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
            }
        }
    }
}
