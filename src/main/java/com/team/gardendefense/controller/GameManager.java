package com.team.gardendefense.controller;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.Image;
import com.google.gson.Gson;
import com.team.gardendefense.model.entity.plant.*;
import com.team.gardendefense.model.entity.monster.*;
import com.team.gardendefense.model.entity.projectile.*;
import com.team.gardendefense.model.entity.coin.Coin;
import com.team.gardendefense.model.entity.coin.CoinType;
import com.team.gardendefense.model.entity.pet.PetSnail;
import com.team.gardendefense.system.AudioManager;
import com.team.gardendefense.system.LootTable;
import com.team.gardendefense.system.WalletManager;
import com.team.gardendefense.view.ui.PauseDialog;
import com.team.gardendefense.view.ui.ShopScreen;
import com.team.gardendefense.model.map.Grid;
import com.team.gardendefense.model.level.LevelData;
import com.team.gardendefense.model.level.Waypoint;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.view.renderer.GamePanel;
import java.awt.RenderingHints;
import java.awt.GradientPaint;

public class GameManager {
    public enum GameState { MENU, LEVEL_SELECT, INSTRUCTIONS, PLAYING, PAUSED, GAMEOVER, GAMEWON, SHOP }
    public GameState state = GameState.MENU;

    public Grid grid;
    public List<Plant> plants = new CopyOnWriteArrayList<>();
    public List<Monster> zombies = new CopyOnWriteArrayList<>();
    public List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    public List<Coin> coins = new CopyOnWriteArrayList<>();
    public PetSnail petSnail = null;
    public ShopScreen shopScreen;
    public PauseDialog pauseDialog;
    public Image coinBankImg;
    
    public int sun = 50; 
    private int passiveSunTimer = 0;
    
    public int mouseX = 0, mouseY = 0;
    public int hoverCol = -1, hoverRow = -1;

    // Dữ liệu Level & Wave
    public int currentLevelId = 1;
    private LevelData currentLevel;
    private WaveManager waveManager;
    private GamePanel panel;

    public void setGamePanel(GamePanel panel) {
        this.panel = panel;
    }

    public GamePanel getPanel() {
        return panel;
    }

    // Thông tin xem trước Level để hiển thị danh sách
    public static class LevelPreview {
        public int id;
        public String name;
        public int initialSun;
        public int waveCount;
        public int cols;
        public int rows;
        public Rectangle cardRect;
        public Rectangle btnPlayRect;
    }
    public List<LevelPreview> availableLevels = new ArrayList<>();

    // Menu động chứa thẻ bài thực vật
    class PlantCard {
        int id; Rectangle rect; Color color; String price;
        java.awt.Image image;
        PlantCard(int id, int x) {
            this.id = id; this.rect = new Rectangle(x, 10, 60, 75);
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
            else if (id==11) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/reanim/Marigold_petals.png"); price = String.valueOf(Constants.COST_MARIGOLD); }
        }
    }
    public List<PlantCard> uiCards = new ArrayList<>();
    public int selectedPlant = 0; 

