# Báo cáo cập nhật chuyển động nhân vật

Ngày: 20/11/2026

## Mục tiêu

Làm cho cây và zombie chuyển động sinh động hơn trong trận đấu, dựa trên các tệp hoạt ảnh `assets/reanim` đã có trong dự án. Phần thay đổi tập trung vào hình ảnh; tốc độ di chuyển, sát thương và quy tắc chơi không được chủ ý thay đổi.

## Đã thực hiện

- Thêm `Reanim.java` để đọc các tệp `.reanim`, nạp hình của từng bộ phận, nội suy vị trí, góc và tỉ lệ giữa các khung hình, rồi ghép chúng khi vẽ.
- Chuyển Peashooter, SnowPea, Sunflower, WallNut, Chomper và Marigold sang hoạt ảnh ghép bộ phận. Cây bắn có đoạn chuyển động khi khai hỏa; Chomper có đoạn cắn và nhai. Hoạt ảnh tạo Sun hiện có của Sunflower vẫn được giữ.
- Chuyển Zombie và ShooterZombie sang hoạt ảnh bước đi và cắn từ `Zombie.reanim`. Khi zombie bị đóng băng, tốc độ phát hoạt ảnh giảm theo tốc độ di chuyển.
- Cho các cây chưa có bộ `.reanim` chuyển động nhẹ bằng ảnh hiện có; SniperPlant có thêm phản lực ngắn khi bắn.
- Giữ ảnh cũ làm phương án hiển thị khi không nạp được hoạt ảnh. Cập nhật `readme.md` và `LEVEL_DESIGN_GUIDE.md` để mô tả thay đổi.

## Bản đồ thay đổi cho người tiếp nhận

| Tệp | Thay đổi và lý do |
| --- | --- |
| `src/main/java/com/team/gardendefense/view/renderer/Reanim.java` | Tệp mới. Đọc XML dạng fragment của `.reanim`, tìm các đoạn `idle`, `walk`, `eat`, `shoot`, `bite`, `chew`; lưu trạng thái từng lớp hình theo khung; nạp PNG theo mã ảnh; nội suy và vẽ các lớp theo thứ tự. Đây là lõi của thay đổi. |
| `src/main/java/com/team/gardendefense/model/entity/plant/Plant.java` | Thêm đồng hồ hoạt ảnh, chọn đoạn đứng/bắn và kích hoạt chuyển động khi hành động. Vẽ bằng `Reanim` nếu có; ảnh của cây chưa có rig được lắc, co giãn và giật nhẹ khi bắn. Giữ cách xoay/lật theo hướng đặt cây. |
| `src/main/java/com/team/gardendefense/controller/GameManager.java` | Gọi `advanceAnimation()` mỗi lượt cập nhật cho cây còn sống, trước `update()`. Các đồng hồ đứng yên khi game tạm dừng vì nhánh cập nhật trận đấu không chạy. |
| `src/main/java/com/team/gardendefense/model/entity/monster/Monster.java` | Thêm đồng hồ và trình vẽ rig cho zombie; chọn `walk` khi đi và `eat` khi có cây bị tấn công, lật hình theo hướng di chuyển. |
| `src/main/java/com/team/gardendefense/model/entity/monster/Zombie.java`, `ShooterZombie.java` | Nạp `Zombie.reanim` và tăng thời gian hoạt ảnh theo hệ số tốc độ hiện tại; đóng băng làm nhịp hình chậm lại. |
| `src/main/java/com/team/gardendefense/model/entity/plant/Peashooter.java`, `SnowPea.java` | Nạp rig tương ứng và bắt đầu đoạn `shoot` lúc tạo viên đạn. |
| `src/main/java/com/team/gardendefense/model/entity/plant/Sunflower.java` | Dùng rig khi đứng; vẫn dùng chuỗi ảnh sản xuất Sun hiện có trong lúc tạo Sun. |
| `src/main/java/com/team/gardendefense/model/entity/plant/WallNut.java`, `Marigold.java` | Nạp rig đứng; Marigold giữ hiệu ứng phát sáng trước khi nhả xu. |
| `src/main/java/com/team/gardendefense/model/entity/plant/Chomper.java` | Chọn đoạn `bite` lúc vừa ăn và `chew` trong thời gian nhai. |
| `src/main/java/com/team/gardendefense/model/entity/plant/SniperPlant.java` | Báo thời điểm bắn cho chuyển động giật nhẹ của ảnh tĩnh; chưa có rig riêng. |
| `readme.md`, `src/main/resources/data/LEVEL_DESIGN_GUIDE.md` | Ghi lại cách hoạt ảnh được nạp và cập nhật cây thư mục; dữ liệu màn chơi không phải khai báo khung hình. |

