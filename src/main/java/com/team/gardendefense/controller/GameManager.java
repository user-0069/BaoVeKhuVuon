package com.team.gardendefense.controller;
import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Iterator;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.google.gson.Gson;
import com.team.gardendefense.model.entity.plant.*;
import com.team.gardendefense.model.entity.monster.*;
import com.team.gardendefense.model.entity.projectile.*;
import com.team.gardendefense.model.map.Grid;
import com.team.gardendefense.model.level.LevelData;
import com.team.gardendefense.utils.Constants;

public class GameManager {
    public enum GameState { MENU, INSTRUCTIONS, PLAYING, GAMEOVER, GAMEWON }
    public GameState state = GameState.MENU;

    public Grid grid;
    public List<Plant> plants = new ArrayList<>();
    public List<Monster> zombies = new ArrayList<>();
    public List<Projectile> projectiles = new ArrayList<>();
    
    public int sun = 50; 
    private int passiveSunTimer = 0;
    
    public int mouseX = 0, mouseY = 0;
    public int hoverCol = -1, hoverRow = -1;

    // Dữ liệu Level & Wave
    private LevelData currentLevel;
    private WaveManager waveManager;

    // Menu động
    class PlantCard {
        int id; Rectangle rect; Color color; String price;
        PlantCard(int id, int x) {
            this.id = id; this.rect = new Rectangle(x, 10, 70, 60);
            if(id==1) { color = Color.YELLOW; price = String.valueOf(Constants.COST_SUNFLOWER); }
            else if (id==2) { color = new Color(34, 139, 34); price = String.valueOf(Constants.COST_PEASHOOTER); }
            else if (id==3) { color = new Color(138, 43, 226); price = String.valueOf(Constants.COST_SNIPER); }
            else if (id==4) { color = new Color(139, 69, 19); price = String.valueOf(Constants.COST_WALLNUT); }
            else if (id==5) { color = new Color(135, 206, 250); price = String.valueOf(Constants.COST_SNOWPEA); }
        }
    }
    public List<PlantCard> uiCards = new ArrayList<>();
    public int selectedPlant = 0; 

    // Các Nút bấm chung
    public Rectangle btnPlay = new Rectangle(412, 300, 200, 60);
    public Rectangle btnInstr = new Rectangle(412, 400, 200, 60);
    public Rectangle btnExit = new Rectangle(412, 500, 200, 60);
    public Rectangle btnBack = new Rectangle(412, 650, 200, 60);
    public Rectangle btnRestart = new Rectangle(412, 530, 200, 60);
    public Rectangle btnExitGO = new Rectangle(412, 610, 200, 60);

    public GameManager() {
        loadLevel("/data/level_1.json"); // Mặc định load level 1 khi mở game
    }

    public void loadLevel(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                currentLevel = new Gson().fromJson(new InputStreamReader(is), LevelData.class);
                grid = new Grid(currentLevel.map);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void resetGame() {
        plants.clear(); zombies.clear(); projectiles.clear();
        selectedPlant = 0; passiveSunTimer = 0;
        
        if (currentLevel != null) {
            sun = currentLevel.initialSun;
            waveManager = new WaveManager(this, currentLevel.waves);
            uiCards.clear();
            int startX = 120;
            for (int pId : currentLevel.allowedPlants) {
                uiCards.add(new PlantCard(pId, startX));
                startX += 80;
            }
        }
        state = GameState.PLAYING;
    }

    public void update() {
        if (state != GameState.PLAYING) return;

        passiveSunTimer++;
        if (passiveSunTimer >= Constants.PASSIVE_SUN_RATE) { sun += Constants.PASSIVE_SUN_AMOUNT; passiveSunTimer = 0; }

        if (waveManager != null) {
            waveManager.update();
            if (waveManager.isAllSpawned() && zombies.isEmpty()) {
                state = GameState.GAMEWON; // Thắng!
            }
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
                sun -= Constants.ZOMBIE_ESCAPE_PENALTY; zIt.remove();
                if (sun < 0) { state = GameState.GAMEOVER; } 
                continue;
            }
            if (z.attackTarget == null && !(z instanceof ShooterZombie)) {
                for (Plant p : plants) {
                    if (z.getBounds().intersects(p.getBounds())) { z.attackTarget = p; break; }
                }
            }
            z.update(this);
            if (z.isDead()) { sun += Constants.ZOMBIE_KILL_REWARD; zIt.remove(); }
        }
    }

