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
    public List<ShopItem> getItems() { return items; }
    private String noticeMsg = "";
    private int noticeTimer = 0;
    private Color noticeColor = Color.GREEN;
    private Image coinBankImg;
    private Image mowerShopImg;
    private Image chocolateImg;
    private Image iceShroomImg;
    private Image shovelImg;
    private Image snailBodyImg;
    private Image snailShellImg;
    private Image btnBackImg;
    private Image btnBackHlImg;

    public ShopScreen(GameManager gm) {
        this.gm = gm;
        this.coinBankImg = ImageLoader.getImage("src/main/resources/assets/images/coinbank.png");
        this.mowerShopImg = ImageLoader.getImage("src/main/resources/assets/images/icon_roofcleaner.png");
        this.chocolateImg = ImageLoader.getImage("src/main/resources/assets/images/chocolate.png");
        this.iceShroomImg = ImageLoader.getImage("src/main/resources/assets/reanim/IceShroom_head.png");
        this.shovelImg = ImageLoader.getImage("src/main/resources/assets/images/Shovel.png");
        this.snailBodyImg = ImageLoader.getImage("src/main/resources/assets/reanim/Stinky_body.png");
        this.snailShellImg = ImageLoader.getImage("src/main/resources/assets/reanim/Stinky_shell.png");
        this.btnBackImg = ImageLoader.getImage("src/main/resources/assets/images/Almanac_CloseButton.png");
        this.btnBackHlImg = ImageLoader.getImage("src/main/resources/assets/images/Almanac_CloseButtonHighlight.png");

        items.add(new ShopItem("snail", "Pet Ốc Sên Stinky", "Tự động bò nhặt xu và mặt trời rơi trên sân", ShopManager.PRICE_PET_SNAIL, "SNAIL"));
        items.add(new ShopItem("chocolate", "Kẹo Chocolate", "Tăng 250% tốc độ bò của Pet trong 3 phút", ShopManager.PRICE_CHOCOLATE, "CHOCO"));
        items.add(new ShopItem("lawnmower", "Xe Cắt Cỏ Càn Quét", "Kích hoạt phóng xe cắt cỏ càn quét toàn bộ 1 hàng ngang", ShopManager.PRICE_LAWN_MOWER, "MOWER"));
        items.add(new ShopItem("iceshroom", "Bùa Đóng Băng", "Đóng băng và làm chậm toàn sân đấu trong 5 giây", ShopManager.PRICE_ICE_SHROOM, "ICE"));
        items.add(new ShopItem("shovel", "Xẻng Vàng", "Đào thu hồi cây được hoàn trả 50% số Mặt trời (dùng theo lượt)", ShopManager.PRICE_GOLDEN_SHOVEL, "SHOVEL"));
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
        String title = "CỬA HÀNG CRAZY DAVE";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (sw - tw) / 2, 60);

        // Nút quay lại đồ họa PvZ
        btnBack.setBounds(40, 28, 115, 36);
        boolean backHover = btnBack.contains(gm.mouseX, gm.mouseY);
        if (btnBackImg != null) {
            Image currentBackImg = (backHover && btnBackHlImg != null) ? btnBackHlImg : btnBackImg;
            g.drawImage(currentBackImg, btnBack.x, btnBack.y, btnBack.width, btnBack.height, null);
            g.setFont(new Font("Arial", Font.BOLD, 13));
            g.setColor(backHover ? Color.WHITE : new Color(220, 220, 220));
            String bText = "QUAY LẠI";
            int btw = g.getFontMetrics().stringWidth(bText);
            g.drawString(bText, btnBack.x + (btnBack.width - btw) / 2, btnBack.y + (btnBack.height + 9) / 2);
        } else {
            drawCustomButton(g, btnBack, "⬅ QUAY LẠI", gm.mouseX, gm.mouseY, new Color(139, 69, 19));
        }

        // Khung ví tiền ở góc phải (co giãn linh hoạt theo số chữ số)
        WalletManager wm = WalletManager.getInstance();
        int coins = wm.getCoins();
        String coinStr = "$ " + String.format(java.util.Locale.US, "%,d", coins);
        Font coinFont = new Font("Arial", Font.BOLD, 22);
        FontMetrics fm = g.getFontMetrics(coinFont);
        int textW = fm.stringWidth(coinStr);
        int bankW = Math.max(190, textW + 68);
        int bankX = sw - bankW - 24;
        int bankY = 25;

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(bankX, bankY, bankW, 48, 12, 12);
        g.setColor(new Color(255, 215, 0));
        g.drawRoundRect(bankX, bankY, bankW, 48, 12, 12);

        if (coinBankImg != null) {
            g.drawImage(coinBankImg, bankX + 8, bankY + 4, 40, 40, null);
        } else {
            g.fillOval(bankX + 12, bankY + 10, 28, 28);
        }
        g.setColor(new Color(255, 220, 50));
        g.setFont(coinFont);
        g.drawString(coinStr, bankX + 54, bankY + 32);

        // Thông báo
        if (noticeTimer > 0) {
            g.setColor(noticeColor);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            int nw = g.getFontMetrics().stringWidth(noticeMsg);
            g.drawString(noticeMsg, (sw - nw) / 2, 95);
        }

        // Danh sách 5 mặt hàng bố trí cân đối: Hàng 1 có 3 ô, Hàng 2 có 2 ô (căn giữa)
        int cardW = 260;
        int cardH = 245;
        int gapX = 24;
        int gapY = 24;
        int startY = 120;

        WalletManager.SaveData data = wm.getData();

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            int row = i < 3 ? 0 : 1;
            int col = i < 3 ? i : (i - 3);
            int itemsInRow = row == 0 ? 3 : 2;
            int totalRowW = itemsInRow * cardW + (itemsInRow - 1) * gapX;
            int rowStartX = (sw - totalRowW) / 2;

            int cx = rowStartX + col * (cardW + gapX);
            int cy = startY + row * (cardH + gapY);
            item.cardRect = new Rectangle(cx, cy, cardW, cardH);

            boolean hover = item.cardRect.contains(gm.mouseX, gm.mouseY);
            g.setColor(hover ? new Color(60, 45, 30) : new Color(48, 35, 24));
            g.fillRoundRect(cx, cy, cardW, cardH, 16, 16);
            g.setColor(new Color(180, 140, 90));
            g.drawRoundRect(cx, cy, cardW, cardH, 16, 16);

            // Icon đồ họa thực tế từ images thay thế emoji
            if (item.id.equals("snail") && snailBodyImg != null && snailShellImg != null) {
                int snailW = 50;
                int snailH = 44;
                int ix = cx + (cardW - snailW) / 2;
                int iy = cy + 22;
                g.drawImage(snailBodyImg, ix, iy, 36, 40, null);
                g.drawImage(snailShellImg, ix + 8, iy - 2, 44, 42, null);
            } else if (item.id.equals("chocolate") && chocolateImg != null) {
                int iw = 40;
                int ih = 46;
                g.drawImage(chocolateImg, cx + (cardW - iw) / 2, cy + 20, iw, ih, null);
            } else if (item.id.equals("lawnmower") && mowerShopImg != null) {
                int iw = 56;
                int ih = 43;
                g.drawImage(mowerShopImg, cx + (cardW - iw) / 2, cy + 22, iw, ih, null);
            } else if (item.id.equals("iceshroom") && iceShroomImg != null) {
                int iw = 56;
                int ih = 30;
                g.drawImage(iceShroomImg, cx + (cardW - iw) / 2, cy + 28, iw, ih, null);
            } else if (item.id.equals("shovel") && shovelImg != null) {
                int iw = 46;
                int ih = 46;
                g.drawImage(shovelImg, cx + (cardW - iw) / 2, cy + 20, iw, ih, null);
            } else {
                g.setColor(new Color(200, 160, 100));
                g.setFont(new Font("Arial", Font.BOLD, 16));
                int iw = g.getFontMetrics().stringWidth(item.iconEmoji);
                g.drawString(item.iconEmoji, cx + (cardW - iw) / 2, cy + 45);
            }

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
            } else if (item.id.equals("iceshroom")) {
                status = "Đang có: x" + data.iceShroomBlastCount;
            } else if (item.id.equals("shovel")) {
                status = "Đang có: x" + data.goldenShovelCount;
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
            gm.closeShop();
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
                        if (success && gm.petSnail == null) {
                            gm.petSnail = new com.team.gardendefense.model.entity.pet.PetSnail(50, gm.getMapHeight() - 60);
                        }
                        break;
                    case "chocolate":
                        success = sm.buyChocolate();
                        break;
                    case "lawnmower":
                        success = sm.buyLawnMower();
                        break;
                    case "iceshroom":
                        success = sm.buyIceShroom();
                        break;
                    case "shovel":
                        success = sm.buyGoldenShovel();
                        break;
                }

                if (success) {
                    String notice = "Đã mua thành công " + item.name + "!";
                    if (item.id.equals("shovel")) {
                        notice += " (Hiện có: x" + wm.getData().goldenShovelCount + ")";
                    }
                    showNotice(notice, Color.GREEN);
                } else {
                    showNotice("Không thể mua vật phẩm!", Color.YELLOW);
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

