package com.team.gardendefense.model.level;
public class LevelData {
    public int id;
    public String name;
    public int initialSun;
    public int startY; // Tọa độ Y bắt đầu của quái
    public int[] allowedPlants; // Các ID cây được phép mang vào
    public Waypoint[] waypoints;
    public int[][] map;
    public WaveData[] waves;
}
