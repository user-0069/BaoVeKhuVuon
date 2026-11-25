package com.team.gardendefense.controller;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
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
import com.team.gardendefense.model.entity.mower.ActiveLawnMower;
import com.team.gardendefense.system.AudioManager;
import com.team.gardendefense.system.LootTable;
import com.team.gardendefense.system.ShopManager;
import com.team.gardendefense.system.WalletManager;
import com.team.gardendefense.view.ui.PauseDialog;
import com.team.gardendefense.view.ui.ShopScreen;
import com.team.gardendefense.model.map.Grid;
import com.team.gardendefense.model.level.LevelData;
import com.team.gardendefense.model.level.LevelDataValidator;
import com.team.gardendefense.model.level.Waypoint;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.view.renderer.GamePanel;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.FontMetrics;

public class GameManager {
    private static final Logger LOGGER = Logger.getLogger(GameManager.class.getName());
    private int coinBankTargetX = -1;
    private int coinBankTargetY = 28;

    public enum GameState { MENU, LEVEL_SELECT, INSTRUCTIONS, PLAYING, PAUSED, GAMEOVER, GAMEWON, SHOP }
    public GameState state = GameState.MENU;
    private GameState shopReturnState = GameState.MENU;

    public Grid grid;
    public List<Plant> plants = new CopyOnWriteArrayList<>();
    public List<Monster> zombies = new CopyOnWriteArrayList<>();
    public List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    public List<Coin> coins = new CopyOnWriteArrayList<>();
    public PetSnail petSnail = null;
    public ShopScreen shopScreen;
    public PauseDialog pauseDialog;
    public Image coinBankImg;
    private Image mainMenuBackgroundImg;
    private Image mainMenuTitleImg;
    private Image[] mainMenuBarImages;
    private Image sunIconImg;
    private Image victoryBoardImg;
    private Image[] victoryChoiceBars;
    private Image gameOverBoardImg;
    private Image[] gameOverChoiceBars;
    
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

    public void openShop() {
        openShop(state);
    }

    public void openShop(GameState returnState) {
        cancelAllTargetingModes();
        shopReturnState = returnState;
        state = GameState.SHOP;
    }

