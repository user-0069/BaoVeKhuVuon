package com.team.gardendefense.view.ui;

import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.system.AudioManager;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.utils.ImageLoader;

import java.awt.*;

/**
 * Bảng điều khiển Tạm dừng (Pause / Options Menu) trong trận đấu.
 * Thiết kế chuẩn phong cách bia đá và khung gỗ cổ điển của Plants vs. Zombies 1.
 */
public class PauseDialog {
    private final GameManager gm;

    private static final int BOARD_IMAGE_WIDTH = 1920;
    private static final int BOARD_IMAGE_HEIGHT = 2208;
    private static final int MAX_DIALOG_HEIGHT = 740;

    // Các nút bấm trong Options Dialog
    public Rectangle btnResume = new Rectangle();
    public Rectangle btnRestart = new Rectangle();
    public Rectangle btnShop = new Rectangle();
    public Rectangle btnLevelSelect = new Rectangle();
    public Rectangle btnMainMenu = new Rectangle();
    public Rectangle btnToggleSound = new Rectangle();

    private Image boardBgImg;
    private Image choiceBarImg;

    public PauseDialog(GameManager gm) {
        this.gm = gm;
        this.boardBgImg = loadBoardImage();
        this.choiceBarImg = loadChoiceBarImage();
    }

    public void update() {
        // Có thể bổ sung animation nếu cần
    }

    public void draw(Graphics2D g) {
        int sw = gm.getScreenWidth();
        int sh = gm.getScreenHeight();

        // 1. Phủ lớp mờ bóng tối toàn màn hình (Dark Overlay 65%)
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, sw, sh);

