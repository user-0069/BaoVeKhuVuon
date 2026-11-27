# 🛠️ Lộ Trình Sửa Lỗi — Thứ Tự Ưu Tiên

> Nguyên tắc: **Ít file chạm vào → ít bug phát sinh → làm nền tảng cho bước sau**

---

## ✅ NHÓM 1 — Sửa ngay, 1 file, không rủi ro

### 🔧 Fix 1: Hardcode giá CherryBomb/Chomper → dùng Constants
**File:** `Constants.java` + `GameManager.java` (1 chỗ)
```java
// Thêm vào Constants.java
public static final int COST_CHERRYBOMB = 150;
public static final int COST_CHOMPER = 100;

// Sửa trong GameManager.java PlantCard constructor
else if (id==6) { ... price = String.valueOf(Constants.COST_CHERRYBOMB); }
else if (id==7) { ... price = String.valueOf(Constants.COST_CHOMPER); }
```
⏱️ **5 phút** | Rủi ro: 0 | Lợi ích: Tránh sai số khi điều chỉnh cân bằng game sau này.

---

### 🔧 Fix 2: Marigold luôn thêm vào — vi phạm level design
**File:** `GameManager.java` (xóa 1 dòng)
```java
// XÓA dòng này trong resetGame():
uiCards.add(new PlantCard(11, startX)); // Luôn thêm Marigold

// Nếu muốn Marigold là mặc định, thêm ID 11 vào file level JSON allowedPlants
```
⏱️ **2 phút** | Rủi ro: 0 | Lợi ích: Level designer kiểm soát hoàn toàn cây nào xuất hiện.

---

### 🔧 Fix 3: Thêm Constants cho CherryBomb/Chomper HP
**File:** `Constants.java` (thêm 2 dòng)
```java
public static final int HP_CHERRYBOMB = 1; // Dùng 1 lần rồi nổ
public static final int HP_CHOMPER = 5;
```
⏱️ **2 phút** | Rủi ro: 0 | Giúp sau này balance dễ hơn.

---

## ✅ NHÓM 2 — Sửa trong 1 buổi, cải thiện UX ngay lập tức

### 🔧 Fix 4: Cooldown visual trên thẻ bài cây
**File:** `GameManager.java` — chỉ thêm logic vào `PlantCard` và `drawGameplay()`

**Ý tưởng:** Thêm `int cooldownTimer` và `int maxCooldown` vào `PlantCard`. Mỗi lần trồng → set timer. Vẽ overlay mờ + số đếm ngược lên thẻ bài.

```java
class PlantCard {
    int id; Rectangle rect; String price;
    java.awt.Image image;
    int cooldownTimer = 0;  // <-- THÊM
    int maxCooldown = 60;   // <-- THÊM (lấy từ Constants tương ứng)
    boolean isReady() { return cooldownTimer <= 0; }
}
```
Khi vẽ thẻ bài: nếu `!isReady()` thì vẽ lớp đen mờ + số giây còn lại.

⏱️ **30-45 phút** | Rủi ro: Thấp (chỉ trong draw + một chỗ trồng cây)

---

### 🔧 Fix 5: Âm thanh khi trồng cây / thiếu Sun
**File:** `AudioManager.java` + `GameManager.java` (2 chỗ gọi)

```java
// Khi trồng thành công:
AudioManager.getInstance().playPlantSound();

// Khi thiếu Sun (click mà không đủ tiền):
AudioManager.getInstance().playErrorSound();
```
⏱️ **15-20 phút** (tùy có sẵn file âm thanh không)

---

### 🔧 Fix 6: Phím R để hủy chọn cây (Deselect)
**File:** `GameManager.java` — thêm vào `handleKeyPress()`
```java
if (keyCode == KeyEvent.VK_R || keyCode == KeyEvent.VK_ESCAPE) {
    if (state == GameState.PLAYING && selectedPlant > 0) {
        selectedPlant = 0;  // Deselect
        return;
    }
    // ... logic Pause hiện tại
}
```
> **Lý do không dùng ESC:** ESC đang là Pause. Dùng R (Right-click concept) hoặc giữ phím khác.

Hoặc tốt hơn: thêm `mouseRightClicked` → deselect.