    public void closeShop() {
        if (state == GameState.SHOP) {
            state = shopReturnState;
            shopReturnState = GameState.MENU;
            if (state == GameState.PLAYING) {
                if (WalletManager.getInstance().getData().hasPetSnail && petSnail == null) {
                    petSnail = new PetSnail(50, getMapHeight() - 60);
                }
            }
        }
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
    // Trạng thái cuộn cho mục chọn Level
    public int levelScrollOffset = 0;
    public int maxLevelScroll = 0;
    public boolean isDraggingLevelScrollbar = false;
    private int dragStartY = 0;
    private int dragStartOffset = 0;
    private Rectangle levelScrollTrackRect = new Rectangle();
    private Rectangle levelScrollThumbRect = new Rectangle();
    private int levelSelectViewY = 120;
    private int levelSelectViewH = 400;
    private int levelSelectScrollTrackH = 390;

    // Menu động chứa thẻ bài thực vật
    public class PlantCard {
        public int id;
        public Rectangle rect;
        public Color color;
        public String price;
        public java.awt.Image image;
        public int maxCooldown;
        public int currentCooldown = 0;

        public PlantCard(int id, int x) {
            this.id = id;
            this.rect = new Rectangle(x, 6, 54, 68);
            this.maxCooldown = getCooldownForPlant(id);
            this.currentCooldown = 0;
            if(id==1) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_sunflower.png"); price = String.valueOf(Constants.COST_SUNFLOWER); }
            else if (id==2) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_peashooter.png"); price = String.valueOf(Constants.COST_PEASHOOTER); }
            else if (id==3) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_sniperplant.png"); price = String.valueOf(Constants.COST_SNIPER); }
            else if (id==4) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_wallnut.png"); price = String.valueOf(Constants.COST_WALLNUT); }
            else if (id==5) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_snowpea.png"); price = String.valueOf(Constants.COST_SNOWPEA); }
            else if (id==6) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_cherrybomb.png"); price = String.valueOf(Constants.COST_CHERRYBOMB); }
            else if (id==7) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_chomper.png"); price = String.valueOf(Constants.COST_CHOMPER); }
            else if (id==8) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_peashooter.png"); price = String.valueOf(Constants.COST_PEASHOOTER); }
            else if (id==9) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_sniperplant.png"); price = String.valueOf(Constants.COST_SNIPER); }
            else if (id==10) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_snowpea.png"); price = String.valueOf(Constants.COST_SNOWPEA); }
            else if (id==11) { image = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/card_marigold.png"); price = String.valueOf(Constants.COST_MARIGOLD); }
        }
    }

    public static int getCooldownForPlant(int id) {
        switch (id) {
            case 1: return Constants.CD_SUNFLOWER;
            case 2: case 8: return Constants.CD_PEASHOOTER;
            case 3: case 9: return Constants.CD_SNIPER;
            case 4: return Constants.CD_WALLNUT;
            case 5: case 10: return Constants.CD_SNOWPEA;
            case 6: return Constants.CD_CHERRYBOMB;
            case 7: return Constants.CD_CHOMPER;
            case 11: return Constants.CD_MARIGOLD;
            default: return 450;
        }
    }
    public List<PlantCard> uiCards = new ArrayList<>();
    public int selectedPlant = 0; 

    // Các thành phần điều hướng thanh thẻ bài thực vật
    public Rectangle btnCardLeft = new Rectangle(0, 0, 22, 40);
    public Rectangle btnCardRight = new Rectangle(0, 0, 22, 40);
    public int cardScrollOffset = 0;
    public boolean needsCardScroll = false;
    public int maxCardScroll = 0;
    public int cardViewX = 110;
    public int cardViewW = 400;

    public void handleKeyPress(int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE || keyCode == java.awt.event.KeyEvent.VK_P) {
            if (state == GameState.PLAYING) {
                AudioManager.getInstance().playBuySound();
                state = GameState.PAUSED;
            } else if (state == GameState.PAUSED) {
                AudioManager.getInstance().playBuySound();
                state = GameState.PLAYING;
            } else if (state == GameState.SHOP) {
                closeShop();
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

    public Rectangle btnInGameMenu = new Rectangle(0, 0, 88, 32);
    public Rectangle btnInGameShop = new Rectangle(0, 0, 120, 24);
    public Rectangle btnIceShroom = new Rectangle(0, 0, 52, 52);
    public Rectangle btnChocolate = new Rectangle(0, 0, 52, 52);
    public Rectangle btnShovel = new Rectangle(0, 0, 52, 52);
    public boolean shovelSelected = false;
    public Image shovelImg;
    public Image shovelBankImg;
    public Rectangle btnLawnMower = new Rectangle(0, 0, 50, 52);
    public boolean mowerTargeting = false;
    public Image mowerIconImg;
    public List<ActiveLawnMower> activeMowers = new CopyOnWriteArrayList<>();
    public List<ZombieSpawnAlert> spawnAlerts = new CopyOnWriteArrayList<>();
    public List<ZombieLimb> zombieLimbs = new CopyOnWriteArrayList<>();
    public int iceShroomDuration = 0; // Đếm lùi 300 ticks (5 giây)
    public float iceShroomWaveRadius = 0;

    // Cooldown đếm lùi cho 4 kỹ năng chủ động mua từ Shop:
    public static final int CD_ICE_SHROOM = 20 * 60;     // 20 giây (1200 frames)
    public static final int CD_CHOCOLATE = 180 * 60;     // 180 giây / 3 phút (10800 frames)
    public static final int CD_SHOVEL = (int)(2.5 * 60); // 2.5 giây giữa các lượt đào cây (150 frames)
    public static final int CD_LAWN_MOWER = 30 * 60;     // 30 giây (1800 frames)

    public int iceShroomCooldown = 0;
    public int chocolateCooldown = 0;
    public int shovelCooldown = 0;
    public int lawnMowerCooldown = 0;

    public Image chocolateIconImg;
    public Image iceShroomIconImg;
    public Image scrollLeftImg;
    public Image scrollLeftHlImg;
    public Image scrollLeftDisImg;
    public Image scrollRightImg;
    public Image scrollRightHlImg;
    public Image scrollRightDisImg;
    public Image btnBackPvzImg;
    public Image btnBackPvzHlImg;

    public void spawnZombieLimb(ZombieLimb limb) {
        if (limb != null) {
            zombieLimbs.add(limb);
        }
    }

    public void triggerZombieSpawnAlert(float x, float y, Waypoint[] waypoints) {
        spawnAlerts.add(new ZombieSpawnAlert(x, y, waypoints));
    }

    public void triggerZombieSpawnAlert(float x, float y, float dx, float dy) {
        spawnAlerts.add(new ZombieSpawnAlert(x, y, dx, dy));
    }

    public GameManager() {
        shopScreen = new ShopScreen(this);
        pauseDialog = new PauseDialog(this);
        coinBankImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/coinbank.png");
        shovelImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/Shovel.png");
        shovelBankImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/ShovelBank.png");
        mowerIconImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/icon_roofcleaner.png");
        chocolateIconImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/chocolate.png");
        iceShroomIconImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/reanim/IceShroom_head.png");
        scrollLeftImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/zombatar_prev_button.png");
        scrollLeftHlImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/zombatar_prev_button_highlight.png");
        scrollLeftDisImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/zombatar_prev_button_disabled.png");
        scrollRightImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/zombatar_next_button.png");
        scrollRightHlImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/zombatar_next_button_highlight.png");
        scrollRightDisImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/zombatar_next_button_disabled.png");
        btnBackPvzImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/Almanac_CloseButton.png");
        btnBackPvzHlImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/Almanac_CloseButtonHighlight.png");
        mainMenuBackgroundImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/mainbg.png");
        mainMenuTitleImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/titlegame.png");
        mainMenuBarImages = loadMainMenuBarImages();
        sunIconImg = loadSunIconImage();
        victoryBoardImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/victoryboard.png");
        victoryChoiceBars = loadVictoryChoiceBars();
        gameOverBoardImg = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/loseboard.png");
        gameOverChoiceBars = loadGameOverChoiceBars();
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

        String[] candidateDirs = {
            "src/main/resources/data",
            "BaoVeKhuVuon/src/main/resources/data",
            "target/classes/data",
            "BaoVeKhuVuon/target/classes/data",
            "../src/main/resources/data",
            "../BaoVeKhuVuon/src/main/resources/data"
        };
        for (String cDir : candidateDirs) {
            File dir = new File(cDir);
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
        }

        // Tự động kiểm tra thêm qua Classpath cho các level từ 1 đến 50
        for (int i = 1; i <= 50; i++) {
            if (getClass().getResource("/data/level_" + i + ".json") != null) {
                foundIds.add(i);
            }
        }

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

    public void openLevelSelect() {
        scanAvailableLevels();
        levelScrollOffset = 0;
        isDraggingLevelScrollbar = false;
        state = GameState.LEVEL_SELECT;
        if (panel != null) panel.resizeForLevel(Constants.COLS, Constants.ROWS);
    }

    private InputStream getLevelStream(String path) {
        InputStream is = getClass().getResourceAsStream(path);
        if (is != null) return is;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            is = cl.getResourceAsStream(path.startsWith("/") ? path.substring(1) : path);
            if (is != null) return is;
        }

        String cleanPath = path.startsWith("/") ? path : ("/" + path);
        String[] candidatePaths = {
            "src/main/resources" + cleanPath,
            "BaoVeKhuVuon/src/main/resources" + cleanPath,
            "target/classes" + cleanPath,
            "BaoVeKhuVuon/target/classes" + cleanPath,
            ".." + cleanPath,
            "../BaoVeKhuVuon/src/main/resources" + cleanPath
        };
        for (String cp : candidatePaths) {
            File f = new File(cp);
            if (f.exists()) {
                try {
                    return new FileInputStream(f);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private LevelData loadLevelDataOnly(int id) {
        String path = "/data/level_" + id + ".json";
        try {
            InputStream is = getLevelStream(path);
            if (is == null) return null;
            try (InputStream input = is;
                 InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                LevelData data = new Gson().fromJson(reader, LevelData.class);
                String validationError = LevelDataValidator.validate(data);
                if (validationError != null) {
                    LOGGER.warning("Bỏ qua level " + id + ": " + validationError);
                    return null;
                }
                return data;
            }
        } catch (Exception e) {
            LOGGER.warning("Không thể đọc level " + id + ": " + e.getMessage());
        }
        return null;
    }

    public boolean hasLevel(int id) {
        for (LevelPreview lp : availableLevels) {
            if (lp.id == id) return true;
        }
        InputStream is = getLevelStream("/data/level_" + id + ".json");
        if (is != null) {
            try { is.close(); } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    public void startLevel(int id) {
        if (loadLevel("/data/level_" + id + ".json")) resetGame();
    }

    public boolean loadLevel(String path) {
        try {
            InputStream is = getLevelStream(path);
            if (is == null) return false;
            try (InputStream input = is;
                 InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                LevelData loadedLevel = new Gson().fromJson(reader, LevelData.class);
                String validationError = LevelDataValidator.validate(loadedLevel);
                if (validationError != null) {
                    LOGGER.warning("Không thể nạp level từ " + path + ": " + validationError);
                    return false;
                }
                currentLevel = loadedLevel;
                currentLevelId = currentLevel.id;
                grid = new Grid(currentLevel.map, currentLevel.id);
                return true;
            }
        } catch (Exception e) {
            LOGGER.warning("Không thể nạp level từ " + path + ": " + e.getMessage());
        }
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

    public float getCoinBankTargetX() {
        return coinBankTargetX >= 0 ? coinBankTargetX : (getScreenWidth() - 218);
    }

    public float getCoinBankTargetY() {
        return coinBankTargetY;
    }

    public void resetGame() {
        plants.clear(); zombies.clear(); projectiles.clear(); coins.clear();
        passiveSunTimer = 0;
        cancelAllTargetingModes();
        activeMowers.clear();
        spawnAlerts.clear();
        zombieLimbs.clear();
        iceShroomDuration = 0; iceShroomWaveRadius = 0;
        iceShroomCooldown = 0; chocolateCooldown = 0; shovelCooldown = 0; lawnMowerCooldown = 0;
        
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
            cardScrollOffset = 0;
            int startX = 110;
            for (int pId : currentLevel.allowedPlants) {
                uiCards.add(new PlantCard(pId, startX));
                startX += 58;
            }
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
        if (state == GameState.GAMEOVER || state == GameState.GAMEWON) {
            updateCollectibles();
            return;
        }
        if (state != GameState.PLAYING) return;

        // Xử lý hiệu lực của Bùa Đóng Băng toàn sân (5 giây)
        if (iceShroomDuration > 0) {
            iceShroomDuration--;
            iceShroomWaveRadius += 32f;
            for (Monster z : zombies) {
                if (!z.isFrozen()) {
                    z.applyFreeze(iceShroomDuration, iceShroomDuration > 180);
                }
            }
        }

        // Cập nhật các Xe Cắt Cỏ đang càn quét
        for (ActiveLawnMower m : activeMowers) {
            m.update(this);
            if (m.finished) {
                activeMowers.remove(m);
            }
        }

        // Đếm lùi thời gian hồi chiêu (countdown) cho các thẻ bài cây
        for (PlantCard c : uiCards) {
            if (c.currentCooldown > 0) {
                c.currentCooldown--;
            }
        }

        // Đếm lùi thời gian hồi chiêu (countdown) cho các kỹ năng chủ động hotbar
        if (iceShroomCooldown > 0) iceShroomCooldown--;
        if (chocolateCooldown > 0) chocolateCooldown--;
        if (shovelCooldown > 0) shovelCooldown--;
        if (lawnMowerCooldown > 0) lawnMowerCooldown--;

        if (petSnail != null && petSnail.isBoosted()) {
            chocolateCooldown = Math.max(chocolateCooldown, petSnail.getBoostTimer());
        }

        // Cập nhật các hiệu ứng cảnh báo Zombie xuất hiện (tam giác chấm than & mũi tên)
        for (ZombieSpawnAlert alert : spawnAlerts) {
            alert.update();
        }
        spawnAlerts.removeIf(ZombieSpawnAlert::isDead);

        // Cập nhật các hiệu ứng mảnh cơ thể Zombie rơi (cánh tay, đầu)
        for (ZombieLimb limb : zombieLimbs) {
            limb.update();
        }
        zombieLimbs.removeIf(ZombieLimb::isDead);

        passiveSunTimer++;
        if (passiveSunTimer >= Constants.PASSIVE_SUN_RATE) { sun += Constants.PASSIVE_SUN_AMOUNT; passiveSunTimer = 0; }

        if (waveManager != null) {
            waveManager.update();
            if (waveManager.isAllSpawned() && zombies.isEmpty()) {
                if (state != GameState.GAMEWON) {
                    // Thưởng Kim Cương khi thắng trọn vẹn màn chơi!
                    spawnCoin(getScreenWidth() / 2.0f, getScreenHeight() / 2.0f, CoinType.DIAMOND);
                    cancelAllTargetingModes();
                }
                state = GameState.GAMEWON; // Thắng!
            }
        }

        updateCollectibles();

        for (Plant p : plants) {
            if (p.isDead()) plants.remove(p);
            else {
                p.advanceAnimation();
                p.update(this);
            }
        }

        int maxW = getMapWidth();
        int maxH = getMapHeight();

        for (Projectile p : projectiles) {
            p.move();
            if (p.x < 0 || p.x > maxW || p.y < 0 || p.y > maxH) { projectiles.remove(p); continue; }
            
            Rectangle pBounds = p.getBounds();
            if (p.isEnemy) {
                for (Plant plant : plants) {
                    if (!plant.isDead() && plant.getBounds().intersects(pBounds)) {
                        plant.takeDamage(p.damage); p.hit(); break;
                    }
                }
            } else {
                for (Monster z : zombies) {
                    if (!z.isDead() && z.getBounds().intersects(pBounds)) {
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
                cancelAllTargetingModes();
                state = GameState.GAMEOVER;
                continue;
            }
            if (z.attackTarget != null && (z.attackTarget.isDead() || !plants.contains(z.attackTarget))) {
                z.attackTarget = null;
            }
            if (z.attackTarget == null) {
                for (Plant p : plants) {
                    if (!p.isDead() && z.getBounds().intersects(p.getBounds())) {
                        z.attackTarget = p;
                        break;
                    }
                }
            }
            z.update(this);
            if (z.isCompletelyDead()) {
                zombies.remove(z);
            }
        }
    }

    private void updateCollectibles() {
        for (Coin c : coins) {
            c.update(this);
            if (c.isDead()) coins.remove(c);
        }
        if (WalletManager.getInstance().getData().hasPetSnail) {
            if (petSnail == null) {
                petSnail = new PetSnail(50, getMapHeight() - 60);
            }
            petSnail.update(this);
        }
        WalletManager.getInstance().updateFloatingTexts();
    }

    private boolean collectCoinAt(int x, int y) {
        for (Coin c : coins) {
            if (!c.isDead() && c.getBounds().contains(x, y)) {
                c.collect(this);
                return true;
            }
        }
        return false;
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
        if (mainMenuBackgroundImg != null
                && mainMenuBackgroundImg.getWidth(null) > 0
                && mainMenuBackgroundImg.getHeight(null) > 0) {
            int imageWidth = mainMenuBackgroundImg.getWidth(null);
            int imageHeight = mainMenuBackgroundImg.getHeight(null);
            double scale = Math.max((double) sw / imageWidth, (double) sh / imageHeight);
            int drawWidth = (int) Math.ceil(imageWidth * scale);
            int drawHeight = (int) Math.ceil(imageHeight * scale);

            Graphics2D backgroundGraphics = (Graphics2D) g.create();
            try {
                backgroundGraphics.clipRect(0, 0, sw, sh);
                backgroundGraphics.drawImage(
                        mainMenuBackgroundImg,
                        (sw - drawWidth) / 2,
                        (sh - drawHeight) / 2,
                        drawWidth,
                        drawHeight,
                        null);
            } finally {
                backgroundGraphics.dispose();
            }
        } else {
            g.setColor(new Color(34, 139, 34));
            g.fillRect(0, 0, sw, sh);
        }

        if (mainMenuTitleImg != null
                && mainMenuTitleImg.getWidth(null) > 0
                && mainMenuTitleImg.getHeight(null) > 0) {
            int titleWidth = sw * 65 / 100;
            int titleHeight = titleWidth * mainMenuTitleImg.getHeight(null) / mainMenuTitleImg.getWidth(null);
            g.drawImage(mainMenuTitleImg, (sw - titleWidth) / 2, 0, titleWidth, titleHeight, null);
        }

        int bx = (sw - 200) / 2;
        btnPlay.x = bx;
        btnLevelSelect.x = bx;
        btnShop.x = bx;
        btnInstr.x = bx;
        btnEditor.x = bx;
        btnExit.x = bx;

        drawMainMenuChoice(g, btnPlay, 0, "CHƠI NGAY", mouseX, mouseY, new Color(110, 80, 50));
        drawMainMenuChoice(g, btnLevelSelect, 1, "CHỌN LEVEL", mouseX, mouseY, new Color(46, 139, 87));
        drawMainMenuChoice(g, btnShop, 2, "CỬA HÀNG", mouseX, mouseY, new Color(180, 100, 30));
        drawMainMenuChoice(g, btnInstr, 3, "HƯỚNG DẪN", mouseX, mouseY, new Color(110, 80, 50));
        drawMainMenuChoice(g, btnEditor, 4, "LEVEL EDITOR", mouseX, mouseY, new Color(110, 80, 50));
        drawMainMenuChoice(g, btnExit, 5, "THOÁT", mouseX, mouseY, new Color(110, 80, 50));

        // Hiển thị khung số dư xu nhỏ ở góc phải menu chính (co giãn linh hoạt theo số chữ số)
        int coins = WalletManager.getInstance().getCoins();
        String coinStr = "$ " + String.format(java.util.Locale.US, "%,d", coins);
        Font coinFont = new Font("Arial", Font.BOLD, 18);
        FontMetrics fm = g.getFontMetrics(coinFont);
        int textW = fm.stringWidth(coinStr);
        int mbW = Math.max(150, textW + 54);
        int mbX = sw - mbW - 20;
        int mbY = 20;

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(mbX, mbY, mbW, 40, 10, 10);
        g.setColor(new Color(255, 215, 0));
        g.drawRoundRect(mbX, mbY, mbW, 40, 10, 10);
        if (coinBankImg != null) {
            g.drawImage(coinBankImg, mbX + 6, mbY + 4, 32, 32, null);
        }
        g.setFont(coinFont);
        g.setColor(new Color(255, 220, 50));
        g.drawString(coinStr, mbX + 44, mbY + 27);
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

        // Kích thước viewport cho danh sách cuộn
        levelSelectViewY = 120;
        levelSelectViewH = Math.max(100, sh - 205); // Chừa 120px phía trên và 85px phía dưới cho thanh điều hướng

        // Danh sách thẻ màn chơi
        int cardW = 380;
        int cardH = 210;
        int gapX = 35;
        int gapY = 25;

        int count = availableLevels.size();
        int cols = (count <= 1) ? 1 : 2;
        int totalW = cols * cardW + (cols - 1) * gapX;
        int startX = (sw - totalW) / 2;

        int totalRows = (count + cols - 1) / cols;
        int contentH = totalRows * cardH + (totalRows > 1 ? (totalRows - 1) * gapY : 0) + 20;
        maxLevelScroll = Math.max(0, contentH - levelSelectViewH);
        levelScrollOffset = Math.max(0, Math.min(maxLevelScroll, levelScrollOffset));

        // Vẽ danh sách thẻ bài với Clipping
        Graphics2D gCards = (Graphics2D) g.create();
        gCards.clipRect(0, levelSelectViewY, sw, levelSelectViewH);

        for (int i = 0; i < count; i++) {
            LevelPreview lp = availableLevels.get(i);
            int colIdx = i % cols;
            int rowIdx = i / cols;
            int cx = startX + colIdx * (cardW + gapX);
            int cy = levelSelectViewY + 10 + rowIdx * (cardH + gapY) - levelScrollOffset;

            lp.cardRect = new Rectangle(cx, cy, cardW, cardH);
            boolean inView = (cy + cardH >= levelSelectViewY && cy <= levelSelectViewY + levelSelectViewH);
            boolean hover = inView && (mouseY >= levelSelectViewY && mouseY <= levelSelectViewY + levelSelectViewH)
                    && lp.cardRect.contains(mouseX, mouseY);

            // Nền thẻ bài
            gCards.setColor(hover ? new Color(42, 60, 48) : new Color(34, 46, 38));
            gCards.fillRoundRect(cx, cy, cardW, cardH, 16, 16);

            // Viền thẻ bài
            gCards.setColor(hover ? new Color(100, 220, 120) : new Color(60, 100, 70));
            gCards.setStroke(new BasicStroke(hover ? 2.5f : 1.5f));
            gCards.drawRoundRect(cx, cy, cardW, cardH, 16, 16);
            gCards.setStroke(new BasicStroke(1.0f));

            // Badge Màn chơi (ví dụ: MÀN 1, MÀN 2)
            gCards.setColor(new Color(46, 139, 87));
            gCards.fillRoundRect(cx + 18, cy + 16, 85, 26, 8, 8);
            gCards.setColor(Color.WHITE);
            gCards.setFont(new Font("Arial", Font.BOLD, 13));
            gCards.drawString("MÀN " + lp.id, cx + 32, cy + 34);

            // Tên màn chơi
            gCards.setColor(Color.WHITE);
            gCards.setFont(new Font("Arial", Font.BOLD, 19));
            String name = lp.name;
            if (name.length() > 22) name = name.substring(0, 20) + "...";
            gCards.drawString(name, cx + 115, cy + 36);

            // Đường gạch ngang phân cách nhẹ
            gCards.setColor(new Color(60, 80, 70));
            gCards.drawLine(cx + 18, cy + 52, cx + cardW - 18, cy + 52);

            // Thông số chi tiết
            gCards.setFont(new Font("Arial", Font.PLAIN, 15));
            gCards.setColor(new Color(230, 230, 230));

            // Sun
            gCards.setColor(new Color(255, 215, 0));
            gCards.fillOval(cx + 22, cy + 68, 14, 14);
            gCards.setColor(new Color(230, 230, 230));
            gCards.drawString("Sun khởi đầu: " + lp.initialSun, cx + 45, cy + 81);

            // Waves
            gCards.drawString("Đợt quái: " + lp.waveCount + " Waves", cx + 22, cy + 106);

            // Map Size
            gCards.drawString("Kích thước: " + lp.cols + " cột x " + lp.rows + " hàng", cx + 22, cy + 131);

            // Nút bấm "VÀO CHƠI"
            int btnW = 200;
            int btnH = 38;
            int bx = cx + (cardW - btnW) / 2;
            int by = cy + cardH - btnH - 16;
            lp.btnPlayRect = new Rectangle(bx, by, btnW, btnH);

            boolean btnHover = inView && (mouseY >= levelSelectViewY && mouseY <= levelSelectViewY + levelSelectViewH)
                    && lp.btnPlayRect.contains(mouseX, mouseY);
            gCards.setColor(btnHover ? new Color(70, 190, 90) : new Color(46, 139, 87));
            gCards.fillRoundRect(bx, by, btnW, btnH, 10, 10);
            gCards.setColor(Color.WHITE);
            gCards.drawRoundRect(bx, by, btnW, btnH, 10, 10);
            gCards.setFont(new Font("Arial", Font.BOLD, 15));
            String playText = "VÀO CHƠI";
            int ptw = gCards.getFontMetrics().stringWidth(playText);
            gCards.drawString(playText, bx + (btnW - ptw) / 2, by + 24);
        }
        gCards.dispose();

        // Vẽ thanh cuộn nếu danh sách màn chơi dài vượt quá khung nhìn
        if (maxLevelScroll > 0) {
            int scrollTrackX = startX + totalW + 20;
            if (scrollTrackX + 16 > sw - 10) scrollTrackX = sw - 22;
            int scrollTrackY = levelSelectViewY + 4;
            int scrollTrackW = 12;
            levelSelectScrollTrackH = levelSelectViewH - 8;

            // Rãnh cuộn (Track)
            g.setColor(new Color(15, 25, 20, 210));
            g.fillRoundRect(scrollTrackX, scrollTrackY, scrollTrackW, levelSelectScrollTrackH, 8, 8);
            g.setColor(new Color(45, 80, 55));
            g.drawRoundRect(scrollTrackX, scrollTrackY, scrollTrackW, levelSelectScrollTrackH, 8, 8);

            // Cục trượt (Thumb)
            int thumbH = Math.max(45, (int) ((float) levelSelectViewH / contentH * levelSelectScrollTrackH));
            int scrollableRange = levelSelectScrollTrackH - thumbH;
            int thumbY = scrollTrackY + (int) ((float) levelScrollOffset / maxLevelScroll * scrollableRange);

            levelScrollTrackRect.setBounds(scrollTrackX - 4, scrollTrackY, scrollTrackW + 8, levelSelectScrollTrackH);
            levelScrollThumbRect.setBounds(scrollTrackX - 2, thumbY, scrollTrackW + 4, thumbH);

            boolean thumbHover = levelScrollThumbRect.contains(mouseX, mouseY) || isDraggingLevelScrollbar;
            g.setColor(thumbHover ? new Color(65, 175, 100) : new Color(46, 130, 80));
            g.fillRoundRect(scrollTrackX + 1, thumbY, scrollTrackW - 2, thumbH, 8, 8);
            g.setColor(thumbHover ? new Color(130, 235, 160) : new Color(75, 165, 110));
            g.drawRoundRect(scrollTrackX + 1, thumbY, scrollTrackW - 2, thumbH, 8, 8);

            // Các khấc gờ ma sát ở giữa cục trượt
            int midY = thumbY + thumbH / 2;
            g.setColor(new Color(25, 60, 40));
            g.drawLine(scrollTrackX + 3, midY - 4, scrollTrackX + scrollTrackW - 4, midY - 4);
            g.drawLine(scrollTrackX + 3, midY, scrollTrackX + scrollTrackW - 4, midY);
            g.drawLine(scrollTrackX + 3, midY + 4, scrollTrackX + scrollTrackW - 4, midY + 4);
            g.setColor(new Color(90, 190, 130, 180));
            g.drawLine(scrollTrackX + 3, midY - 3, scrollTrackX + scrollTrackW - 4, midY - 3);
            g.drawLine(scrollTrackX + 3, midY + 1, scrollTrackX + scrollTrackW - 4, midY + 1);
            g.drawLine(scrollTrackX + 3, midY + 5, scrollTrackX + scrollTrackW - 4, midY + 5);
        } else {
            levelScrollTrackRect.setBounds(0, 0, 0, 0);
            levelScrollThumbRect.setBounds(0, 0, 0, 0);
        }

        // Băng đáy (Footer) che mờ bên dưới giúp nút Quay Lại nổi bật và các thẻ bài cuộn ẩn dưới chân trang
        g.setColor(new Color(24, 34, 28));
        g.fillRect(0, sh - 85, sw, 85);
        g.setColor(new Color(40, 60, 48));
        g.drawLine(0, sh - 85, sw, sh - 85);

        // Nút quay lại Menu
        btnBack.setBounds((sw - 180) / 2, sh - 68, 180, 46);
        drawBackButton(g, btnBack, mouseX, mouseY);
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
        
        btnBack.setBounds((sw - 180) / 2, sh - 90, 180, 46);
        drawBackButton(g, btnBack, mouseX, mouseY);
    }

    public void drawBackButton(Graphics2D g, Rectangle r, int mX, int mY) {
        boolean hover = r.contains(mX, mY);
        if (btnBackPvzImg != null) {
            Image img = (hover && btnBackPvzHlImg != null) ? btnBackPvzHlImg : btnBackPvzImg;
            g.drawImage(img, r.x, r.y, r.width, r.height, null);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(hover ? Color.WHITE : new Color(225, 225, 225));
            String text = "QUAY LẠI";
            int tw = g.getFontMetrics().stringWidth(text);
            g.drawString(text, r.x + (r.width - tw) / 2, r.y + (r.height + 12) / 2);
        } else {
            drawButton(g, r, "QUAY LẠI", mX, mY);
        }
    }

    private Image loadSunIconImage() {
        Image source = com.team.gardendefense.utils.ImageLoader.getImage("src/main/resources/assets/images/sunimg.png");
        if (!(source instanceof java.awt.image.BufferedImage)) return source;

        java.awt.image.BufferedImage original = (java.awt.image.BufferedImage) source;
        int minX = original.getWidth();
        int minY = original.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int pixel = original.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                if (isSunImageBackground(red, green, blue)) continue;

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) return null;

        java.awt.image.BufferedImage cropped = new java.awt.image.BufferedImage(
                maxX - minX + 1, maxY - minY + 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                int pixel = original.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                if (isSunImageBackground(red, green, blue)) pixel = 0;
                cropped.setRGB(x - minX, y - minY, pixel);
            }
        }
        return cropped;
    }

    private boolean isSunImageBackground(int red, int green, int blue) {
        int min = Math.min(red, Math.min(green, blue));
        int max = Math.max(red, Math.max(green, blue));
        return min >= 240 && max - min <= 15;
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

        // Hiệu ứng rê xẻng lên ô cây có thể đào
        if (shovelSelected && hoverCol >= 0 && hoverCol < numCols && hoverRow >= 0 && hoverRow < numRows) {
            int hX = hoverCol * Constants.TILE_SIZE; int hY = hoverRow * Constants.TILE_SIZE;
            Plant target = getPlantAt(hoverCol, hoverRow);
            if (target != null) {
                boolean isGolden = WalletManager.getInstance().getData().goldenShovelCount > 0;
                g.setColor(isGolden ? new Color(255, 215, 0, 110) : new Color(255, 60, 60, 100));
                g.fillRoundRect(hX + 2, hY + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4, 10, 10);
                g.setColor(isGolden ? new Color(255, 235, 100) : new Color(255, 80, 80));
                g.setStroke(new BasicStroke(2.0f));
                g.drawRoundRect(hX + 2, hY + 2, Constants.TILE_SIZE - 4, Constants.TILE_SIZE - 4, 10, 10);

                int refund = target.getRefundAmount(isGolden);
                String tag = isGolden ? ("+" + refund + " Sun (Hoàn 50%)") : "Đào cây";
                g.setFont(new Font("Arial", Font.BOLD, 12));
                int tagW = g.getFontMetrics().stringWidth(tag);
                int tagX = hX + (Constants.TILE_SIZE - tagW) / 2;
                int tagY = hY - 6;

                g.setColor(new Color(0, 0, 0, 190));
                g.fillRoundRect(tagX - 5, tagY - 13, tagW + 10, 17, 6, 6);
                g.setColor(isGolden ? new Color(255, 220, 50) : Color.WHITE);
                g.drawString(tag, tagX, tagY);
            }
        }

        for(Plant p : plants) {
            p.draw(g);
            if (p.health < p.maxHealth) {
                g.setColor(Color.GREEN); g.fillRect(p.x + 10, p.y - 5, (Constants.TILE_SIZE - 20) * p.health / p.maxHealth, 4);
            }
        }
        for(Monster z : zombies) z.draw(g);
        for(ZombieLimb limb : zombieLimbs) limb.draw(g);
        for(Projectile p : projectiles) p.draw(g);

        // Lớp che tiền cảnh (cây cối trên bản đồ ở màn 2 & 3 che zombie khi mới xuất hiện, tạo hiệu ứng đi từ phía sau cây ra)
        if (grid != null) {
            grid.drawForeground(g);
        }

        // Vẽ Pet Ốc sên Stinky nếu đã mở khóa
        if (WalletManager.getInstance().getData().hasPetSnail) {
            if (petSnail == null) {
                petSnail = new PetSnail(50, getMapHeight() - 60);
            }
            petSnail.draw(g, mouseX, mouseY);
        }

        // Vẽ các đồng xu và kim cương đang rơi hoặc bay trên sân
        for(Coin c : coins) {
            c.draw(g);
        }

        // Vẽ toàn bộ các xe cắt cỏ đang chạy càn quét trên sân
        for (ActiveLawnMower m : activeMowers) {
            m.draw(g);
        }

        // Vẽ toàn bộ các cảnh báo Zombie xuất hiện (tam giác chấm than ⚠️ & mũi tên chỉ chiều đi trong 2s)
        for (ZombieSpawnAlert alert : spawnAlerts) {
            alert.draw(g, getMapWidth(), getMapHeight());
        }

        // Hiệu ứng tia ngắm chọn hàng ngang phóng Xe Cắt Cỏ
        if (mowerTargeting && hoverRow >= 0 && hoverRow < numRows) {
            int rowY = hoverRow * Constants.TILE_SIZE;
            int lawnW = numCols * Constants.TILE_SIZE;
            Graphics2D gMower = (Graphics2D) g.create();
            gMower.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            float pulse = (float) Math.sin(System.currentTimeMillis() * 0.015);
            int alpha = (int) (65 + 30 * pulse);
            gMower.setColor(new Color(255, 60, 40, alpha));
            gMower.fillRect(0, rowY, lawnW, Constants.TILE_SIZE);

            gMower.setColor(new Color(255, 220, 60, 200));
            gMower.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{12, 8}, (float)(System.currentTimeMillis() * 0.04 % 20)));
            gMower.drawLine(0, rowY + 2, lawnW, rowY + 2);
            gMower.drawLine(0, rowY + Constants.TILE_SIZE - 2, lawnW, rowY + Constants.TILE_SIZE - 2);

            // Mũi tên cảnh báo hướng chạy từ phải qua trái ◀ ◀ ◀
            gMower.setFont(new Font("Arial", Font.BOLD, 18));
            gMower.setColor(new Color(255, 240, 100, 220));
            int arrowStep = 80;
            int arrowOffset = (int) ((System.currentTimeMillis() / 25) % arrowStep);
            for (int ax = lawnW - arrowOffset; ax > 0; ax -= arrowStep) {
                gMower.drawString("◀ ◀", ax, rowY + (Constants.TILE_SIZE + 6) / 2);
            }

            // Nhãn chỉ dẫn ở cạnh phải
            String rowTag = "PHÓNG XE CẮT CỎ HÀNG " + (hoverRow + 1) + " ◀◀";
            gMower.setFont(new Font("Arial", Font.BOLD, 14));
            int rtw = gMower.getFontMetrics().stringWidth(rowTag);
            int rtx = lawnW - rtw - 20;
            int rty = rowY + (Constants.TILE_SIZE - 12) / 2;

            gMower.setColor(new Color(40, 10, 10, 230));
            gMower.fillRoundRect(rtx - 8, rty - 14, rtw + 16, 24, 8, 8);
            gMower.setColor(new Color(255, 215, 0));
            gMower.drawRoundRect(rtx - 8, rty - 14, rtw + 16, 24, 8, 8);
            gMower.setColor(Color.WHITE);
            gMower.drawString(rowTag, rtx, rty + 4);

            gMower.dispose();
        }

        // UI Header
        int sw = getScreenWidth();
        int sh = getScreenHeight();
        g.setColor(new Color(0, 0, 0, 160)); g.fillRect(0, 0, sw, 80);
        if (sunIconImg != null) {
            g.drawImage(sunIconImg, 10, 20, 40, 40, null);
        } else {
            g.setColor(new Color(255, 215, 0));
            g.fillOval(10, 20, 40, 40);
        }
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 22)); g.drawString(String.valueOf(sun), 58, 48);

        // Nút Menu ở góc trên cùng bên phải
        int menuW = 88;
        int menuX = sw - 98;
        btnInGameMenu.setBounds(menuX, 12, menuW, 32);
        drawPvZWoodButton(g, btnInGameMenu, "MENU", mouseX, mouseY);

        // Khung hiển thị Ví Tiền ở bên trái nút Menu (co giãn linh hoạt, không bao giờ tràn khung hay che nút khác)
        int coins = WalletManager.getInstance().getCoins();
        String coinStr = "$ " + String.format(java.util.Locale.US, "%,d", coins);
        Font coinFont = new Font("Arial", Font.BOLD, 15);
        FontMetrics fm = g.getFontMetrics(coinFont);
        int textW = fm.stringWidth(coinStr);
        int bankW = Math.max(125, textW + 42);
        int bankX = menuX - bankW - 12;
        int bankY = 12;
        coinBankTargetX = bankX + 17;
        coinBankTargetY = bankY + 16;

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(bankX, bankY, bankW, 32, 10, 10);
        g.setColor(new Color(255, 215, 0));
        g.drawRoundRect(bankX, bankY, bankW, 32, 10, 10);
        if (coinBankImg != null) {
            g.drawImage(coinBankImg, bankX + 4, bankY + 3, 26, 26, null);
        } else {
            g.fillOval(bankX + 6, bankY + 4, 24, 24);
        }
        g.setColor(new Color(255, 220, 50));
        g.setFont(coinFont);
        g.drawString(coinStr, bankX + 34, bankY + 22);

        // Nút Shop trong game đồng bộ kích thước và vị trí ngay dưới Ví Tiền
        btnInGameShop.setBounds(bankX, 48, bankW, 24);
        drawSmallButton(g, btnInGameShop, "CỬA HÀNG", mouseX, mouseY);

        // Hiệu ứng số tiền nảy bay lên (+10, +50, +1000)
        for (WalletManager.FloatingText ft : WalletManager.getInstance().floatingTexts) {
            Color c = ft.color != null ? ft.color : new Color(255, 225, 30);
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), ft.alpha));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(ft.text, (int) ft.x, (int) ft.y);
        }

        if (currentLevel != null) {
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(new Color(230, 230, 230));
            String lvlText = "Màn " + currentLevel.id;
            if (currentLevel.name != null && !currentLevel.name.isEmpty()) {
                lvlText += ": " + currentLevel.name;
            }
            int ltw = g.getFontMetrics().stringWidth(lvlText);
            if (ltw > menuW + 10) {
                lvlText = "Màn " + currentLevel.id;
                ltw = g.getFontMetrics().stringWidth(lvlText);
            }
            g.drawString(lvlText, menuX + (menuW - ltw) / 2, 60);
        }

        // Các nút vật phẩm hỗ trợ (Items hotbar): Bùa Đóng Băng [1], Kẹo Chocolate [2], Xẻng Đào [3], Xe Cắt Cỏ [4]
        int itemW = 50;
        int itemH = 54;
        int itemY = 12;
        int gap = 6;

        int totalHotbarW = itemW * 4 + gap * 3;
        int startHotbarX = bankX - totalHotbarW - 10;

        int iceX = startHotbarX;
        btnIceShroom.setBounds(iceX, itemY, itemW, itemH);
        drawIceShroomButton(g, btnIceShroom, mouseX, mouseY);

        int chocX = iceX + itemW + gap;
        btnChocolate.setBounds(chocX, itemY, itemW, itemH);
        drawChocolateButton(g, btnChocolate, mouseX, mouseY);

        int shovelX = chocX + itemW + gap;
        btnShovel.setBounds(shovelX, itemY, itemW, itemH);
        drawShovelButton(g, btnShovel, mouseX, mouseY);

        int mowerX = shovelX + itemW + gap;
        btnLawnMower.setBounds(mowerX, itemY, itemW, itemH);
        drawLawnMowerButton(g, btnLawnMower, mouseX, mouseY);

        // Thanh thẻ bài thực vật: Giới hạn vùng vẽ nghiêm ngặt để không bao giờ tràn ra Xe/Xẻng/Bùa/Kẹo/Bank/Shop/Screen
        int cardW = 54;
        int cardH = 68;
        int cardGap = 4;
        int cardY = 6;
        int cardAreaX = 110;
        int rightBoundary = startHotbarX - 10;
        int cardAreaW = Math.max(100, rightBoundary - cardAreaX);
        int totalCardsW = uiCards.isEmpty() ? 0 : (uiCards.size() * cardW + (uiCards.size() - 1) * cardGap);

        needsCardScroll = totalCardsW > cardAreaW;
        if (needsCardScroll) {
            btnCardLeft.setBounds(cardAreaX, 16, 22, 42);
            btnCardRight.setBounds(rightBoundary - 22, 16, 22, 42);
            cardViewX = cardAreaX + 24;
            cardViewW = cardAreaW - 48;
            maxCardScroll = Math.max(0, totalCardsW - cardViewW);
            cardScrollOffset = Math.max(0, Math.min(cardScrollOffset, maxCardScroll));
            drawCardScrollButton(g, btnCardLeft, "◀", mouseX, mouseY, cardScrollOffset > 0);
            drawCardScrollButton(g, btnCardRight, "▶", mouseX, mouseY, cardScrollOffset < maxCardScroll);
        } else {
            btnCardLeft.setBounds(0, 0, 0, 0);
            btnCardRight.setBounds(0, 0, 0, 0);
            cardViewX = cardAreaX;
            cardViewW = cardAreaW;
            maxCardScroll = 0;
            cardScrollOffset = 0;
        }

        Shape origClip = g.getClip();
        g.setClip(cardViewX, 0, cardViewW, 80);

        for (int i = 0; i < uiCards.size(); i++) {
            PlantCard c = uiCards.get(i);
            int cx = cardViewX - cardScrollOffset + i * (cardW + cardGap);
            c.rect.setBounds(cx, cardY, cardW, cardH);

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
                g.fillRect(c.rect.x + 2, c.rect.y + 52, c.rect.width - 4, 14);
                
                // Vẽ mặt trời nhỏ
                g.setColor(new Color(255, 215, 0));
                g.fillOval(c.rect.x + 36, c.rect.y + 54, 10, 10);
                g.setColor(Color.BLACK);
                g.drawOval(c.rect.x + 36, c.rect.y + 54, 10, 10);
                
                // Vẽ giá tiền
                g.setFont(new Font("Arial", Font.BOLD, 11));
                g.drawString(c.price, c.rect.x + 6, c.rect.y + 63);
            } else {
                g.setColor((selectedPlant == c.id) ? Color.YELLOW : new Color(80, 80, 80));
                g.fillRect(c.rect.x, c.rect.y, c.rect.width, c.rect.height);
                g.setColor(c.color != null ? c.color : Color.GREEN);
                g.fillOval(c.rect.x + 12, c.rect.y + 5, 30, 30);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString(c.price, c.rect.x + 12, c.rect.y + 50);
            }

            // Lớp phủ thiếu ánh sáng mặt trời (khi không trong thời gian hồi chiêu)
            int cardPrice = 0;
            try { cardPrice = Integer.parseInt(c.price); } catch (Exception ignored) {}
            if (c.currentCooldown <= 0 && sun < cardPrice) {
                g.setColor(new Color(0, 0, 0, 110));
                g.fillRect(c.rect.x, c.rect.y, c.rect.width, c.rect.height);
            }

            // Hiệu ứng Countdown hồi chiêu sau khi trồng cây
            if (c.currentCooldown > 0) {
                float cdRatio = (float) c.currentCooldown / (float) c.maxCooldown;
                int shadeH = (int) Math.ceil(c.rect.height * cdRatio);

                // Lớp phủ tối đen bán trong suốt quét từ trên xuống
                g.setColor(new Color(0, 0, 0, 185));
                g.fillRect(c.rect.x, c.rect.y, c.rect.width, shadeH);

                // Đường viền quét sáng ở mép đếm lùi
                g.setColor(new Color(255, 255, 255, 140));
                g.drawLine(c.rect.x, c.rect.y + shadeH, c.rect.x + c.rect.width, c.rect.y + shadeH);

                // Hiển thị số giây còn lại (ví dụ: 7.5s, 6.2s, 15s...)
                float secLeft = c.currentCooldown / 60.0f;
                String cdStr = (secLeft >= 10.0f) ? String.format(java.util.Locale.US, "%.0fs", secLeft)
                        : String.format(java.util.Locale.US, "%.1fs", secLeft);

                Font cdFont = new Font("Arial", Font.BOLD, 13);
                g.setFont(cdFont);
                FontMetrics cdFm = g.getFontMetrics(cdFont);
                int cdW = cdFm.stringWidth(cdStr);
                int textX = c.rect.x + (c.rect.width - cdW) / 2;
                int textY = c.rect.y + (c.rect.height + 6) / 2;

                // Viền bóng chữ đen
                g.setColor(Color.BLACK);
                g.drawString(cdStr, textX - 1, textY);
                g.drawString(cdStr, textX + 1, textY);
                g.drawString(cdStr, textX, textY - 1);
                g.drawString(cdStr, textX, textY + 1);

                // Chữ chính màu vàng sáng
                g.setColor(new Color(255, 235, 120));
                g.drawString(cdStr, textX, textY);
            }
        }
        g.setClip(origClip);

        // Hiệu ứng sóng băng tuyết toàn sân của Bùa Đóng Băng
        if (iceShroomDuration > 0) {
            drawIceShroomScreenEffect(g, sw, sh);
        }

        // Vẽ xẻng bay theo con trỏ chuột khi đang cầm xẻng đào cây
        if (shovelSelected) {
            boolean isGolden = WalletManager.getInstance().getData().hasGoldenShovel;
            int sx = mouseX - 18;
            int sy = mouseY - 32;
            if (isGolden) {
                float pulse = (float) Math.sin(System.currentTimeMillis() * 0.015);
                g.setColor(new Color(255, 215, 0, (int)(80 + 35 * pulse)));
                g.fillOval(sx - 4, sy - 4, 44, 44);
            }
            if (shovelImg != null) {
                g.drawImage(shovelImg, sx, sy, 36, 36, null);
            } else {
                g.setColor(new Color(180, 140, 60));
                g.fillRoundRect(mouseX - 10, mouseY - 10, 20, 20, 4, 4);
            }
            if (isGolden) {
                int spX = sx + 26;
                int spY = sy + 8;
                g.setColor(Color.YELLOW);
                g.drawLine(spX - 3, spY, spX + 3, spY);
                g.drawLine(spX, spY - 3, spX, spY + 3);
            }
        }
    }

    private void drawCardScrollButton(Graphics2D g, Rectangle r, String arrow, int mX, int mY, boolean enabled) {
        if (r.width <= 0) return;
        boolean hover = enabled && r.contains(mX, mY);
        boolean isLeft = "◀".equals(arrow);

        Image img = null;
        if (isLeft) {
            if (!enabled) img = scrollLeftDisImg;
            else if (hover) img = scrollLeftHlImg;
            else img = scrollLeftImg;
        } else {
            if (!enabled) img = scrollRightDisImg;
            else if (hover) img = scrollRightHlImg;
            else img = scrollRightImg;
        }

        if (img != null) {
            g.drawImage(img, r.x, r.y, r.width, r.height, null);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color bg = !enabled ? new Color(40, 40, 40, 180) : (hover ? new Color(80, 160, 60) : new Color(45, 95, 45));
        g2.setColor(bg);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 6, 6);
        g2.setColor(enabled ? (hover ? Color.YELLOW : new Color(150, 220, 130)) : new Color(90, 90, 90));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 6, 6);
        
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        int strW = g2.getFontMetrics().stringWidth(arrow);
        g2.setColor(enabled ? Color.WHITE : new Color(120, 120, 120));
        g2.drawString(arrow, r.x + (r.width - strW) / 2, r.y + (r.height + 8) / 2);
        g2.dispose();
    }

    private void drawGameOver(Graphics2D g) {
        int sw = getScreenWidth();
        int sh = getScreenHeight();
        g.setColor(new Color(0, 0, 0, 195)); g.fillRect(0, 0, sw, sh);

        int dw = Math.min((int) (sw * 0.68), (int) (sh * 0.96));
        int dh = (int) (dw * 1792.0 / 2400.0);
        dh = Math.min(dh, (int) (sh * 0.80));
        dw = (int) (dh * 2400.0 / 1792.0);
        int dx = (sw - dw) / 2;
        int dy = (sh - dh) / 2;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameOverBoardImg != null) {
            g2.drawImage(gameOverBoardImg, dx, dy, dw, dh, null);
        } else {
            GradientPaint stoneGrad = new GradientPaint(dx, dy, new Color(45, 30, 25),
                    dx, dy + dh, new Color(25, 15, 12));
            g2.setPaint(stoneGrad);
            g2.fillRoundRect(dx, dy, dw, dh, 24, 24);
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(160, 60, 50));
            g2.drawRoundRect(dx + 2, dy + 2, dw - 4, dh - 4, 22, 22);
            g2.setFont(new Font("Serif", Font.BOLD, 32));
            g2.setColor(new Color(255, 60, 50));
            String title = "THE ZOMBIES ATE YOUR BRAINS!";
            int titleWidth = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (sw - titleWidth) / 2, dy + 60);
        }

