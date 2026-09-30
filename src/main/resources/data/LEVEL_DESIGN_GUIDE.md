# HƯỚNG DẪN THIẾT KẾ MÀN CHƠI (LEVEL DESIGN GUIDE)

Tài liệu này dành cho những người tạo màn chơi (Level Designer). Bất kỳ ai cũng có thể tự tạo một màn chơi (Level) mới bằng cách viết một file `.json` và ném vào thư mục `src/main/resources/data/`. Game sẽ tự động load dựa trên logic của hệ thống.

---

## 1. BẢNG TRA CỨU ID (ID REFERENCE)

### 🌿 THỰC VẬT (PLANTS) - Dùng trong mảng `allowedPlants`
| ID | Tên Plant | Giá (Sun) | Tính năng |
|----|-----------|-----------|-----------|
| 1  | Sunflower | 50        | Sinh 25 Sun mỗi 5 giây. |
| 2  | Peashooter| 50        | Bắn đạn đơn, sát thương 1. |
| 3  | Sniper    | 100       | Tầm xa x2, sát thương 2, tốc độ đạn bay nhanh. |
| 4  | WallNut   | 50        | Máu siêu trâu (30 HP). CHỈ được trồng trên `Đường đi (ID = 1)`. |
| 5  | SnowPea   | 150       | Đạn băng giảm 60% tốc độ quái trong 3s. |

### 🧟 THÂY MA (ZOMBIES) - Dùng trong mảng `zombies`
| ID | Tên Zombie     | HP  | Khả năng |
|----|----------------|-----|----------|
| 1  | Red Zombie     | 5   | Đi theo đường, cắn cây (Wallnut) chặn đường. |
| 2  | Shooter Zombie | 3   | Quét 250px thấy cây sẽ dừng bước và ném xương. |

### 🗺️ BẢN ĐỒ (TILES) - Dùng trong ma trận `map`
0 = Cỏ trang trí. 1 = Đường đất. 2 = Bệ trồng cây.

---

## 2. HỆ THỐNG WAVES VÀ LÀN ĐƯỜNG ĐA LUỒNG (MULTI-SPAWNERS)

Để hỗ trợ một Wave có thể tràn ra từ **Nhiều cửa** hoặc **Nhiều làn đường** cùng một lúc, mỗi Wave (Đợt) sẽ chứa một mảng các `spawners` (Cửa đẻ quái).

```json
"waves": [
  {
    "delayBefore": 300,
    "spawners": [
      {
        "spawnInterval": 150,
        "zombies": [1, 1],
        "waypoints": [
          {"col": -1, "row": 2}, {"col": 16, "row": 2}
        ]
      },
      {
        "spawnInterval": 120,
        "zombies": [2, 2],
        "waypoints": [
          {"col": -1, "row": 8}, {"col": 16, "row": 8}
        ]
      }
    ]
  }
]
```

**Cách hoạt động:**
* Trò chơi chờ `delayBefore` (Vd: 300 khung hình = 5 giây) rồi **Kích hoạt toàn bộ `spawners` trong Wave đó CÙNG MỘT LÚC**.
* Ở ví dụ trên: Cửa trên (Hàng 2) sẽ đẻ 2 con Red Zombie cách nhau 150 frames. Cùng lúc đó, Cửa dưới (Hàng 8) sẽ đẻ 2 con Xạ thủ cách nhau 120 frames. Hai cửa hoạt động hoàn toàn song song!
* Chỉ khi **tất cả các Spawners xả hết quái**, game mới coi như xong Wave này và bắt đầu đếm ngược `delayBefore` của Wave tiếp theo.

## 3. CƠ CHẾ WAYPOINTS VÀ LƯỚI
* Toạ độ Cột/Hàng (`col`, `row`) tính từ 0. Bảng đồ chuẩn là 16 cột x 12 hàng.
* `col: -1` dùng để cho quái xuất hiện tàng hình từ ngoài rìa màn hình bên trái đi vào.
* Quái vật sẽ đi theo các Waypoints nối tiếp nhau, giúp bạn thiết kế bẻ lái mọi ngã rẽ bạn tự vẽ ra ở ma trận `map`.

## 4. TINH CHỈNH CHỈ SỐ (BALANCING)
Sức mạnh và Giá tiền của game được tập trung quản lý tại: `src/main/java/com/team/gardendefense/utils/Constants.java`. Bạn có thể thay đổi tùy thích mà không sợ hỏng logic game.
