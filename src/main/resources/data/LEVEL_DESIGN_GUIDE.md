# HƯỚNG DẪN THIẾT KẾ MÀN CHƠI (LEVEL DESIGN GUIDE)

Tài liệu này dành cho những người tạo màn chơi (Level Designer). Bất kỳ ai cũng có thể tự tạo một màn chơi (Level) mới bằng cách viết một file `.json` và ném vào thư mục `src/main/resources/data/`. Game sẽ tự động load dựa trên logic của hệ thống.

---

## 1. BẢNG TRA CỨU ID (ID REFERENCE)

### 🌿 THỰC VẬT (PLANTS) - Dùng trong mảng `allowedPlants`
| ID  | Tên Plant  | Giá (Sun) | Tính năng  | Chú thích                                                                |
| --- | ---------- | --------- | ---------- | ------------------------------------------------------------------------ |
| 1   | Sunflower  | 50        | Kinh tế    | Sinh 25 Sun mỗi 5 giây.                                                  |
| 2   | Peashooter | 50        | Bắn gần    | Bắn đạn đơn, sát thương 1.                                               |
| 3   | Sniper     | 100       | Bắn xa     | Tầm xa x2, sát thương 2, tốc độ đạn bay nhanh.                           |
| 4   | WallNut    | 50        | Chặn đường | Máu siêu trâu (30 HP). Cây DUY NHẤT được trồng trên `Đường đi (ID = 1)`. |
| 5   | SnowPea    | 150       | Đóng băng  | Bắn đạn băng làm chậm quái (tốc độ giảm 60%) trong 3 giây.               |

### 🧟 THÂY MA (ZOMBIES) - Dùng trong mảng `zombies` của từng Wave
| ID  | Tên Zombie     | HP  | Khả năng                                                               |
| --- | -------------- | --- | ---------------------------------------------------------------------- |
| 1   | Red Zombie     | 5   | Đi dọc theo đường, dừng lại cắn nát thực vật (Wallnut) ngáng đường.    |
| 2   | Shooter Zombie | 3   | Quét thấy cây trong tầm 250px sẽ dừng bước và ném xương phá cây từ xa. |

### 🗺️ BẢN ĐỒ (TILES) - Dùng trong ma trận 2D `map`
| ID  | Loại Ô lưới  | Ý nghĩa trong game                                                     |
| --- | ------------ | ---------------------------------------------------------------------- |
| 0   | Cỏ nền       | Ô trang trí (Không thể trồng cây, quái không đi vào).                  |
| 1   | Đường đất    | Đường dành riêng cho Zombie đi qua. (Chỉ cho phép trồng Wallnut ID=4). |
| 2   | Bệ trồng cây | Trạm phòng thủ. (Cho phép trồng các loại tháp bắn súng).               |

*(Lưu ý: Game đang dùng Grid 16 cột x 12 hàng. Tương đương độ phân giải 1024x768. Mỗi ô vuông (Tile) có kích thước 64x64 pixels).*

---

## 2. GIẢI THÍCH CHI TIẾT CẤU TRÚC FILE LEVEL JSON

Dưới đây là một bộ khung chuẩn và ý nghĩa của từng biến:

```json
{
  "id": 1,
  "name": "Level 1: The Beginning",
  "initialSun": 100,
  "startY": 320,
  "allowedPlants": [1, 2, 4],
  "map": [ ... Ma trận 12 dòng, 16 cột (Chứa các số 0, 1, 2) ... ],
  "waypoints": [ ... ],
  "waves": [ ... ]
}
```

* `id`: Số thứ tự của Level.
* `name`: Tên Level (Sẽ hiển thị góc trên cùng bên phải màn hình).
* `initialSun`: Số Sun cung cấp cho người chơi lúc bắt đầu ván.
* `startY`: Tọa độ Y (pixel) lúc quái xuất hiện ở ngoài rìa bên trái màn hình. (Ví dụ: Xuất hiện ở Hàng 5 => `startY` = 5 * 64 = 320).
* `allowedPlants`: Giới hạn các Thẻ bài (Plants) người chơi được phép mang vào màn này. Giao diện sẽ tự động vẽ thẻ bài dựa trên danh sách này.

---

## 3. CƠ CHẾ WAYPOINTS (DÒ ĐƯỜNG)

Vì Zombie không đủ thông minh để tự tìm đường, bạn phải cắm mốc (Waypoints) ở các **GÓC CUA** để nó biết khi nào cần bẻ lái. 
Zombie sẽ đi thẳng từng điểm một theo thứ tự trên mảng.

```json
"waypoints": [
  {"x": 640, "y": 320}, 
  {"x": 640, "y": 576},
  {"x": 1024, "y": 576}
]
```
**Cách tính toạ độ góc cua:** Lấy `Số cột * 64` hoặc `Số hàng * 64`.
*Ví dụ:* Góc cua nằm ở cột 10, hàng 5 -> `x` = 10 * 64 = 640, `y` = 5 * 64 = 320. 
*(Điểm waypoint cuối cùng thường nằm ngoài rìa phải màn hình `x = 1024` để Zombie thoát ra ngoài, kích hoạt trigger phạt 50 Sun).*

---

## 4. CHI TIẾT THÔNG SỐ CỦA TỪNG ĐỢT QUÁI (WAVES)

```json
"waves": [
  {
    "delayBefore": 300,
    "spawnInterval": 150,
    "zombies": [1, 1, 2, 1]
  }
]
```

* `delayBefore` (Thời gian nghỉ): Số khung hình (Frames) game sẽ chờ **TRƯỚC KHI** đợt quái (Wave) này bắt đầu. Game chạy ở 60 FPS, do đó `300` tương đương với **5 giây**.
* `spawnInterval` (Độ trễ sinh quái): Số khung hình khoảng cách giữa việc thả 2 con quái liên tiếp. (Ví dụ: `150` -> Cứ cách **2.5 giây** lại thả 1 con quái ra khỏi chuồng).
* `zombies`: Danh sách quy định thứ tự và loại quái vật sẽ được thả ra. `[1, 1, 2]` nghĩa là: Thả 1 con Red Zombie -> chờ 2.5s -> Thả 1 con Red Zombie -> chờ 2.5s -> Thả 1 con Shooter Zombie.

## 5. TINH CHỈNH CHỈ SỐ (BALANCING)
Toàn bộ các chỉ số (HP, Damage, Speed, Tầm bắn, Thời gian hồi chiêu) của Cây và Quái đã được gỡ bỏ "code cứng" (hardcode).
Bạn có thể dễ dàng chỉnh sửa lại toàn bộ sức mạnh của mọi thực thể trong game chỉ bằng cách mở file duy nhất:
👉 `src/main/java/com/team/gardendefense/utils/Constants.java`
