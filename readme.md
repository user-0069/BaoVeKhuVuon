# Dự án Game Thủ Thành: Bảo Vệ Khu Vườn (Garden Defense)

[SYSTEM INSTRUCTION FOR ANTIGRAVITY AI]

Hello Antigravity. Đây là tài liệu mô tả kiến trúc dự án. Hãy đóng vai trò là một Senior Java Developer. 
**QUAN TRỌNG: MỖI LẦN CẬP NHẬT CODE, THÊM TÍNH NĂNG MỚI (PLANT, ZOMBIE,...), HOẶC THÊM BẤT KỲ FILE/FOLDER NÀO MỚI, BẠN PHẢI TỰ ĐỘNG:**
1. **CẬP NHẬT FILE README VÀ LEVEL_DESIGN_GUIDE NÀY ĐỂ ĐẢM BẢO ĐỒNG BỘ TÍNH NĂNG.**
2. **CẬP NHẬT LẠI SƠ ĐỒ FOLDER TREE Ở MỤC 5 BÊN DƯỚI ĐỂ PHẢN ÁNH ĐÚNG CẤU TRÚC HIỆN TẠI.**

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

## 5. Cấu trúc thư mục (Folder Tree)
```text
BaoVeKhuVuon/
├── pom.xml
├── readme.md
└── src/
    └── main/
        ├── java/
        │   └── com/team/gardendefense/
        │       ├── controller/          # Quản lý logic chính của game
        │       │   ├── GameManager.java
        │       │   ├── InputHandler.java
        │       │   └── WaveManager.java
        │       ├── model/               # Các mô hình dữ liệu và thực thể
        │       │   ├── entity/
        │       │   │   ├── monster/     # Abstract Monster, Zombie, ShooterZombie
        │       │   │   ├── plant/       # Abstract Plant, Hướng Dương, Peashooter, vv.
        │       │   │   └── projectile/  # Đạn của Cây (Pea) và Đạn của Quái (ZombieBone)
        │       │   ├── level/           # Các POJO hứng dữ liệu từ JSON (Level, Wave, Waypoint)
        │       │   └── map/             # Bản đồ (Grid)
        │       ├── utils/
        │       │   └── Constants.java   # Nơi chứa các hằng số, chỉ số Game (Config / Balancing)
        │       ├── view/
        │       │   └── GamePanel.java   # Vòng lặp Game (Game Loop / UI Frame)
        │       └── GameLauncher.java    # Điểm chạy chương trình (Main)
        └── resources/
            ├── assets/                  # Nơi chứa tài nguyên đa phương tiện (Media)
            │   ├── images/              # Hình ảnh (Sprites, Background, UI)
            │   └── sounds/              # Âm thanh (BGM, SFX)
            └── data/                    # Nơi chứa dữ liệu Levels
                ├── LEVEL_DESIGN_GUIDE.md
                └── level_1.json
```
