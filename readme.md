# Dự án Game Thủ Thành: Bảo Vệ Khu Vườn (Garden Defense)

[SYSTEM INSTRUCTION FOR ANTIGRAVITY AI]

Hello Antigravity. Đây là tài liệu mô tả kiến trúc dự án. Hãy đóng vai trò là một Senior Java Developer. 
**QUAN TRỌNG: MỖI LẦN CẬP NHẬT CODE HAY THÊM TÍNH NĂNG MỚI (PLANT, ZOMBIE,...), BẠN PHẢI TỰ ĐỘNG CẬP NHẬT LUÔN FILE README NÀY ĐỂ ĐẢM BẢO TÀI LIỆU LUÔN ĐỒNG BỘ VỚI TÍNH NĂNG THỰC TẾ.**

## 1. Tổng quan dự án
- **Thể loại:** Tower Defense (Kết hợp Carrot Defense và Plants vs Zombies).
- **Nền tảng:** PC (Desktop Application).
- **Ngôn ngữ:** Java (JDK 11 trở lên).
- **UI Framework:** Java Swing / AWT.
- **Mô hình kiến trúc:** MVC.

## 2. Cấu trúc và Tính năng hiện tại
- **Hệ thống Game State:** Menu Chính -> Hướng Dẫn -> Chơi -> Game Over.
- **Bản đồ (Grid):** Lưới 16x12. Đường đi đất sét cho quái vật (ID=1). Bệ đỡ trồng cây (ID=2). Cỏ trang trí (ID=0).
- **Cơ chế Tiền tệ (Sun):** Khởi đầu 50 Sun. Tự động rơi chậm (25 Sun mỗi 10 giây). Giết quái được 10 Sun. Quái lọt lưới trừ 50 Sun. Dưới 0 Sun -> Thua (Game Over).
- **Thực vật (Plants):**
  1. Sunflower (50 Sun): Sinh 25 Sun mỗi 5 giây.
  2. Peashooter (50 Sun): Bắn gần, sát thương 1.
  3. Sniper (100 Sun): Bắn xa, sát thương 2.
  4. WallNut (50 Sun): Máu 30, chỉ trồng trên đường chặn quái.
  5. SnowPea (150 Sun): Đạn đóng băng, làm chậm tốc độ quái trong 3 giây.
- **Thây ma (Zombies):**
  1. Red Zombie (5 HP): Đi theo đường, cắn cây ngáng đường.
  2. Shooter Zombie (Cam - 3 HP): Phát hiện cây trong tầm 250px sẽ dừng lại ném xương phá cây từ xa.

## 3. Yêu cầu Lập trình hướng đối tượng (OOP)
Tính đóng gói, đa hình, kế thừa (Abstract Plant, Monster, Projectile) đều đã được áp dụng triệt để.

## 4. Hướng dẫn phát triển tiếp
(Liên tục cập nhật mỗi khi có thay đổi)
