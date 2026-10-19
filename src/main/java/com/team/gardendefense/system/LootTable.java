package com.team.gardendefense.system;

import com.team.gardendefense.model.entity.coin.CoinType;
import java.util.Random;

/**
 * Xử lý cơ chế Weighted Random Drop Rate cho tiền tệ rơi từ quái vật.
 * - Xu bạc ($10): 20%
 * - Xu vàng ($50): 5%
 * - Kim cương ($1000): 1%
 * - Không rơi gì: 74%
 */
public class LootTable {
    private static final Random random = new Random();

    /**
     * Quay thưởng ngẫu nhiên xác định loại xu rớt ra khi quái chết.
     * @param isMiniBoss true nếu là quái to, quái bắn xương hoặc quái đặc biệt
     * @param isFinalWaveBoss true nếu là Boss wave cuối cùng
     * @return CoinType hoặc null nếu rơi vào 74% không rớt
     */
    public static CoinType rollDrop(boolean isMiniBoss, boolean isFinalWaveBoss) {
        if (isFinalWaveBoss) {
            float bossRoll = random.nextFloat() * 100f;
            if (bossRoll < 60f) return CoinType.DIAMOND; // 60% rơi Kim Cương
            return CoinType.GOLD;                        // 40% rơi Xu Vàng
        }

        if (isMiniBoss) {
            float miniRoll = random.nextFloat() * 100f;
            if (miniRoll < 5f) return CoinType.DIAMOND;  // 5% Kim Cương
            if (miniRoll < 40f) return CoinType.GOLD;    // 35% Xu Vàng
            if (miniRoll < 80f) return CoinType.SILVER;  // 40% Xu Bạc
            return null;                                 // 20% Không rớt
        }

        // Tỉ lệ chuẩn cho quái thường:
        // [0..1%)      : Diamond (1%)
        // [1..6%)      : Gold (5%)
        // [6..26%)     : Silver (20%)
        // [26..100%)   : None (74%)
        float roll = random.nextFloat() * 100f;

        if (roll < 1.0f) {
            return CoinType.DIAMOND;
        } else if (roll < 6.0f) {
            return CoinType.GOLD;
        } else if (roll < 26.0f) {
            return CoinType.SILVER;
        }

        return null;
    }
}

