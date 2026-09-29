package com.team.gardendefense.controller;
import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Iterator;
import com.team.gardendefense.model.entity.plant.*;
import com.team.gardendefense.model.entity.monster.*;
import com.team.gardendefense.model.entity.projectile.*;
import com.team.gardendefense.model.map.Grid;
import com.team.gardendefense.utils.Constants;

public class GameManager {
    public enum GameState { MENU, INSTRUCTIONS, PLAYING, GAMEOVER }
    public GameState state = GameState.MENU; // Bắt đầu ở Menu chính

    public Grid grid;
    public List<Plant> plants;
    public List<Monster> zombies;
    public List<Projectile> projectiles;
    
    public int sun = 50; 
    private int spawnTimer = 0; private int passiveSunTimer = 0;
    
    public int mouseX = 0, mouseY = 0;
    public int hoverCol = -1, hoverRow = -1;

    // Menu Cards UI
    public Rectangle card1 = new Rectangle(120, 10, 70, 60);
    public Rectangle card2 = new Rectangle(200, 10, 70, 60);
    public Rectangle card3 = new Rectangle(280, 10, 70, 60);
    public Rectangle card4 = new Rectangle(360, 10, 70, 60);
    public Rectangle card5 = new Rectangle(440, 10, 70, 60);
    public int selectedPlant = 0; 

    // Các Nút bấm (Buttons)
    public Rectangle btnPlay = new Rectangle(412, 300, 200, 60);
    public Rectangle btnInstr = new Rectangle(412, 400, 200, 60);
    public Rectangle btnExit = new Rectangle(412, 500, 200, 60);
    public Rectangle btnBack = new Rectangle(412, 650, 200, 60);
    public Rectangle btnRestart = new Rectangle(412, 530, 200, 60);
    public Rectangle btnExitGO = new Rectangle(412, 610, 200, 60);

    public GameManager() {
        grid = new Grid();
        plants = new ArrayList<>(); zombies = new ArrayList<>(); projectiles = new ArrayList<>();
    }

    public void resetGame() {
        plants.clear(); zombies.clear(); projectiles.clear();
        sun = 50; spawnTimer = 0; passiveSunTimer = 0; selectedPlant = 0;
        state = GameState.PLAYING;
    }

    public void update() {
        if (state != GameState.PLAYING) return; // Chỉ chạy Logic khi đang PLAYING

        passiveSunTimer++;
        if (passiveSunTimer >= 600) { sun += 25; passiveSunTimer = 0; }

        spawnTimer++; 
        if (spawnTimer >= 220) {
            if (Math.random() < 0.3) zombies.add(new ShooterZombie());
            else zombies.add(new Zombie());
            spawnTimer = 0; 
        }

        Iterator<Plant> pltIt = plants.iterator();
        while(pltIt.hasNext()) {
            Plant p = pltIt.next();
            if (p.isDead()) pltIt.remove(); else p.update(this);
        }

        Iterator<Projectile> pIt = projectiles.iterator();
        while(pIt.hasNext()) {
            Projectile p = pIt.next(); p.move();
            if (p.x < 0 || p.x > Constants.SCREEN_WIDTH || p.y < 0 || p.y > Constants.SCREEN_HEIGHT) { pIt.remove(); continue; }
            
            Rectangle pBounds = p.getBounds();
            if (p.isEnemy) {
                for (Plant plant : plants) {
                    if (plant.getBounds().intersects(pBounds)) {
                        plant.takeDamage(p.damage); p.hit(); break;
                    }
                }
            } else {
                for (Monster z : zombies) {
                    if (z.getBounds().intersects(pBounds)) {
                        z.takeDamage(p.damage);
                        if (p.isFreeze) z.applyFreeze();
                        p.hit(); break;
                    }
                }
            }
            if (!p.active) pIt.remove();
        }

        Iterator<Monster> zIt = zombies.iterator();
        while(zIt.hasNext()) {
            Monster z = zIt.next();
            if (z.x > Constants.SCREEN_WIDTH) {
                sun -= 50; zIt.remove();
                if (sun < 0) { state = GameState.GAMEOVER; } 
                continue;
            }
            if (z.attackTarget == null && !(z instanceof ShooterZombie)) {
                for (Plant p : plants) {
                    if (z.getBounds().intersects(p.getBounds())) { z.attackTarget = p; break; }
                }
            }
            z.update(this);
            if (z.isDead()) { sun += 10; zIt.remove(); }
        }
    }

