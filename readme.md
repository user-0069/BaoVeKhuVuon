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
Toàn bộ dữ liệu màn chơi đã được tách riêng ra file JSON (ví dụ `level_1.json`, `level_2.json`). Nhờ vậy, người thiết kế màn chơi không cần biết code Java vẫn có thể tạo Map mới.
- **Hệ thống Chọn Màn (Level Selection):** Giao diện "CHỌN LEVEL" trực quan từ Menu chính, tự động quét toàn bộ file `level_*.json` trong hệ thống và hiển thị các thẻ bài thông số (Sun ban đầu, số đợt quái, kích thước Map) để người chơi tự do lựa chọn.
- **Kích thước Map Linh Hoạt (Dynamic Map Dimensions):** Game tự động nhận diện và mở rộng kích thước cửa sổ / lưới ô vuông theo số cột và số hàng của từng màn chơi (hỗ trợ cả các map lớn như 23x17).
- **Tiến trình Màn chơi:** Khi chiến thắng một màn chơi (`VICTORY!`), game cung cấp nút **"MÀN TIẾP THEO ▶"** để sang ngay màn kế tiếp hoặc nút **"CHỌN MÀN KHÁC"**.
- **Map:** Ma trận 2D (0: Cỏ, 1: Đường đi, 2: Bệ trồng cây).
- **Waypoints:** Mảng các điểm neo {x, y} để định tuyến đường đi gập ghềnh cho Zombie. Thuật toán của Zombie sẽ tự động di chuyển bám sát theo các toạ độ này.
- **AllowedPlants:** Mảng ID các loại cây được phép mang vào màn chơi. Thanh công cụ UI (Thẻ bài) sẽ *tự động* render ra đúng những cây này.
- **Waves:** Mỗi màn chơi có nhiều đợt tấn công (Wave). Mỗi đợt có thời gian nghỉ, khoảng cách sinh quái, và danh sách thứ tự quái vật.
- **Thắng / Thua:** Quái đến cuối đường -> GAME OVER. Giết hết quái của tất cả Wave -> VICTORY.

## 3. Cấu trúc và Tính năng hiện tại
- **Hệ thống Game State:** Menu Chính -> Chọn Level -> Hướng Dẫn -> Chơi -> Game Over / Victory. Có nút Menu nhanh trên thanh HUD trong trận đấu để quay lại Menu bất kỳ lúc nào.
- **Cơ chế Tiền tệ (Sun):** Đọc `initialSun` từ JSON. Tự động rơi chậm (25 Sun mỗi 10 giây). Giết quái được 10 Sun. Quái lọt lưới trừ 50 Sun. 
- **Cơ chế Bắn (Targeting):** Các cây ngắm bắn theo trục thẳng (Trái hoặc Phải). Người chơi dùng phím A/D (hoặc Mũi tên Trái/Phải), HOẶC **click trực tiếp vào nút [HƯỚNG]** trên góc phải màn hình để chọn hướng bắn cho cây trước khi đặt.
- **Hệ thống Thẻ Bài (Battle Cards):** UI sử dụng ảnh thẻ bài thực tế từ hệ thống tài nguyên để hiển thị trên thanh công cụ (Seed Bank).
- **Thực vật (Plants) [Được chuẩn hóa ID 1 - 10]:**
  - **ID 1:** Sunflower (50 Sun): Sinh 25 Sun mỗi 5 giây.
  - **ID 2:** Peashooter R (50 Sun): Bắn đạn đậu sát thương 1 sang PHẢI.
  - **ID 8:** Peashooter L (50 Sun): Bắn đạn đậu sát thương 1 sang TRÁI.
  - **ID 3:** Sniper R (100 Sun): Bắn xa sát thương 2 sang PHẢI.
  - **ID 9:** Sniper L (100 Sun): Bắn xa sát thương 2 sang TRÁI.
  - **ID 4:** WallNut (50 Sun): Máu 30 HP, dùng làm khiên chặn đường quái.
  - **ID 5:** SnowPea R (150 Sun): Đạn băng làm chậm quái sang PHẢI.
  - **ID 10:** SnowPea L (150 Sun): Đạn băng làm chậm quái sang TRÁI.
  - **ID 6:** CherryBomb (150 Sun): Sau 0.5s phát nổ tiêu diệt toàn bộ zombie trong phạm vi 3x3 quanh nó. Dùng 1 lần.
  - **ID 7:** Chomper (150 Sun): Nuốt chửng 1 zombie (tử vong ngay) lọt vào phạm vi 3x3 của nó. Sau khi nuốt mất 10s nhai không thể tấn công.
- **Thây ma (Zombies) [Được chuẩn hóa ID]:**
  - **ID 1:** Red Zombie (5 HP): Đi theo đường, cắn cây ngáng đường.
  - **ID 2:** Shooter Zombie (Cam - 3 HP): Vừa đi vừa ném xương phá cây từ xa nếu phát hiện cây trong tầm ngắm. Lật ảnh tự động theo hướng di chuyển.

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
                ├── level_1.json
                └── level_2.json
```

## 6. Hướng dẫn Biên dịch và Chạy Game
- **Chạy trực tiếp trong VS Code (Khuyên dùng):**
  - Mở file `src/main/java/com/team/gardendefense/main/GameLauncher.java`.
  - Nhấp vào nút **Run Java ▶️** ở góc trên bên phải thanh công cụ.
- **Biên dịch bằng Terminal PowerShell:**
  > ⚠️ Luôn dùng cờ `--release 11` khi biên dịch bằng `javac` để đảm bảo tương thích với mọi phiên bản JDK (tránh lỗi `UnsupportedClassVersionError`):
  ```powershell
  javac --release 11 -cp "C:\Users\Admin\.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar" -sourcepath src\main\java -d target\classes (Get-ChildItem -Recurse -Filter "*.java" src\main\java | ForEach-Object { $_.FullName })
  ```
- **Chạy bằng Terminal PowerShell:**
  ```powershell
  java -cp "target/classes;C:\Users\Admin\.m2\repository\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar" com.team.gardendefense.main.GameLauncher
  ```

