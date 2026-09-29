package com.team.gardendefense.model.map;
import com.team.gardendefense.utils.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

public class Grid {
    // 0 = Cỏ nền (Cấm trồng), 1 = Đường đi (Quái đi), 2 = Ô bệ đỡ (Được phép trồng)
    public int[][] map; 

    public Grid() {
        map = new int[Constants.ROWS][Constants.COLS];
        
        for(int r = 0; r < Constants.ROWS; r++) {
            for(int c = 0; c < Constants.COLS; c++) {
                map[r][c] = 0; // Mặc định là cỏ nền không trồng được
                
                // Trải Đường đi
                if (r == 5 && c <= 10) map[r][c] = 1;
                else if (c == 10 && r > 5 && r <= 9) map[r][c] = 1;
                else if (r == 9 && c >= 10) map[r][c] = 1;
            }
        }

        // Định vị các bệ đỡ cho phép trồng tháp (Tower Slots)
        int[][] slots = {
            {4, 2}, {4, 5}, {4, 8}, // Dãy trên đường đi 1
            {6, 2}, {6, 5}, {6, 8}, // Dãy dưới đường đi 1
            {7, 9}, {7, 11},        // 2 bên cột dọc
            {8, 12}, {8, 14},       // Dãy trên đường đi 2
            {10, 12}, {10, 14}      // Dãy dưới đường đi 2
        };
        
        for (int[] slot : slots) {
            map[slot[0]][slot[1]] = 2; // Đánh dấu là Ô đất đặc biệt
        }
    }

    public void draw(Graphics2D g) {
        for(int r = 0; r < Constants.ROWS; r++) {
            for(int c = 0; c < Constants.COLS; c++) {
                int x = c * Constants.TILE_SIZE;
                int y = r * Constants.TILE_SIZE;
                
                if (map[r][c] == 1) { // Đường đi
                    g.setColor(new Color(210, 180, 140));
                    g.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                } 
                else if (map[r][c] == 2) { // Bệ đỡ tháp (Có thể trồng)
                    // Vẽ cỏ nền trước
                    g.setColor(new Color(60, 170, 60)); 
                    g.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                    
                    // Vẽ bệ đỡ màu Vàng Cát nhô lên
                    g.setColor(new Color(238, 232, 170)); 
                    g.fillRect(x + 8, y + 8, Constants.TILE_SIZE - 16, Constants.TILE_SIZE - 16);
                    
                    // Viền bệ
                    g.setColor(new Color(139, 137, 100));
                    g.drawRect(x + 8, y + 8, Constants.TILE_SIZE - 16, Constants.TILE_SIZE - 16);
                } 
                else { // Cỏ ngoài lề (Không thể trồng)
                    g.setColor(new Color(60, 170, 60)); 
                    g.fillRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
                }
                
                // Lưới siêu mờ
                g.setColor(new Color(0, 50, 0, 15));
                g.drawRect(x, y, Constants.TILE_SIZE, Constants.TILE_SIZE);
            }
        }
    }
}
