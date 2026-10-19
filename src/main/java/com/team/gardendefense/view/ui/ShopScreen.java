package com.team.gardendefense.view.ui;

import com.team.gardendefense.controller.GameManager;
import com.team.gardendefense.system.ShopManager;
import com.team.gardendefense.system.WalletManager;
import com.team.gardendefense.utils.Constants;
import com.team.gardendefense.utils.ImageLoader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Cửa hàng Meta Shop mua Pet, vật phẩm bổ trợ và bản đồ giải trí.
 */
public class ShopScreen {
    private final GameManager gm;
    public Rectangle btnBack = new Rectangle(50, 40, 120, 42);

    public static class ShopItem {
        public String id;
        public String name;
        public String desc;
        public int price;
        public Rectangle cardRect;
        public Rectangle btnBuyRect;
        public String iconEmoji;

        public ShopItem(String id, String name, String desc, int price, String iconEmoji) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.price = price;
            this.iconEmoji = iconEmoji;
        }
    }

    private final List<ShopItem> items = new ArrayList<>();
    private String noticeMsg = "";
    private int noticeTimer = 0;
    private Color noticeColor = Color.GREEN;
    private Image coinBankImg;

    public ShopScreen(GameManager gm) {
        this.gm = gm;
        this.coinBankImg = ImageLoader.getImage("src/main/resources/assets/images/coinbank.png");

        items.add(new ShopItem("snail", "Pet Ốc Sên Stinky", "Tự động bò nhặt xu và mặt trời rơi trên sân", ShopManager.PRICE_PET_SNAIL, "🐌"));
        items.add(new ShopItem("chocolate", "Kẹo Chocolate", "Tăng 250% tốc độ bò của Pet trong 3 phút", ShopManager.PRICE_CHOCOLATE, "🍫"));
        items.add(new ShopItem("lawnmower", "Xe Cắt Cỏ Dự Phòng", "Hồi phục 1 xe cắt cỏ ở hàng bị Zombie phá", ShopManager.PRICE_LAWN_MOWER, "🚜"));
        items.add(new ShopItem("plantfood", "Phân Bón Siêu Cấp", "Năng lượng bộc phát kỹ năng tối thượng cho cây", ShopManager.PRICE_PLANT_FOOD, "🧪"));
        items.add(new ShopItem("iceshroom", "Bùa Đóng Băng", "Đóng băng và làm chậm toàn sân đấu trong 5 giây", ShopManager.PRICE_ICE_SHROOM, "❄️"));
        items.add(new ShopItem("shovel", "Xẻng Vàng", "Đào thu hồi cây được hoàn trả 50% số Mặt trời", ShopManager.PRICE_GOLDEN_SHOVEL, "⛏️"));
        items.add(new ShopItem("vase", "Map Đập Hũ", "Mở khóa Minigame giải đố đập hũ Vasebreaker", ShopManager.PRICE_VASEBREAKER, "🏺"));
        items.add(new ShopItem("izombie", "Map I, Zombie", "Mở khóa Minigame hóa thân thành Zombie công thành", ShopManager.PRICE_I_ZOMBIE, "🧟"));
    }

    public void update() {
        if (noticeTimer > 0) noticeTimer--;
    }

    public void draw(Graphics2D g) {
        int sw = gm.getScreenWidth();
        int sh = gm.getScreenHeight();

        // Nền cửa hàng màu gỗ ấm cúng
        g.setColor(new Color(36, 26, 18));
        g.fillRect(0, 0, sw, sh);

        // Header
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String title = "🛒 CỬA HÀNG CRAZY DAVE";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (sw - tw) / 2, 60);

        // Nút quay lại
        btnBack.setBounds(40, 30, 110, 40);
        drawCustomButton(g, btnBack, "⬅ QUAY LẠI", gm.mouseX, gm.mouseY, new Color(139, 69, 19));

        // Khung ví tiền ở góc phải
        WalletManager wm = WalletManager.getInstance();
        int bankX = sw - 230;
        int bankY = 25;
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(bankX, bankY, 190, 48, 12, 12);
        g.setColor(new Color(255, 215, 0));
        g.drawRoundRect(bankX, bankY, 190, 48, 12, 12);

        if (coinBankImg != null) {
            g.drawImage(coinBankImg, bankX + 8, bankY + 4, 40, 40, null);
        } else {
            g.fillOval(bankX + 12, bankY + 10, 28, 28);
        }
        g.setColor(new Color(255, 220, 50));
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("$ " + wm.getCoins(), bankX + 58, bankY + 32);

        // Thông báo
        if (noticeTimer > 0) {
            g.setColor(noticeColor);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            int nw = g.getFontMetrics().stringWidth(noticeMsg);
            g.drawString(noticeMsg, (sw - nw) / 2, 95);
        }

        // Danh sách mặt hàng (Lưới 4 cột x 2 hàng hoặc 2 cột x 4 hàng)
        int cols = 4;
        int cardW = (sw - 120) / cols;
        int cardH = 240;
        int startX = 60;
        int startY = 115;

        WalletManager.SaveData data = wm.getData();

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = startX + col * (cardW + 15);
            int cy = startY + row * (cardH + 20);
            item.cardRect = new Rectangle(cx, cy, cardW, cardH);

            boolean hover = item.cardRect.contains(gm.mouseX, gm.mouseY);
            g.setColor(hover ? new Color(60, 45, 30) : new Color(48, 35, 24));
            g.fillRoundRect(cx, cy, cardW, cardH, 16, 16);
            g.setColor(new Color(180, 140, 90));
            g.drawRoundRect(cx, cy, cardW, cardH, 16, 16);

            // Icon
            g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
            g.drawString(item.iconEmoji, cx + (cardW - 46) / 2, cy + 60);

            // Tên
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 15));
            int nw = g.getFontMetrics().stringWidth(item.name);
            g.drawString(item.name, cx + (cardW - nw) / 2, cy + 95);

            // Mô tả
            g.setColor(new Color(200, 200, 200));
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            drawWrappedString(g, item.desc, cx + 12, cy + 120, cardW - 24);

            // Số lượng hiện có hoặc trạng thái
            String status = "";
            boolean isOwned = false;
            if (item.id.equals("snail")) {
                if (data.hasPetSnail) { status = "ĐÃ SỞ HỮU"; isOwned = true; }
            } else if (item.id.equals("chocolate")) {
                status = "Đang có: x" + data.chocolateCount;
            } else if (item.id.equals("lawnmower")) {
                status = "Đang có: x" + data.lawnMowerBackupCount;
            } else if (item.id.equals("plantfood")) {
                status = "Đang có: x" + data.plantFoodCount;
            } else if (item.id.equals("iceshroom")) {
                status = "Đang có: x" + data.iceShroomBlastCount;
            } else if (item.id.equals("shovel")) {
                if (data.hasGoldenShovel) { status = "ĐÃ SỞ HỮU"; isOwned = true; }
            } else if (item.id.equals("vase")) {
                if (data.unlockedVasebreaker) { status = "ĐÃ MỞ KHÓA"; isOwned = true; }
            } else if (item.id.equals("izombie")) {
                if (data.unlockedIZombie) { status = "ĐÃ MỞ KHÓA"; isOwned = true; }
            }

            if (!status.isEmpty()) {
                g.setColor(isOwned ? Color.GREEN : new Color(255, 200, 100));
                g.setFont(new Font("Arial", Font.BOLD, 12));
                int stw = g.getFontMetrics().stringWidth(status);
                g.drawString(status, cx + (cardW - stw) / 2, cy + 180);
            }

            // Nút Mua
            int btnW = cardW - 40;
            int btnH = 34;
            item.btnBuyRect = new Rectangle(cx + 20, cy + cardH - 45, btnW, btnH);

            if (isOwned) {
                g.setColor(new Color(80, 80, 80));
                g.fillRoundRect(item.btnBuyRect.x, item.btnBuyRect.y, btnW, btnH, 8, 8);
                g.setColor(Color.LIGHT_GRAY);
                g.setFont(new Font("Arial", Font.BOLD, 13));
                String boughtText = "ĐÃ CÓ";
                int bw = g.getFontMetrics().stringWidth(boughtText);
                g.drawString(boughtText, item.btnBuyRect.x + (btnW - bw) / 2, item.btnBuyRect.y + 22);
            } else {
                boolean canBuy = wm.canAfford(item.price);
                Color btnBg = canBuy ? new Color(46, 139, 87) : new Color(100, 100, 100);
                drawCustomButton(g, item.btnBuyRect, "$ " + item.price + " - MUA", gm.mouseX, gm.mouseY, btnBg);
            }
        }
    }

    public void handleClick(int mX, int mY) {
        if (btnBack.contains(mX, mY)) {
            gm.state = GameManager.GameState.MENU;
            return;
        }

        ShopManager sm = ShopManager.getInstance();
        WalletManager wm = WalletManager.getInstance();

        for (ShopItem item : items) {
            if (item.btnBuyRect != null && item.btnBuyRect.contains(mX, mY)) {
                if (!wm.canAfford(item.price)) {
                    showNotice("Bạn không đủ tiền! Cần thêm $ " + (item.price - wm.getCoins()), Color.RED);
                    return;
                }

                boolean success = false;
                switch (item.id) {
                    case "snail":
                        success = sm.buyPetSnail();
                        break;
                    case "chocolate":
                        success = sm.buyChocolate();
                        break;
                    case "lawnmower":
                        success = sm.buyLawnMower();
                        break;
                    case "plantfood":
                        success = sm.buyPlantFood();
                        break;
                    case "iceshroom":
                        success = sm.buyIceShroom();
                        break;
                    case "shovel":
                        success = sm.buyGoldenShovel();
                        break;
                    case "vase":
                        success = sm.buyVasebreaker();
                        break;
                    case "izombie":
                        success = sm.buyIZombie();
                        break;
                }

                if (success) {
                    showNotice("Đã mua thành công " + item.name + "!", Color.GREEN);
                } else {
                    showNotice("Vật phẩm này bạn đã sở hữu rồi!", Color.YELLOW);
                }
                return;
            }
        }
    }

    private void showNotice(String msg, Color color) {
        this.noticeMsg = msg;
        this.noticeColor = color;
        this.noticeTimer = 150; // 2.5s
    }

    private void drawCustomButton(Graphics2D g, Rectangle r, String text, int mX, int mY, Color baseColor) {
        boolean hover = r.contains(mX, mY);
        Color c = hover ? baseColor.brighter() : baseColor;
        g.setColor(c);
        g.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
        g.setColor(Color.WHITE);
        g.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        int strW = g.getFontMetrics().stringWidth(text);
        g.drawString(text, r.x + (r.width - strW) / 2, r.y + (r.height + 6) / 2);
    }

    private void drawWrappedString(Graphics2D g, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (fm.stringWidth(line + word) > maxWidth) {
                g.drawString(line.toString(), x, y);
                line = new StringBuilder(word + " ");
                y += fm.getHeight();
            } else {
                line.append(word).append(" ");
            }
        }
        g.drawString(line.toString(), x, y);
    }
}