        int bw = Math.min((int) (dw * 0.34), 500);
        int bh = gameOverChoiceBars != null && gameOverChoiceBars.length > 0 && gameOverChoiceBars[0] != null
                ? Math.max(1, bw * gameOverChoiceBars[0].getHeight(null)
                        / gameOverChoiceBars[0].getWidth(null))
                : 44;
        int bx = (sw - bw) / 2;
        int buttonGap = Math.max(4, bh / 12);
        int by = dy + (int) (dh * 0.40);

        btnRestart.setBounds(bx, by, bw, bh);
        drawGameOverChoiceBar(g2, btnRestart, 0, "CHƠI LẠI TỪ ĐẦU");
        by += bh + buttonGap;

        btnLevelSelectGW.setBounds(bx, by, bw, bh);
        drawGameOverChoiceBar(g2, btnLevelSelectGW, 1, "CHỌN MÀN KHÁC");
        by += bh + buttonGap;

        btnExitGO.setBounds(bx, by, bw, bh);
        drawGameOverChoiceBar(g2, btnExitGO, 2, "VỀ MENU CHÍNH");

        g2.dispose();
    }

    private Image[] loadGameOverChoiceBars() {
        Image source = com.team.gardendefense.utils.ImageLoader.getImage(
                "src/main/resources/assets/images/losebars.png");
        if (!(source instanceof java.awt.image.BufferedImage)) return new Image[0];

        java.awt.image.BufferedImage sheet = (java.awt.image.BufferedImage) source;
        int barHeight = sheet.getHeight() / 3;
        Image[] bars = new Image[3];
        for (int i = 0; i < bars.length; i++) {
            int y = i * barHeight;
            int height = i == bars.length - 1 ? sheet.getHeight() - y : barHeight;
            bars[i] = sheet.getSubimage(0, y, sheet.getWidth(), height);
        }
        return bars;
    }

    private void drawGameOverChoiceBar(Graphics2D g, Rectangle bounds, int index, String fallbackText) {
        if (gameOverChoiceBars != null && index >= 0 && index < gameOverChoiceBars.length && gameOverChoiceBars[index] != null) {
            g.drawImage(gameOverChoiceBars[index], bounds.x, bounds.y,
                    bounds.width, bounds.height, null);
            if (bounds.contains(mouseX, mouseY)) {
                g.setColor(new Color(255, 255, 255, 38));
                g.fillRoundRect(bounds.x + 5, bounds.y + 4,
                        bounds.width - 10, bounds.height - 8, 10, 10);
            }
        } else {
            drawButton(g, bounds, fallbackText, mouseX, mouseY);
        }
    }

    private void drawGameWon(Graphics2D g) {
        int sw = getScreenWidth();
        int sh = getScreenHeight();
        g.setColor(new Color(0, 0, 0, 190)); g.fillRect(0, 0, sw, sh);

        int dw = Math.min((int) (sw * 0.88), (int) (sh * 1.25));
        int dh = (int) (dw * 1792.0 / 2400.0);
        dh = Math.min(dh, (int) (sh * 0.82));
        dw = (int) (dh * 2400.0 / 1792.0);
        int dx = (sw - dw) / 2;
        int dy = (sh - dh) / 2 - 12;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (victoryBoardImg != null) {
            g2.drawImage(victoryBoardImg, dx, dy, dw, dh, null);
        } else {
            GradientPaint fallback = new GradientPaint(dx, dy, new Color(30, 48, 32),
                    dx, dy + dh, new Color(15, 28, 18));
            g2.setPaint(fallback);
            g2.fillRoundRect(dx, dy, dw, dh, 24, 24);
        }

        int bw = Math.max(220, Math.min((int) (dw * 0.44), 300));
        int bh = Math.max(42, Math.min((int) (dh * 0.082), 50));
        int bx = (sw - bw) / 2;
        int buttonGap = Math.max(8, (int) (bh * 0.18));
        int boardBottomInset = (int) (dh * 0.14);
        int firstButtonY = dy + (int) (dh * 0.48);
        int lowestButtonBottom = firstButtonY + (bh + buttonGap) * 3 + bh;
        int by = lowestButtonBottom > dy + dh - boardBottomInset
                ? dy + dh - boardBottomInset - ((bh + buttonGap) * 3 + bh)
                : firstButtonY;
        by -= 16;

        boolean hasNext = hasLevel(currentLevelId + 1);
        if (hasNext) {
            btnNextLevel.setBounds(bx, by, bw, bh);
            drawVictoryChoiceBar(g2, btnNextLevel, 0);
            by += bh + buttonGap;
        }

        btnRestart.setBounds(bx, by, bw, bh);
        drawVictoryChoiceBar(g2, btnRestart, 1);
        by += bh + buttonGap;

        btnLevelSelectGW.setBounds(bx, by, bw, bh);
        drawVictoryChoiceBar(g2, btnLevelSelectGW, 2);
        by += bh + buttonGap;

        btnExitGO.setBounds(bx, by, bw, bh);
        drawVictoryChoiceBar(g2, btnExitGO, 3);

        g2.dispose();
    }

    private Image[] loadVictoryChoiceBars() {
        Image source = com.team.gardendefense.utils.ImageLoader.getImage(
                "src/main/resources/assets/images/victorychoicebars.png");
        if (!(source instanceof java.awt.image.BufferedImage)) return new Image[0];

        java.awt.image.BufferedImage sheet = (java.awt.image.BufferedImage) source;
        int barX = (int) (sheet.getWidth() * 0.075);
        int barWidth = (int) (sheet.getWidth() * 0.850);
        int barHeight = (int) (sheet.getHeight() * 0.210);
        int[] barYs = {
                (int) (sheet.getHeight() * 0.035),
                (int) (sheet.getHeight() * 0.278),
                (int) (sheet.getHeight() * 0.518),
                (int) (sheet.getHeight() * 0.758)
        };

        Image[] bars = new Image[4];
        for (int i = 0; i < bars.length; i++) {
            int height = Math.min(barHeight, sheet.getHeight() - barYs[i]);
            bars[i] = sheet.getSubimage(barX, barYs[i], barWidth, height);
        }
        return bars;
    }

    private void drawVictoryChoiceBar(Graphics2D g, Rectangle bounds, int index) {
        if (victoryChoiceBars != null && index >= 0 && index < victoryChoiceBars.length
                && victoryChoiceBars[index] != null) {
            g.drawImage(victoryChoiceBars[index], bounds.x, bounds.y,
                    bounds.width, bounds.height, null);
            if (bounds.contains(mouseX, mouseY)) {
                g.setColor(new Color(255, 255, 255, 38));
                g.fillRoundRect(bounds.x + 5, bounds.y + 4,
                        bounds.width - 10, bounds.height - 8, 10, 10);
            }
        } else {
            drawButton(g, bounds, "THAO TÁC", mouseX, mouseY);
        }
    }

    private Image[] loadMainMenuBarImages() {
        Image source = com.team.gardendefense.utils.ImageLoader.getImage(
                "src/main/resources/assets/images/mainmenubar.png");
        if (!(source instanceof java.awt.image.BufferedImage)) return new Image[0];

        java.awt.image.BufferedImage sheet = (java.awt.image.BufferedImage) source;
        int[][] bounds = {
                {640, 65, 1080, 265},
                {640, 344, 1080, 265},
                {610, 615, 1110, 270},
                {610, 900, 1090, 265},
                {580, 1163, 1140, 278},
                {566, 1455, 1128, 265}
        };
        Image[] bars = new Image[bounds.length];
        for (int i = 0; i < bounds.length; i++) {
            int x = bounds[i][0];
            int y = bounds[i][1];
            int width = Math.min(bounds[i][2], sheet.getWidth() - x);
            int height = Math.min(bounds[i][3], sheet.getHeight() - y);
            bars[i] = removeMenuBarBackground(sheet.getSubimage(x, y, width, height));
        }
        return bars;
    }

    private Image removeMenuBarBackground(java.awt.image.BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        java.awt.image.BufferedImage cleaned = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = cleaned.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();

        int[] queue = new int[width * height];
        int queueEnd = 0;
        for (int x = 0; x < width; x++) {
            queueEnd = enqueueMenuBarBackground(cleaned, x, queue, queueEnd);
            queueEnd = enqueueMenuBarBackground(cleaned, (height - 1) * width + x, queue, queueEnd);
        }
        for (int y = 1; y < height - 1; y++) {
            queueEnd = enqueueMenuBarBackground(cleaned, y * width, queue, queueEnd);
            queueEnd = enqueueMenuBarBackground(cleaned, y * width + width - 1, queue, queueEnd);
        }
        for (int i = 0; i < queueEnd; i++) {
            int pixelIndex = queue[i];
            int x = pixelIndex % width;
            int y = pixelIndex / width;
            if (x > 0) queueEnd = enqueueMenuBarBackground(cleaned, pixelIndex - 1, queue, queueEnd);
            if (x + 1 < width) queueEnd = enqueueMenuBarBackground(cleaned, pixelIndex + 1, queue, queueEnd);
            if (y > 0) queueEnd = enqueueMenuBarBackground(cleaned, pixelIndex - width, queue, queueEnd);
            if (y + 1 < height) queueEnd = enqueueMenuBarBackground(cleaned, pixelIndex + width, queue, queueEnd);
        }

        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((cleaned.getRGB(x, y) >>> 24) == 0) continue;
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) return cleaned;
        return cleaned.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private int enqueueMenuBarBackground(
            java.awt.image.BufferedImage image, int pixelIndex, int[] queue, int queueEnd) {
        int x = pixelIndex % image.getWidth();
        int y = pixelIndex / image.getWidth();
        int pixel = image.getRGB(x, y);
        if ((pixel >>> 24) == 0) return queueEnd;

        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = pixel & 0xff;
        int min = Math.min(red, Math.min(green, blue));
        int max = Math.max(red, Math.max(green, blue));
        if (min < 170 || max - min > 24 || Math.abs(red - 210) > 40
                || Math.abs(green - 210) > 40 || Math.abs(blue - 210) > 40) {
            return queueEnd;
        }

        image.setRGB(x, y, 0);
        queue[queueEnd] = pixelIndex;
        return queueEnd + 1;
    }

    private void drawMainMenuChoice(
            Graphics2D g, Rectangle bounds, int imageIndex, String fallbackText,
            int mX, int mY, Color fallbackColor) {
        if (mainMenuBarImages != null && imageIndex < mainMenuBarImages.length && mainMenuBarImages[imageIndex] != null) {
            g.drawImage(mainMenuBarImages[imageIndex], bounds.x, bounds.y, bounds.width, bounds.height, null);
            if (bounds.contains(mX, mY)) {
                g.setColor(new Color(255, 255, 255, 35));
                g.fillRoundRect(bounds.x + 5, bounds.y + 3, bounds.width - 10, bounds.height - 6, 12, 12);
            }
        } else {
            drawButton(g, bounds, fallbackText, mX, mY, fallbackColor);
        }
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
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint bg = new GradientPaint(
            r.x, r.y, hover ? new Color(185, 125, 45) : new Color(140, 85, 30),
            r.x, r.y + r.height, hover ? new Color(135, 80, 25) : new Color(95, 50, 15)
        );
        g2.setPaint(bg);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);

        g2.setColor(hover ? new Color(255, 230, 100) : new Color(210, 165, 70));
        g2.setStroke(new BasicStroke(hover ? 1.8f : 1.2f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);

        g2.setFont(new Font("Arial", Font.BOLD, 12));
        int tw = g2.getFontMetrics().stringWidth(text);
        int tx = r.x + (r.width - tw) / 2;
        int ty = r.y + (r.height + 9) / 2;

        g2.setColor(Color.BLACK);
        g2.drawString(text, tx + 1, ty + 1);
        g2.setColor(hover ? Color.WHITE : new Color(255, 235, 140));
        g2.drawString(text, tx, ty);
        g2.dispose();
    }
    
    private boolean isPlanted(int col, int row) {
        for(Plant p : plants) if (p.x / Constants.TILE_SIZE == col && p.y / Constants.TILE_SIZE == row) return true;
        return false;
    }

    public Plant getPlantAt(int col, int row) {
        for(Plant p : plants) {
            if (p.x / Constants.TILE_SIZE == col && p.y / Constants.TILE_SIZE == row) return p;
        }
        return null;
    }

    /**
     * Hủy bỏ toàn bộ các trạng thái ngắm/chọn công cụ trên tay (Cây, Xẻng, Xe cắt cỏ, Phân bón...)
     * Đảm bảo các chế độ chọn là độc quyền (mutually exclusive) và không bao giờ bị dùng cùng lúc đè lên nhau.
     */
    public void cancelAllTargetingModes() {
        selectedPlant = 0;
        shovelSelected = false;
        mowerTargeting = false;
        // Bất kỳ công cụ/chế độ ngắm tương lai nào cũng sẽ được tự động dọn dẹp tại đây
    }

    public void toggleShovel() {
        if (state != GameState.PLAYING) return;
        WalletManager wm = WalletManager.getInstance();

        if (shovelCooldown > 0) {
            float secLeft = shovelCooldown / 60.0f;
            wm.floatingTexts.add(new WalletManager.FloatingText(
                String.format(java.util.Locale.US, "Xẻng đang hồi chiêu! (%.1fs)", secLeft), mouseX, mouseY, new Color(255, 140, 0)));
            AudioManager.getInstance().playBuzzerSound();
            return;
        }

        if (wm.getData().goldenShovelCount <= 0) {
            wm.floatingTexts.add(new WalletManager.FloatingText(
                "Hết lượt dùng Xẻng! Mua thêm trong Shop ($ " + ShopManager.PRICE_GOLDEN_SHOVEL + ")", mouseX, mouseY, new Color(255, 140, 0)));
            AudioManager.getInstance().playBuySound();
            openShop(GameState.PLAYING);
            return;
        }

        boolean willActivate = !shovelSelected;
        cancelAllTargetingModes();
        if (willActivate) {
            shovelSelected = true;
            AudioManager.getInstance().playShovelSound();
        }
    }

    public void handleRightClick(int mX, int mY) {
        if (state == GameState.PLAYING) {
            if (mowerTargeting || shovelSelected || selectedPlant > 0) {
                cancelAllTargetingModes();
                return;
            }
        }
    }

    public void digPlant(Plant p) {
        if (p == null) return;
        WalletManager wm = WalletManager.getInstance();
        if (wm.getData().goldenShovelCount <= 0) {
            cancelAllTargetingModes();
            return;
        }

        // Tiêu hao 1 lượt dùng xẻng
        wm.getData().goldenShovelCount--;
        wm.save();
        shovelCooldown = CD_SHOVEL;
        shovelSelected = false;

        p.takeDamage(999999); // Đánh dấu cây đã chết để zombie lập tức nhận biết isDead()
        plants.remove(p);

        // Hủy ngay lập tức mục tiêu cắn của toàn bộ zombie đang gặm cây này
        for (Monster z : zombies) {
            if (z.attackTarget == p) {
                z.attackTarget = null;
            }
        }

        int refund = p.getRefundAmount(true);
        sun += refund;
        int remaining = wm.getData().goldenShovelCount;
        wm.floatingTexts.add(new WalletManager.FloatingText(
            "+" + refund + " Sun (Còn " + remaining + " lượt)", p.x + 8, p.y - 12, new Color(255, 220, 50)));
        AudioManager.getInstance().playGoldenShovelSound();

        // Nếu đã hết lượt dùng thì tự động cất xẻng
        if (remaining <= 0) {
            cancelAllTargetingModes();
        }
    }

    public void handleClick(int mX, int mY) {
        if (state == GameState.MENU) {
            if (btnPlay.contains(mX, mY)) {
                if (currentLevel == null && !loadLevel("/data/level_1.json")) return;
                if (currentLevel != null) resetGame();
            }
            else if (btnLevelSelect.contains(mX, mY)) {
                openLevelSelect();
            }
            else if (btnShop.contains(mX, mY)) {
                openShop();
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

            // Click trên thanh cuộn (page up / page down khi click vào rãnh ngoài thumb)
            if (maxLevelScroll > 0 && levelScrollTrackRect != null && levelScrollTrackRect.contains(mX, mY)) {
                if (levelScrollThumbRect != null && !levelScrollThumbRect.contains(mX, mY)) {
                    if (mY < levelScrollThumbRect.y) {
                        levelScrollOffset = Math.max(0, levelScrollOffset - 180);
                    } else {
                        levelScrollOffset = Math.min(maxLevelScroll, levelScrollOffset + 180);
                    }
                    return;
                }
            }

            // Chỉ cho phép click vào màn chơi nếu con trỏ nằm trong khung viewport
            if (mY >= levelSelectViewY && mY <= levelSelectViewY + levelSelectViewH) {
                for (LevelPreview lp : availableLevels) {
                    if ((lp.btnPlayRect != null && lp.btnPlayRect.contains(mX, mY)) ||
                        (lp.cardRect != null && lp.cardRect.contains(mX, mY))) {
                        startLevel(lp.id);
                        return;
                    }
                }
            }
            return;
        } else if (state == GameState.INSTRUCTIONS) {
            if (btnBack.contains(mX, mY)) state = GameState.MENU;
            return;
        } else if (state == GameState.GAMEOVER) {
            if (btnRestart.contains(mX, mY)) resetGame();
            else if (btnLevelSelectGW.contains(mX, mY)) {
                openLevelSelect();
            }
            else if (btnExitGO.contains(mX, mY)) {
                state = GameState.MENU;
                if (panel != null) panel.resizeForLevel(Constants.COLS, Constants.ROWS);
            }
            else collectCoinAt(mX, mY);
            return;
        } else if (state == GameState.GAMEWON) {
            if (hasLevel(currentLevelId + 1) && btnNextLevel.contains(mX, mY)) {
                startLevel(currentLevelId + 1);
            }
            else if (btnRestart.contains(mX, mY)) resetGame();
            else if (btnLevelSelectGW.contains(mX, mY)) {
                openLevelSelect();
            }
            else if (btnExitGO.contains(mX, mY)) {
                state = GameState.MENU;
                if (panel != null) panel.resizeForLevel(Constants.COLS, Constants.ROWS);
            }
            else collectCoinAt(mX, mY);
            return;
        }

        if (state == GameState.PLAYING) {
            if (btnInGameMenu.contains(mX, mY)) {
                AudioManager.getInstance().playBuySound();
                state = GameState.PAUSED;
                return;
            }
            if (btnInGameShop.contains(mX, mY)) {
                openShop();
                return;
            }
            if (btnIceShroom.contains(mX, mY)) {
                cancelAllTargetingModes();
                activateIceShroom();
                return;
            }
            if (btnChocolate.contains(mX, mY)) {
                cancelAllTargetingModes();
                activateChocolate();
                return;
            }
            if (btnShovel.contains(mX, mY)) {
                toggleShovel();
                return;
            }
            if (btnLawnMower.contains(mX, mY)) {
                activateLawnMowerItem();
                return;
            }
            if (petSnail != null && WalletManager.getInstance().getData().hasPetSnail && petSnail.getBounds().contains(mX, mY)) {
                cancelAllTargetingModes();
                activateChocolate();
                return;
            }

            // Click trực tiếp vào đồng xu để nhặt
            if (collectCoinAt(mX, mY)) return;

            // Click nút cuộn thanh thẻ bài
            if (needsCardScroll) {
                if (btnCardLeft.contains(mX, mY)) {
                    AudioManager.getInstance().playBuySound();
                    cardScrollOffset = Math.max(0, cardScrollOffset - (54 + 4) * 2);
                    return;
                }
                if (btnCardRight.contains(mX, mY)) {
                    AudioManager.getInstance().playBuySound();
                    cardScrollOffset = Math.min(maxCardScroll, cardScrollOffset + (54 + 4) * 2);
                    return;
                }
            }

            for(PlantCard c : uiCards) {
                if (c.rect.contains(mX, mY)) {
                    if (!needsCardScroll || (mX >= cardViewX && mX <= cardViewX + cardViewW)) {
                        if (c.currentCooldown > 0) {
                            // Đang trong thời gian đếm ngược (cooldown), không cho chọn
                            AudioManager.getInstance().playBuySound();
                            return;
                        }
                        boolean wasSelected = (selectedPlant == c.id);
                        cancelAllTargetingModes();
                        if (!wasSelected) {
                            selectedPlant = c.id;
                        }
                        return;
                    }
                }
            }

            if (mowerTargeting) {
                int row = mY / Constants.TILE_SIZE;
                if (row >= 0 && row < getRows()) {
                    launchLawnMower(row);
                }
                cancelAllTargetingModes();
                return;
            }

            if (shovelSelected) {
                int col = mX / Constants.TILE_SIZE;
                int row = mY / Constants.TILE_SIZE;
                Plant target = getPlantAt(col, row);
                if (target != null) {
                    digPlant(target);
                }
                cancelAllTargetingModes();
                return;
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

                if (!canPlant) { cancelAllTargetingModes(); return; }

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
                        // Kích hoạt thời gian hồi chiêu (countdown) cho thẻ bài vừa trồng
                        for (PlantCard card : uiCards) {
                            if (card.id == selectedPlant) {
                                card.currentCooldown = card.maxCooldown;
                                break;
                            }
                        }
                    }
                    cancelAllTargetingModes(); 
                } else { cancelAllTargetingModes(); }
            }
        }
    }

    public void handleMouseWheel(int rotation, int mX, int mY) {
        if (state == GameState.PLAYING && needsCardScroll) {
            if (mY <= 85) {
                cardScrollOffset = Math.max(0, Math.min(maxCardScroll, cardScrollOffset + rotation * 32));
            }
        } else if (state == GameState.LEVEL_SELECT && maxLevelScroll > 0) {
            levelScrollOffset = Math.max(0, Math.min(maxLevelScroll, levelScrollOffset + rotation * 48));
        }
    }

    public void handleMousePressed(int mX, int mY) {
        if (state == GameState.LEVEL_SELECT && maxLevelScroll > 0) {
            if (levelScrollThumbRect != null && levelScrollThumbRect.contains(mX, mY)) {
                isDraggingLevelScrollbar = true;
                dragStartY = mY;
                dragStartOffset = levelScrollOffset;
            }
        }
    }

    public void handleMouseDragged(int mX, int mY) {
        if (state == GameState.LEVEL_SELECT && isDraggingLevelScrollbar && maxLevelScroll > 0) {
            int deltaY = mY - dragStartY;
            int trackScrollableH = levelSelectScrollTrackH - levelScrollThumbRect.height;
            if (trackScrollableH > 0) {
                int newOffset = dragStartOffset + (int) ((float) deltaY / trackScrollableH * maxLevelScroll);
                levelScrollOffset = Math.max(0, Math.min(maxLevelScroll, newOffset));
            }
        }
    }

    public void handleMouseReleased(int mX, int mY) {
        isDraggingLevelScrollbar = false;
    }

    public void activateIceShroom() {
        if (state != GameState.PLAYING) return;
        WalletManager wm = WalletManager.getInstance();

        if (iceShroomCooldown > 0) {
            float secLeft = iceShroomCooldown / 60.0f;
            String cdStr = (secLeft >= 10.0f) ? String.format(java.util.Locale.US, "%.0fs", secLeft)
                    : String.format(java.util.Locale.US, "%.1fs", secLeft);
            wm.floatingTexts.add(new WalletManager.FloatingText(
                "Bùa Đóng Băng đang hồi chiêu! (" + cdStr + ")", mouseX, mouseY, new Color(255, 140, 0)));
            AudioManager.getInstance().playBuzzerSound();
            return;
        }

        if (wm.getData().iceShroomBlastCount <= 0) {
            wm.floatingTexts.add(new WalletManager.FloatingText("Hết Bùa Đóng Băng! Mở Cửa Hàng để mua thêm", mouseX, mouseY, new Color(255, 140, 0)));
            AudioManager.getInstance().playBuySound();
            openShop(GameState.PLAYING);
            return;
        }

        // Tiêu hao 1 bùa
        wm.getData().iceShroomBlastCount--;
        wm.save();
        iceShroomCooldown = CD_ICE_SHROOM;

        // Kích hoạt hiệu ứng đóng băng toàn sân trong 5 giây (300 ticks tại 60 FPS)
        iceShroomDuration = 300;
        iceShroomWaveRadius = 0;
        AudioManager.getInstance().playFreezeSound();

        // Đóng băng toàn bộ zombie hiện có trên sân
        for (Monster z : zombies) {
            z.applyFreeze(300, true);
        }

        wm.floatingTexts.add(new WalletManager.FloatingText("BÙA ĐÓNG BĂNG! (5s)", getScreenWidth() / 2, getScreenHeight() / 2 - 40, Color.CYAN));
    }

    public void activateChocolate() {
        if (state != GameState.PLAYING) return;
        WalletManager wm = WalletManager.getInstance();

        if (chocolateCooldown > 0 || (petSnail != null && petSnail.isBoosted())) {
            int secLeft = (petSnail != null && petSnail.isBoosted()) ? petSnail.getRemainingSeconds() : ((chocolateCooldown + 59) / 60);
            wm.floatingTexts.add(new WalletManager.FloatingText(
                "Pet đang có buff tốc độ! (Còn " + secLeft + "s)", mouseX, mouseY, new Color(255, 140, 0)));
            AudioManager.getInstance().playBuzzerSound();
            return;
        }

        // 1. Kiểm tra đã mở khóa Pet Ốc Sên chưa
        if (!wm.getData().hasPetSnail) {
            wm.floatingTexts.add(new WalletManager.FloatingText(
                "Chưa có Pet Ốc Sên! Mua Pet trong Cửa Hàng trước", mouseX, mouseY, new Color(255, 140, 0)));
            AudioManager.getInstance().playBuySound();
            openShop(GameState.PLAYING);
            return;
        }

        // 2. Kiểm tra số lượng kẹo Chocolate
        if (wm.getData().chocolateCount <= 0) {
            wm.floatingTexts.add(new WalletManager.FloatingText(
                "Hết Kẹo Chocolate! Mua thêm trong Cửa Hàng", mouseX, mouseY, new Color(255, 140, 0)));
            AudioManager.getInstance().playBuySound();
            openShop(GameState.PLAYING);
            return;
        }

        // 3. Đảm bảo PetSnail đã được khởi tạo
        if (petSnail == null) {
            petSnail = new PetSnail(50, getMapHeight() - 60);
        }

        // 4. Trừ 1 kẹo và kích hoạt tăng tốc cho Pet
        wm.getData().chocolateCount--;
        wm.save();
        petSnail.feedChocolate();
        chocolateCooldown = CD_CHOCOLATE;

        // 5. Âm thanh và thông báo nổi
        AudioManager.getInstance().playPowerUpSound();
        int spawnX = (int) petSnail.x;
        int spawnY = Math.max(30, (int) petSnail.y - 30);
        wm.floatingTexts.add(new WalletManager.FloatingText(
            "PET TĂNG TỐC 250%! (3 PHÚT)", 
            spawnX, spawnY, 
            new Color(255, 215, 0)));
    }

    public void activateLawnMowerItem() {
        if (state != GameState.PLAYING) return;
        WalletManager wm = WalletManager.getInstance();

        if (lawnMowerCooldown > 0) {
            float secLeft = lawnMowerCooldown / 60.0f;
            String cdStr = (secLeft >= 10.0f) ? String.format(java.util.Locale.US, "%.0fs", secLeft)
                    : String.format(java.util.Locale.US, "%.1fs", secLeft);
            wm.floatingTexts.add(new WalletManager.FloatingText(
                "Xe Cắt Cỏ đang hồi chiêu! (" + cdStr + ")", mouseX, mouseY, new Color(255, 140, 0)));
            AudioManager.getInstance().playBuzzerSound();
            return;
        }

        if (wm.getData().lawnMowerBackupCount <= 0) {
            cancelAllTargetingModes();
            wm.floatingTexts.add(new WalletManager.FloatingText(
                "Hết Xe Cắt Cỏ! Mở Cửa Hàng để mua thêm", mouseX, mouseY, new Color(255, 140, 0)));
            AudioManager.getInstance().playBuySound();
            openShop(GameState.PLAYING);
            return;
        }

        boolean willActivate = !mowerTargeting;
        cancelAllTargetingModes();
        if (willActivate) {
            mowerTargeting = true;
            AudioManager.getInstance().playMowerRevSound();
            wm.floatingTexts.add(new WalletManager.FloatingText(
                "CHỌN 1 HÀNG ĐỂ PHÓNG XE! (Chuột phải để hủy)", getScreenWidth() / 2, 100, Color.CYAN));
        }
    }

    public void launchLawnMower(int row) {
        WalletManager wm = WalletManager.getInstance();
        if (wm.getData().lawnMowerBackupCount <= 0) return;

        wm.getData().lawnMowerBackupCount--;
        wm.save();
        lawnMowerCooldown = CD_LAWN_MOWER;
        mowerTargeting = false;

        float startX = getCols() * Constants.TILE_SIZE + 30f;
        float endX = -90f;
        activeMowers.add(new ActiveLawnMower(row, startX, endX));
        AudioManager.getInstance().playMowerLaunchSound();

        int notifyX = (int) (getCols() * Constants.TILE_SIZE * 0.6f);
        int notifyY = row * Constants.TILE_SIZE + 20;
        wm.floatingTexts.add(new WalletManager.FloatingText(
            "XE CẮT CỎ XUẤT KÍCH! HÀNG " + (row + 1), notifyX, notifyY, new Color(255, 215, 0)));
    }

    private void drawIceShroomButton(Graphics2D g, Rectangle r, int mX, int mY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean hover = r.contains(mX, mY);
        int count = WalletManager.getInstance().getData().iceShroomBlastCount;

        // Nền nút hình vuông bo góc với dải màu băng giá
        GradientPaint bg = new GradientPaint(
            r.x, r.y, hover ? new Color(25, 60, 100) : new Color(15, 38, 68),
            r.x, r.y + r.height, hover ? new Color(15, 40, 75) : new Color(10, 24, 46)
        );
        g2.setPaint(bg);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Viền nút phát sáng băng tuyết (nhấp nháy trắng khi đang đóng băng)
        if (iceShroomDuration > 0) {
            int pulse = (int) (180 + 75 * Math.sin(System.currentTimeMillis() * 0.015));
            g2.setColor(new Color(200, 245, 255, Math.min(255, pulse)));
        } else {
            g2.setColor(hover ? new Color(160, 235, 255) : new Color(90, 180, 230, 180));
        }
        g2.setStroke(new BasicStroke(hover ? 2.2f : 1.5f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Icon Bùa Đóng Băng đồ họa ở trung tâm
        if (iceShroomIconImg != null) {
            int iw = 36;
            int ih = 19;
            int ix = r.x + (r.width - iw) / 2;
            int iy = r.y + (r.height - ih) / 2 - 1;
            g2.drawImage(iceShroomIconImg, ix, iy, iw, ih, null);
        } else {
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(iceShroomDuration > 0 ? Color.WHITE : (count > 0 ? new Color(200, 240, 255) : new Color(130, 160, 185)));
            g2.drawString("BĂNG", r.x + 8, r.y + 30);
        }

        // Badge phím tắt [1] ở góc trên bên trái
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(r.x + 3, r.y + 3, 16, 13, 4, 4);
        g2.setColor(new Color(200, 230, 255));
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("1", r.x + 8, r.y + 13);

        // Badge hiển thị số lượng xN ở góc dưới bên phải
        if (count > 0) {
            String countText = "x" + count;
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            int tw = g2.getFontMetrics().stringWidth(countText);
            int bw = Math.max(22, tw + 8);
            int bh = 14;
            int bx = r.x + r.width - bw - 3;
            int by = r.y + r.height - bh - 3;

            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRoundRect(bx, by, bw, bh, 6, 6);
            g2.setColor(new Color(100, 220, 255));
            g2.drawRoundRect(bx, by, bw, bh, 6, 6);
            g2.setColor(new Color(255, 225, 50));
            g2.drawString(countText, bx + (bw - tw) / 2, by + 11);
        } else {
            String buyText = "+MUA";
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            int tw = g2.getFontMetrics().stringWidth(buyText);
            int bw = tw + 6;
            int bh = 13;
            int bx = r.x + r.width - bw - 2;
            int by = r.y + r.height - bh - 2;

            g2.setColor(new Color(180, 50, 40, 220));
            g2.fillRoundRect(bx, by, bw, bh, 4, 4);
            g2.setColor(Color.WHITE);
            g2.drawString(buyText, bx + 3, by + 10);
        }

        // Hiệu ứng Countdown hồi chiêu (bóng quét + hiển thị số giây)
        if (iceShroomCooldown > 0) {
            drawCooldownOverlay(g2, r, (float) iceShroomCooldown / CD_ICE_SHROOM, iceShroomCooldown, false);
        }

        // Tooltip hướng dẫn khi rê chuột vào nút
        if (hover) {
            String tip = "Bùa Đóng Băng [Phím 1]: Đóng băng & làm chậm toàn sân 5s (" + count + " còn lại)";
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            int tipW = g2.getFontMetrics().stringWidth(tip) + 16;
            int tipX = Math.max(10, r.x + (r.width - tipW) / 2);
            int tipY = r.y + r.height + 22;

            g2.setColor(new Color(10, 25, 45, 235));
            g2.fillRoundRect(tipX, tipY - 14, tipW, 20, 8, 8);
            g2.setColor(new Color(120, 215, 255));
            g2.drawRoundRect(tipX, tipY - 14, tipW, 20, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawString(tip, tipX + 8, tipY);
        }

        g2.dispose();
    }

    private void drawChocolateButton(Graphics2D g, Rectangle r, int mX, int mY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean hover = r.contains(mX, mY);

        WalletManager wm = WalletManager.getInstance();
        int count = wm.getData().chocolateCount;
        boolean hasPet = wm.getData().hasPetSnail;
        boolean isPetBoosted = (petSnail != null && petSnail.isBoosted());

        // Nền nút hình vuông bo góc với dải màu sô-cô-la ấm áp
        Color topC = hover ? new Color(115, 62, 32) : new Color(80, 42, 20);
        Color botC = hover ? new Color(75, 38, 18) : new Color(50, 24, 10);
        if (isPetBoosted) {
            float pulse = (float) Math.sin(System.currentTimeMillis() * 0.012);
            topC = new Color(Math.min(255, 135 + (int)(35 * pulse)), 75, 25);
            botC = new Color(Math.min(255, 95 + (int)(30 * pulse)), 48, 15);
        }

        GradientPaint bg = new GradientPaint(r.x, r.y, topC, r.x, r.y + r.height, botC);
        g2.setPaint(bg);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Viền nút
        if (isPetBoosted) {
            float pulse = (float) Math.sin(System.currentTimeMillis() * 0.015);
            int alpha = Math.min(255, (int)(180 + 75 * pulse));
            g2.setColor(new Color(255, 215, 0, alpha));
        } else {
            g2.setColor(hover ? new Color(255, 200, 110) : new Color(175, 120, 65, 180));
        }
        g2.setStroke(new BasicStroke(hover || isPetBoosted ? 2.2f : 1.5f));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Icon kẹo Chocolate đồ họa ở trung tâm
        if (chocolateIconImg != null) {
            int iw = 26;
            int ih = 30;
            int ix = r.x + (r.width - iw) / 2;
            int iy = r.y + (r.height - ih) / 2 - 1;
            g2.drawImage(chocolateIconImg, ix, iy, iw, ih, null);
        } else {
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.setColor(new Color(255, 220, 160));
            g2.drawString("KẸO", r.x + 12, r.y + 30);
        }

        // Badge phím tắt [2] ở góc trên bên trái
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(r.x + 3, r.y + 3, 16, 13, 4, 4);
        g2.setColor(new Color(255, 225, 160));
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("2", r.x + 8, r.y + 13);

        // Badge hiển thị ở góc dưới bên phải
        if (isPetBoosted) {
            int totalSec = petSnail.getRemainingSeconds();
            String timeText = String.format("%d:%02d", totalSec / 60, totalSec % 60);
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            int tw = g2.getFontMetrics().stringWidth(timeText);
            int bw = tw + 8;
            int bh = 14;
            int bx = r.x + r.width - bw - 2;
            int by = r.y + r.height - bh - 3;

            g2.setColor(new Color(180, 110, 0, 230));
            g2.fillRoundRect(bx, by, bw, bh, 5, 5);
            g2.setColor(new Color(255, 235, 120));
            g2.drawRoundRect(bx, by, bw, bh, 5, 5);
            g2.setColor(Color.WHITE);
            g2.drawString(timeText, bx + 4, by + 10);
        } else if (count > 0) {
            String countText = "x" + count;
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            int tw = g2.getFontMetrics().stringWidth(countText);
            int bw = Math.max(22, tw + 8);
            int bh = 14;
            int bx = r.x + r.width - bw - 3;
            int by = r.y + r.height - bh - 3;

            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRoundRect(bx, by, bw, bh, 6, 6);
            g2.setColor(new Color(255, 190, 80));
            g2.drawRoundRect(bx, by, bw, bh, 6, 6);
            g2.setColor(new Color(255, 225, 50));
            g2.drawString(countText, bx + (bw - tw) / 2, by + 11);
        } else {
            String buyText = "+MUA";
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            int tw = g2.getFontMetrics().stringWidth(buyText);
            int bw = tw + 6;
            int bh = 13;
            int bx = r.x + r.width - bw - 2;
            int by = r.y + r.height - bh - 2;

            g2.setColor(new Color(180, 50, 40, 220));
            g2.fillRoundRect(bx, by, bw, bh, 4, 4);
            g2.setColor(Color.WHITE);
            g2.drawString(buyText, bx + 3, by + 10);
        }

        // Hiệu ứng Countdown hồi chiêu (bóng quét + hiển thị thời gian)
        if (chocolateCooldown > 0) {
            drawCooldownOverlay(g2, r, (float) chocolateCooldown / CD_CHOCOLATE, chocolateCooldown, true);
        }

        // Tooltip hướng dẫn khi hover
        if (hover) {
            String tip;
            if (!hasPet) {
                tip = "Kẹo Chocolate [Phím 2]: Mua Pet Ốc Sên trước để sử dụng!";
            } else if (isPetBoosted) {
                tip = "Pet đang tăng tốc 250%! Còn " + petSnail.getRemainingSeconds() + "s (Bấm để cộng dồn)";
            } else {
                tip = "Kẹo Chocolate [Phím 2]: Cho Pet ăn tăng 250% tốc độ bò trong 3 phút (" + count + " còn lại)";
            }
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            int tipW = g2.getFontMetrics().stringWidth(tip) + 16;
            int tipX = Math.max(10, r.x + (r.width - tipW) / 2);
            int tipY = r.y + r.height + 22;

            g2.setColor(new Color(40, 24, 12, 240));
            g2.fillRoundRect(tipX, tipY - 14, tipW, 20, 8, 8);
            g2.setColor(new Color(255, 195, 90));
            g2.drawRoundRect(tipX, tipY - 14, tipW, 20, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawString(tip, tipX + 8, tipY);
        }

        g2.dispose();
    }

    private void drawShovelButton(Graphics2D g, Rectangle r, int mX, int mY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean hover = r.contains(mX, mY);

        WalletManager wm = WalletManager.getInstance();
        int count = wm.getData().goldenShovelCount;
        boolean isGolden = count > 0;
        boolean active = shovelSelected;

        // Bóng đổ
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(r.x + 2, r.y + 2, r.width, r.height, 10, 10);

        // Nền nút
        Color topC, botC;
        if (isGolden) {
            if (active) {
                float pulse = (float) Math.sin(System.currentTimeMillis() * 0.015);
                topC = new Color(190 + (int)(35 * pulse), 140, 20);
                botC = new Color(130 + (int)(30 * pulse), 85, 10);
            } else if (hover) {
                topC = new Color(165, 120, 30);
                botC = new Color(110, 75, 15);
            } else {
                topC = new Color(125, 85, 20);
                botC = new Color(80, 50, 10);
            }
        } else {
            if (active) {
                topC = new Color(90, 85, 80);
                botC = new Color(60, 55, 50);
            } else if (hover) {
                topC = new Color(75, 70, 65);
                botC = new Color(50, 45, 40);
            } else {
                topC = new Color(55, 50, 45);
                botC = new Color(35, 30, 25);
            }
        }

        GradientPaint bg = new GradientPaint(r.x, r.y, topC, r.x, r.y + r.height, botC);
        g2.setPaint(bg);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Viền nút
        if (active) {
            float pulse = (float) Math.sin(System.currentTimeMillis() * 0.018);
            int alpha = Math.min(255, (int)(180 + 75 * pulse));
            g2.setColor(isGolden ? new Color(255, 240, 120, alpha) : new Color(255, 255, 255, alpha));
            g2.setStroke(new BasicStroke(2.6f));
        } else {
            g2.setColor(isGolden ? (hover ? new Color(255, 225, 100) : new Color(215, 165, 60)) :
                                   (hover ? new Color(200, 190, 180) : new Color(130, 120, 110)));
            g2.setStroke(new BasicStroke(hover ? 2.0f : 1.5f));
        }
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Vẽ xẻng
        if (shovelImg != null) {
            int sw = 36, sh = 36;
            int sx = r.x + (r.width - sw) / 2;
            int sy = r.y + 4;
            g2.drawImage(shovelImg, sx, sy, sw, sh, null);
            if (isGolden) {
                int spX = sx + 26;
                int spY = sy + 8;
                g2.setColor(Color.YELLOW);
                g2.drawLine(spX - 3, spY, spX + 3, spY);
                g2.drawLine(spX, spY - 3, spX, spY + 3);
            }
        } else {
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.setColor(isGolden ? new Color(255, 235, 150) : Color.LIGHT_GRAY);
            g2.drawString("XẺNG", r.x + 8, r.y + 30);
        }

        // Badge phím tắt [3] ở góc trên bên trái
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(r.x + 3, r.y + 3, 16, 13, 4, 4);
        g2.setColor(isGolden ? new Color(255, 235, 150) : Color.LIGHT_GRAY);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("3", r.x + 8, r.y + 13);

        // Badge hiển thị số lượng lượt dùng xN ở góc dưới bên phải (đồng bộ với nút [1], [2], [4])
        String badge = "x" + count;
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        int tw = g2.getFontMetrics().stringWidth(badge);
        int bw = tw + 6;
        int bh = 13;
        int bx = r.x + r.width - bw - 2;
        int by = r.y + r.height - bh - 3;

        if (count > 0) {
            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRoundRect(bx, by, bw, bh, 5, 5);
            g2.setColor(new Color(255, 215, 0));
            g2.drawRoundRect(bx, by, bw, bh, 5, 5);
            g2.setColor(new Color(255, 235, 100));
            g2.drawString(badge, bx + 3, by + 10);
        } else {
            g2.setColor(new Color(40, 40, 40, 210));
            g2.fillRoundRect(bx, by, bw, bh, 4, 4);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString(badge, bx + 3, by + 10);
        }

        // Hiệu ứng Countdown hồi chiêu giữa các lần đào cây
        if (shovelCooldown > 0) {
            drawCooldownOverlay(g2, r, (float) shovelCooldown / CD_SHOVEL, shovelCooldown, false);
        }

        // Tooltip hướng dẫn khi hover
        if (hover) {
            String tip;
            if (count > 0) {
                tip = active ? "Xẻng Vàng [Phím 3]: Đang cầm! Click vào cây để đào (hoàn 50% Mặt trời, còn " + count + " lượt)"
                             : "Xẻng Vàng [Phím 3]: Đào thu hồi cây, nhận lại 50% Mặt trời (" + count + " lượt còn lại)";
            } else {
                tip = "Xẻng Vàng [Phím 3]: Đã hết lượt dùng! Bấm phím 3 hoặc vào Shop để mua thêm ($ " + com.team.gardendefense.system.ShopManager.PRICE_GOLDEN_SHOVEL + " xu)";
            }
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            int tipW = g2.getFontMetrics().stringWidth(tip) + 16;
            int tipX = Math.max(10, r.x + (r.width - tipW) / 2);
            int tipY = r.y + r.height + 22;

            g2.setColor(new Color(25, 20, 15, 240));
            g2.fillRoundRect(tipX, tipY - 14, tipW, 20, 8, 8);
            g2.setColor(count > 0 ? new Color(255, 215, 80) : new Color(180, 180, 180));
            g2.drawRoundRect(tipX, tipY - 14, tipW, 20, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawString(tip, tipX + 8, tipY);
        }

        g2.dispose();
    }

    private void drawLawnMowerButton(Graphics2D g, Rectangle r, int mX, int mY) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean hover = r.contains(mX, mY);
        boolean active = mowerTargeting;

        WalletManager wm = WalletManager.getInstance();
        int count = wm.getData().lawnMowerBackupCount;

        // Bóng đổ
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(r.x + 2, r.y + 2, r.width, r.height, 10, 10);

        // Nền nút
        Color topC, botC;
        if (active) {
            float pulse = (float) Math.sin(System.currentTimeMillis() * 0.018);
            topC = new Color(200 + (int)(35 * pulse), 40, 30);
            botC = new Color(130 + (int)(30 * pulse), 20, 15);
        } else if (hover) {
            topC = new Color(165, 45, 35);
            botC = new Color(110, 25, 20);
        } else {
            topC = new Color(125, 35, 25);
            botC = new Color(80, 20, 15);
        }

        GradientPaint bg = new GradientPaint(r.x, r.y, topC, r.x, r.y + r.height, botC);
        g2.setPaint(bg);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Viền nút
        if (active) {
            float pulse = (float) Math.sin(System.currentTimeMillis() * 0.02);
            int alpha = Math.min(255, (int)(180 + 75 * pulse));
            g2.setColor(new Color(255, 220, 50, alpha));
            g2.setStroke(new BasicStroke(2.6f));
        } else {
            g2.setColor(hover ? new Color(255, 180, 120) : new Color(200, 90, 70));
            g2.setStroke(new BasicStroke(hover ? 2.0f : 1.5f));
        }
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Icon Xe Cắt Cỏ ở trung tâm
        if (mowerIconImg != null) {
            int iw = 36;
            int ih = 28;
            int ix = r.x + (r.width - iw) / 2;
            int iy = r.y + (r.height - ih) / 2 - 2;
            Graphics2D gIcon = (Graphics2D) g2.create();
            gIcon.translate(ix + iw, iy);
            gIcon.scale(-1, 1);
            gIcon.drawImage(mowerIconImg, 0, 0, iw, ih, null);
            gIcon.dispose();
        } else {
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.setColor(new Color(255, 220, 100));
            g2.drawString("XE", r.x + 16, r.y + 30);
        }

        // Badge phím tắt [4] ở góc trên bên trái
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(r.x + 3, r.y + 3, 16, 13, 4, 4);
        g2.setColor(new Color(255, 225, 140));
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("4", r.x + 8, r.y + 13);

        // Badge hiển thị ở góc dưới bên phải
        if (active) {
            String badge = "TARGET";
            g2.setFont(new Font("Arial", Font.BOLD, 8));
            int tw = g2.getFontMetrics().stringWidth(badge);
            int bw = tw + 6;
            int bh = 13;
            int bx = r.x + r.width - bw - 2;
            int by = r.y + r.height - bh - 3;

            g2.setColor(new Color(220, 30, 20, 230));
            g2.fillRoundRect(bx, by, bw, bh, 5, 5);
            g2.setColor(new Color(255, 240, 100));
            g2.drawRoundRect(bx, by, bw, bh, 5, 5);
            g2.setColor(Color.WHITE);
            g2.drawString(badge, bx + 3, by + 10);
        } else if (count > 0) {
            String countText = "x" + count;
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            int tw = g2.getFontMetrics().stringWidth(countText);
            int bw = Math.max(22, tw + 8);
            int bh = 14;
            int bx = r.x + r.width - bw - 3;
            int by = r.y + r.height - bh - 3;

            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRoundRect(bx, by, bw, bh, 6, 6);
            g2.setColor(new Color(255, 140, 80));
            g2.drawRoundRect(bx, by, bw, bh, 6, 6);
            g2.setColor(new Color(255, 225, 50));
            g2.drawString(countText, bx + (bw - tw) / 2, by + 11);
        } else {
            String buyText = "+MUA";
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            int tw = g2.getFontMetrics().stringWidth(buyText);
            int bw = tw + 6;
            int bh = 13;
            int bx = r.x + r.width - bw - 2;
            int by = r.y + r.height - bh - 2;

            g2.setColor(new Color(180, 50, 40, 220));
            g2.fillRoundRect(bx, by, bw, bh, 4, 4);
            g2.setColor(Color.WHITE);
            g2.drawString(buyText, bx + 3, by + 10);
        }

        // Hiệu ứng Countdown hồi chiêu
        if (lawnMowerCooldown > 0) {
            drawCooldownOverlay(g2, r, (float) lawnMowerCooldown / CD_LAWN_MOWER, lawnMowerCooldown, false);
        }

        // Tooltip hướng dẫn khi hover
        if (hover) {
            String tip = active ? "Xe Cắt Cỏ [Phím 4]: Click 1 hàng ngang để phóng xe (Chuột phải để hủy)!"
                                : "Xe Cắt Cỏ [Phím 4]: Phóng xe càn quét toàn bộ 1 hàng ngang từ phải qua trái (" + count + " còn lại)";
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            int tipW = g2.getFontMetrics().stringWidth(tip) + 16;
            int tipX = Math.max(10, r.x + (r.width - tipW) / 2);
            int tipY = r.y + r.height + 22;

            g2.setColor(new Color(35, 15, 10, 240));
            g2.fillRoundRect(tipX, tipY - 14, tipW, 20, 8, 8);
            g2.setColor(new Color(255, 140, 70));
            g2.drawRoundRect(tipX, tipY - 14, tipW, 20, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawString(tip, tipX + 8, tipY);
        }

        g2.dispose();
    }

    private void drawCooldownOverlay(Graphics2D g, Rectangle r, float cdRatio, int cdFrames, boolean isTimeFormat) {
        cdRatio = Math.max(0.0f, Math.min(1.0f, cdRatio));
        int shadeH = (int) Math.ceil(r.height * cdRatio);

        Graphics2D gOverlay = (Graphics2D) g.create();
        gOverlay.clip(new java.awt.geom.RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 14, 14));
        gOverlay.setColor(new Color(0, 0, 0, 185));
        gOverlay.fillRect(r.x, r.y, r.width, shadeH);

        gOverlay.setColor(new Color(255, 255, 255, 160));
        gOverlay.drawLine(r.x, r.y + shadeH, r.x + r.width, r.y + shadeH);
        gOverlay.dispose();

        String cdStr;
        if (isTimeFormat) {
            int totalSec = (cdFrames + 59) / 60;
            cdStr = String.format("%d:%02d", totalSec / 60, totalSec % 60);
        } else {
            float secLeft = cdFrames / 60.0f;
            cdStr = (secLeft >= 10.0f) ? String.format(java.util.Locale.US, "%.0fs", secLeft)
                    : String.format(java.util.Locale.US, "%.1fs", secLeft);
        }

        Font cdFont = new Font("Arial", Font.BOLD, isTimeFormat ? 11 : 12);
        g.setFont(cdFont);
        FontMetrics fm = g.getFontMetrics(cdFont);
        int tw = fm.stringWidth(cdStr);
        int tx = r.x + (r.width - tw) / 2;
        int ty = r.y + (r.height + 6) / 2;

        g.setColor(Color.BLACK);
        g.drawString(cdStr, tx - 1, ty);
        g.drawString(cdStr, tx + 1, ty);
        g.drawString(cdStr, tx, ty - 1);
        g.drawString(cdStr, tx, ty + 1);

        g.setColor(new Color(255, 235, 120));
        g.drawString(cdStr, tx, ty);
    }

    private void drawIceShroomScreenEffect(Graphics2D g, int sw, int sh) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Sóng xung kích băng tuyết nở rộng từ tâm màn hình
        if (iceShroomWaveRadius > 0 && iceShroomWaveRadius < sw * 1.5f) {
            int cx = sw / 2;
            int cy = sh / 2;
            int r = (int) iceShroomWaveRadius;
            float waveAlpha = Math.max(0, 1.0f - (r / (float)(sw * 1.5f)));
            g2.setColor(new Color(180, 235, 255, (int)(160 * waveAlpha)));
            g2.setStroke(new BasicStroke(16f * waveAlpha));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);

            g2.setColor(new Color(255, 255, 255, (int)(220 * waveAlpha)));
            g2.setStroke(new BasicStroke(5f * waveAlpha));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }

        // 2. Viền màn hình sương giá tuyết lạnh
        float freezeProgress = iceShroomDuration / 300f; // 1.0 -> 0.0
        int tintAlpha = (int)(55 * freezeProgress);
        g2.setColor(new Color(135, 206, 250, tintAlpha));
        g2.fillRect(0, 0, sw, sh);

        // 3. Tinh thể hoa tuyết bay lơ lửng trên màn hình (vẽ vector hoa tuyết)
        g2.setColor(new Color(255, 255, 255, (int)(180 * freezeProgress)));
        int numSnowflakes = 16;
        for (int i = 0; i < numSnowflakes; i++) {
            int fx = ((i * 137 + 53) % sw);
            int fy = ((i * 97 + (int)((300 - iceShroomDuration) * (2.2 + (i % 3)))) % sh);
            g2.fillOval(fx - 2, fy - 2, 4, 4);
            g2.drawLine(fx - 4, fy, fx + 4, fy);
            g2.drawLine(fx, fy - 4, fx, fy + 4);
        }

        // 4. Banner thông báo nghệ thuật xuất hiện trong 1.5 giây đầu
        if (iceShroomDuration > 210) {
            float bannerAlpha = Math.min(1.0f, (iceShroomDuration - 210) / 30f);
            String bannerText = "BÙA ĐÓNG BĂNG ĐÃ KÍCH HOẠT! (5 GIÂY)";
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            int tw = g2.getFontMetrics().stringWidth(bannerText);
            int bx = (sw - tw) / 2;
            int by = sh / 4;

            g2.setColor(new Color(0, 20, 50, (int)(200 * bannerAlpha)));
            g2.fillRoundRect(bx - 20, by - 26, tw + 40, 36, 12, 12);
            g2.setColor(new Color(100, 220, 255, (int)(230 * bannerAlpha)));
            g2.drawRoundRect(bx - 20, by - 26, tw + 40, 36, 12, 12);
            g2.setColor(new Color(230, 250, 255, (int)(255 * bannerAlpha)));
            g2.drawString(bannerText, bx, by);
        }

        g2.dispose();
    }
}
