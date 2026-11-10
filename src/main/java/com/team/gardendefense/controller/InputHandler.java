package com.team.gardendefense.controller;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.view.renderer.GamePanel;

public class InputHandler extends MouseAdapter implements KeyListener {
    private GameManager gm;
    private GamePanel gamePanel;

    public InputHandler(GameManager gm, GamePanel gamePanel) {
        this.gm = gm;
        this.gamePanel = gamePanel;
    }

    private int toGameX(int screenX) {
        return (int) ((double) screenX * gm.getScreenWidth() / Math.max(1, gamePanel.getWidth()));
    }

    private int toGameY(int screenY) {
        return (int) ((double) screenY * gm.getScreenHeight() / Math.max(1, gamePanel.getHeight()));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        gm.handleClick(toGameX(e.getX()), toGameY(e.getY()));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        gm.mouseX = toGameX(e.getX());
        gm.mouseY = toGameY(e.getY());
        gm.hoverCol = gm.mouseX / Constants.TILE_SIZE;
        gm.hoverRow = gm.mouseY / Constants.TILE_SIZE;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        gm.mouseX = toGameX(e.getX());
        gm.mouseY = toGameY(e.getY());
        gm.hoverCol = gm.mouseX / Constants.TILE_SIZE;
        gm.hoverRow = gm.mouseY / Constants.TILE_SIZE;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        gm.handleKeyPress(e.getKeyCode());
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
}
