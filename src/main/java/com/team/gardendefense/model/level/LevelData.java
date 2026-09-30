package com.team.gardendefense.model.level;
public class LevelData {
    public int id;
    public String name;
    public int initialSun;
    public int[] allowedPlants;
    public int[][] map;
    public WaveData[] waves;
    // Đã xóa startY và waypoints chung toàn Map
}
