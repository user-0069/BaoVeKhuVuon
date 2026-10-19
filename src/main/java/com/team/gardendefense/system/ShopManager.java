package com.team.gardendefense.system;

/**
 * Quản lý logic mua sắm và giá cả của các vật phẩm trong Cửa Hàng Meta-game.
 */
public class ShopManager {
    private static final ShopManager instance = new ShopManager();
    public static ShopManager getInstance() { return instance; }

    public static final int PRICE_PET_SNAIL = 3000;
    public static final int PRICE_CHOCOLATE = 50;
    public static final int PRICE_LAWN_MOWER = 500;
    public static final int PRICE_PLANT_FOOD = 200;
    public static final int PRICE_ICE_SHROOM = 350;
    public static final int PRICE_GOLDEN_SHOVEL = 2500;
    public static final int PRICE_VASEBREAKER = 5000;
    public static final int PRICE_I_ZOMBIE = 5000;

    private ShopManager() {}

    public boolean buyPetSnail() {
        WalletManager wm = WalletManager.getInstance();
        if (wm.getData().hasPetSnail) return false; // Đã mua rồi
        if (wm.spendCoins(PRICE_PET_SNAIL)) {
            wm.getData().hasPetSnail = true;
            wm.save();
            return true;
        }
        return false;
    }

    public boolean buyChocolate() {
        WalletManager wm = WalletManager.getInstance();
        if (wm.spendCoins(PRICE_CHOCOLATE)) {
            wm.getData().chocolateCount++;
            wm.save();
            return true;
        }
        return false;
    }

    public boolean buyLawnMower() {
        WalletManager wm = WalletManager.getInstance();
        if (wm.spendCoins(PRICE_LAWN_MOWER)) {
            wm.getData().lawnMowerBackupCount++;
            wm.save();
            return true;
        }
        return false;
    }

    public boolean buyPlantFood() {
        WalletManager wm = WalletManager.getInstance();
        if (wm.spendCoins(PRICE_PLANT_FOOD)) {
            wm.getData().plantFoodCount++;
            wm.save();
            return true;
        }
        return false;
    }

    public boolean buyIceShroom() {
        WalletManager wm = WalletManager.getInstance();
        if (wm.spendCoins(PRICE_ICE_SHROOM)) {
            wm.getData().iceShroomBlastCount++;
            wm.save();
            return true;
        }
        return false;
    }

    public boolean buyGoldenShovel() {
        WalletManager wm = WalletManager.getInstance();
        if (wm.getData().hasGoldenShovel) return false;
        if (wm.spendCoins(PRICE_GOLDEN_SHOVEL)) {
            wm.getData().hasGoldenShovel = true;
            wm.save();
            return true;
        }
        return false;
    }

    public boolean buyVasebreaker() {
        WalletManager wm = WalletManager.getInstance();
        if (wm.getData().unlockedVasebreaker) return false;
        if (wm.spendCoins(PRICE_VASEBREAKER)) {
            wm.getData().unlockedVasebreaker = true;
            wm.save();
            return true;
        }
        return false;
    }

    public boolean buyIZombie() {
        WalletManager wm = WalletManager.getInstance();
        if (wm.getData().unlockedIZombie) return false;
        if (wm.spendCoins(PRICE_I_ZOMBIE)) {
            wm.getData().unlockedIZombie = true;
            wm.save();
            return true;
        }
        return false;
    }
}

