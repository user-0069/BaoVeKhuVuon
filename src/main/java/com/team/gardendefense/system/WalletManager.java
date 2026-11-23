package com.team.gardendefense.system;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.team.gardendefense.model.entity.coin.CoinType;

import java.awt.Color;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý tổng tài sản tiền xu và trạng thái mở khóa Meta-game của người chơi.
 * Lưu trữ bền vững (Save/Load) qua file JSON.
 */
public class WalletManager {
    private static final String SAVE_FILE = "wallet_save.json";
    private static final Path USER_SAVE_PATH = Paths.get(
            System.getProperty("user.home"), ".garden-defense", SAVE_FILE);
    private static final Path SAVE_PATH = Paths.get("saves", SAVE_FILE);
    private static final Path DEFAULT_SAVE_PATH = Paths.get("saves", "wallet_default.json");
    private static final Path LEGACY_SAVE_PATH = Paths.get(SAVE_FILE);
    private static final Logger LOGGER = Logger.getLogger(WalletManager.class.getName());
    private static final WalletManager instance = new WalletManager();

    public static WalletManager getInstance() { return instance; }

    // Dữ liệu mặc định của một người chơi mới
    public static class SaveData {
        public int coins = 100; // Người chơi mới có 100 xu
        public boolean hasPetSnail = false; // Trừ sên là không có
        public int chocolateCount = 0; // Kẹo chocolate không có
        public int goldenShovelCount = 2; // Mỗi loại vật phẩm có 2 cái
        public boolean hasGoldenShovel = false; // Trường cũ để tương thích dữ liệu
        public int iceShroomBlastCount = 2; // Mỗi loại vật phẩm có 2 cái
        public int lawnMowerBackupCount = 2; // Mỗi loại vật phẩm có 2 cái
        public int plantFoodCount = 2; // Mỗi loại vật phẩm có 2 cái
        public boolean unlockedVasebreaker = false;
        public boolean unlockedIZombie = false;
        public boolean unlockedSurvival = false;
    }

    private SaveData data = new SaveData();

    // Hiệu ứng số bay lên trên UI (+10, +50, +1000)
    public static class FloatingText {
        public String text;
        public float x, y;
        public int alpha = 255;
        public int life = 60; // 1 giây
        public Color color = new Color(255, 225, 30);

        public FloatingText(String text, float x, float y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }

        public FloatingText(String text, float x, float y, Color color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public void update() {
            y -= 0.8f;
            life--;
            alpha = Math.max(0, (life * 255) / 60);
        }

        public boolean isDead() {
            return life <= 0;
        }
    }
    public final List<FloatingText> floatingTexts = new CopyOnWriteArrayList<>();

    private WalletManager() {
        load();
    }

    public synchronized int getCoins() {
        return data.coins;
    }

    public synchronized void addCoins(int amount, CoinType type, float uiX, float uiY) {
        data.coins = (int) Math.min(2_000_000_000L, (long) data.coins + amount);
        save();

        // Tạo hiệu ứng chữ nổi
        String prefix = "+";
        floatingTexts.add(new FloatingText(prefix + amount, uiX, uiY));

        // Phát âm thanh
        if (type == CoinType.DIAMOND) {
            AudioManager.getInstance().playDiamondSound();
        } else {
            AudioManager.getInstance().playCoinSound();
        }
    }

    public synchronized boolean spendCoins(int amount) {
        if (data.coins >= amount) {
            data.coins -= amount;
            save();
            AudioManager.getInstance().playBuySound();
            return true;
        }
        return false;
    }

    public synchronized boolean canAfford(int amount) {
        return data.coins >= amount;
    }

    public SaveData getData() {
        return data;
    }

    public synchronized void updateFloatingTexts() {
        for (FloatingText ft : floatingTexts) {
            ft.update();
        }
        floatingTexts.removeIf(FloatingText::isDead);
    }

    public synchronized void save() {
        // 1. Thử lưu vào USER_SAVE_PATH (ngoài git repository, đảm bảo không làm bẩn repo của người chơi)
        try {
            if (USER_SAVE_PATH.getParent() != null) {
                Files.createDirectories(USER_SAVE_PATH.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(USER_SAVE_PATH, StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(data, writer);
                return;
            }
        } catch (Exception ignored) {
            // Môi trường hạn chế quyền user home (ví dụ sandbox), fallback lưu cục bộ
        }

        // 2. Fallback: Lưu vào SAVE_PATH (saves/wallet_save.json đã được gitignore và untrack khỏi git)
        try {
            if (SAVE_PATH.getParent() != null) {
                Files.createDirectories(SAVE_PATH.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(SAVE_PATH, StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(data, writer);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not save player progress to " + SAVE_PATH, ex);
        }
    }

    public synchronized void load() {
        Path source = Files.exists(USER_SAVE_PATH) ? USER_SAVE_PATH
                : Files.exists(SAVE_PATH) ? SAVE_PATH
                : Files.exists(DEFAULT_SAVE_PATH) ? DEFAULT_SAVE_PATH
                : Files.exists(LEGACY_SAVE_PATH) ? LEGACY_SAVE_PATH
                : null;
        if (source == null || !Files.exists(source)) {
            this.data = new SaveData();
            return;
        }

        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            SaveData loaded = new Gson().fromJson(reader, SaveData.class);
            if (loaded != null) {
                this.data = loaded;
                // Di chuyển dữ liệu cũ nếu người chơi trước đó đã sở hữu xẻng vàng
                if (this.data.hasGoldenShovel && this.data.goldenShovelCount <= 0) {
                    this.data.goldenShovelCount = 10;
                }
            }
        } catch (IOException | JsonParseException ex) {
            LOGGER.log(Level.SEVERE, "Could not load player progress from " + source, ex);
            this.data = new SaveData();
        }
    }
}