    public void draw(Graphics2D g) {
        if (state == GameState.MENU) drawMainMenu(g);
        else if (state == GameState.INSTRUCTIONS) drawInstructions(g);
        else {
            drawGameplay(g);
            if (state == GameState.GAMEOVER) drawGameOver(g);
            else if (state == GameState.GAMEWON) drawGameWon(g);
        }
    }

    private void drawMainMenu(Graphics2D g) {
        g.setColor(new Color(34, 139, 34)); g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 70));
        g.drawString("BẢO VỆ KHU VƯỜN", 160, 180);
        drawButton(g, btnPlay, "CHƠI NGAY", mouseX, mouseY);
        drawButton(g, btnInstr, "HƯỚNG DẪN", mouseX, mouseY);
        drawButton(g, btnExit, "THOÁT", mouseX, mouseY);
    }

    private void drawInstructions(Graphics2D g) {
        g.setColor(new Color(30, 30, 40)); g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40)); g.drawString("HƯỚNG DẪN CHƠI", 330, 80);
        g.setFont(new Font("Arial", Font.PLAIN, 20)); int y = 140;
        g.drawString("Quy tắc: Quái đi lọt trừ 50 Sun. Dưới 0 Sun -> Thua. Tiêu diệt hết Quái -> Thắng.", 50, y);
        y += 50; g.setColor(Color.YELLOW); g.drawString("THỰC VẬT (PLANTS):", 50, y);
        g.setColor(Color.WHITE);
        y += 30; g.drawString("- 1. Hướng Dương (Vàng - 50 Sun): Tạo 25 Sun mỗi 5 giây.", 70, y);
        y += 30; g.drawString("- 2. Peashooter (Xanh lá - 50 Sun): Sát thương 1.", 70, y);
        y += 30; g.drawString("- 3. Sniper (Tím - 100 Sun): Bắn xa, sát thương 2.", 70, y);
        y += 30; g.drawString("- 4. WallNut (Nâu - 50 Sun): Máu 30. (Trồng trực tiếp trên đường quái đi).", 70, y);
        y += 30; g.drawString("- 5. SnowPea (Xanh lơ - 150 Sun): Bắn đạn đóng băng.", 70, y);
        y += 50; g.setColor(Color.RED); g.drawString("THÂY MA (ZOMBIES):", 50, y);
        g.setColor(Color.WHITE);
        y += 30; g.drawString("- 1. Red Zombie (Đỏ - 5 HP): Đi cắn cây ngáng đường.", 70, y);
        y += 30; g.drawString("- 2. Shooter Zombie (Cam - 3 HP): Dừng lại bắn xương từ xa phá cây.", 70, y);
        drawButton(g, btnBack, "QUAY LẠI", mouseX, mouseY);
    }

    private void drawGameplay(Graphics2D g) {
        if(grid != null) grid.draw(g);
        
        if (selectedPlant > 0 && hoverCol >= 0 && hoverCol < Constants.COLS && hoverRow >= 0 && hoverRow < Constants.ROWS) {
            int hX = hoverCol * Constants.TILE_SIZE; int hY = hoverRow * Constants.TILE_SIZE;
            boolean canPlant = false;
            if (grid != null) {
                if (selectedPlant == 4 && grid.map[hoverRow][hoverCol] == 1) canPlant = true;
                else if (selectedPlant != 4 && grid.map[hoverRow][hoverCol] == 2) canPlant = true;
            }
            if (isPlanted(hoverCol, hoverRow)) canPlant = false;

            if (!canPlant) g.setColor(new Color(255, 0, 0, 100));
            else g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(hX, hY, Constants.TILE_SIZE, Constants.TILE_SIZE);
        }

        for(Plant p : plants) {
            p.draw(g);
            if (p.health < p.maxHealth) {
                g.setColor(Color.GREEN); g.fillRect(p.x + 10, p.y - 5, (Constants.TILE_SIZE - 20) * p.health / p.maxHealth, 4);
            }
        }
        for(Monster z : zombies) z.draw(g);
        for(Projectile p : projectiles) p.draw(g);

        // UI Header
        g.setColor(new Color(0, 0, 0, 150)); g.fillRect(0, 0, Constants.SCREEN_WIDTH, 80);
        g.setColor(new Color(255, 215, 0)); g.fillOval(10, 20, 40, 40);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 22)); g.drawString(String.valueOf(sun), 60, 48);

        if (currentLevel != null) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(currentLevel.name, Constants.SCREEN_WIDTH - 250, 45);
        }

        // Draw Dynamic Cards
        for(PlantCard c : uiCards) {
            g.setColor((selectedPlant == c.id) ? Color.YELLOW : new Color(80, 80, 80));
            g.fillRect(c.rect.x, c.rect.y, c.rect.width, c.rect.height);
            g.setColor(c.color); g.fillOval(c.rect.x + 15, c.rect.y + 5, 40, 40);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 14)); g.drawString(c.price, c.rect.x + 20, c.rect.y + 55);
        }
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180)); g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 80));
        g.drawString("GAME OVER", Constants.SCREEN_WIDTH / 2 - 240, Constants.SCREEN_HEIGHT / 2 - 50);
        drawButton(g, btnRestart, "CHƠI LẠI", mouseX, mouseY);
        drawButton(g, btnExitGO, "THOÁT", mouseX, mouseY);
    }

    private void drawGameWon(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180)); g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        g.setColor(Color.GREEN); g.setFont(new Font("Arial", Font.BOLD, 80));
        g.drawString("VICTORY!", Constants.SCREEN_WIDTH / 2 - 200, Constants.SCREEN_HEIGHT / 2 - 50);
        drawButton(g, btnRestart, "CHƠI LẠI", mouseX, mouseY);
        drawButton(g, btnExitGO, "THOÁT", mouseX, mouseY);
    }

    private void drawButton(Graphics2D g, Rectangle r, String text, int mX, int mY) {
        g.setColor(r.contains(mX, mY) ? new Color(200, 200, 200) : Color.WHITE);
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.BLACK); g.drawRect(r.x, r.y, r.width, r.height);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        int strW = g.getFontMetrics().stringWidth(text);
        g.drawString(text, r.x + (r.width - strW)/2, r.y + 35); 
    }
    
    private boolean isPlanted(int col, int row) {
        for(Plant p : plants) if (p.x / Constants.TILE_SIZE == col && p.y / Constants.TILE_SIZE == row) return true;
        return false;
    }

    public void handleClick(int mX, int mY) {
        if (state == GameState.MENU) {
            if (btnPlay.contains(mX, mY)) resetGame();
            else if (btnInstr.contains(mX, mY)) state = GameState.INSTRUCTIONS;
            else if (btnExit.contains(mX, mY)) System.exit(0);
            return;
        } else if (state == GameState.INSTRUCTIONS) {
            if (btnBack.contains(mX, mY)) state = GameState.MENU;
            return;
        } else if (state == GameState.GAMEOVER || state == GameState.GAMEWON) {
            if (btnRestart.contains(mX, mY)) resetGame();
            else if (btnExitGO.contains(mX, mY)) System.exit(0);
            return;
        }

        for(PlantCard c : uiCards) {
            if(c.rect.contains(mX, mY)) { selectedPlant = c.id; return; }
        }

        if (selectedPlant > 0) {
            int col = mX / Constants.TILE_SIZE; int row = mY / Constants.TILE_SIZE;
            if (col < 0 || col >= Constants.COLS || row < 0 || row >= Constants.ROWS) return;
            if (grid == null) return;
            
            boolean canPlant = false;
            if (selectedPlant == 4 && grid.map[row][col] == 1) canPlant = true;
            else if (selectedPlant != 4 && grid.map[row][col] == 2) canPlant = true;
            if (!canPlant || isPlanted(col, row)) { selectedPlant = 0; return; }

            int cost = 0;
            if (selectedPlant == 1) cost = Constants.COST_SUNFLOWER; else if (selectedPlant == 2) cost = Constants.COST_SUNFLOWER;
            else if (selectedPlant == 3) cost = 100; else if (selectedPlant == 4) cost = Constants.COST_SUNFLOWER;
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