⏱️ **10 phút** | Rủi ro: 0

---

### 🔧 Fix 7: Hiển thị Wave countdown HUD
**File:** `GameManager.java` — trong `drawGameplay()`, đọc từ `WaveManager`

Cần expose thêm `getTimeUntilNextWave()` từ `WaveManager`, rồi vẽ lên HUD:
```
⚠️ Đợt 2 đến trong: 3s
```
⏱️ **20-30 phút** | Rủi ro: Thấp (chỉ thêm getter + 1 drawString)

---

## ✅ NHÓM 3 — Quan trọng về gameplay, cần thiết kế thêm

### 🔧 Fix 8: Shovel (Nhổ cây) — QUAN TRỌNG NHẤT VỀ GAMEPLAY
**Strategy:** Thêm "Shovel Mode" tương tự như chọn cây.

**Cần sửa:**
- `GameManager.java`: Thêm `boolean shovelMode`
- Khi `hasGoldenShovel == true` → hiển thị nút Shovel trên HUD
- Click nút Shovel → `shovelMode = true`
- Click vào ô có cây → xóa cây đó, hoàn trả 50% Sun, `shovelMode = false`

⏱️ **1-2 giờ** | Rủi ro: Trung bình (cần test edge case cây đang bị zombie cắn)

---

### 🔧 Fix 9: IceShroom — Dùng bùa đóng băng toàn sân
**Cần sửa:** Thêm nút/phím trong HUD, thêm `applyGlobalFreeze()` trong GameManager

```java
// Trong GameManager, thêm phím I hoặc nút UI:
public void useIceShroom() {
    if (data.iceShroomBlastCount <= 0) return;
    for (Monster z : zombies) {
        z.applyFreeze(); // Đã có sẵn trong Monster!
    }
    data.iceShroomBlastCount--;
    WalletManager.getInstance().save();
}
```
> `applyFreeze()` **đã được implement** trong `Monster.java` → chỉ cần gọi!

⏱️ **30-45 phút** | Rủi ro: Thấp vì logic freeze đã có sẵn

---

### 🔧 Fix 10: Lưu tiến trình màn đã hoàn thành
**Cần sửa:** Thêm `Set<Integer> completedLevels` vào `WalletManager.SaveData`

```java
public static class SaveData {
    // ... existing fields
    public List<Integer> completedLevels = new ArrayList<>(); // THÊM
}
```
Khi `GAMEWON`: `data.completedLevels.add(currentLevelId)` → `save()`.
Trong Level Select: hiển thị ✅ trên màn đã qua.

⏱️ **45 phút** | Rủi ro: Thấp (chỉ thêm field vào SaveData, Gson tự xử lý)

---

## ✅ NHÓM 4 — Cần thiết kế lớn, làm sau cùng

| Fix | Mô tả | Khó khăn |
|-----|-------|----------|
| Điểm số / Xếp hạng sao | Tính toán sau GAMEWON | Cần thiết kế công thức |
| Tooltip thẻ bài | Popup khi hover | Cần layout mới |
| Nhạc nền gameplay | Thêm BGM loop | Cần file nhạc |
| Thêm loại Zombie mới | Zombie giáp, zombie nhanh | Cần asset + balance |
| Speed x2 | Nhân tốc độ game loop | Ảnh hưởng toàn bộ update() |
| Vasebreaker / I,Zombie | Minigame mới | Dự án mới trong dự án |

---

## 🗺️ Thứ Tự Thực Hiện Khuyến Nghị

```
Fix 1 (5 phút) ──▶ Fix 2 (2 phút) ──▶ Fix 6 (10 phút)
        ──▶ Fix 4 (Cooldown) ──▶ Fix 5 (Âm thanh) ──▶ Fix 7 (Wave HUD)
                ──▶ Fix 9 (IceShroom, dễ vì freeze đã có) ──▶ Fix 8 (Shovel)
                        ──▶ Fix 10 (Lưu tiến trình)
```

> [!TIP]
> **Bắt đầu từ Fix 9 (IceShroom)** nếu muốn có thành quả nhanh và ấn tượng: logic `applyFreeze()` **đã có sẵn trong code**, chỉ cần thêm 1 nút và gọi nó — 30 phút ra tính năng ngay!