    public void draw(Graphics2D g) {
        if (state == GameState.MENU) {
            drawMainMenu(g);
        } else if (state == GameState.INSTRUCTIONS) {
            drawInstructions(g);
        } else {
            drawGameplay(g);
            if (state == GameState.GAMEOVER) {
                drawGameOver(g);
            }
        }
    }

    // --- CÁC HÀM VẼ (UI) ---

    private void drawMainMenu(Graphics2D g) {
        g.setColor(new Color(34, 139, 34)); // Phông nền xanh lá
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 70));
        g.drawString("BẢO VỆ KHU VƯỜN", 160, 180);

        drawButton(g, btnPlay, "CHƠI NGAY", mouseX, mouseY);
        drawButton(g, btnInstr, "HƯỚNG DẪN", mouseX, mouseY);
        drawButton(g, btnExit, "THOÁT", mouseX, mouseY);
    }

    private void drawInstructions(Graphics2D g) {
        // Hướng dẫn được gom gọn tại đây để dễ dàng thêm thông tin sau này
        g.setColor(new Color(30, 30, 40)); 
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("HƯỚNG DẪN CHƠI", 330, 80);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        int y = 140;
        g.drawString("Quy tắc: Tiêu diệt quái nhận 10 Sun. Quái lọt mất 50 Sun. Sun < 0 => THUA.", 50, y);
        
        y += 50; g.setColor(Color.YELLOW); g.drawString("THỰC VẬT (PLANTS):", 50, y);
        g.setColor(Color.WHITE);
        y += 30; g.drawString("- Hoa Hướng Dương (Vàng - 50 Sun): Tạo 25 Sun mỗi 5 giây.", 70, y);
        y += 30; g.drawString("- Peashooter (Xanh lá - 50 Sun): Bắn đạn đơn, tầm gần, sát thương 1.", 70, y);
        y += 30; g.drawString("- Sniper (Tím - 100 Sun): Bắn đạn xa, sát thương 2.", 70, y);
        y += 30; g.drawString("- WallNut (Nâu - 50 Sun): Máu 30. CHỈ ĐƯỢC TRỒNG TRÊN ĐƯỜNG ĐI ĐỂ CHẶN.", 70, y);
        y += 30; g.drawString("- SnowPea (Xanh lơ - 150 Sun): Bắn đạn làm chậm quái trong 3 giây.", 70, y);

        y += 50; g.setColor(Color.RED); g.drawString("THÂY MA (ZOMBIES):", 50, y);
        g.setColor(Color.WHITE);
        y += 30; g.drawString("- Red Zombie (Đỏ - 5 HP): Đi bộ theo đường, cắn nát thực vật cản đường.", 70, y);
        y += 30; g.drawString("- Shooter Zombie (Cam - 3 HP): Dừng lại bắn xương từ xa phá cây.", 70, y);

        drawButton(g, btnBack, "QUAY LẠI", mouseX, mouseY);
    }

    private void drawGameplay(Graphics2D g) {
        grid.draw(g);
        if (selectedPlant > 0 && hoverCol >= 0 && hoverCol < Constants.COLS && hoverRow >= 0 && hoverRow < Constants.ROWS) {
            int hX = hoverCol * Constants.TILE_SIZE; int hY = hoverRow * Constants.TILE_SIZE;
            boolean canPlant = false;
            if (selectedPlant == 4 && grid.map[hoverRow][hoverCol] == 1) canPlant = true;
            else if (selectedPlant != 4 && grid.map[hoverRow][hoverCol] == 2) canPlant = true;
            if (isPlanted(hoverCol, hoverRow)) canPlant = false;

            if (!canPlant) g.setColor(new Color(255, 0, 0, 100));
            else g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(hX, hY, Constants.TILE_SIZE, Constants.TILE_SIZE);
        }

        for(Plant p : plants) {
            p.draw(g);
            if (p.health < p.maxHealth) {
                g.setColor(Color.GREEN);
                g.fillRect(p.x + 10, p.y - 5, (Constants.TILE_SIZE - 20) * p.health / p.maxHealth, 4);
            }
        }
        for(Monster z : zombies) z.draw(g);
        for(Projectile p : projectiles) p.draw(g);

        g.setColor(new Color(0, 0, 0, 150)); g.fillRect(0, 0, Constants.SCREEN_WIDTH, 80);
        g.setColor(new Color(255, 215, 0)); g.fillOval(10, 20, 40, 40);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 22)); g.drawString(String.valueOf(sun), 60, 48);

        drawCard(g, card1, selectedPlant == 1, Color.YELLOW, "50"); 
        drawCard(g, card2, selectedPlant == 2, new Color(34, 139, 34), "50"); 
        drawCard(g, card3, selectedPlant == 3, new Color(138, 43, 226), "100"); 
        drawCard(g, card4, selectedPlant == 4, new Color(139, 69, 19), "50"); 
        drawCard(g, card5, selectedPlant == 5, new Color(135, 206, 250), "150"); 
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 80));
        g.drawString("GAME OVER", Constants.SCREEN_WIDTH / 2 - 240, Constants.SCREEN_HEIGHT / 2 - 50);
        
        drawButton(g, btnRestart, "CHƠI LẠI", mouseX, mouseY);
        drawButton(g, btnExitGO, "THOÁT", mouseX, mouseY);
    }

    private void drawButton(Graphics2D g, Rectangle r, String text, int mX, int mY) {
        if (r.contains(mX, mY)) g.setColor(new Color(200, 200, 200)); // Hover xám nhạt
        else g.setColor(Color.WHITE);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.BLACK); g.drawRect(r.x, r.y, r.width, r.height);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        int strW = g.getFontMetrics().stringWidth(text);
        g.drawString(text, r.x + (r.width - strW)/2, r.y + 35); // Căn giữa Text
    }

    private void drawCard(Graphics2D g, Rectangle r, boolean selected, Color c, String price) {
        g.setColor(selected ? Color.YELLOW : new Color(80, 80, 80)); g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(c); g.fillOval(r.x + 15, r.y + 5, 40, 40);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 14)); g.drawString(price, r.x + 20, r.y + 55);
    }
    
    private boolean isPlanted(int col, int row) {
        for(Plant p : plants) if (p.x / Constants.TILE_SIZE == col && p.y / Constants.TILE_SIZE == row) return true;
        return false;
    }

    // --- XỬ LÝ CLICK CHUỘT DỰA VÀO STATE ---
    public void handleClick(int mX, int mY) {
        if (state == GameState.MENU) {
            if (btnPlay.contains(mX, mY)) resetGame();
            else if (btnInstr.contains(mX, mY)) state = GameState.INSTRUCTIONS;
            else if (btnExit.contains(mX, mY)) System.exit(0);
            return;
        }
        else if (state == GameState.INSTRUCTIONS) {
            if (btnBack.contains(mX, mY)) state = GameState.MENU;
            return;
        }
        else if (state == GameState.GAMEOVER) {
            if (btnRestart.contains(mX, mY)) resetGame();
            else if (btnExitGO.contains(mX, mY)) System.exit(0);
            return;
        }

        // Đang chơi (PLAYING)
        if (card1.contains(mX, mY)) { selectedPlant = 1; return; }
        if (card2.contains(mX, mY)) { selectedPlant = 2; return; }
        if (card3.contains(mX, mY)) { selectedPlant = 3; return; }
        if (card4.contains(mX, mY)) { selectedPlant = 4; return; }
        if (card5.contains(mX, mY)) { selectedPlant = 5; return; }

        if (selectedPlant > 0) {
            int col = mX / Constants.TILE_SIZE; int row = mY / Constants.TILE_SIZE;
            if (col < 0 || col >= Constants.COLS || row < 0 || row >= Constants.ROWS) return;
            
            boolean canPlant = false;
            if (selectedPlant == 4 && grid.map[row][col] == 1) canPlant = true;
            else if (selectedPlant != 4 && grid.map[row][col] == 2) canPlant = true;
            
            if (!canPlant || isPlanted(col, row)) { selectedPlant = 0; return; }

            int cost = 0;
            if (selectedPlant == 1) cost = 50; else if (selectedPlant == 2) cost = 50;
            else if (selectedPlant == 3) cost = 100; else if (selectedPlant == 4) cost = 50;
            else if (selectedPlant == 5) cost = 150;

            if (sun >= cost) {
                if (selectedPlant == 1) plants.add(new Sunflower(col, row));
                else if (selectedPlant == 2) plants.add(new Peashooter(col, row));
                else if (selectedPlant == 3) plants.add(new SniperPlant(col, row));
                else if (selectedPlant == 4) plants.add(new WallNut(col, row));
                else if (selectedPlant == 5) plants.add(new SnowPea(col, row));
                sun -= cost; selectedPlant = 0; 
            } else { selectedPlant = 0; }
        }
    }
}