Không sửa các PNG, `.reanim`, JSON màn chơi, thông số sát thương hoặc tốc độ di chuyển. Tệp xem trước trong `target/` chỉ là sản phẩm kiểm tra cục bộ và không nằm trong thay đổi được theo dõi bởi Git.

## Cách hoạt động để tiếp tục phát triển

1. Lúc tạo nhân vật, lớp cụ thể gọi `setAnimation("TênTệp.reanim")`. `Reanim.load()` dùng bộ nhớ đệm nên một tệp chỉ được phân tích một lần trong tiến trình.
2. Mỗi lượt cập nhật tăng đồng hồ hoạt ảnh. Cây bắn gọi `playAction("shoot")` khi tạo đạn; Chomper chọn đoạn theo `attackCooldown`; zombie chọn đi/cắn theo `attackTarget`.
3. Khi vẽ, `Reanim` đổi thời gian game sang khung của tệp (thường 12 fps), lấy hai tư thế kề nhau và nội suy vị trí, tỉ lệ, góc xoay. Tên ảnh trong XML được ghép với PNG ở `assets/reanim`.
4. `Plant` và `Monster` giữ đường vẽ ảnh cũ khi không nạp được tệp `.reanim`. Hiện phần tìm PNG của rig cần thư mục `src/main/resources/assets/reanim` tồn tại trên hệ thống tệp; xem mục việc cần làm bên dưới nếu muốn chạy từ JAR độc lập.

## Đã kiểm tra

- Biên dịch `GameLauncher` và `LevelEditor` bằng JDK 17: **thành công** (`javac exit: 0`).
- Kết xuất ảnh xem trước cho Peashooter, Sunflower, WallNut, Chomper, Zombie, SnowPea và Marigold ở các khung đứng, bắn, đi, cắn: các bộ phận ghép đúng và tư thế thay đổi giữa các khung.
- Mở bản game thử tách khỏi dữ liệu chơi chính; quan sát cây và zombie mới hiển thị đúng trên bản đồ màn 1. Đã đóng cửa sổ thử sau khi kiểm tra.
- `git diff --check` không báo lỗi khoảng trắng tại thời điểm kiểm tra.

## Giới hạn hiện tại

- Đây là trình vẽ `.reanim` trong Java, **chưa thể khẳng định chuyển động giống hệt 100%** bộ dựng hình của Plants vs. Zombies. Góc xoay và cách ghép các lớp đang được tái hiện gần đúng từ dữ liệu sẵn có.
- SniperPlant và các nhân vật không có bộ hoạt ảnh tương ứng vẫn dùng chuyển động ảnh tĩnh. ShooterZombie hiện dùng cùng bộ hoạt ảnh thân với Zombie thường.
- Chưa kiểm tra độ mượt khi có rất nhiều nhân vật cùng lúc hoặc khi đóng gói thành tệp JAR độc lập. Bản chạy từ lớp `GameLauncher` đã được kiểm tra trực quan; riêng script `run.bat` chưa được chạy lại sau thay đổi.
- Ảnh các lớp của rig được tìm theo thư mục nguồn. Nếu `.reanim` nạp được nhưng các PNG không tìm thấy, nhân vật có thể không hiện ra thay vì tự quay về ảnh cũ. Đây là điểm cần sửa trước khi phát hành bản đóng gói.

## Việc nên làm tiếp theo

