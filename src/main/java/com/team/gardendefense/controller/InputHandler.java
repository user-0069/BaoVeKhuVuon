package com.team.gardendefense.controller;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Point;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.view.renderer.GamePanel;

public class InputHandler extends MouseAdapter implements KeyListener, MouseWheelListener {
    private GameManager gm;
    private GamePanel gamePanel;

    public InputHandler(GameManager gm, GamePanel gamePanel) {
        this.gm = gm;
        this.gamePanel = gamePanel;
    }

    private void updateMousePosition(MouseEvent e) {
        Point gamePoint = gamePanel.screenToGameCoordinates(e.getX(), e.getY());
        gm.mouseX = gamePoint.x;
        gm.mouseY = gamePoint.y;
        gm.hoverCol = gamePoint.x >= 0 ? gamePoint.x / Constants.TILE_SIZE : -1;
        gm.hoverRow = gamePoint.y >= 0 ? gamePoint.y / Constants.TILE_SIZE : -1;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        updateMousePosition(e);
        gm.handleClick(gm.mouseX, gm.mouseY);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateMousePosition(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updateMousePosition(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Point gamePoint = gamePanel.screenToGameCoordinates(e.getX(), e.getY());
        gm.handleMouseWheel(e.getWheelRotation(), gamePoint.x, gamePoint.y);
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
