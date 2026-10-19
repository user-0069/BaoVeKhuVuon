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

    // Kích thước hộp thoại
    private final int dialogWidth = 440;
    private final int dialogHeight = 520;

    // Các nút bấm trong Options Dialog
    public Rectangle btnResume = new Rectangle();
    public Rectangle btnRestart = new Rectangle();
    public Rectangle btnShop = new Rectangle();
    public Rectangle btnLevelSelect = new Rectangle();
    public Rectangle btnMainMenu = new Rectangle();
    public Rectangle btnToggleSound = new Rectangle();

    private Image buttonBgImg;
    private Image buttonGlowImg;

    public PauseDialog(GameManager gm) {
        this.gm = gm;
        this.buttonBgImg = ImageLoader.getImage("src/main/resources/assets/images/SeedChooser_Button.png");
        this.buttonGlowImg = ImageLoader.getImage("src/main/resources/assets/images/SeedChooser_Button_Glow.png");
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
        int dx = (sw - dialogWidth) / 2;
        int dy = (sh - dialogHeight) / 2;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Bóng đổ ngoài của bia đá
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(dx + 10, dy + 12, dialogWidth, dialogHeight, 28, 28);

        // Nền bia đá chính phong cách PvZ (Màu nâu đá sẫm kết hợp xanh rêu rêu phong)
        GradientPaint stoneGrad = new GradientPaint(
                dx, dy, new Color(48, 38, 28),
                dx, dy + dialogHeight, new Color(28, 22, 16)
        );
        g2.setPaint(stoneGrad);
        g2.fillRoundRect(dx, dy, dialogWidth, dialogHeight, 24, 24);

        // Viền đá đục đẽo 3D
        g2.setStroke(new BasicStroke(4f));
        g2.setColor(new Color(110, 85, 55));
        g2.drawRoundRect(dx + 2, dy + 2, dialogWidth - 4, dialogHeight - 4, 22, 22);

        // Viền trong mạ vàng cổ
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(185, 145, 75));
        g2.drawRoundRect(dx + 10, dy + 10, dialogWidth - 20, dialogHeight - 20, 18, 18);

        // Đinh tán trang trí 4 góc (Rivets)
        drawRivet(g2, dx + 18, dy + 18);
        drawRivet(g2, dx + dialogWidth - 18, dy + 18);
        drawRivet(g2, dx + 18, dy + dialogHeight - 18);
        drawRivet(g2, dx + dialogWidth - 18, dy + dialogHeight - 18);

        // 3. Tiêu đề "GAME PAUSED" (TẠM DỪNG)
        String title = "GAME PAUSED";
        g2.setFont(new Font("Serif", Font.BOLD, 36));
        int tw = g2.getFontMetrics().stringWidth(title);

        // Bóng chữ nổi 3D
        g2.setColor(new Color(20, 15, 10));
        g2.drawString(title, dx + (dialogWidth - tw) / 2 + 2, dy + 56);
        // Chữ vàng óng ánh
        g2.setColor(new Color(255, 215, 50));
        g2.drawString(title, dx + (dialogWidth - tw) / 2, dy + 54);

        // Thông tin màn chơi
        String sub = "Màn " + gm.currentLevelId;
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(190, 220, 170));
        int subW = g2.getFontMetrics().stringWidth(sub);
        g2.drawString(sub, dx + (dialogWidth - subW) / 2, dy + 82);

        // Đường gạch chia khắc đá
        g2.setColor(new Color(130, 95, 55));
        g2.drawLine(dx + 35, dy + 95, dx + dialogWidth - 35, dy + 95);

        // 4. Các nút bấm lựa chọn (Vertical Stack)
        int btnW = 320;
        int btnH = 46;
        int btnX = dx + (dialogWidth - btnW) / 2;
        int startBtnY = dy + 115;
        int gapY = 58;

        // Button 1: TIẾP TỤC (Xanh lá cây rực rỡ)
        btnResume.setBounds(btnX, startBtnY, btnW, btnH);
        drawPvZButton(g2, btnResume, "TIẾP TỤC", gm.mouseX, gm.mouseY, new Color(46, 139, 87), new Color(60, 179, 113));

        // Button 2: CHƠI LẠI (Cam đất đá)
        btnRestart.setBounds(btnX, startBtnY + gapY, btnW, btnH);
        drawPvZButton(g2, btnRestart, "CHƠI LẠI TỪ ĐẦU", gm.mouseX, gm.mouseY, new Color(180, 82, 35), new Color(205, 92, 38));

        // Button 3: CỬA HÀNG CRAZY DAVE (Vàng mật ong)
        btnShop.setBounds(btnX, startBtnY + gapY * 2, btnW, btnH);
        drawPvZButton(g2, btnShop, "CỬA HÀNG 🛒", gm.mouseX, gm.mouseY, new Color(160, 110, 30), new Color(190, 135, 40));

        // Button 4: ÂM THANH SFX (Toggle)
        boolean soundOn = AudioManager.getInstance().isSfxEnabled();
        btnToggleSound.setBounds(btnX, startBtnY + gapY * 3, btnW, btnH);
        String soundText = soundOn ? "ÂM THANH: [ BẬT 🔊 ]" : "ÂM THANH: [ TẮT 🔇 ]";
        drawPvZButton(g2, btnToggleSound, soundText, gm.mouseX, gm.mouseY, new Color(70, 90, 120), new Color(85, 110, 145));

        // Button 5: CHỌN MÀN KHÁC (Xanh ngọc bích sẫm)
        btnLevelSelect.setBounds(btnX, startBtnY + gapY * 4, btnW, btnH);
        drawPvZButton(g2, btnLevelSelect, "CHỌN MÀN KHÁC 🗺️", gm.mouseX, gm.mouseY, new Color(45, 85, 75), new Color(55, 105, 90));

        // Button 6: VỀ MENU CHÍNH (Đỏ gạch / Nâu gỗ sẫm)
        btnMainMenu.setBounds(btnX, startBtnY + gapY * 5, btnW, btnH);
        drawPvZButton(g2, btnMainMenu, "VỀ MENU CHÍNH 🏠", gm.mouseX, gm.mouseY, new Color(110, 40, 40), new Color(135, 50, 50));

        g2.dispose();
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
            gm.state = GameManager.GameState.SHOP;
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
            gm.scanAvailableLevels();
            gm.state = GameManager.GameState.LEVEL_SELECT;
            if (gm.getPanel() != null) gm.getPanel().resizeForLevel(Constants.COLS, Constants.ROWS);
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

        // Hiệu ứng bóng đổ phía dưới của nút
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRoundRect(r.x + 2, r.y + 3, r.width, r.height, 14, 14);

        // Thân nút bấm dạng dải màu dốc Gradient 3D
        Color topColor = hover ? hoverColor.brighter() : hoverColor;
        Color bottomColor = hover ? baseColor.brighter() : baseColor;
        GradientPaint btnGrad = new GradientPaint(r.x, r.y, topColor, r.x, r.y + r.height, bottomColor);
        g.setPaint(btnGrad);
        g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        // Viền viền kép sáng/tối tạo hiệu ứng nổi gờ (Beveled Edge)
        g.setStroke(new BasicStroke(hover ? 2.5f : 1.5f));
        g.setColor(hover ? new Color(255, 235, 120) : new Color(240, 205, 120));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        // Highlight viền trong góc trên
        g.setColor(new Color(255, 255, 255, hover ? 110 : 60));
        g.drawLine(r.x + 6, r.y + 3, r.x + r.width - 6, r.y + 3);

        // Chữ trên nút với bóng đổ tối (Drop shadow)
        g.setFont(new Font("Arial", Font.BOLD, 16));
        int strW = g.getFontMetrics().stringWidth(text);
        int strX = r.x + (r.width - strW) / 2;
        int strY = r.y + (r.height + 6) / 2;

        g.setColor(new Color(15, 15, 15, 180));
        g.drawString(text, strX + 1, strY + 2);

        g.setColor(hover ? Color.WHITE : new Color(250, 245, 230));
        g.drawString(text, strX, strY);
    }

    private void drawRivet(Graphics2D g, int cx, int cy) {
        g.setColor(new Color(20, 15, 10));
        g.fillOval(cx - 5, cy - 5, 10, 10);
        g.setColor(new Color(180, 145, 75));
        g.fillOval(cx - 4, cy - 4, 8, 8);
        g.setColor(new Color(255, 240, 160));
        g.fillOval(cx - 2, cy - 2, 3, 3);
    }
}