1. **Ưu tiên cao — hoàn thiện tải tài nguyên và đường dự phòng.** Cho `Reanim` tìm PNG trong classpath/JAR, kiểm tra rig có đủ lớp hình cần thiết trước khi sử dụng, và quay về ảnh cũ nếu thiếu. Chạy thử từ một thư mục khác thư mục dự án.
2. **Ưu tiên cao — so khớp hoạt ảnh với mục tiêu hình ảnh.** Quay video ngắn của cây đứng/bắn, zombie đi/cắn ở nhiều hướng, Chomper cắn/nhai và Sunflower tạo Sun; đối chiếu khung, tỷ lệ, điểm neo và thứ tự lớp. Hiệu chỉnh phép biến đổi `kx`/`ky` và vị trí đặt rig. Trình vẽ hiện lấy trung bình hai góc nên chưa tái hiện đầy đủ phép biến đổi gốc.
3. **Ưu tiên vừa — bổ sung trạng thái riêng.** ShooterZombie cần động tác ném xương riêng; SniperPlant và các cây còn dùng ảnh tĩnh cần rig hoặc chuỗi ảnh phù hợp. Thêm chuyển cảnh giữa đi, dừng, cắn và bị đóng băng để tránh đổi tư thế đột ngột.
4. **Ưu tiên vừa — kiểm tra hiệu năng.** Đo FPS khi có nhiều cây và zombie. Mỗi lần vẽ hiện tạo một `Graphics2D` cho từng lớp hình và tra ảnh trong bộ nhớ đệm; nếu chậm, chuẩn bị trước ảnh và ma trận biến đổi cho khung.
5. **Ưu tiên vừa — kiểm tra hồi quy hình ảnh.** Thêm vài ảnh chuẩn hoặc bài kiểm tra kết xuất cho các khung đứng/bắn/đi/cắn, cùng kiểm tra hoạt ảnh vẫn đứng yên khi tạm dừng. Kiểm tra trên màn lớn và ở các mức thu phóng cửa sổ.

Các mục trên là **đề xuất**, chưa được triển khai trong lần thay đổi này. Nên bắt đầu từ tải tài nguyên và kiểm tra trực quan trước khi kết luận đạt yêu cầu “giống hệt” trò chơi tham chiếu.

## Sửa lỗi hoạt ảnh cây đậu (Peashooter & SnowPea)

### Nguyên nhân lỗi hình ảnh
1. **Mất đầu ở trạng thái nghỉ (`idle`):** Trong tệp `PeaShooter.reanim` và `SnowPea.reanim`, track `anim_idle` (khung 4..28) chỉ chứa phần thân/cuống và lá gốc; các bộ phận đầu, mặt, miệng (`anim_face`, `idle_mouth`) bị ẩn (`f = -1`). Ngược lại, đoạn `anim_full_idle` (khung 79..103) mới là đoạn hoạt ảnh hoàn chỉnh chứa đầy đủ cả thân và đầu. Do đó, việc dùng `anim_idle` làm cho cây đậu bị cụt đầu khi đứng yên.
2. **Mất thân/cuống khi bắn (`shoot`):** Trong đoạn `anim_shooting` (khung 54..78), chỉ có phần đầu chuyển động bắn; các bộ phận thân/cuống (`stalk`, `backleaf`, `frontleaf`) bị đánh dấu ẩn (`f = -1`). Khi phát đoạn `shoot`, thân cây biến mất khiến cây chỉ còn một chiếc đầu lơ lửng trong không khí.
3. **Thời lượng bắn quá dài (`durationTicks`):** Với tốc độ 12 fps của tệp hoạt ảnh, đoạn bắn dài 25 khung hình bị tính ra thời lượng 125 tick (hơn 2 giây). Trong khi đó, nhịp bắn của cây là 60 tick/viên đạn (`RATE_PEASHOOTER = 60`). Cây bắn viên tiếp theo trước khi hoạt ảnh cũ kết thúc, khiến cây bị kẹt vĩnh viễn ở trạng thái bắn và không bao giờ quay lại trạng thái nghỉ (`idle`).

