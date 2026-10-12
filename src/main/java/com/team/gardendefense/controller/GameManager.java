package com.team.gardendefense.controller;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
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
    public List<Plant> plants = new CopyOnWriteArrayList<>();
    public List<Monster> zombies = new CopyOnWriteArrayList<>();
    public List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    
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
        java.awt.Image image;
        PlantCard(int id, int x) {
            this.id = id; this.rect = new Rectangle(x, 10, 60, 75); // Taller for cards
            if(id==1) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_sunflower.png"); price = String.valueOf(Constants.COST_SUNFLOWER); }
            else if (id==2) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_peashooter.png"); price = String.valueOf(Constants.COST_PEASHOOTER); }
            else if (id==3) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_sniperplant.png"); price = String.valueOf(Constants.COST_SNIPER); }
            else if (id==4) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_wallnut.png"); price = String.valueOf(Constants.COST_WALLNUT); }
            else if (id==5) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_snowpea.png"); price = String.valueOf(Constants.COST_SNOWPEA); }
                        else if (id==6) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_cherrybomb.png"); price = "150"; }
            else if (id==7) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_chomper.png"); price = "150"; }
            else if (id==8) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_peashooter.png"); price = String.valueOf(Constants.COST_PEASHOOTER); }
            else if (id==9) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_sniperplant.png"); price = String.valueOf(Constants.COST_SNIPER); }
            else if (id==10) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_snowpea.png"); price = String.valueOf(Constants.COST_SNOWPEA); }
        }
    }
    public List<PlantCard> uiCards = new ArrayList<>();
    public int selectedPlant = 0; 

    public void handleKeyPress(int keyCode) {
        // Implement keyboard panning or card selection if needed
    }

    // Các Nút bấm chung
    public Rectangle btnPlay = new Rectangle(412, 280, 200, 55);
    public Rectangle btnInstr = new Rectangle(412, 360, 200, 55);
    public Rectangle btnEditor = new Rectangle(412, 440, 200, 55);
    public Rectangle btnExit = new Rectangle(412, 520, 200, 55);
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

    // ... (skipping some logic to get to handleClick) ...
    // Using a more targeted replace block for handleclick


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

        for (Plant p : plants) {
            if (p.isDead()) plants.remove(p);
            else p.update(this);
        }

        for (Projectile p : projectiles) {
        
             p.move();
            if (p.x < 0 || p.x > Constants.SCREEN_WIDTH || p.y < 0 || p.y > Constants.SCREEN_HEIGHT) { projectiles.remove(p); continue; }
            
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
            if (!p.active) projectiles.remove(p);
        }

        for (Monster z : zombies) {
        
            
            if (z.x > Constants.SCREEN_WIDTH) {
                state = GameState.GAMEOVER;
                continue;
            }
            if (z.attackTarget == null ) {
                for (Plant p : plants) {
                    if (z.getBounds().intersects(p.getBounds())) { z.attackTarget = p; break; }
                }
            }
            z.update(this);
            if (z.isDead()) { sun += Constants.ZOMBIE_KILL_REWARD; zombies.remove(z); }
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
        drawButton(g, btnEditor, "LEVEL EDITOR", mouseX, mouseY);
        drawButton(g, btnExit, "THOÁT", mouseX, mouseY);
    }

    private void drawInstructions(Graphics2D g) {
        g.setColor(new Color(30, 30, 40)); g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40)); g.drawString("HƯỚNG DẪN CHƠI", 330, 60);
        g.setFont(new Font("Arial", Font.PLAIN, 18)); int y = 100;
        
        g.drawString("Quy tắc: Tiêu diệt hết Quái -> Thắng. Để 1 con Zombie lọt qua cửa -> THUA NGAY LẬP TỨC!", 50, y);
        
        y += 40; g.setColor(Color.YELLOW); g.drawString("CÁCH TRỒNG & THỰC VẬT:", 50, y);
        g.setColor(Color.WHITE);
        y += 25; g.drawString("- Bạn chỉ có thể trồng cây trên Chậu (Pot) hoặc trực tiếp trên Đường (Path).", 70, y);
        y += 25; g.drawString("- 1. Hướng Dương (50 Sun): Tạo 25 Sun mỗi 5 giây.", 70, y);
        y += 25; g.drawString("- 2/3/5. Peashooter, Sniper, SnowPea: Có 2 thẻ bài riêng biệt cho hướng bắn Trái (L) và Phải (R).", 70, y);
        y += 25; g.drawString("- 4. WallNut (50 Sun): Siêu trâu bò (30 HP), dùng để chặn đường quái.", 70, y);
        y += 25; g.drawString("- 6. CherryBomb (150 Sun): Nổ banh xác toàn bộ Zombie trong ô 3x3 rồi biến mất.", 70, y);
        y += 25; g.drawString("- 7. Chomper (150 Sun): Nuốt 1 Zombie trong ô 3x3, mất 10s để nhai (không thể đánh lúc nhai).", 70, y);
        
        y += 40; g.setColor(Color.RED); g.drawString("THÂY MA (ZOMBIES):", 50, y);
        g.setColor(Color.WHITE);
        y += 25; g.drawString("- Red Zombie (5 HP): Đi cắn mọi cây ngáng đường.", 70, y);
        y += 25; g.drawString("- Shooter Zombie (3 HP): Vừa đi vừa ném xương phá cây từ xa. Nếu đụng cây sẽ đứng cắn.", 70, y);
        
        drawButton(g, btnBack, "QUAY LẠI", mouseX, mouseY);
    }

    private void drawGameplay(Graphics2D g) {
        if(grid != null) grid.draw(g);
        
        if (selectedPlant > 0 && hoverCol >= 0 && hoverCol < Constants.COLS && hoverRow >= 0 && hoverRow < Constants.ROWS) {
            int hX = hoverCol * Constants.TILE_SIZE; int hY = hoverRow * Constants.TILE_SIZE;
            boolean canPlant = false;
            if (grid != null) {
                if (grid.map[hoverRow][hoverCol] == 1 || grid.map[hoverRow][hoverCol] == 2) canPlant = true;
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

        

        // Draw Dynamic Cards
        for(PlantCard c : uiCards) {
            if (c.image != null) {
                if (c.id == 8 || c.id == 9 || c.id == 10) {
                    g.drawImage(c.image, c.rect.x + c.rect.width, c.rect.y, -c.rect.width, c.rect.height, null);
                } else {
                    g.drawImage(c.image, c.rect.x, c.rect.y, c.rect.width, c.rect.height, null);
                }
                if (selectedPlant == c.id) {
                    g.setColor(Color.YELLOW);
                    g.drawRect(c.rect.x - 1, c.rect.y - 1, c.rect.width + 1, c.rect.height + 1);
                    g.drawRect(c.rect.x - 2, c.rect.y - 2, c.rect.width + 3, c.rect.height + 3);
                }
                
                // Che đi phần giá gốc trên ảnh bằng một hình chữ nhật trắng
                g.setColor(new Color(240, 240, 240)); 
                g.fillRect(c.rect.x + 2, c.rect.y + 58, c.rect.width - 4, 15);
                
                // Vẽ mặt trời nhỏ
                g.setColor(new Color(255, 215, 0));
                g.fillOval(c.rect.x + 40, c.rect.y + 60, 11, 11);
                g.setColor(Color.BLACK);
                g.drawOval(c.rect.x + 40, c.rect.y + 60, 11, 11);
                
                // Vẽ giá tiền
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString(c.price, c.rect.x + 12, c.rect.y + 70);
            } else {
                g.setColor((selectedPlant == c.id) ? Color.YELLOW : new Color(80, 80, 80));
                g.fillRect(c.rect.x, c.rect.y, c.rect.width, c.rect.height);
                g.setColor(c.color); g.fillOval(c.rect.x + 15, c.rect.y + 5, 40, 40);
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 14)); g.drawString(c.price, c.rect.x + 20, c.rect.y + 55);
            }
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
            else if (btnEditor.contains(mX, mY)) {
                new com.team.gardendefense.editor.LevelEditor().setVisible(true);
            }
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
            if (grid.map[row][col] == 1 || grid.map[row][col] == 2) canPlant = true;
            
            if (isPlanted(col, row)) { canPlant = false; }

            if (!canPlant) { selectedPlant = 0; return; }

            int cost = 0;
            if (selectedPlant == 1) cost = Constants.COST_SUNFLOWER; 
            else if (selectedPlant == 2 || selectedPlant == 8) cost = Constants.COST_PEASHOOTER;
            else if (selectedPlant == 3 || selectedPlant == 9) cost = Constants.COST_SNIPER; 
            else if (selectedPlant == 4) cost = Constants.COST_WALLNUT;
            else if (selectedPlant == 5 || selectedPlant == 10) cost = Constants.COST_SNOWPEA;
            else if (selectedPlant == 6) cost = 150;
            else if (selectedPlant == 7) cost = 150;

            if (sun >= cost) {
                Plant p = null;
                if (selectedPlant == 1) p = new Sunflower(col, row);
                else if (selectedPlant == 2) { p = new Peashooter(col, row); p.direction = 1; }
                else if (selectedPlant == 8) { p = new Peashooter(col, row); p.direction = 3; }
                else if (selectedPlant == 3) { p = new SniperPlant(col, row); p.direction = 1; }
                else if (selectedPlant == 9) { p = new SniperPlant(col, row); p.direction = 3; }
                else if (selectedPlant == 4) p = new WallNut(col, row);
                else if (selectedPlant == 5) { p = new SnowPea(col, row); p.direction = 1; }
                else if (selectedPlant == 10) { p = new SnowPea(col, row); p.direction = 3; }
                else if (selectedPlant == 6) p = new com.team.gardendefense.model.entity.plant.CherryBomb(col, row, this);
                else if (selectedPlant == 7) p = new com.team.gardendefense.model.entity.plant.Chomper(col, row, this);
                
                if (p != null) {
                    
                    plants.add(p);
                    sun -= cost; 
                }
                selectedPlant = 0; 
            } else { selectedPlant = 0; }
        }
    }
}
