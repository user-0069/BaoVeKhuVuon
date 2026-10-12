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
- **Cơ chế Bắn (Targeting):** Các cây ngắm bắn theo trục thẳng (Trái hoặc Phải). Người chơi dùng phím A/D (hoặc Mũi tên Trái/Phải), HOẶC **click trực tiếp vào nút [HƯỚNG]** trên góc phải màn hình để chọn hướng bắn cho cây trước khi đặt.
- **Hệ thống Thẻ Bài (Battle Cards):** UI sử dụng ảnh thẻ bài thực tế từ hệ thống tài nguyên để hiển thị trên thanh công cụ (Seed Bank).
- **Thực vật (Plants) [Được chuẩn hóa ID]:**
  - **ID 1:** Sunflower (50 Sun): Sinh 25 Sun mỗi 5 giây.
  - **ID 2:** Peashooter (50 Sun): Bắn đạn đậu, sát thương 1.
  - **ID 3:** Sniper (100 Sun): Bắn xa, sát thương 2.
  - **ID 4:** WallNut (50 Sun): Máu 30, chỉ trồng trên đường chặn quái.
  - **ID 5:** SnowPea (150 Sun): Đạn đóng băng, làm chậm tốc độ quái.
  - **ID 6:** CherryBomb (150 Sun): Sau 0.5s phát nổ tiêu diệt toàn bộ zombie trong phạm vi 3x3 quanh nó. Dùng 1 lần. Đặt trên đường/chậu.
  - **ID 7:** Chomper (150 Sun): Nuốt chửng 1 zombie (tử vong ngay) lọt vào phạm vi 3x3 của nó. Sau khi nuốt sẽ mất 10s nhai (cooldown) không thể tấn công. Đặt trên đường/chậu.
- **Thây ma (Zombies) [Được chuẩn hóa ID]:**
  - **ID 1:** Red Zombie (5 HP): Đi theo đường, cắn cây ngáng đường.
  - **ID 2:** Shooter Zombie (Cam - 3 HP): Vừa đi vừa ném đá (rock) phá cây từ xa nếu phát hiện cây trong tầm ngắm. Lật ảnh tự động theo hướng di chuyển.

## 4. Công cụ Thiết kế Màn chơi (Level Editor)
- Trò chơi tích hợp sẵn công cụ **Level Editor trực quan bằng Java Swing** (`com.team.gardendefense.editor.LevelEditor`).
- **Cách mở:**
  - Cách 1: Bấm nút **"LEVEL EDITOR"** ngay trên Menu chính của Game.
  - Cách 2: Chạy trực tiếp class `LevelEditor.java` trong IDE hoặc lệnh Maven.
- **Tính năng của Level Editor:**
  - 🎨 **Tùy chỉnh Kích thước Map:** Thay đổi số Cột (Cols) và Hàng (Rows) linh hoạt, tự động co giãn lưới ô vuông.
  - 🖌️ **Tô màu bản đồ bằng chuột:** Chọn cọ Cỏ (0), Đường đi (1), Bệ đặt tháp (2), click và kéo rê chuột để vẽ. Hỗ trợ chuột phải để xóa.
  - 🌱 **Chọn Cây cho phép (Allowed Plants):** Chọn nhanh các loại cây được mang vào màn từ danh sách checkbox 10 loại cây.
  - 🧟 **Thiết kế Wave & Đường đi đa luồng (Multi-Spawners):** Tạo danh sách Wave, thêm nhiều Cửa quái (Spawner) chạy song song, chọn khoảng cách nhả quái (`spawnInterval`), danh sách quái vật.
  - 📍 **Click cắm cờ Waypoints trực quan:** Bật chế độ đặt Waypoint và click trực tiếp lên Map, Editor sẽ tự động vẽ cờ mốc và đường nối mũi tên chỉ đường đi của Zombie!
  - 💾 **Lưu & Mở file JSON:** Mở bất kỳ file `level_X.json` để sửa hoặc bấm Save để tự động xuất ra JSON chuẩn chỉ.

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
        │       ├── editor/              # Công cụ thiết kế màn chơi trực quan (GUI)
        │       │   └── LevelEditor.java # Ứng dụng Level Editor (Java Swing)
        │       ├── model/               # Các mô hình dữ liệu và thực thể
        │       │   ├── entity/
        │       │   │   ├── monster/     # Abstract Monster, Zombie, ShooterZombie
        │       │   │   ├── plant/       # Abstract Plant, Hướng Dương, Peashooter, CherryBomb, Chomper vv.
        │       │   │   └── projectile/  # Đạn của Cây (Pea) và Đạn của Quái (ZombieBone)
        │       │   ├── level/           # Các POJO hứng dữ liệu từ JSON (Level, Wave, Spawner, Waypoint)
        │       │   └── map/             # Bản đồ (Grid, Tile)
        │       ├── utils/
        │       │   ├── Constants.java   # Nơi chứa các hằng số, chỉ số Game (Config / Balancing)
        │       │   └── ImageLoader.java # Bộ nạp và cache hình ảnh sprites/cards
        │       ├── view/
        │       │   ├── renderer/
        │       │   │   └── GamePanel.java # Vòng lặp Game (Game Loop / UI Frame)
        │       │   └── ui/
        │       │       └── HUD.java       # Giao diện hiển thị
        │       └── main/
        │           └── GameLauncher.java  # Điểm chạy chương trình chính (Main)
        └── resources/
            ├── assets/                  # Nơi chứa tài nguyên đa phương tiện (Media)
            │   ├── images/              # Hình ảnh (Sprites, Background, UI, thẻ bài, vv.)
            │   └── sounds/              # Âm thanh (BGM, SFX)
            └── data/                    # Nơi chứa dữ liệu Levels
                ├── LEVEL_DESIGN_GUIDE.md
                └── level_1.json
```