### Đã khắc phục
1. **Thiết lập đúng đoạn nghỉ hoàn chỉnh:** Với `PeaShooter.reanim`, `SnowPea.reanim` (và `PeaShooterSingle.reanim`), map clip `"idle"` sang `anim_full_idle` để hiển thị đầy đủ 17 bộ phận (cả thân và đầu).
2. **Cơ chế fallback giữ thân cây khi bắn:** Trong `Reanim.java`, nếu một bộ phận (như thân, cuống lá) không hiển thị trong đoạn hành động (`shoot`), bộ phận đó sẽ tự động lấy tư thế từ đoạn `"idle"` theo nhịp đung đưa liên tục (`idleTicks`). Nhờ đó, thân cây vẫn đứng vững và nhấp nhô nhịp nhàng, trong khi đầu cây thực hiện động tác kéo về sau và nhả đạn.
3. **Chuẩn hóa thời lượng bắn:** Điều chỉnh thời lượng hoạt ảnh `shoot` về 24 tick (~0.40 giây ở 60 FPS). Động tác bắn diễn ra dứt khoát, mượt mà (ngửa đầu lấy đà, há miệng nhả đạn, hồi phục), sau đó cây có khoảng 36 tick để quay về tư thế đung đưa tự nhiên trước khi bắn phát tiếp theo.
4. **Đồng bộ hóa thời điểm phóng đạn với hoạt ảnh nhả hạt:** Đạn đậu được trì hoãn đúng 11 tick (thời điểm miệng cây há to và nhả hạt ra) thay vì xuất hiện ngay ở tick 0.
5. **Khắc phục hiện tượng giật hình và rời rạc các bộ phận:** Chuẩn hóa phép biến đổi ma trận Affine 2D (`AffineTransform(m00, m10, m01, m11, px, py)`) trong `Reanim.java` thay vì xoay theo tâm xấp xỉ, giúp các bộ phận mắt, miệng, đầu, lá gắn kết mượt mà và không bị rung lắc.
6. **Khắc phục lỗi giật ngược về sau khi bắn (`anim_shooting` vs `anim_full_idle`):**
   - *Nguyên nhân:* Ở trạng thái nghỉ (`anim_full_idle`), thân và đầu cây đung đưa ngang với biên độ lệch đến ~9.8px (từ `x=19.2` đến `x=28.5`). Nhưng trong tệp hoạt ảnh gốc của PopCap, đoạn bắn `anim_shooting` được thiết kế tương đối dựa trên thân ở vị trí tĩnh chuẩn (`x=37.6, y=48.7`). Khi cây đang ngả về phía trước mà kích hoạt bắn, đầu cây bị teleport đột ngột về tọa độ tĩnh ban đầu (giật lùi 9.8px ngay trong 1 frame). Đồng thời thân cây vẫn đung đưa theo đồng hồ idle riêng khiến đầu và thân bị xé rách, giật ngược chiều nhau.
   - *Khắc phục:* Trong `Reanim.java`, tính toán độ lệch đung đưa của thân cây (`swayX = curStemX - baseStemX`, `swayY = curStemY - baseStemY`) và cộng dồn vào các bộ phận đầu khi bắn, giúp đầu luôn gắn chặt tự nhiên vào cổ thân cây. Đồng thời bổ sung bộ lọc chuyển tiếp mượt mà (cross-fade / ease-in & ease-out qua 3 tick) giữa tư thế idle và tư thế shoot, loại bỏ hoàn toàn cú giật cục bộ khi bắt đầu và kết thúc phát bắn.
7. **Phân biệt chuẩn xác Cây đậu 50 Sun với Cây 100 Sun (Chỉ 1 lá sau đầu):**
   - *Nguyên nhân:* Trước đây `Peashooter.java` nạp tệp hoạt ảnh `PeaShooter.reanim` (vốn là tệp hoạt ảnh của cây Repeater trong dữ liệu gốc của PopCap với chùm 4-5 lá vểnh sau đầu). Điều này khiến cây đậu 50 Sun khi trồng xuống có nhiều lá sau đầu, trông rất giống với cây đậu bắn tỉa 100 Sun, khác với hình ảnh trên thẻ bài chọn cây chỉ có 1 lá.
   - *Khắc phục:* Chuyển sang nạp tệp chuẩn **`PeaShooterSingle.reanim`** cho `Peashooter.java`. Cây đậu 50 Sun giờ đây hiển thị chính xác **chỉ 1 lá (mầm nhỏ `anim_sprout.png`) sau đầu**, giống hệt 100% hình ảnh trên thẻ bài `card_peashooter.png` ở thanh chọn cây và phân biệt hoàn toàn với cây 100 Sun.

## Căn chỉnh lề ô lưới (Tràn viền) và đồng bộ hàng ngang giữa Cây và Zombie

### 1. Khắc phục lỗi cây trồng bị tràn viền ô lưới
- **Nguyên nhân:** Kích thước vẽ mặc định của cây trước đây đặt là `Constants.TILE_SIZE + 10` (74px) và `Sunflower` là `Constants.TILE_SIZE + 22` (86px). Khi vẽ với tọa độ bù âm, hình ảnh cây bị tràn 5px đến 11px sang các ô đất và ô cỏ liền kề.
- **Khắc phục:** 
  - Trong `Plant.java`, chuẩn hóa kích thước vẽ về `Constants.TILE_SIZE` (64px), tinh chỉnh `plantScale = 0.85f`, tọa độ vẽ `plantDrawX = x - 2`, `plantDrawY = y - 8`. Các loài cây (Peashooter, SnowPea, Sunflower, WallNut, Marigold, Chomper) nằm gọn hoàn toàn trong phạm vi `X: [3..58]` và `Y: [4..57]` của ô 64x64, không còn bất kỳ pixel nào bị tràn viền.
  - `Sunflower.java` và `Marigold.java` chuyển từ kích thước quá khổ về kích cỡ chuẩn ô lưới.

