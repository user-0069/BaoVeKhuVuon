# Dự án Game Thủ Thành: Bảo Vệ Khu Vườn (Garden Defense)

[SYSTEM INSTRUCTION FOR ANTIGRAVITY AI]

Hello Antigravity. Đây là tài liệu mô tả kiến trúc dự án. Hãy đóng vai trò là một Senior Java Developer. 
**QUAN TRỌNG: MỖI LẦN CẬP NHẬT CODE, THÊM TÍNH NĂNG MỚI (PLANT, ZOMBIE,...), HOẶC THÊM BẤT KỲ FILE/FOLDER NÀO MỚI, BẠN PHẢI TỰ ĐỘNG:**
1. **CẬP NHẬT FILE README VÀ LEVEL_DESIGN_GUIDE NÀY ĐỂ ĐẢM BẢO ĐỒNG BỘ TÍNH NĂNG.**
2. **CẬP NHẬT LẠI SƠ ĐỒ FOLDER TREE Ở MỤC BÊN DƯỚI ĐỂ PHẢN ÁNH ĐÚNG CẤU TRÚC HIỆN TẠI.**

---

## 1. Tổng quan dự án
- **Thể loại:** Tower Defense (Kết hợp Carrot Defense và Plants vs Zombies).
- **Nền tảng:** PC (Desktop Application).
- **Ngôn ngữ:** Java (JDK 11 trở lên).
- **Phụ thuộc (Dependencies):** Thư viện `Gson` (Dùng để parse Map & Level JSON, Save/Load ví tiền).
- **Mô hình kiến trúc:** MVC (Model - View - Controller & Modular Systems).

---

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

---

## 3. Hệ thống Kinh tế Toàn cục & Tính năng Coin (Meta-Economy System)

Game tích hợp trọn bộ hệ sinh thái tiền tệ Meta-game mang phong cách chuẩn **Plants vs. Zombies 1 (PvZ 1)**:

### 3.1. Bảng Tỉ lệ Rơi Coin (Loot Table & Drop Rates)
Khi tiêu diệt quái vật hoặc hoàn thành wave, hệ thống kích hoạt tính toán ngẫu nhiên có trọng số (Weighted Random):
- **Xu Bạc (Silver Coin - $10):** Tỉ lệ rơi **20%** từ quái thường, quái có phụ kiện.
- **Xu Vàng (Gold Coin - $50):** Tỉ lệ rơi **5%** từ quái thường, **35%** từ Mini-Boss (Shooter Zombie, quái to).
- **Kim Cương (Diamond - $1000):** Tỉ lệ rơi **1%** từ quái thường, **5%** từ Mini-Boss, và **100% thưởng chắc chắn** khi chiến thắng Wave cuối cùng của màn chơi.
- **Tỉ lệ không rơi gì:** **74%** cho quái thường (tạo cảm giác phấn khích và giá trị khi nhặt được xu).

### 3.2. Hiệu ứng Vật lý & Tương tác (Juice & UX)
- **Spawn (Nảy Parabol):** Khi quái chết, đồng xu bắn vọt lên theo đường cong parabol tự nhiên với gia tốc trọng lực và độ văng ngang ngẫu nhiên, nảy nhẹ một nhịp khi chạm đất.
- **Thời gian tồn tại (Despawn):** Đồng xu nằm trên sân tối đa **6 giây (360 frames)**. Trong **2 giây cuối**, đồng xu nhấp nháy mờ dần báo hiệu trước khi biến mất.
- **Interaction:** Người chơi có thể **Di chuột qua (Hover)** hoặc **Click chuột** để nhặt ngay lập tức.
- **Collect Motion & Pitch Combo:** Khi được nhặt, đồng xu tự động thu nhỏ dần kích thước và bay mượt mà (Lerp) về biểu tượng Ví tiền `coinbank.png` ở góc trên màn hình. Kèm theo hiệu ứng âm thanh `ting` trong trẻo với cao độ (pitch) tăng dần theo chuỗi combo liên tiếp!

### 3.3. Cây Hoa Cúc (Marigold - ID 11)
- Giá trồng: **50 Sun**, Máu: **5 HP**.
- Cứ sau mỗi **24 giây (1440 frames)**, cây sẽ tự động nở hoa phát sáng và nhả ra 1 đồng xu ngẫu nhiên (tỉ lệ: **85% Xu Bạc, 15% Xu Vàng**).

### 3.4. Linh vật Pet Ốc Sên Stinky (Auto Collector)
- Linh vật tự động bò tuần tra trên màn hình, quét tìm đồng xu hoặc mặt trời gần nhất và tự động nhặt giúp người chơi khi tới gần.
- Có thể cho ăn **Kẹo Chocolate** mua từ Shop để tăng **250% tốc độ di chuyển** trong vòng 3 phút (kèm hiệu ứng hào quang vàng rực rỡ).

