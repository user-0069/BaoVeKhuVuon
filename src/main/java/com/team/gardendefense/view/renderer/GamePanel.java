package com.team.gardendefense.view.renderer;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.controller.InputHandler;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.Color;
import java.util.concurrent.locks.LockSupport;

public class GamePanel extends JPanel implements Runnable {
    private Thread gameThread;
    private volatile boolean running = false;
    private GameManager gm;

    public GamePanel() {
        this.setPreferredSize(new Dimension(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);

        gm = new GameManager();
        gm.setGamePanel(this);
        InputHandler input = new InputHandler(gm, this);
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

    public synchronized void startGameThread() {
        if (running) return;
        gameThread = new Thread(this);
        running = true;
        gameThread.start();
    }

    public Point screenToGameCoordinates(int screenX, int screenY) {
        int gameWidth = gm.getScreenWidth();
        int gameHeight = gm.getScreenHeight();
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        if (panelWidth <= 0 || panelHeight <= 0) return new Point(-1, -1);

        double scale = Math.min((double) panelWidth / gameWidth, (double) panelHeight / gameHeight);
        double offsetX = (panelWidth - gameWidth * scale) / 2.0;
        double offsetY = (panelHeight - gameHeight * scale) / 2.0;
        if (screenX < offsetX || screenX >= offsetX + gameWidth * scale
                || screenY < offsetY || screenY >= offsetY + gameHeight * scale) {
            return new Point(-1, -1);
        }
        return new Point((int) ((screenX - offsetX) / scale), (int) ((screenY - offsetY) / scale));
    }

    @Override
    public void run() {
        long frameDuration = 1_000_000_000L / Constants.FPS;
        long nextFrame = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            long remaining = nextFrame - now;
            if (remaining > 0) {
                LockSupport.parkNanos(remaining);
                continue;
            }

            gm.update();
            repaint();

            nextFrame += frameDuration;
            long frameEnd = System.nanoTime();
            if (frameEnd - nextFrame > frameDuration * 5) {
                nextFrame = frameEnd + frameDuration;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getWidth() <= 0 || getHeight() <= 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            int gameWidth = gm.getScreenWidth();
            int gameHeight = gm.getScreenHeight();
            double scale = Math.min((double) getWidth() / gameWidth, (double) getHeight() / gameHeight);
            double offsetX = (getWidth() - gameWidth * scale) / 2.0;
            double offsetY = (getHeight() - gameHeight * scale) / 2.0;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.translate(offsetX, offsetY);
            g2.scale(scale, scale);
            gm.draw(g2);
        } finally {
            g2.dispose();
        }
    }
}