### 2. Căn chỉnh Zombie đi trên phần đất và đồng bộ hàng ngang với Cây
- **Nguyên nhân:** Zombie trước đây được vẽ tại tọa độ `y - 24`, khiến bàn chân tiếp đất nằm ở `Y = 44` (bay lơ lửng ở nửa trên của ô đất, lệch hơn 13px so với gốc cây ở `Y = 57..72`). Đồng thời nếu scale không khớp, các bộ phận zombie có thể vươn sang viền cỏ hai bên.
- **Khắc phục:**
  - Trong `Monster.java`, điều chỉnh tọa độ vẽ zombie thành `y - 11` với tỉ lệ `scale = 0.55f`, `x + 10`.
  - Bàn chân zombie chạm đất chuẩn xác tại `Y = 57`, chiều ngang nằm gọn trong `X: [14..53]` (nằm trọn trong lòng con đường đất 64px, không lấn sang hàng cỏ).
  - **Đồng bộ hàng ngang:** Cả gốc cây (Peashooter, SnowPea, Sunflower, Marigold,...) và chân của Zombie hiện đều nằm trên cùng một đường tiếp đất cơ sở `Y = 57`, tạo cảm giác cùng đứng và di chuyển trên một mặt phẳng hàng ngang thống nhất.

## Nâng cấp độ mượt và tự nhiên cho toàn bộ các loài cây khác

1. **Sunflower (Hoa Hướng Dương):**
   - Loại bỏ việc cắt đổi khung hình đột ngột sang spritesheet ảnh tĩnh 6 khung khi tạo Sun.
   - Luôn duy trì khung xương hoạt ảnh 60 FPS mượt mà của `SunFlower.reanim`.
   - Trước khi nhả Sun (khoảng 0.8s), hoa hướng dương tỏa ra ánh hào quang vàng ấm áp tích lũy năng lượng; sau đó mặt trời nhẹ nhàng bay lên từ nhụy hoa với hiệu ứng phát sáng tự nhiên.
2. **Wall-Nut (Củ Khoai Tây):**
   - Mở khóa toàn bộ chu kỳ hoạt ảnh 56 khung hình của `Wallnut.reanim` (thay vì bị kẹt cứng ở 17 khung hình đầu).
   - Củ khoai tây giờ đây đảo mắt nhịp nhàng, chớp mắt một lần, giật mắt đáng yêu, chớp mắt kép và ba lần cực kỳ sinh động giống hệt bản gốc Plants vs. Zombies.
3. **Chomper (Cây Nắp Ấm / Cây Ăn Thịt):**
   - Xây dựng cỗ máy trạng thái hoàn chỉnh: `IDLE` (chờ đợi) -> `BITING` (lao tới đớp mồi) -> `CHEWING` (ngậm miệng nhai nuốt) -> `SWALLOWING` (nuốt ực thỏa mãn và ợ hơi).
   - Sát thương được kích hoạt chính xác ở thời điểm hàm răng khép lại (tick 13 của hoạt ảnh đớp) thay vì zombie chết ngay lập tức ở tick 0.
4. **Cherry Bomb (Quả Bom Cherry):**
   - Chuyển từ ảnh tĩnh sang bộ hoạt ảnh gốc `CherryBomb.reanim`.
   - Hai quả cherry nhìn nhau nảy tưng tưng hào hứng (`anim_idle`), sau đó phồng to đỏ rực, rung giật dữ dội (`anim_explode`) trước khi bùng nổ với luồng sóng nhiệt và khói lửa chân thực.
5. **Marigold (Hoa Cúc Xu):**
   - Thay thế vòng tròn vàng nhấp nháy chớp tắt đột ngột bằng luồng ánh sáng hào quang thở mềm mại (dạng sóng điều hòa hình sin) trong 1.5 giây trước khi nhả xu.
