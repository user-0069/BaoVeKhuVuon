package com.team.gardendefense.utils;

public class Constants {
    public static final int TILE_SIZE = 64;
    public static final int COLS = 16;
    public static final int ROWS = 12;
    public static final int SCREEN_WIDTH = TILE_SIZE * COLS;
    public static final int SCREEN_HEIGHT = TILE_SIZE * ROWS;
    public static final int FPS = 60;

    // --- GAME ECONOMY ---
    public static final int PASSIVE_SUN_RATE = 600; 
    public static final int PASSIVE_SUN_AMOUNT = 25;
    public static final int ZOMBIE_KILL_REWARD = 10;
    public static final int ZOMBIE_ESCAPE_PENALTY = 50;
    public static final int FREEZE_DURATION = 180; 
    public static final float FREEZE_SPEED_MULTI = 0.4f; 

    // --- PLANT STATS ---
    public static final int COST_SUNFLOWER = 50;
    public static final int HP_SUNFLOWER = 5;
    public static final int RATE_SUNFLOWER = 300; 
    public static final int AMOUNT_SUNFLOWER = 25;

    public static final int COST_PEASHOOTER = 50;
    public static final int HP_PEASHOOTER = 5;
    public static final int RATE_PEASHOOTER = 60; // Thời gian nạp đạn (frames)
    public static final int DMG_PEASHOOTER = 1; // Sát thương
    public static final float RANGE_PEASHOOTER = 250f; // Tầm bắn
    public static final float SPEED_PEA = 8f; // Tốc độ đạn bay

    public static final int COST_SNIPER = 100;
    public static final int HP_SNIPER = 5;
    public static final int RATE_SNIPER = 120;
    public static final int DMG_SNIPER = 2;
    public static final float RANGE_SNIPER = 500f;
    public static final float SPEED_SNIPER_PEA = 12f;

    public static final int COST_WALLNUT = 50;
    public static final int HP_WALLNUT = 30;

    public static final int COST_SNOWPEA = 150;
    public static final int HP_SNOWPEA = 5;
    public static final int RATE_SNOWPEA = 60;
    public static final int DMG_SNOWPEA = 1;
    public static final float RANGE_SNOWPEA = 250f;
    public static final float SPEED_SNOWPEA_PEA = 8f;

    // --- ZOMBIE STATS ---
    public static final int HP_ZOMBIE_RED = 5;
    public static final float SPEED_ZOMBIE_RED = 1.0f;
    public static final int RATE_ZOMBIE_RED_ATK = 60; 
    public static final int DMG_ZOMBIE_RED = 1;

    public static final int HP_ZOMBIE_SHOOTER = 3;
    public static final float SPEED_ZOMBIE_SHOOTER = 0.8f;
    public static final int RATE_ZOMBIE_SHOOTER = 100;
    public static final float RANGE_ZOMBIE_SHOOTER = 250f;
    public static final int DMG_ZOMBIE_SHOOTER = 1;
    public static final float SPEED_BONE = 5f;

    // --- CHERRYBOMB STATS ---
    public static final int COST_CHERRYBOMB = 150;
    public static final int HP_CHERRYBOMB = 1;
    // CherryBomb không có HP thực — override takeDamage() để bất tử trong lúc chờ nổ
    public static final int DELAY_CHERRYBOMB = 30;  // 0.5s chờ trước khi nổ (frames)
    public static final int BOOM_DISPLAY_TIME = 30; // 0.5s hiện hiệu ứng nổ (frames)
    public static final int DMG_CHERRYBOMB = 1800;  // Sát thương 1 phát giết mọi zombie
    public static final float RANGE_CHERRYBOMB = 1.5f; // Bán kính 1.5 ô (3x3)

    // --- CHOMPER STATS ---
    public static final int COST_CHOMPER = 150;
    public static final int HP_CHOMPER = 5;
    public static final int COOLDOWN_CHOMPER = 600;  // 10 giây nhai (frames)
    public static final int DMG_CHOMPER = 1800;      // Nuốt chửng 1 phát
    public static final float RANGE_CHOMPER = 1.5f;  // Bán kính 1.5 ô (3x3)

    // --- MARIGOLD STATS ---
    public static final int COST_MARIGOLD = 50;
    public static final int HP_MARIGOLD = 5;
    public static final int RATE_MARIGOLD = 1440; // 24 giây

    // --- COIN SYSTEM STATS ---
    public static final int COIN_VAL_SILVER = 10;
    public static final int COIN_VAL_GOLD = 50;
    public static final int COIN_VAL_DIAMOND = 1000;
    public static final float DROP_RATE_SILVER = 0.20f;
    public static final float DROP_RATE_GOLD = 0.05f;
    public static final float DROP_RATE_DIAMOND = 0.01f;
    public static final int COIN_LIFETIME = 360; // 6 giây
    public static final int COIN_FADE_START = 240; // 2 giây cuối nhấp nháy

    // --- PLANT COOLDOWNS (frames, 60 FPS) ---
    public static final int CD_SUNFLOWER = 450;    // 7.5 giây - Cây kinh tế cơ bản hồi nhanh
    public static final int CD_PEASHOOTER = 450;   // 7.5 giây - Cây bắn đậu cơ bản hồi nhanh
    public static final int CD_SNOWPEA = 450;      // 7.5 giây - Đậu băng làm chậm hồi nhanh
    public static final int CD_SNIPER = 600;       // 10.0 giây - Cây bắn tỉa tầm xa sát thương lớn
    public static final int CD_MARIGOLD = 600;     // 10.0 giây - Cúc vàng sinh tiền
    public static final int CD_CHOMPER = 900;      // 15.0 giây - Cây ngoạm nuốt chửng zombie
    public static final int CD_WALLNUT = 1200;     // 20.0 giây - Tường phòng thủ trâu bò (30 HP)
    public static final int CD_CHERRYBOMB = 1800;  // 30.0 giây - Quả anh đào nổ sát thương cực lớn
}
