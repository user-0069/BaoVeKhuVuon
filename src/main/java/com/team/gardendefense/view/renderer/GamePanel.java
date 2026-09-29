package com.team.gardendefense.view.renderer;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.controller.InputHandler;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel implements Runnable {
    private Thread gameThread;
    private boolean running = false;
    private GameManager gm;

    public GamePanel() {
        this.setPreferredSize(new Dimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        this.setDoubleBuffered(true);

        gm = new GameManager();
        InputHandler input = new InputHandler(gm);
        this.addMouseListener(input);
        this.addMouseMotionListener(input); // Thêm dòng này để lắng nghe di chuột
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        running = true;
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / Constants.FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        while (running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                gm.update();
                repaint();
                delta--;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        gm.draw(g2);
        g2.dispose();
    }
}
