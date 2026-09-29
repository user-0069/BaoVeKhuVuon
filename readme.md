# Dự án Game Thủ Thành: Bảo Vệ Khu Vườn (Garden Defense)

[SYSTEM INSTRUCTION FOR ANTIGRAVITY AI]

Hello Antigravity. Đây là tài liệu mô tả kiến trúc dự án. Hãy đóng vai trò là một Senior Java Developer. 
**QUAN TRỌNG: MỖI LẦN CẬP NHẬT CODE HAY THÊM TÍNH NĂNG MỚI (PLANT, ZOMBIE,...), BẠN PHẢI TỰ ĐỘNG CẬP NHẬT LUÔN FILE README NÀY ĐỂ ĐẢM BẢO TÀI LIỆU LUÔN ĐỒNG BỘ VỚI TÍNH NĂNG THỰC TẾ.**

## 1. Tổng quan dự án
- **Thể loại:** Tower Defense (Kết hợp Carrot Defense và Plants vs Zombies).
- **Nền tảng:** PC (Desktop Application).
- **Ngôn ngữ:** Java (JDK 11 trở lên).
- **Phụ thuộc (Dependencies):** Thư viện `Gson` (Dùng để parse Map & Level JSON).
- **Mô hình kiến trúc:** MVC.

## 2. Hệ thống Màn chơi (Level & Wave System)
Toàn bộ dữ liệu màn chơi đã được tách riêng ra file JSON (ví dụ `src/main/resources/data/level_1.json`). Nhờ vậy, người thiết kế màn chơi không cần biết code Java vẫn có thể tạo Map mới.
- **Map:** Ma trận 2D (0: Cỏ, 1: Đường đi, 2: Bệ trồng cây).
- **Waypoints:** Mảng các điểm neo {x, y} để định tuyến đường đi gập ghềnh cho Zombie. Thuật toán của Zombie sẽ tự động di chuyển bám sát theo các toạ độ này.
- **AllowedPlants:** Mảng ID các loại cây được phép mang vào màn chơi. Thanh công cụ UI (Thẻ bài) sẽ *tự động* render ra đúng những cây này.
- **Waves:** Mỗi màn chơi có nhiều đợt tấn công (Wave). Mỗi đợt có thời gian nghỉ, khoảng cách sinh quái, và danh sách thứ tự quái vật.
- **Thắng / Thua:** Hết Sun -> GAME OVER. Giết hết quái của tất cả Wave -> VICTORY.

## 3. Cấu trúc và Tính năng hiện tại
- **Hệ thống Game State:** Menu Chính -> Hướng Dẫn -> Chơi -> Game Over / Victory.
- **Cơ chế Tiền tệ (Sun):** Đọc `initialSun` từ JSON. Tự động rơi chậm (25 Sun mỗi 10 giây). Giết quái được 10 Sun. Quái lọt lưới trừ 50 Sun. 
- **Thực vật (Plants) [Được chuẩn hóa ID]:**
  - **ID 1:** Sunflower (50 Sun): Sinh 25 Sun mỗi 5 giây.
  - **ID 2:** Peashooter (50 Sun): Bắn gần, sát thương 1.
  - **ID 3:** Sniper (100 Sun): Bắn xa, sát thương 2.
  - **ID 4:** WallNut (50 Sun): Máu 30, chỉ trồng trên đường chặn quái.
  - **ID 5:** SnowPea (150 Sun): Đạn đóng băng, làm chậm tốc độ quái trong 3 giây.
- **Thây ma (Zombies) [Được chuẩn hóa ID]:**
  - **ID 1:** Red Zombie (5 HP): Đi theo đường, cắn cây ngáng đường.
  - **ID 2:** Shooter Zombie (Cam - 3 HP): Phát hiện cây trong tầm 250px sẽ dừng lại ném xương phá cây từ xa.

## 4. Hướng dẫn phát triển tiếp
- Để tạo màn chơi mới: Chỉ cần copy file `level_1.json`, đổi tên, chỉnh sửa mảng `map`, mảng `waypoints` (để bẻ góc cua) và thiết kế lại danh sách `waves`. Mã nguồn sẽ tự động thích ứng!
