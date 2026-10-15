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
        gm.setGamePanel(this);
        InputHandler input = new InputHandler(gm);
        this.addMouseListener(input);
        this.addMouseMotionListener(input); // Thêm dòng này để lắng nghe di chuột
        this.addKeyListener(input);
        this.setFocusable(true);
    }

    public void resizeForLevel(int cols, int rows) {
        int width = Math.max(Constants.SCREEN_WIDTH, cols * Constants.TILE_SIZE);
        int height = Math.max(Constants.SCREEN_HEIGHT, rows * Constants.TILE_SIZE);

        java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        if (height > screen.height - 70) {
            height = screen.height - 70;
        }
        if (width > screen.width - 40) {
            width = screen.width - 40;
        }

        this.setPreferredSize(new Dimension(width, height));
        java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (win instanceof javax.swing.JFrame) {
            win.pack();
            win.setLocationRelativeTo(null);
        }
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