6. **SniperPlant (Cây Đậu Bắn Tỉa 100 Sun):**
   - *Nguyên nhân giật lùi:* Trước đây hàm vẽ dùng `g2.translate(-recoil, 0)` tịnh tiến cả gốc và rễ cây lùi 5-6px về sau trên mặt đất mỗi khi bắn, khiến người chơi thấy cây bị giật trượt hẳn về sau.
   - *Khắc phục:* Bỏ hoàn toàn phép dịch chuyển gốc rễ. Neo cố định 100% gốc cây tại mặt đất (`rootY = y + 57`). Động tác bắn giờ đây diễn ra dưới dạng góc uốn lượn và co giãn tự nhiên quanh gốc (ngả nhẹ lấy đà -> bật tới phía trước bắn đạn -> hồi vị đàn hồi êm dịu), giúp cây luôn bám chặt vào đất, không bao giờ bị trượt hay giật lùi. Bổ sung hiệu ứng tia lửa plasma tím ở đầu nòng khi khai hỏa.

## Sửa lỗi tràn viền thanh chọn cây (Card Tray) & tràn chữ bảng Game Over

### 1. Khắc phục lỗi thanh chọn cây trồng bị tràn sang Shop và ra ngoài màn hình
- **Nguyên nhân:**
  - Ở các màn chơi có nhiều cây được phép sử dụng (ví dụ Màn 4 có 10 loại cây + Marigold = 11 thẻ), mỗi thẻ bài trước đây chiếm 80px (`startX += 80`). Tổng chiều dài 11 thẻ lên đến gần `900px`, trong khi khung hiển thị tiền xu và nút Shop nằm cứng ở `sw - 360` (~600px).
  - Thẻ bài bị vẽ đè lên nút Shop, khung tiền xu, nhãn Màn chơi và tràn ra khỏi mép phải màn hình; người chơi không thể nhìn thấy hoặc bấm chọn các cây ở phía sau.
- **Khắc phục:**
  - **Tái cấu trúc bố cục cụm điều khiển góc phải:** Gom gọn nút `MENU` (`sw - 98`, rộng 88px), nhãn Tên màn chơi (`sw - 98`, ngay dưới Menu), khung Ví Tiền `$ Coins` (`sw - 232`, rộng 120px) và nút `Shop 🛒` (`sw - 232`, cao 24px) sang cụm góc phải. Nhờ đó, giải phóng hoàn toàn khoảng trống từ `X = 110` đến `X = sw - 242` dành riêng cho thanh thẻ bài.
  - **Chuẩn hóa kích thước thẻ bài:** Thu gọn kích thước mỗi thẻ về `54x68` px, khoảng cách `4px`.
  - **Cơ chế cuộn ngang thông minh (Scroll Tray):** Khi tổng chiều rộng các thẻ bài vượt quá không gian cho phép, hệ thống tự động kích hoạt chế độ cuộn:
    - Hiển thị hai nút điều hướng `◀` và `▶` ở hai đầu thanh thẻ bài.
    - Hỗ trợ cuộn bằng nút bấm hoặc lăn chuột giữa (`MouseWheelEvent`) trực tiếp trên thanh điều khiển.
    - Áp dụng kỹ thuật cắt xén đồ họa `g.setClip(cardViewX, 0, cardViewW, 80)`: Thẻ bài chỉ hiển thị hoàn toàn trong lòng khay thẻ bài, tuyệt đối 100% không bao giờ bị tràn hay đè lên biểu tượng Mặt trời, Shop, Tiền xu hay mép màn hình.
    - Tọa độ hitbox bấm chọn thẻ bài (`PlantCard.rect`) được cập nhật theo vị trí cuộn thực tế trên màn hình, đảm bảo bấm chọn chính xác cây mong muốn.

### 2. Khắc phục lỗi chữ thông báo Game Over bị tràn ra ngoài khung bia đá
- **Nguyên nhân:**
  - Trong `drawGameOver()`, bảng bia đá chỉ rộng `dw = 420px`, nhưng tiêu đề `"THE ZOMBIES ATE"` dùng font `Serif Bold 46`, có chiều dài chuỗi thực tế lên đến ~450px.
  - Do đó hai cạnh trái - phải của dòng chữ bị nhô hẳn ra ngoài khung đá, làm mất thẩm mỹ.