    public void handleKeyPress(int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE || keyCode == java.awt.event.KeyEvent.VK_P) {
            if (state == GameState.PLAYING) {
                AudioManager.getInstance().playBuySound();
                state = GameState.PAUSED;
            } else if (state == GameState.PAUSED) {
                AudioManager.getInstance().playBuySound();
                state = GameState.PLAYING;
            } else if (state == GameState.SHOP) {
                state = GameState.PLAYING;
            }
        }
    }

    // Các Nút bấm chung
    public Rectangle btnPlay = new Rectangle(412, 190, 200, 48);
    public Rectangle btnLevelSelect = new Rectangle(412, 250, 200, 48);
    public Rectangle btnShop = new Rectangle(412, 310, 200, 48);
    public Rectangle btnInstr = new Rectangle(412, 370, 200, 48);
    public Rectangle btnEditor = new Rectangle(412, 430, 200, 48);
    public Rectangle btnExit = new Rectangle(412, 490, 200, 48);

    public Rectangle btnBack = new Rectangle(412, 660, 200, 50);

    public Rectangle btnNextLevel = new Rectangle(412, 440, 220, 50);
    public Rectangle btnRestart = new Rectangle(412, 505, 220, 50);
    public Rectangle btnLevelSelectGW = new Rectangle(412, 570, 220, 50);
    public Rectangle btnExitGO = new Rectangle(412, 635, 220, 50);

    public Rectangle btnInGameMenu = new Rectangle(0, 0, 90, 30);
    public Rectangle btnInGameShop = new Rectangle(0, 0, 90, 30);

    public GameManager() {
        shopScreen = new ShopScreen(this);
        pauseDialog = new PauseDialog(this);
        coinBankImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/coinbank.png");
        scanAvailableLevels();
        loadLevel("/data/level_1.json");
    }

    public void spawnCoin(float x, float y, CoinType type) {
        if (type != null) {
            coins.add(new Coin(x, y, type));
        }
    }

    // Quét toàn bộ file level_*.json để nạp danh sách chọn màn
    public void scanAvailableLevels() {
        availableLevels.clear();
        Set<Integer> foundIds = new TreeSet<>();

        File dir = new File("src/main/resources/data");
        if (!dir.exists()) {
            dir = new File("target/classes/data");
        }
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.matches("level_\\d+\\.json"));
            if (files != null) {
                for (File f : files) {
                    try {
                        String num = f.getName().replaceAll("[^0-9]", "");
                        foundIds.add(Integer.parseInt(num));
                    } catch (Exception ignored) {}
                }
            }
        }

        // Đảm bảo tối thiểu kiểm tra level 1 và 2
        foundIds.add(1);
        foundIds.add(2);

        for (int id : foundIds) {
            LevelData data = loadLevelDataOnly(id);
            if (data != null) {
                LevelPreview lp = new LevelPreview();
                lp.id = id;
                lp.name = (data.name != null && !data.name.trim().isEmpty()) ? data.name : ("Màn " + id);
                lp.initialSun = data.initialSun;
                lp.waveCount = (data.waves != null) ? data.waves.length : 0;
                lp.rows = (data.map != null) ? data.map.length : Constants.ROWS;
                lp.cols = (data.map != null && data.map.length > 0) ? data.map[0].length : Constants.COLS;
                availableLevels.add(lp);
            }
        }
    }

    private LevelData loadLevelDataOnly(int id) {
        String path = "/data/level_" + id + ".json";
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                File f = new File("src/main/resources" + path);
                if (f.exists()) is = new FileInputStream(f);
            }
            if (is != null) {
                return new Gson().fromJson(new InputStreamReader(is), LevelData.class);
            }
        } catch (Exception ignored) {}
        return null;
    }

    public boolean hasLevel(int id) {
        for (LevelPreview lp : availableLevels) {
            if (lp.id == id) return true;
        }
        return getClass().getResource("/data/level_" + id + ".json") != null 
            || new File("src/main/resources/data/level_" + id + ".json").exists();
    }

    public void startLevel(int id) {
        currentLevelId = id;
        loadLevel("/data/level_" + id + ".json");
        resetGame();
    }

    public boolean loadLevel(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                File f = new File("src/main/resources" + path);
                if (f.exists()) is = new FileInputStream(f);
            }
            if (is != null) {
                currentLevel = new Gson().fromJson(new InputStreamReader(is), LevelData.class);
                if (currentLevel != null) {
                    currentLevelId = currentLevel.id;
                    grid = new Grid(currentLevel.map);
                    return true;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Các hàm tính kích thước linh hoạt theo màn chơi
    public int getCols() {
        return (grid != null && grid.map != null && grid.map.length > 0) ? grid.map[0].length : Constants.COLS;
    }

    public int getRows() {
        return (grid != null && grid.map != null) ? grid.map.length : Constants.ROWS;
    }

    public int getMapWidth() {
        return getCols() * Constants.TILE_SIZE;
    }

    public int getMapHeight() {
        return getRows() * Constants.TILE_SIZE;
    }

    public int getScreenWidth() {
        if (state == GameState.PLAYING || state == GameState.GAMEOVER || state == GameState.GAMEWON) {
            return Math.max(Constants.SCREEN_WIDTH, getMapWidth());
        }
        return Constants.SCREEN_WIDTH;
    }

    public int getScreenHeight() {
        if (state == GameState.PLAYING || state == GameState.GAMEOVER || state == GameState.GAMEWON) {
            return Math.max(Constants.SCREEN_HEIGHT, getMapHeight());
        }
        return Constants.SCREEN_HEIGHT;
    }

    public void resetGame() {
        plants.clear(); zombies.clear(); projectiles.clear(); coins.clear();
        selectedPlant = 0; passiveSunTimer = 0;
        
        if (WalletManager.getInstance().getData().hasPetSnail) {
            petSnail = new PetSnail(50, getMapHeight() - 60);
        } else {
            petSnail = null;
        }
        
        if (currentLevel != null) {
            currentLevelId = currentLevel.id;
            sun = currentLevel.initialSun;
            waveManager = new WaveManager(this, currentLevel.waves);
            uiCards.clear();
            int startX = 120;
            for (int pId : currentLevel.allowedPlants) {
                uiCards.add(new PlantCard(pId, startX));
                startX += 80;
            }
            // Luôn thêm thẻ bài Marigold (Hoa Cúc - ID 11) để nhả xu!
            uiCards.add(new PlantCard(11, startX));
        }
        state = GameState.PLAYING;
        if (panel != null) {
            panel.resizeForLevel(getCols(), getRows());
        }
    }

    public void update() {
        if (state == GameState.SHOP) {
            shopScreen.update();
            return;
        }
        if (state == GameState.PAUSED) {
            pauseDialog.update();
            return;
        }
        if (state != GameState.PLAYING) return;

        passiveSunTimer++;
        if (passiveSunTimer >= Constants.PASSIVE_SUN_RATE) { sun += Constants.PASSIVE_SUN_AMOUNT; passiveSunTimer = 0; }

        if (waveManager != null) {
            waveManager.update();
            if (waveManager.isAllSpawned() && zombies.isEmpty()) {
                if (state != GameState.GAMEWON) {
                    // Thưởng Kim Cương khi thắng trọn vẹn màn chơi!
                    spawnCoin(getScreenWidth() / 2.0f, getScreenHeight() / 2.0f, CoinType.DIAMOND);
                }
                state = GameState.GAMEWON; // Thắng!
            }
        }

        // Cập nhật Coin và Pet Snail
        for (Coin c : coins) {
            c.update(this);
            if (c.isDead()) coins.remove(c);
        }
        if (petSnail != null && WalletManager.getInstance().getData().hasPetSnail) {
            petSnail.update(this);
        }
        WalletManager.getInstance().updateFloatingTexts();

        for (Plant p : plants) {
            if (p.isDead()) plants.remove(p);
            else p.update(this);
        }

        int maxW = getMapWidth();
        int maxH = getMapHeight();

        for (Projectile p : projectiles) {
            p.move();
            if (p.x < 0 || p.x > maxW || p.y < 0 || p.y > maxH) { projectiles.remove(p); continue; }
            
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
            boolean reachedEnd = false;
            if (z.x >= maxW || z.y >= maxH) {
                reachedEnd = true;
            } else if (z.waypoints != null && z.waypoints.length > 0 && z.wpIdx >= z.waypoints.length) {
                Waypoint lastWp = z.waypoints[z.waypoints.length - 1];
                float endPixelX = lastWp.col * Constants.TILE_SIZE;
                if (z.x > endPixelX + 25) {
                    reachedEnd = true;
                }
            }
            if (reachedEnd) {
                state = GameState.GAMEOVER;
                continue;
            }
            if (z.attackTarget == null ) {
                for (Plant p : plants) {
                    if (z.getBounds().intersects(p.getBounds())) { z.attackTarget = p; break; }
                }
            }
            z.update(this);
            if (z.isDead()) {
                sun += Constants.ZOMBIE_KILL_REWARD;
                // Rơi xu: 20% Xu Bạc, 5% Xu Vàng, 1% Kim Cương, 74% Trượt
                CoinType drop = LootTable.rollDrop(z.isMiniBoss, false);
                if (drop != null) {
                    spawnCoin(z.x + 20, z.y + 20, drop);
                }
                zombies.remove(z);
            }
        }
    }

    public void draw(Graphics2D g) {
        if (state == GameState.SHOP) shopScreen.draw(g);
        else if (state == GameState.MENU) drawMainMenu(g);
        else if (state == GameState.LEVEL_SELECT) drawLevelSelect(g);
        else if (state == GameState.INSTRUCTIONS) drawInstructions(g);
        else {
            drawGameplay(g);
            if (state == GameState.PAUSED) pauseDialog.draw(g);
            else if (state == GameState.GAMEOVER) drawGameOver(g);
            else if (state == GameState.GAMEWON) drawGameWon(g);
        }
    }

    private void drawMainMenu(Graphics2D g) {
        int sw = getScreenWidth();
        int sh = getScreenHeight();
        g.setColor(new Color(34, 139, 34)); g.fillRect(0, 0, sw, sh);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 64));
        String title = "BẢO VỆ KHU VƯỜN";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (sw - tw) / 2, 140);

        int bx = (sw - 200) / 2;
        btnPlay.x = bx;
        btnLevelSelect.x = bx;
        btnShop.x = bx;
        btnInstr.x = bx;
        btnEditor.x = bx;
        btnExit.x = bx;

        drawButton(g, btnPlay, "CHƠI NGAY", mouseX, mouseY);
        drawButton(g, btnLevelSelect, "CHỌN LEVEL", mouseX, mouseY, new Color(46, 139, 87));
        drawButton(g, btnShop, "CỬA HÀNG 🛒", mouseX, mouseY, new Color(180, 100, 30));
        drawButton(g, btnInstr, "HƯỚNG DẪN", mouseX, mouseY);
        drawButton(g, btnEditor, "LEVEL EDITOR", mouseX, mouseY);
        drawButton(g, btnExit, "THOÁT", mouseX, mouseY);

        // Hiển thị khung số dư xu nhỏ ở góc phải menu chính
        int mbX = sw - 170;
        int mbY = 20;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(mbX, mbY, 145, 40, 10, 10);
        g.setColor(new Color(255, 215, 0));
        g.drawRoundRect(mbX, mbY, 145, 40, 10, 10);
        if (coinBankImg != null) {
            g.drawImage(coinBankImg, mbX + 6, mbY + 4, 32, 32, null);
        }
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(255, 220, 50));
        g.drawString("$ " + WalletManager.getInstance().getCoins(), mbX + 44, mbY + 27);
    }

    private void drawLevelSelect(Graphics2D g) {
        int sw = getScreenWidth();
        int sh = getScreenHeight();

        // Nền tối phong cách khu vườn
        g.setColor(new Color(24, 34, 28));
        g.fillRect(0, 0, sw, sh);

        // Tiêu đề
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "CHỌN MÀN CHƠI";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (sw - tw) / 2, 65);

        g.setColor(new Color(200, 220, 200));
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        String sub = "Hãy chọn màn phòng thủ bạn muốn thử sức:";
        int subW = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, (sw - subW) / 2, 98);

        // Danh sách thẻ màn chơi
        int cardW = 380;
        int cardH = 210;
        int gapX = 35;
        int gapY = 25;
        int startY = 135;

        int count = availableLevels.size();
        int cols = (count <= 1) ? 1 : 2;
        int totalW = cols * cardW + (cols - 1) * gapX;
        int startX = (sw - totalW) / 2;

        for (int i = 0; i < count; i++) {
            LevelPreview lp = availableLevels.get(i);
            int colIdx = i % cols;
            int rowIdx = i / cols;
            int cx = startX + colIdx * (cardW + gapX);
            int cy = startY + rowIdx * (cardH + gapY);

            lp.cardRect = new Rectangle(cx, cy, cardW, cardH);
            boolean hover = lp.cardRect.contains(mouseX, mouseY);

            // Nền thẻ bài
            g.setColor(hover ? new Color(42, 60, 48) : new Color(34, 46, 38));
            g.fillRoundRect(cx, cy, cardW, cardH, 16, 16);

            // Viền thẻ bài
            g.setColor(hover ? new Color(100, 220, 120) : new Color(60, 100, 70));
            g.setStroke(new BasicStroke(hover ? 2.5f : 1.5f));
            g.drawRoundRect(cx, cy, cardW, cardH, 16, 16);
            g.setStroke(new BasicStroke(1.0f));

            // Badge Màn chơi (ví dụ: MÀN 1, MÀN 2)
            g.setColor(new Color(46, 139, 87));
            g.fillRoundRect(cx + 18, cy + 16, 85, 26, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 13));
            g.drawString("MÀN " + lp.id, cx + 32, cy + 34);

            // Tên màn chơi
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 19));
            String name = lp.name;
            if (name.length() > 22) name = name.substring(0, 20) + "...";
            g.drawString(name, cx + 115, cy + 36);

            // Đường gạch ngang phân cách nhẹ
            g.setColor(new Color(60, 80, 70));
            g.drawLine(cx + 18, cy + 52, cx + cardW - 18, cy + 52);

            // Thông số chi tiết
            g.setFont(new Font("Arial", Font.PLAIN, 15));
            g.setColor(new Color(230, 230, 230));

            // Sun
            g.setColor(new Color(255, 215, 0));
            g.fillOval(cx + 22, cy + 68, 14, 14);
            g.setColor(new Color(230, 230, 230));
            g.drawString("Sun khởi đầu: " + lp.initialSun, cx + 45, cy + 81);

            // Waves
            g.drawString("🧟 Đợt quái: " + lp.waveCount + " Waves", cx + 22, cy + 106);

            // Map Size
            g.drawString("🗺️ Kích thước: " + lp.cols + " cột x " + lp.rows + " hàng", cx + 22, cy + 131);

            // Nút bấm "VÀO CHƠI ▶"
            int btnW = 200;
            int btnH = 38;
            int bx = cx + (cardW - btnW) / 2;
            int by = cy + cardH - btnH - 16;
            lp.btnPlayRect = new Rectangle(bx, by, btnW, btnH);

            boolean btnHover = lp.btnPlayRect.contains(mouseX, mouseY);
            g.setColor(btnHover ? new Color(70, 190, 90) : new Color(46, 139, 87));
            g.fillRoundRect(bx, by, btnW, btnH, 10, 10);
            g.setColor(Color.WHITE);
            g.drawRoundRect(bx, by, btnW, btnH, 10, 10);
            g.setFont(new Font("Arial", Font.BOLD, 15));
            String playText = "VÀO CHƠI ▶";
            int ptw = g.getFontMetrics().stringWidth(playText);
            g.drawString(playText, bx + (btnW - ptw) / 2, by + 24);
        }

        // Nút quay lại Menu
        btnBack.setBounds((sw - 200) / 2, sh - 95, 200, 48);
        drawButton(g, btnBack, "QUAY LẠI", mouseX, mouseY);
    }

    private void drawInstructions(Graphics2D g) {
        int sw = getScreenWidth();
        int sh = getScreenHeight();
        g.setColor(new Color(30, 30, 40)); g.fillRect(0, 0, sw, sh);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40)); g.drawString("HƯỚNG DẪN CHƠI", (sw - 360) / 2, 60);
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
        
        btnBack.setBounds((sw - 200) / 2, sh - 95, 200, 48);
        drawButton(g, btnBack, "QUAY LẠI", mouseX, mouseY);
    }

    private void drawGameplay(Graphics2D g) {
        if(grid != null) grid.draw(g);
        
        int numCols = getCols();
        int numRows = getRows();
        if (selectedPlant > 0 && hoverCol >= 0 && hoverCol < numCols && hoverRow >= 0 && hoverRow < numRows) {
            int hX = hoverCol * Constants.TILE_SIZE; int hY = hoverRow * Constants.TILE_SIZE;
            boolean canPlant = false;
            if (grid != null && hoverRow < grid.map.length && hoverCol < grid.map[hoverRow].length) {
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

        // Vẽ Pet Ốc sên Stinky nếu đã mở khóa
        if (petSnail != null && WalletManager.getInstance().getData().hasPetSnail) {
            petSnail.draw(g);
        }

        // Vẽ các đồng xu và kim cương đang rơi hoặc bay trên sân
        for(Coin c : coins) {
            c.draw(g);
        }

        // UI Header
        int sw = getScreenWidth();
        g.setColor(new Color(0, 0, 0, 160)); g.fillRect(0, 0, sw, 80);
        g.setColor(new Color(255, 215, 0)); g.fillOval(10, 20, 40, 40);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 22)); g.drawString(String.valueOf(sun), 60, 48);

        // Khung hiển thị Ví Tiền ở góc trên bên phải
        int bankX = sw - 360;
        int bankY = 16;
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(bankX, bankY, 115, 36, 10, 10);
        g.setColor(new Color(255, 215, 0));
        g.drawRoundRect(bankX, bankY, 115, 36, 10, 10);
        if (coinBankImg != null) {
            g.drawImage(coinBankImg, bankX + 4, bankY + 3, 30, 30, null);
        } else {
            g.fillOval(bankX + 6, bankY + 6, 24, 24);
        }
        g.setColor(new Color(255, 220, 50));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("$ " + WalletManager.getInstance().getCoins(), bankX + 38, bankY + 25);

        // Nút Shop trong game
        btnInGameShop.setBounds(bankX, 55, 115, 22);
        drawSmallButton(g, btnInGameShop, "Shop 🛒", mouseX, mouseY);

        // Hiệu ứng số tiền nảy bay lên (+10, +50, +1000)
        for (WalletManager.FloatingText ft : WalletManager.getInstance().floatingTexts) {
            g.setColor(new Color(255, 225, 30, ft.alpha));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(ft.text, (int) ft.x, (int) ft.y);
        }

        // Thông tin Level hiện tại ở góc trên bên phải
        if (currentLevel != null) {
            g.setFont(new Font("Arial", Font.BOLD, 15));
            g.setColor(new Color(240, 240, 240));
            String lvlText = "Màn " + currentLevel.id + ": " + (currentLevel.name != null ? currentLevel.name : "");
            int ltw = g.getFontMetrics().stringWidth(lvlText);
            g.drawString(lvlText, sw - ltw - 120, 48);

            btnInGameMenu.setBounds(sw - 105, 20, 92, 36);
            drawPvZWoodButton(g, btnInGameMenu, "MENU", mouseX, mouseY);
        }

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
        int sw = getScreenWidth();
        int sh = getScreenHeight();
        g.setColor(new Color(0, 0, 0, 195)); g.fillRect(0, 0, sw, sh);

        int dw = 420;
        int dh = 380;
        int dx = (sw - dw) / 2;
        int dy = (sh - dh) / 2 - 20;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền bia đá Game Over
        GradientPaint stoneGrad = new GradientPaint(dx, dy, new Color(45, 30, 25), dx, dy + dh, new Color(25, 15, 12));
        g2.setPaint(stoneGrad);
        g2.fillRoundRect(dx, dy, dw, dh, 24, 24);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(160, 60, 50));
        g2.drawRoundRect(dx + 2, dy + 2, dw - 4, dh - 4, 22, 22);

        // Tiêu đề Game Over
        g2.setFont(new Font("Serif", Font.BOLD, 46));
        String title = "THE ZOMBIES ATE";
        String title2 = "YOUR BRAINS!";
        int tw1 = g2.getFontMetrics().stringWidth(title);
        g2.setColor(Color.BLACK);
        g2.drawString(title, (sw - tw1) / 2 + 2, dy + 55);
        g2.setColor(new Color(255, 60, 50));
        g2.drawString(title, (sw - tw1) / 2, dy + 53);

        int tw2 = g2.getFontMetrics().stringWidth(title2);
        g2.setColor(Color.BLACK);
        g2.drawString(title2, (sw - tw2) / 2 + 2, dy + 100);
        g2.setColor(new Color(255, 80, 70));
        g2.drawString(title2, (sw - tw2) / 2, dy + 98);

        int bw = 260, bh = 46;
        int bx = (sw - bw) / 2;
        int by = dy + 130;

        btnRestart.setBounds(bx, by, bw, bh);
        drawButton(g2, btnRestart, "CHƠI LẠI TỪ ĐẦU", mouseX, mouseY, new Color(180, 80, 30));
        by += 60;

        btnLevelSelectGW.setBounds(bx, by, bw, bh);
        drawButton(g2, btnLevelSelectGW, "CHỌN MÀN KHÁC 🗺️", mouseX, mouseY, new Color(45, 95, 75));
        by += 60;

        btnExitGO.setBounds(bx, by, bw, bh);
        drawButton(g2, btnExitGO, "VỀ MENU CHÍNH 🏠", mouseX, mouseY, new Color(110, 45, 45));

        g2.dispose();
    }

    private void drawGameWon(Graphics2D g) {
        int sw = getScreenWidth();
        int sh = getScreenHeight();
        g.setColor(new Color(0, 0, 0, 190)); g.fillRect(0, 0, sw, sh);

        int dw = 440;
        int dh = 420;
        int dx = (sw - dw) / 2;
        int dy = (sh - dh) / 2 - 30;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền bia đá Victory
        GradientPaint stoneGrad = new GradientPaint(dx, dy, new Color(30, 48, 32), dx, dy + dh, new Color(15, 28, 18));
        g2.setPaint(stoneGrad);
        g2.fillRoundRect(dx, dy, dw, dh, 24, 24);
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(215, 175, 70));
        g2.drawRoundRect(dx + 2, dy + 2, dw - 4, dh - 4, 22, 22);

        // Tiêu đề Victory
        g2.setFont(new Font("Serif", Font.BOLD, 48));
        String title = "VICTORY!";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.setColor(Color.BLACK);
        g2.drawString(title, (sw - tw) / 2 + 2, dy + 58);
        g2.setColor(new Color(255, 225, 50));
        g2.drawString(title, (sw - tw) / 2, dy + 56);

        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.setColor(new Color(210, 245, 210));
        String sub = "Chúc mừng! Bạn đã bảo vệ an toàn khu vườn!";
        int subW = g2.getFontMetrics().stringWidth(sub);
        g2.drawString(sub, (sw - subW) / 2, dy + 92);

        int bw = 280, bh = 46;
        int bx = (sw - bw) / 2;
        int by = dy + 120;

        boolean hasNext = hasLevel(currentLevelId + 1);
        if (hasNext) {
            btnNextLevel.setBounds(bx, by, bw, bh);
            drawButton(g2, btnNextLevel, "MÀN TIẾP THEO ▶", mouseX, mouseY, new Color(46, 139, 87));
            by += 58;
        }

        btnRestart.setBounds(bx, by, bw, bh);
        drawButton(g2, btnRestart, "CHƠI LẠI MÀN NÀY", mouseX, mouseY, new Color(180, 90, 30));
        by += 58;

        btnLevelSelectGW.setBounds(bx, by, bw, bh);
        drawButton(g2, btnLevelSelectGW, "CHỌN MÀN KHÁC 🗺️", mouseX, mouseY, new Color(45, 95, 75));
        by += 58;

        btnExitGO.setBounds(bx, by, bw, bh);
        drawButton(g2, btnExitGO, "VỀ MENU CHÍNH 🏠", mouseX, mouseY, new Color(110, 45, 45));

        g2.dispose();
    }

    private void drawButton(Graphics2D g, Rectangle r, String text, int mX, int mY) {
        drawButton(g, r, text, mX, mY, new Color(110, 80, 50));
    }

    private void drawButton(Graphics2D g, Rectangle r, String text, int mX, int mY, Color baseColor) {
        boolean hover = r.contains(mX, mY);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Bóng đổ
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(r.x + 2, r.y + 3, r.width, r.height, 12, 12);

        Color topColor = hover ? baseColor.brighter() : baseColor;
        Color bottomColor = hover ? baseColor : baseColor.darker();
        GradientPaint btnGrad = new GradientPaint(r.x, r.y, topColor, r.x, r.y + r.height, bottomColor);
        g2.setPaint(btnGrad);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        // Viền gờ nổi 3D
        g2.setStroke(new BasicStroke(hover ? 2.2f : 1.5f));
        g2.setColor(hover ? new Color(255, 235, 120) : new Color(220, 180, 100));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        g2.setFont(new Font("Serif", Font.BOLD, 17));
        int strW = g2.getFontMetrics().stringWidth(text);
        int strX = r.x + (r.width - strW) / 2;
        int strY = r.y + (r.height + 6) / 2;

        g2.setColor(new Color(15, 15, 15, 180));
        g2.drawString(text, strX + 1, strY + 2);

        g2.setColor(hover ? Color.WHITE : new Color(250, 245, 225));
        g2.drawString(text, strX, strY);
        g2.dispose();
    }

    public void drawPvZWoodButton(Graphics2D g, Rectangle r, String text, int mX, int mY) {
        boolean hover = r.contains(mX, mY);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Bóng đổ
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(r.x + 2, r.y + 3, r.width, r.height, 12, 12);

        // Nền dải màu vân gỗ nâu ấm
        Color topC = hover ? new Color(185, 125, 65) : new Color(145, 90, 45);
        Color botC = hover ? new Color(135, 85, 38) : new Color(105, 62, 28);
        GradientPaint grad = new GradientPaint(r.x, r.y, topC, r.x, r.y + r.height, botC);
        g2.setPaint(grad);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);

        // Viền vàng nổi 3D
        g2.setStroke(new BasicStroke(hover ? 2.5f : 1.8f));
        g2.setColor(hover ? new Color(255, 235, 130) : new Color(215, 175, 95));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);

        // Chữ
        g2.setFont(new Font("Serif", Font.BOLD, 16));
        int tw = g2.getFontMetrics().stringWidth(text);
        int tx = r.x + (r.width - tw) / 2;
        int ty = r.y + (r.height + 6) / 2;

        g2.setColor(new Color(20, 10, 5, 200));
        g2.drawString(text, tx + 1, ty + 2);
        g2.setColor(hover ? Color.WHITE : new Color(255, 245, 210));
        g2.drawString(text, tx, ty);
        g2.dispose();
    }

    private void drawSmallButton(Graphics2D g, Rectangle r, String text, int mX, int mY) {
        boolean hover = r.contains(mX, mY);
        g.setColor(hover ? new Color(190, 190, 190) : new Color(240, 240, 240));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 6, 6);
        g.setColor(Color.BLACK);
        g.drawRoundRect(r.x, r.y, r.width, r.height, 6, 6);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, r.x + (r.width - tw) / 2, r.y + (r.height + 6) / 2);
    }
    
    private boolean isPlanted(int col, int row) {
        for(Plant p : plants) if (p.x / Constants.TILE_SIZE == col && p.y / Constants.TILE_SIZE == row) return true;
        return false;
    }

    public void handleClick(int mX, int mY) {
        if (state == GameState.MENU) {
            if (btnPlay.contains(mX, mY)) {
                if (currentLevel == null) loadLevel("/data/level_1.json");
                resetGame();
            }
            else if (btnLevelSelect.contains(mX, mY)) {
                scanAvailableLevels();
                state = GameState.LEVEL_SELECT;
            }
            else if (btnShop.contains(mX, mY)) {
                state = GameState.SHOP;
            }
            else if (btnInstr.contains(mX, mY)) state = GameState.INSTRUCTIONS;
            else if (btnEditor.contains(mX, mY)) {
                new com.team.gardendefense.editor.LevelEditor().setVisible(true);
            }
            else if (btnExit.contains(mX, mY)) System.exit(0);
            return;
        } else if (state == GameState.SHOP) {
            shopScreen.handleClick(mX, mY);
            return;
        } else if (state == GameState.PAUSED) {
            pauseDialog.handleClick(mX, mY);
            return;
        } else if (state == GameState.LEVEL_SELECT) {
            if (btnBack.contains(mX, mY)) {
                state = GameState.MENU;
                return;
            }
            for (LevelPreview lp : availableLevels) {
                if ((lp.btnPlayRect != null && lp.btnPlayRect.contains(mX, mY)) ||
                    (lp.cardRect != null && lp.cardRect.contains(mX, mY))) {
                    startLevel(lp.id);
                    return;
                }
            }
            return;
        } else if (state == GameState.INSTRUCTIONS) {
            if (btnBack.contains(mX, mY)) state = GameState.MENU;
            return;
        } else if (state == GameState.GAMEOVER) {
            if (btnRestart.contains(mX, mY)) resetGame();
            else if (btnLevelSelectGW.contains(mX, mY)) {
                scanAvailableLevels();
                state = GameState.LEVEL_SELECT;
                if (panel != null) panel.resizeForLevel(Constants.COLS, Constants.ROWS);
            }
            else if (btnExitGO.contains(mX, mY)) {
                state = GameState.MENU;
                if (panel != null) panel.resizeForLevel(Constants.COLS, Constants.ROWS);
            }
            return;
        } else if (state == GameState.GAMEWON) {
            if (hasLevel(currentLevelId + 1) && btnNextLevel.contains(mX, mY)) {
                startLevel(currentLevelId + 1);
            }
            else if (btnRestart.contains(mX, mY)) resetGame();
            else if (btnLevelSelectGW.contains(mX, mY)) {
                scanAvailableLevels();
                state = GameState.LEVEL_SELECT;
                if (panel != null) panel.resizeForLevel(Constants.COLS, Constants.ROWS);
            }
            else if (btnExitGO.contains(mX, mY)) {
                state = GameState.MENU;
                if (panel != null) panel.resizeForLevel(Constants.COLS, Constants.ROWS);
            }
            return;
        }

        if (state == GameState.PLAYING) {
            if (btnInGameMenu.contains(mX, mY)) {
                AudioManager.getInstance().playBuySound();
                state = GameState.PAUSED;
                return;
            }
            if (btnInGameShop.contains(mX, mY)) {
                state = GameState.SHOP;
                return;
            }

            // Click trực tiếp vào đồng xu để nhặt
            for (Coin c : coins) {
                if (!c.isDead() && c.getBounds().contains(mX, mY)) {
                    c.collect(this);
                    return;
                }
            }

            for(PlantCard c : uiCards) {
                if(c.rect.contains(mX, mY)) { selectedPlant = c.id; return; }
            }

            if (selectedPlant > 0) {
                int col = mX / Constants.TILE_SIZE; int row = mY / Constants.TILE_SIZE;
                int numCols = getCols();
                int numRows = getRows();
                if (col < 0 || col >= numCols || row < 0 || row >= numRows) return;
                if (grid == null || row >= grid.map.length || col >= grid.map[row].length) return;
                
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
                else if (selectedPlant == 11) cost = Constants.COST_MARIGOLD;

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
                    else if (selectedPlant == 11) p = new Marigold(col, row);
                    
                    if (p != null) {
                        plants.add(p);
                        sun -= cost; 
                    }
                    selectedPlant = 0; 
                } else { selectedPlant = 0; }
            }
        }
    }
}
