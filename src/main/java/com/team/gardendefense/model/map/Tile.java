package com.team.gardendefense.model.map;

public class Tile {
    private int x;
    private int y;
    private boolean isOccupied;

    public Tile() {}

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }
}