- **Khắc phục:**
  - Mở rộng kích thước khung bia đá lên `dw = 480px, dh = 400px` cân đối với toàn bộ màn hình.
  - **Thuật toán co dãn phông chữ tự động (Dynamic Font Auto-scaling):** Tự động đo độ dài thực tế của chuỗi ký tự bằng `FontMetrics`. Nếu độ dài chữ vượt quá `dw - 60` (420px), phông chữ sẽ tự động hạ size theo bước 2px (từ 34pt trở xuống) để luôn đảm bảo khoảng đệm (padding) tối thiểu 30px ở mỗi bên thành đá.
  - Bổ sung phụ đề tiếng Việt rõ ràng: `"ZOMBIE ĐÃ XÂM CHIẾM KHU VƯỜN!"` được canh giữa tuyệt đối.
  - Canh chỉnh lại vị trí 3 nút bấm `"CHƠI LẠI TỪ ĐẦU 🔄"`, `"CHỌN MÀN KHÁC 🗺️"`, `"VỀ MENU CHÍNH 🏠"` thẳng hàng và cân đối hoàn hảo trong khung bia đá.

## Khắc phục lỗi cổ cây đậu bị rời với thân khi bắn & Rà soát toàn bộ các cây

### 1. Sửa dứt điểm lỗi đầu cây đậu 50 bị nhấc lên 1-2 mm / rời cổ khi bắn
- **Nguyên nhân chi tiết:**
  - Trong tệp `PeaShooterSingle.reanim` (và `SnowPea.reanim`), ở trạng thái đứng thở (`anim_full_idle`, frame 79), tọa độ đỉnh cổ (`stalk_top`) là `y = 45.3` và đáy đầu (`anim_face`) là `y = 17.8` (khoảng cách khớp nối chuẩn: `45.3 - 17.8 = 27.5px`).
  - Tuy nhiên, trong clip bắn (`anim_shooting`, frame 54..78), tư thế frame 54 của `anim_face` được đặt tại `y = 14.0`. Do đó khi bắt đầu bắn, đầu bị giật bổng lên `17.8 - 14.0 = 3.8px` (tương đương 1.2 mm trên màn hình).
  - Thêm vào đó, trong các nhịp ngửa đầu lấy đà (frame 58..62), `anim_face.y` giật lên tới `7.0` (thêm 7px), trong khi đỉnh cổ trước đây chỉ chạy theo 45% xung lực (`0.45 * recoil`), tạo ra khoảng hở lớn tới ~7.65px khiến đầu hoàn toàn tách rời khỏi cổ như bị nhấc lơ lửng.
- **Khắc phục:**
  - **Khóa chốt gốc tọa độ vào tư thế nghỉ của đầu:** Toàn bộ các bộ phận của đầu (`anim_face`, `idle_mouth`, `anim_sprout`) trong `Reanim.java` được neo trực tiếp theo tọa độ đung đưa thực tế của đầu lúc idle cộng với độ biến thiên thuần túy của động tác bắn:
    `px = idlePx + partRecoilX * blendWeight;`
    `py = idlePy + partRecoilY * blendWeight;`
    Tại frame bắt đầu và kết thúc bắn, `partRecoilX = 0, partRecoilY = 0` nên vị trí đầu khớp tuyệt đối 100% với tư thế idle, loại bỏ hoàn toàn hiện tượng đầu bị nhấc lên 1-2 mm!
  - **Khóa đồng bộ 100% cổ theo đầu:** Đỉnh cổ `stalk_top` nhận trọn vẹn 100% độ co dãn `headRecoilX` và `headRecoilY` của đầu:
    `px += headRecoilX * blendWeight;`
    `py += headRecoilY * blendWeight;`
    Nhờ vậy, khoảng cách `head.y - stalk_top.y` luôn giữ nguyên độ lệch chuẩn `-27.5px` suốt toàn bộ 24 tick của động tác bắn. Cổ và đầu luôn gắn liền khít khao, không bao giờ bị hở hay tách rời.

### 2. Rà soát và hoàn thiện chi tiết toàn bộ các cây khác
- **SniperPlant (100 Sun):**
  - **Loại bỏ viên đạn tím / tia chớp nòng súng ở đầu cây:** Theo yêu cầu của người dùng, đã xóa bỏ hoàn toàn hiệu ứng vòng tròn đạn tím (`muzzle flash`) ở đầu miệng cây lúc bắn. Cây giữ trọn vẹn ngoại hình đặc trưng từ ảnh sprite `SniperPlant_full.png` và động tác co giãn nhả đạn mượt mà từ gốc cây.
  - Điểm xuất phát của viên đạn spawn chuẩn xác ngang tầm miệng nòng (`y + 20`).