### 3.5. Cửa hàng Crazy Dave (Meta Shop)
Giao diện Shop chuyên nghiệp truy cập được từ Menu chính hoặc nút Shop trực tiếp trong trận đấu:
1. **Pet Ốc Sên Stinky ($3,000):** Mở khóa vĩnh viễn linh vật hỗ trợ nhặt xu tự động.
2. **Kẹo Chocolate ($50):** Tăng tốc 250% tốc độ di chuyển của Pet trong 3 phút.
3. **Xe Cắt Cỏ Dự Phòng ($500):** Khôi phục xe cắt cỏ phòng thủ khi bị zombie chọc thủng hàng.
4. **Phân Bón Siêu Cấp ($200):** Năng lượng tức thì kích hoạt kỹ năng đặc biệt của cây.
5. **Bùa Đóng Băng ($350):** Đóng băng và làm chậm toàn bộ quái trên màn hình trong 5 giây.
6. **Xẻng Vàng ($2,500):** Đào thu hồi cây sẽ hoàn trả lại ngay 50% số Mặt trời đã dùng.
7. **Map Đập Hũ - Vasebreaker ($5,000):** Mở khóa chế độ Minigame đập hũ giải đố.
8. **Map I, Zombie ($5,000):** Mở khóa chế độ điều khiển Zombie công thành.

### 3.6. Bảng Tùy Chọn & Tạm Dừng Trong Trận (In-Game Options / Pause Dialog)
Chuẩn phong cách bia đá và biển gỗ nổi tiếng của Plants vs. Zombies 1:
- **Nút MENU Biển Gỗ:** Nằm ở góc trên bên phải màn hình trận đấu (`btnInGameMenu`) với viền vàng 3D, hiệu ứng sáng viền khi rê chuột và phát âm thanh tương tác.
- **Phím tắt tiện lợi:** Nhấn **ESC** hoặc phím **P** bất kỳ lúc nào để Tạm dừng trận đấu và mở Bảng tùy chọn.
- **Bảng Bia Đá "GAME PAUSED":** Khi tạm dừng, toàn bộ trận đấu dừng lại và xuất hiện hộp thoại bia đá đục đẽo với đinh tán góc và viền nổi 3D:
  1. **TIẾP TỤC (RESUME):** Trở lại trận đấu ngay lập tức.
  2. **CHƠI LẠI TỪ ĐẦU (RESTART):** Khởi động lại màn chơi hiện tại.
  3. **CỬA HÀNG CRAZY DAVE 🛒:** Mở nhanh giao diện Shop để mua vật phẩm hỗ trợ.
  4. **ÂM THANH [ BẬT 🔊 / TẮT 🔇 ]:** Bật/Tắt âm thanh hiệu ứng (SFX).
  5. **CHỌN MÀN KHÁC 🗺️:** Trở ra danh sách chọn Level.
  6. **VỀ MENU CHÍNH 🏠:** Quay lại màn hình chính của trò chơi.
- **Màn hình Kết Thúc Trận:** Giao diện **GAME OVER ("THE ZOMBIES ATE YOUR BRAINS!")** và **VICTORY** được đồng bộ hóa với bia đá khắc chữ cổ điển PvZ.

---

## 4. Danh sách Thực vật & Quái vật (Plants & Zombies)
- **Thực vật (Plants) [Chuẩn hóa ID 1 - 11]:**
  - **ID 1:** Sunflower (50 Sun): Sinh 25 Sun mỗi 5 giây.
  - **ID 2:** Peashooter R (50 Sun): Bắn đạn đậu sát thương 1 sang PHẢI.
  - **ID 8:** Peashooter L (50 Sun): Bắn đạn đậu sát thương 1 sang TRÁI.
  - **ID 3:** Sniper R (100 Sun): Bắn xa sát thương 2 sang PHẢI.
  - **ID 9:** Sniper L (100 Sun): Bắn xa sát thương 2 sang TRÁI.
  - **ID 4:** WallNut (50 Sun): Máu 30 HP, dùng làm khiên chặn đường quái.
  - **ID 5:** SnowPea R (150 Sun): Đạn băng làm chậm quái sang PHẢI.
  - **ID 10:** SnowPea L (150 Sun): Đạn băng làm chậm quái sang TRÁI.
  - **ID 6:** CherryBomb (150 Sun): Nổ tiêu diệt zombie trong ô 3x3. Dùng 1 lần.
  - **ID 7:** Chomper (150 Sun): Nuốt chửng 1 zombie, mất 10s nhai.
  - **ID 11:** Marigold (50 Sun): Cây hoa cúc tự động nhả xu bạc/vàng sau mỗi 24 giây.
- **Thây ma (Zombies):**
  - **ID 1:** Red Zombie (5 HP): Quái thường đi theo đường, cắn cây ngáng đường.
  - **ID 2:** Shooter Zombie (3 HP): Mini-Boss ném xương từ xa, tỉ lệ rớt xu vàng/kim cương cao hơn.

---

