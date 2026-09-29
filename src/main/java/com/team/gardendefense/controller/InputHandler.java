package com.team.gardendefense.controller;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.team.gardendefense.utils.Constants;

public class InputHandler extends MouseAdapter {
    private GameManager gm;
    public InputHandler(GameManager gm) { this.gm = gm; }

    @Override
    public void mousePressed(MouseEvent e) {
        gm.handleClick(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        gm.mouseX = e.getX();
        gm.mouseY = e.getY();
        gm.hoverCol = e.getX() / Constants.TILE_SIZE;
        gm.hoverRow = e.getY() / Constants.TILE_SIZE;
    }
}
