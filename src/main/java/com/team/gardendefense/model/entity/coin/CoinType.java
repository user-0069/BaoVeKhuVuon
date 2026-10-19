package com.team.gardendefense.model.entity.coin;

/**
 * Định nghĩa các loại tiền tệ trong game:
 * - SILVER: Xu bạc ($10, tỉ lệ rơi 20%)
 * - GOLD: Xu vàng ($50, tỉ lệ rơi 5%)
 * - DIAMOND: Kim cương ($1000, tỉ lệ rơi 1%)
 */
public enum CoinType {
    SILVER(10, "Xu Bạc", "src/main/resources/assets/reanim/coin_silver2.png"),
    GOLD(50, "Xu Vàng", "src/main/resources/assets/reanim/coin_gold2.png"),
    DIAMOND(1000, "Kim Cương", "src/main/resources/assets/reanim/Diamond.png");

    private final int value;
    private final String displayName;
    private final String imagePath;

    CoinType(int value, String displayName, String imagePath) {
        this.value = value;
        this.displayName = displayName;
        this.imagePath = imagePath;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImagePath() {
        return imagePath;
    }
}