## 5. Cấu trúc thư mục (Folder Tree)
```text
BaoVeKhuVuon/
├── pom.xml
├── readme.md
├── wallet_save.json                     # File lưu trữ số dư và mở khóa Meta Shop
└── src/
    └── main/
        ├── java/
        │   └── com/team/gardendefense/
        │       ├── controller/          # Quản lý logic điều khiển chính của game
        │       │   ├── GameManager.java # Vòng lặp gameplay, tích hợp Coin, Pet & HUD
        │       │   ├── InputHandler.java# Xử lý Click, Hover nhặt xu, Drag và Phím tắt
        │       │   └── WaveManager.java # Quản lý spawn các đợt quái
        │       ├── editor/              # Công cụ thiết kế màn chơi trực quan (GUI)
        │       │   └── LevelEditor.java # Ứng dụng Level Editor (Java Swing)
        │       ├── model/               # Các mô hình dữ liệu và thực thể
        │       │   ├── entity/
        │       │   │   ├── coin/        # Hệ thống Tiền tệ
        │       │   │   │   ├── Coin.java       # Thực thể xu (Nảy parabol, nhấp nháy, bay về UI)
        │       │   │   │   └── CoinType.java   # Enum Xu Bạc ($10), Xu Vàng ($50), Kim Cương ($1000)
        │       │   │   ├── monster/     # Abstract Monster, Zombie, ShooterZombie
        │       │   │   ├── pet/         # Hệ thống Linh vật hỗ trợ
        │       │   │   │   ├── IAutoCollector.java # Interface chuẩn cho Pet tự nhặt đồ
        │       │   │   │   └── PetSnail.java       # Ốc sên Stinky AI tự bò nhặt xu & boost Chocolate
        │       │   │   ├── plant/       # Sunflower, Peashooter, Marigold, Chomper vv.
        │       │   │   │   └── Marigold.java       # Cây hoa cúc nhả xu sau mỗi 24 giây
        │       │   │   └── projectile/  # Đạn của Cây (Pea) và Đạn của Quái (ZombieBone)
        │       │   ├── level/           # POJO dữ liệu JSON (Level, Wave, Spawner, Waypoint)
        │       │   └── map/             # Bản đồ (Grid, Tile)
        │       ├── system/              # Các hệ thống nghiệp vụ toàn cục
        │       │   ├── AudioManager.java# Tổng hợp âm thanh ting pitch combo & phát audio
        │       │   ├── LootTable.java   # Thuật toán Weighted Random Drop Rate
        │       │   ├── ShopManager.java # Logic mua sắm vật phẩm, mở khóa minigame
        │       │   └── WalletManager.java # Quản lý tài sản, Save/Load JSON, hiệu ứng số bay
        │       ├── utils/
        │       │   ├── Constants.java   # Thông số Game (Drop rate, Coin value, Balancing)
        │       │   └── ImageLoader.java # Bộ nạp và cache hình ảnh sprites/cards
        │       ├── view/
        │       │   ├── renderer/
        │       │   │   └── GamePanel.java # Vòng lặp Game (Game Loop / UI Frame)
        │       │   └── ui/
        │       │       ├── HUD.java       # Giao diện hiển thị
        │       │       ├── PauseDialog.java # Bảng Options / Tạm dừng phong cách bia đá PvZ
        │       │       └── ShopScreen.java# Giao diện Cửa Hàng Crazy Dave chuyên nghiệp
        │       └── main/
        │           └── GameLauncher.java  # Điểm chạy chương trình chính (Main)
        └── resources/
            ├── assets/                  # Nơi chứa tài nguyên đa phương tiện (Media gốc PvZ 1)
            │   ├── images/              # Hình ảnh (Sprites, Background, coinbank.png, cards)
            │   ├── reanim/              # Sprites Coin Bạc, Vàng, Kim Cương, Marigold, Stinky
            │   └── sounds/              # Âm thanh (coin.ogg, diamond.au, moneyfalls.ogg)
            └── data/                    # Nơi chứa dữ liệu Levels
                ├── LEVEL_DESIGN_GUIDE.md
                ├── level_1.json
                └── level_2.json
```

---

## 6. Hướng dẫn Biên dịch và Chạy Game

### Cách 1: Chạy trực tiếp trong VS Code (Khuyên dùng)
- Mở file `src/main/java/com/team/gardendefense/main/GameLauncher.java`.
- Nhấn nút **Run Java ▶️** ở góc trên bên phải giao diện VS Code.

### Cách 2: Chạy nhanh bằng file script có sẵn (Khuyên Dùng Nhất)
- **Windows Command Prompt:** Nhấp đúp chuột vào file `run.bat` hoặc gõ `run.bat` trong Terminal.
- **PowerShell:** Gõ `.\run.ps1`

### Cách 3: Biên dịch và chạy thủ công qua Terminal
1. **Biên dịch:**
   ```powershell
   javac -encoding UTF-8 -sourcepath src/main/java -cp "lib/gson-2.10.1.jar" -d target/classes src/main/java/com/team/gardendefense/main/GameLauncher.java
   ```
2. **Khởi chạy:**
   ```powershell
   java -cp "target/classes;lib/gson-2.10.1.jar" com.team.gardendefense.main.GameLauncher
   ```
