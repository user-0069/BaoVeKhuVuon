package com.team.gardendefense.system;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.team.gardendefense.model.entity.coin.CoinType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý tổng tài sản tiền xu và trạng thái mở khóa Meta-game của người chơi.
 * Lưu trữ bền vững (Save/Load) qua file JSON.
 */
public class WalletManager {
    private static final WalletManager instance = new WalletManager();
    public static WalletManager getInstance() { return instance; }

    private static final String SAVE_FILE = "wallet_save.json";

    // Dữ liệu cần lưu
    public static class SaveData {
        public int coins = 100; // Tặng sẵn 100 xu khởi đầu trải nghiệm
        public boolean hasPetSnail = false;
        public int chocolateCount = 0;
        public boolean hasGoldenShovel = false;
        public int iceShroomBlastCount = 1; // Tặng 1 bùa dùng thử
        public int lawnMowerBackupCount = 1; // Tặng 1 xe dự phòng
        public int plantFoodCount = 2; // Tặng 2 phân bón
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

        public FloatingText(String text, float x, float y) {
            this.text = text;
            this.x = x;
            this.y = y;
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
    public final List<FloatingText> floatingTexts = new ArrayList<>();

    private WalletManager() {
        load();
    }

    public synchronized int getCoins() {
        return data.coins;
    }

    public synchronized void addCoins(int amount, CoinType type, float uiX, float uiY) {
        data.coins += amount;
        save();

        // Tạo hiệu ứng chữ nổi
        String prefix = (type == CoinType.DIAMOND) ? "💎 +" : "+";
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
        for (int i = floatingTexts.size() - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.update();
            if (ft.isDead()) {
                floatingTexts.remove(i);
            }
        }
    }

    public synchronized void save() {
        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(data, writer);
        } catch (Exception ignored) {}
    }

    public synchronized void load() {
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                SaveData loaded = new Gson().fromJson(reader, SaveData.class);
                if (loaded != null) {
                    this.data = loaded;
                }
            } catch (Exception ignored) {}
        }
    }
}

