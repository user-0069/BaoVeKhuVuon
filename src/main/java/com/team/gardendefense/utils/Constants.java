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
}