        // 2. Tọa độ tâm màn hình
        int dialogHeight = Math.min(MAX_DIALOG_HEIGHT, sh - 24);
        int dialogWidth = dialogHeight * BOARD_IMAGE_WIDTH / BOARD_IMAGE_HEIGHT;
        if (dialogWidth > sw - 24) {
            dialogWidth = sw - 24;
            dialogHeight = dialogWidth * BOARD_IMAGE_HEIGHT / BOARD_IMAGE_WIDTH;
        }
        int dx = (sw - dialogWidth) / 2;
        int dy = (sh - dialogHeight) / 2;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (boardBgImg != null) {
            g2.drawImage(boardBgImg, dx, dy, dialogWidth, dialogHeight, null);
        } else {
            g2.setColor(new Color(48, 38, 28));
            g2.fillRoundRect(dx, dy, dialogWidth, dialogHeight, 24, 24);
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(185, 145, 75));
            g2.drawRoundRect(dx + 2, dy + 2, dialogWidth - 4, dialogHeight - 4, 22, 22);
        }

        // 4. Các nút lựa chọn xếp thành hai cột
        int btnW = dialogWidth * 29 / 100;
        int btnH = choiceBarImg == null
                ? dialogHeight * 6 / 100
                : btnW * choiceBarImg.getHeight(null) / choiceBarImg.getWidth(null);
        int columnGap = dialogWidth * 4 / 100;
        int gridWidth = btnW * 2 + columnGap;
        int leftColumnX = dx + (dialogWidth - gridWidth) / 2 + dialogWidth * 3 / 100;
        int rightColumnX = leftColumnX + btnW + columnGap;
        int rowGap = dialogHeight * 2 / 100;
        int startBtnY = dy + dialogHeight * 45 / 100;
        int buttonStepY = btnH + rowGap;

        // Button 1: TIẾP TỤC (Xanh lá cây rực rỡ)
        btnResume.setBounds(leftColumnX, startBtnY, btnW, btnH);
        drawPvZButton(g2, btnResume, "TIẾP TỤC", gm.mouseX, gm.mouseY, new Color(46, 139, 87), new Color(60, 179, 113));

        // Button 2: CHƠI LẠI (Cam đất đá)
        btnRestart.setBounds(rightColumnX, startBtnY, btnW, btnH);
        drawPvZButton(g2, btnRestart, "CHƠI LẠI TỪ ĐẦU", gm.mouseX, gm.mouseY, new Color(180, 82, 35), new Color(205, 92, 38));

        // Button 3: CỬA HÀNG CRAZY DAVE (Vàng mật ong)
        btnShop.setBounds(leftColumnX, startBtnY + buttonStepY, btnW, btnH);
        drawPvZButton(g2, btnShop, "CỬA HÀNG", gm.mouseX, gm.mouseY, new Color(160, 110, 30), new Color(190, 135, 40));

        // Button 4: ÂM THANH SFX (Toggle)
        boolean soundOn = AudioManager.getInstance().isSfxEnabled();
        btnToggleSound.setBounds(rightColumnX, startBtnY + buttonStepY, btnW, btnH);
        String soundText = soundOn ? "ÂM THANH: [ BẬT ]" : "ÂM THANH: [ TẮT ]";
        drawPvZButton(g2, btnToggleSound, soundText, gm.mouseX, gm.mouseY, new Color(70, 90, 120), new Color(85, 110, 145));

        // Button 5: CHỌN MÀN KHÁC (Xanh ngọc bích sẫm)
        btnLevelSelect.setBounds(leftColumnX, startBtnY + buttonStepY * 2, btnW, btnH);
        drawPvZButton(g2, btnLevelSelect, "CHỌN MÀN KHÁC", gm.mouseX, gm.mouseY, new Color(45, 85, 75), new Color(55, 105, 90));

        // Button 6: VỀ MENU CHÍNH (Đỏ gạch / Nâu gỗ sẫm)
        btnMainMenu.setBounds(rightColumnX, startBtnY + buttonStepY * 2, btnW, btnH);
        drawPvZButton(g2, btnMainMenu, "VỀ MENU CHÍNH", gm.mouseX, gm.mouseY, new Color(110, 40, 40), new Color(135, 50, 50));

        g2.dispose();
    }

    private Image loadBoardImage() {
        Image source = ImageLoader.getImage("src/main/resources/assets/images/gamepausedboard.png");
        if (!(source instanceof java.awt.image.BufferedImage)) return source;

        java.awt.image.BufferedImage original = (java.awt.image.BufferedImage) source;
        java.awt.image.BufferedImage transparent = new java.awt.image.BufferedImage(
                original.getWidth(), original.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int pixel = original.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                int max = Math.max(red, Math.max(green, blue));
                int min = Math.min(red, Math.min(green, blue));
                if (min >= 185 && max <= 225 && max - min <= 15) {
                    pixel = 0;
                }
                transparent.setRGB(x, y, pixel);
            }
        }
        return transparent;
    }

    private Image loadChoiceBarImage() {
        Image source = ImageLoader.getImage("src/main/resources/assets/images/choicebar.png");
        if (!(source instanceof java.awt.image.BufferedImage)) return source;

        java.awt.image.BufferedImage original = (java.awt.image.BufferedImage) source;
        int minX = original.getWidth();
        int minY = original.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int pixel = original.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                if (isChoiceBarBackground(red, green, blue)) continue;

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) return null;

        java.awt.image.BufferedImage cropped = new java.awt.image.BufferedImage(
                maxX - minX + 1, maxY - minY + 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                int pixel = original.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = pixel & 0xff;
                if (isChoiceBarBackground(red, green, blue)) pixel = 0;
                cropped.setRGB(x - minX, y - minY, pixel);
            }
        }
        return cropped;
    }

    private boolean isChoiceBarBackground(int red, int green, int blue) {
        int min = Math.min(red, Math.min(green, blue));
        int max = Math.max(red, Math.max(green, blue));
        return min >= 185 && max - min <= 20;
    }

    public void handleClick(int mX, int mY) {
        // 1. Tiếp tục
        if (btnResume.contains(mX, mY)) {
            AudioManager.getInstance().playBuySound();
            gm.state = GameManager.GameState.PLAYING;
            return;
        }

        // 2. Chơi lại
        if (btnRestart.contains(mX, mY)) {
            AudioManager.getInstance().playBuySound();
            gm.resetGame();
            return;
        }

        // 3. Mở Cửa hàng
        if (btnShop.contains(mX, mY)) {
            AudioManager.getInstance().playBuySound();
            gm.openShop(GameManager.GameState.PLAYING);
            return;
        }

        // 4. Bật / Tắt âm thanh
        if (btnToggleSound.contains(mX, mY)) {
            AudioManager.getInstance().toggleSfx();
            if (AudioManager.getInstance().isSfxEnabled()) {
                AudioManager.getInstance().playCoinSound();
            }
            return;
        }

        // 5. Chọn màn khác
        if (btnLevelSelect.contains(mX, mY)) {
            AudioManager.getInstance().playBuySound();
            gm.openLevelSelect();
            return;
        }

        // 6. Về Menu chính
        if (btnMainMenu.contains(mX, mY)) {
            AudioManager.getInstance().playBuySound();
            gm.state = GameManager.GameState.MENU;
            if (gm.getPanel() != null) gm.getPanel().resizeForLevel(Constants.COLS, Constants.ROWS);
            return;
        }
    }

    /**
     * Vẽ nút bấm 3D phong cách bia đá/khung gỗ nổi chuẩn PvZ 1.
     */
    private void drawPvZButton(Graphics2D g, Rectangle r, String text, int mX, int mY, Color baseColor, Color hoverColor) {
        boolean hover = r.contains(mX, mY);

        if (choiceBarImg != null) {
            g.drawImage(choiceBarImg, r.x, r.y, r.width, r.height, null);
            if (hover) {
                g.setColor(new Color(255, 255, 255, 45));
                g.fillRoundRect(r.x + 3, r.y + 3, r.width - 6, r.height - 6, 12, 12);
            }
        } else {
            g.setColor(new Color(0, 0, 0, 90));
            g.fillRoundRect(r.x + 2, r.y + 3, r.width, r.height, 14, 14);
            Color topColor = hover ? hoverColor.brighter() : hoverColor;
            Color bottomColor = hover ? baseColor.brighter() : baseColor;
            GradientPaint btnGrad = new GradientPaint(r.x, r.y, topColor, r.x, r.y + r.height, bottomColor);
            g.setPaint(btnGrad);
            g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g.setStroke(new BasicStroke(hover ? 2.5f : 1.5f));
            g.setColor(hover ? new Color(255, 235, 120) : new Color(240, 205, 120));
            g.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        }

        // Chữ trên nút với bóng đổ tối (Drop shadow)
        int fontSize = 14;
        Font buttonFont = new Font("Arial", Font.BOLD, fontSize);
        while (fontSize > 9 && g.getFontMetrics(buttonFont).stringWidth(text) > r.width - 16) {
            buttonFont = new Font("Arial", Font.BOLD, --fontSize);
        }
        g.setFont(buttonFont);
        FontMetrics metrics = g.getFontMetrics();
        int strW = metrics.stringWidth(text);
        int strX = r.x + (r.width - strW) / 2;
        int strY = r.y + (r.height - metrics.getHeight()) / 2 + metrics.getAscent();

        g.setColor(new Color(15, 15, 15, 180));
        g.drawString(text, strX + 1, strY + 2);

        g.setColor(hover ? Color.WHITE : new Color(250, 245, 230));
        g.drawString(text, strX, strY);
    }

}
