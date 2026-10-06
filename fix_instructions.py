import re

with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'r') as f:
    content = f.read()

instructions = """
    private void drawInstructions(Graphics2D g) {
        g.setColor(new Color(30, 30, 40)); g.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40)); g.drawString("HƯỚNG DẪN CHƠI", 330, 60);
        g.setFont(new Font("Arial", Font.PLAIN, 18)); int y = 100;
        
        g.drawString("Quy tắc: Tiêu diệt hết Quái -> Thắng. Quái lọt lưới trừ 50 Sun. Hết Sun -> Thua.", 50, y);
        
        y += 40; g.setColor(Color.YELLOW); g.drawString("CÁCH TRỒNG & THỰC VẬT:", 50, y);
        g.setColor(Color.WHITE);
        y += 25; g.drawString("- Bạn chỉ có thể trồng cây trên Chậu (Pot) hoặc trực tiếp trên Đường (Path).", 70, y);
        y += 25; g.drawString("- 1. Hướng Dương (50 Sun): Tạo 25 Sun mỗi 5 giây.", 70, y);
        y += 25; g.drawString("- 2/3/5. Peashooter, Sniper, SnowPea: Có 2 thẻ bài riêng biệt cho hướng bắn Trái (L) và Phải (R).", 70, y);
        y += 25; g.drawString("- 4. WallNut (50 Sun): Siêu trâu bò (30 HP), dùng để chặn đường quái.", 70, y);
        y += 25; g.drawString("- 6. CherryBomb (150 Sun): Nổ banh xác toàn bộ Zombie trong ô 3x3 rồi biến mất.", 70, y);
        y += 25; g.drawString("- 7. Chomper (150 Sun): Nuốt 1 Zombie trong ô 3x3, mất 10s để nhai (không thể đánh lúc nhai).", 70, y);
        
        y += 40; g.setColor(Color.RED); g.drawString("THÂY MA (ZOMBIES):", 50, y);
        g.setColor(Color.WHITE);
        y += 25; g.drawString("- Red Zombie (5 HP): Đi cắn mọi cây ngáng đường.", 70, y);
        y += 25; g.drawString("- Shooter Zombie (3 HP): Vừa đi vừa ném xương phá cây từ xa. Nếu đụng cây sẽ đứng cắn.", 70, y);
        
        drawButton(g, btnBack, "QUAY LẠI", mouseX, mouseY);
    }
"""

content = re.sub(r'private void drawInstructions.*?drawButton\(g, btnBack, "QUAY LẠI", mouseX, mouseY\);\s*\}', instructions.strip(), content, flags=re.DOTALL)

with open('src/main/java/com/team/gardendefense/controller/GameManager.java', 'w') as f:
    f.write(content)