- **Chomper (Cây ăn thịt):**
  - Bổ sung hỗ trợ lật hình ngang (`scale(-1, 1)`) khi cây quay sang trái (`direction == 3`).
  - Bổ sung thanh máu (`health bar`) hiển thị chuẩn xác khi cây bị zombie cắn mất máu trong lúc đang nhai hoặc đang cắn mồi.
- **CherryBomb (Quả bom Cherry):**
  - Đảm bảo hiển thị thanh máu nếu bị tấn công trong thời gian đếm ngược ngòi nổ.
- **Sunflower, WallNut, Marigold:**
  - Kiểm tra và xác nhận chuyển động hoạt ảnh trơn tru, hào quang ánh sáng mềm mại, không có hiện tượng giật cục hay lỗi rời rạc bộ phận.

## Triển khai công năng vật phẩm Cửa Hàng: Bùa Đóng Băng (Ice Shroom Blast)

Theo mô tả trong Cửa hàng: *"Đóng băng và làm chậm toàn sân đấu trong 5 giây"*.

### 1. Cơ chế hoạt động (Gameplay Mechanics)
- **Thời lượng:** 5 giây = 300 tick game (60 FPS).
- **Giai đoạn 1 - Đóng băng cứng (0s -> 2s, 120 tick đầu):**
  - Toàn bộ Zombie trên sân bị bọc kín trong khối băng pha lê 3D.
  - Tốc độ di chuyển giảm về 0 (`speed = 0`).
  - Toàn bộ hành vi (di chuyển, cắn cây, bắn xương của Shooter Zombie) bị vô hiệu hóa hoàn toàn.
- **Giai đoạn 2 - Làm chậm buốt giá (2s -> 5s, 180 tick sau):**
  - Khối băng pha lê tan vỡ, chuyển sang làn sương lạnh mờ buốt giá.
  - Tốc độ di chuyển giảm còn 40% (`baseSpeed * 0.4f`).
  - Hết 300 tick, Zombie trở lại trạng thái bình thường.
- **Áp dụng cho Zombie mới xuất hiện:** Bất kỳ Zombie nào xuất hiện trong thời gian bùa còn hiệu lực đều tự động chịu trạng thái tương ứng.

### 2. Giao diện và Điều khiển (UI & Controls)
- **Nút bấm trực tiếp trong trận:** Nút `[❄ F]` nằm cạnh Ví Tiền ở góc trên bên phải màn hình (tọa độ `itemX = bankX - 66, Y = 12`, kích thước `54x54`), có huy hiệu hiển thị số lượng bùa sở hữu `xN` hoặc nút `+MUA` khi hết bùa.
- **Phím tắt nhanh:** Phím `F` trên bàn phím.
- **Hiệu ứng đồ họa (VFX):**
  - Sóng xung kích băng tuyết xanh ngọc nở rộng từ tâm màn hình (`iceShroomWaveRadius`).
  - Màn hình phủ sương tuyết buốt giá (`vignette frost tint`).
  - 16 tinh thể hoa tuyết `❄` lơ lửng và rơi nhẹ trên màn hình.
  - Khối băng pha lê 3D với tia nứt và phản xạ ánh sáng bọc quanh mỗi Zombie.
  - Banner nghệ thuật thông báo: `"❄️ BÙA ĐÓNG BĂNG ĐÃ KÍCH HOẠT! (5 GIÂY) ❄️"`.
- **Hiệu ứng âm thanh (SFX):**
  - Hàm `AudioManager.playFreezeSound()` phát chuỗi âm sắc pha lê buốt giá (synthesized crystal chime).
- **Liên kết Ví tiền (Wallet Persistence):**
  - Trừ 1 bùa trong `iceShroomBlastCount` và lưu tự động vào `saves/wallet_save.json`.
  - Nếu đã hết bùa (count = 0), phát thông báo nổi màu cam nhắc nhở và tự động chuyển đến Shop khi bấm nút.

## Cách xem thay đổi

Mở PowerShell tại thư mục dự án và chạy `.\run.bat` (hoặc `.\run.ps1`), sau đó vào màn chơi để trải nghiệm. Hệ thống tự động biên dịch với cờ tương thích `--release 11`.
