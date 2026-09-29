package com.team.gardendefense.main;

import com.team.gardendefense.view.renderer.GamePanel;
import javax.swing.JFrame;

public class GameLauncher {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Bảo vệ khu vườn");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread(); // Khởi động Game Loop
    }
}
