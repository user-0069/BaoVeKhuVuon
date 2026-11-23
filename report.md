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

## Triển khai công năng vật phẩm Cửa Hàng: Kẹo Chocolate (Pet Snail Boost)

Theo mô tả trong Cửa hàng: *"Tăng 250% tốc độ bò của Pet trong 3 phút"*.

### 1. Cơ chế hoạt động (Gameplay Mechanics)
- **Thời lượng:** 3 phút = 180 giây = 10,800 tick game (60 FPS).
- **Tăng tốc siêu cấp 250%:** Tốc độ bò của Pet Ốc Sên Stinky tăng vọt từ `1.6 px/frame` lên `4.0 px/frame` (`baseSpeed * 2.5f`).
- **Tăng bán kính từ tính nhặt xu:** Bán kính gom tiền mở rộng từ `38 px` lên `52 px`, giúp ốc sên lướt nhanh và quét sạch toàn bộ xu trên sân đấu.
- **Hỗ trợ cộng dồn thời gian (Stacking):** Nếu cho ăn thêm trong lúc đang có hiệu lực, thời gian tăng tốc được cộng dồn thêm 3 phút (tối đa 10 phút = 36,000 tick).
- **Bảo vệ an toàn:**
  - Nếu người chơi chưa mua Pet Ốc Sên trong Cửa hàng: Hiện thông báo nhắc nhở màu cam *"Chưa có Pet Ốc Sên! Mua Pet 🐌 trong Shop trước"* và mở nhanh Cửa Hàng.
  - Nếu hết kẹo (số lượng = 0): Hiện thông báo *"Hết Kẹo Chocolate! Mua thêm trong Shop 🛒"* và không bị trừ âm.

### 2. Giao diện và Điều khiển (UI & Controls)
- **Nút bấm trực tiếp trên thanh điều khiển:** Nút `[🍫 C]` nằm cạnh nút Bùa Đóng Băng và Ví Tiền ở góc trên bên phải.
  - Nếu Pet đang tăng tốc: Nút chuyển sang viền vàng phát sáng nhấp nháy, hiển thị đồng hồ đếm ngược phút:giây thời gian còn lại (ví dụ `2:58`).
  - Nếu có kẹo: Hiện huy hiệu `xN`.
  - Nếu hết kẹo: Hiện huy hiệu `+MUA`.
- **Phím tắt tiện lợi:** Nhấn phím **`C`** trên bàn phím để cho Pet ăn ngay lập tức.
- **Tương tác trực tiếp trong vườn (Direct Click):** Người chơi có thể click chuột trực tiếp vào chú Ốc Sên đang bò trên sân để cho ăn kẹo. Khi rê chuột vào Pet, hiển thị viền sáng và bong bóng hướng dẫn *"🍫 Cho ăn Kẹo [C]"*.
- **Hiệu ứng đồ họa (VFX):**
  - Vòng hào quang năng lượng vàng cam rực rỡ đập nhịp nhàng quanh Ốc Sên.
  - Tia chớp tốc độ `⚡` phát ra phía sau khi di chuyển.
  - Huy hiệu nhỏ đếm ngược thời gian `🍫 M:SS ⚡` nổi ngay trên lưng Ốc Sên.
  - Chữ nổi thông báo: *"⚡ PET TĂNG TỐC 250%! (3 PHÚT) 🍫"*.
- **Hiệu ứng âm thanh (SFX):**
  - Hàm `AudioManager.playPowerUpSound()` tổng hợp chuỗi nốt nhạc arpeggio 4 âm tăng dần vui tươi (C5 - E5 - G5 - C6).
- **Lưu trữ dữ liệu Ví tiền:**
  - Tiêu thụ 1 viên kẹo trong `chocolateCount` và tự động lưu vào `saves/wallet_save.json`.

## Triển khai công năng vật phẩm Cửa Hàng: Xẻng Vàng (Golden Shovel)

Theo mô tả trong Cửa hàng: *"Đào thu hồi cây được hoàn trả 50% số Mặt trời"*.

### 1. Cơ chế hoạt động (Gameplay Mechanics)
- **Công cụ Đào cây (Shovel Tool):** Cho phép người chơi đào bỏ bất kỳ cây trồng nào trên sân để giải phóng ô đất trồng cây mới hoặc cơ cấu lại chiến thuật phòng thủ.
- **Xẻng Thường (Chưa sở hữu Xẻng Vàng):**
  - Đào cây tức thời (`plants.remove(p)`).
  - Không hoàn trả Mặt trời (hoàn 0 Sun).
  - Phát âm thanh xúc đất đào cây `AudioManager.playShovelSound()`.
- **Xẻng Vàng (Đã mua trong Shop - 2500 xu):**
  - Nâng cấp xẻng lên đẳng cấp Hoàng Kim với ánh hào quang vàng lấp lánh `✨`.
  - **Hoàn trả chính xác 50% số Mặt trời** theo giá mua gốc của từng loại cây:
    - Hoa Hướng Dương (Sunflower, giá 50): Hoàn lại **25 ☀️**.
    - Cây Bắn Đậu 50 Sun (Peashooter, giá 50): Hoàn lại **25 ☀️**.
    - Cây Bắn Đậu 100 Sun (SniperPlant, giá 100): Hoàn lại **50 ☀️**.
    - Củ Khoai Tây (WallNut, giá 50): Hoàn lại **25 ☀️**.
    - Cây Bắn Băng (SnowPea, giá 150): Hoàn lại **75 ☀️**.
    - Cây Ăn Thịt (Chomper, giá 150): Hoàn lại **75 ☀️**.
    - Bom Cherry (CherryBomb, giá 150): Hoàn lại **75 ☀️**.
    - Hoa Cúc Xu (Marigold, giá 50): Hoàn lại **25 ☀️**.
  - Mặt trời thu hồi được cộng dồn trực tiếp vào quỹ năng lượng mặt trời (`sun += refund`) để tái sử dụng ngay lập tức.
  - Chữ nổi ánh kim vàng rực rỡ hiển thị trên đầu cây: `"+XX ☀️ (Xẻng Vàng)"`.
  - Phát âm thanh tiếng xẻng xúc đất kết hợp tiếng chuông ngân vang thu hoạch Mặt trời `AudioManager.playGoldenShovelSound()`.

### 2. Giao diện và Điều khiển (UI & Controls)
- **Nút bấm Xẻng trên thanh Header:** Nút `[⛏️ S]` nằm trong khay vật phẩm (bên cạnh Kẹo Chocolate, Bùa Đóng Băng và Ví Tiền).
  - Khi chưa nâng cấp: Hiển thị xẻng cơ bản với huy hiệu xám `"0% ☀️"`.
  - Khi đã sở hữu Xẻng Vàng: Chuyển sang tông vàng kim rực rỡ, viền vàng nổi bật và huy hiệu vàng `"50% ☀️"`.
  - Khi đang cầm xẻng: Nút phát xung viền sáng nhấp nháy báo hiệu xẻng đang ở trên tay.
- **Phím tắt nhanh:** Nhấn phím **`S`** trên bàn phím để cầm xẻng hoặc cất xẻng nhanh.
- **Hủy thao tác tiện lợi bằng Chuột Phải:** Nhấp chuột phải (`Right-Click`) bất kỳ lúc nào để hủy chọn xẻng hoặc hủy chọn cây đang cầm trên tay.
- **Hiệu ứng đồ họa (VFX):**
  - Khi cầm xẻng, chiếc xẻng bay linh hoạt theo vị trí con trỏ chuột (có vầng hào quang vàng và tia sáng lấp lánh `✨` nếu là Xẻng Vàng).
  - Khi rê chuột lên một ô có cây trồng: Ô cây sáng viền nổi bật (viền vàng kim nếu có Xẻng Vàng, viền đỏ nếu là xẻng thường) kèm theo nhãn thông tin xem trước số Mặt trời sẽ nhận lại: `"+XX ☀️ Hoàn 50%"`.
  - Click vào cây để đào bỏ, hoặc click vào ô đất trống để cất xẻng an toàn.

## Triển khai công năng vật phẩm Cửa Hàng: Xe Cắt Cỏ (Lawn Mower Row Sweep)

Theo yêu cầu tùy biến riêng: *"Dùng thì xe sẽ chạy và tiêu diệt toàn bộ zombie trên 1 hàng ngang mà người chơi chọn, có hoạt ảnh xe cắt cỏ chạy từ bên phải qua bên trái trong vòng 3.5s"*.

### 1. Cơ chế hoạt động (Gameplay Mechanics)
- **Kích hoạt ngắm hàng ngang (Row Targeting Mode):**
  - Người chơi bấm vào nút `[🚜 M]` trên thanh Header hoặc nhấn phím tắt **`M`**.
  - Con trỏ chuột chuyển sang chế độ chọn hàng. Toàn bộ hàng ngang nơi con trỏ đang rê qua sẽ được quét sáng bằng một chùm tia cảnh báo nguy hiểm (hazard beam) với dải viền cảnh báo sọc chéo vàng-đen và các mũi tên chỉ hướng phóng xe `◀ ◀ ◀ 🚜`.
  - Nhấp chuột trái vào bất kỳ ô nào trên hàng để xác nhận phóng xe cắt cỏ càn quét hàng đó.
  - Nhấp chuột phải bất kỳ lúc nào để hủy thao tác ngắm an toàn.
- **Thời lượng và Hướng chạy:**
  - **3.5 giây chuẩn xác:** `TOTAL_TICKS = 210` tick ở tốc độ 60 FPS.
  - **Từ phải qua trái:** Xe xuất phát từ mép phải màn hình (`startX = getScreenWidth() + 30px`) và lao sang mép trái ngoài màn hình (`endX = -90px`) với vận tốc đều đặn `speed = (startX - endX) / 210` (khoảng 5.45 px/tick).
- **Càn quét và Tiêu diệt triệt để:**
  - Toàn bộ Zombie trên hàng ngang được chọn (kể cả Zombie thường, Zombie đội xô, Cột giao thông hay Mini-Boss) khi chạm phải xe cắt cỏ đều nhận sát thương cực đại (`99,999 DMG`) và bị nghiền nát ngay lập tức.
  - Zombie ở các hàng khác hoàn toàn không bị ảnh hưởng.
  - Sau khi hoàn tất hành trình 3.5 giây hoặc chạy khuất mép trái màn hình, xe cắt cỏ tự động giải phóng bộ nhớ.

### 2. Hoạt ảnh và Hiệu ứng (Animations & Visual Effects)
- **Hoạt ảnh khung xương Reanim (`LawnMower.reanim`):**
  - Xe được dựng hình hoạt ảnh reanim chân thực: bánh xe trước/sau cuộn tròn liên tục theo tốc độ di chuyển, thân xe và động cơ rung giật cơ học (`engineBob`), lật mặt quay sang trái chuẩn xác.
- **Vết cỏ bị san phẳng (Mowed Grass Trail):**
  - Xe chạy qua đến đâu để lại vệt cỏ bị cắt phẳng lì màu xanh đậm sắc nét kéo dài từ mép phải tới vị trí xe hiện tại.
- **Hệ thống hạt động lực học (Dynamic Particles):**
  - Khói xả động cơ xám mù mịt phụt ra từ ống bô phía sau xe.
  - Mảnh cỏ tươi xanh biếc văng tung tóe từ gầm lưỡi cắt phía trước.
  - Các mảnh vụn văng tung tóe (đỏ rực, xanh zombie) khi Zombie bị xe cắt cỏ nghiền nát.
- **Hiệu ứng âm thanh (Audio SFX):**
  - `playMowerRevSound()`: Tiếng động cơ xe giật nổ gầm rú khi người chơi kích hoạt chọn hàng.
  - `playMowerLaunchSound()`: Tiếng xe phóng vút đi với động cơ rít lên mạnh mẽ khi xuất phát.
  - `playMowerKillSound()`: Tiếng va chạm cơ học lưỡi cắt nghiền nát zombie.

### 3. Giao diện và Ví tiền (UI & Wallet)
- **Nút bấm trên thanh Header:** Nút `[🚜 M]` nằm cạnh nút Xẻng `[⛏️ S]`, Bùa Đóng Băng `[❄ F]`, Kẹo Chocolate `[🍫 C]` và Ví Tiền.
- **Huy hiệu số lượng:** Hiển thị số lượng xe sở hữu `xN` hoặc nút `+MUA` khi trong kho đã hết.
- **Lưu trữ dữ liệu:** Trừ 1 xe trong `lawnMowerBackupCount` và lưu an toàn vào `saves/wallet_save.json`.

## Cập nhật sửa lỗi Xe Cắt Cỏ & Cơ chế loại trừ độc quyền công cụ (Mutual Exclusivity)

### 1. Cập nhật hình ảnh xe cắt cỏ với `icon_roofcleaner`
- **Nguyên nhân trước đó:** Tệp hoạt ảnh `LawnMower.reanim` bị thiếu định nghĩa track/clip nội bộ trong cơ chế Reanim của game dẫn đến thực thể không kết xuất được hình ảnh thân xe.
- **Giải pháp xử lý:**
  - Nạp trực tiếp hình ảnh chuẩn `icon_roofcleaner.png` từ thư mục tài nguyên (`assets/images/icon_roofcleaner.png`).
  - **Lật ảnh ngang sang bên trái (`scale(-1, 1)`):** Vì xe chạy từ bên phải qua bên trái, hình ảnh được lật ngang chuẩn xác để đầu xe/lưỡi quét hướng sang trái theo đúng chiều di chuyển.
  - Đồng bộ hiển thị `icon_roofcleaner`:
    1. **Thực thể xe đang chạy trên sân (`ActiveLawnMower`):** Kích thước 68x52, có bóng đổ, rung cơ học (`engineBob`), vệt cỏ san phẳng, khói xả đuôi xe và tia lửa/cỏ văng đầu xe.
    2. **Nút bấm trên thanh Header (`drawLawnMowerButton`):** Hiển thị biểu tượng `icon_roofcleaner` lật trái sắc nét ngay trên nút `[M]`.
    3. **Thẻ vật phẩm Cửa Hàng Dave (`ShopScreen`):** Hiển thị biểu tượng xe cắt cỏ đồng bộ trong thẻ mua hàng.

### 2. Khắc phục lỗi xung đột đè công cụ (Mutually Exclusive Targeting Architecture)
- **Vấn đề:** Khi đang kích hoạt chế độ chọn hàng cho Xe Cắt Cỏ nhưng chưa click chọn hàng mà lại bấm dùng Xẻng (hoặc phím tắt `S`), cả 2 công cụ cùng hoạt động đè lên nhau (vừa hiện tia quét hàng vừa hiện xẻng theo chuột).
- **Kiến trúc giải quyết triệt để:**
  - Xây dựng phương thức tập trung `GameManager.cancelAllTargetingModes()` quản lý toàn bộ các công cụ/chế độ ngắm trên tay:
    - `selectedPlant = 0` (Hủy chọn thẻ cây)
    - `shovelSelected = false` (Cất xẻng)
    - `mowerTargeting = false` (Hủy ngắm xe cắt cỏ)
    - Tự động tích hợp cho các công cụ tương lai (như Phân bón siêu cấp `plantFoodTargeting`...).
  - **Cơ chế độc quyền (Mutually Exclusive):**
    - Khi bật Xẻng (`toggleShovel`): Tự động hủy ngắm Xe Cắt Cỏ và bỏ chọn thẻ cây.
    - Khi bật Xe Cắt Cỏ (`activateLawnMowerItem`): Tự động cất Xẻng và bỏ chọn thẻ cây.
    - Khi chọn thẻ cây (`uiCards`): Tự động hủy cả Xẻng và Xe Cắt Cỏ.
    - Khi nhấp chuột phải (`handleRightClick`): Hủy toàn bộ công cụ đang cầm an toàn.
    - Khi mở Shop hoặc Reset game: Dọn dẹp toàn bộ trạng thái công cụ.

### 3. Tinh chỉnh văn bản Xe Cắt Cỏ & Tối ưu hóa Cửa Hàng (Shop Cleanup)
- **Bỏ chữ 3.5s khi chọn hàng:**
  - Nhãn hiển thị trên chùm tia ngắm chọn hàng ngang được đổi từ `"🚜 PHÓNG XE CẮT CỎ HÀNG X (3.5s) ◀"` thành `"🚜 PHÓNG XE CẮT CỎ HÀNG X ◀"`.
  - Tooltip hướng dẫn phím tắt `[M]` và mô tả trong Shop cũng được làm gọn gàng, súc tích.
- **Xóa bỏ 3 mặt hàng chưa làm trong Cửa Hàng Crazy Dave:**
  - Loại bỏ hoàn toàn 3 mục không còn cần thiết:
    1. *Phân Bón Siêu Cấp (`plantfood`)*
    2. *Map Đập Hũ (`vase`)*
    3. *Map I, Zombie (`izombie`)*
  - Giữ lại trọn vẹn **5 mặt hàng chính thức** đã hoàn thiện 100% công năng:
    1. **Pet Ốc Sên Stinky (`snail`):** Bò nhặt xu và mặt trời tự động.
    2. **Kẹo Chocolate (`chocolate`):** Tăng 250% tốc độ cho ốc sên trong 3 phút (phím `C`).
    3. **Xe Cắt Cỏ Càn Quét (`lawnmower`):** Phóng xe càn quét toàn bộ 1 hàng ngang (phím `M`).
    4. **Bùa Đóng Băng (`iceshroom`):** Đóng băng cứng và làm chậm toàn sân đấu trong 5 giây (phím `F`).
    5. **Xẻng Vàng (`shovel`):** Đào thu hồi cây hoàn trả 50% số Mặt trời (phím `S`).
  - **Tái cấu trúc bố cục Cửa Hàng (Centered 2-Row Grid):** Giao diện 5 thẻ được căn giữa trang trọng và cân đối trên màn hình (Hàng 1 gồm 3 thẻ, Hàng 2 gồm 2 thẻ), thẻ to rõ ràng với đầy đủ nút bấm, trạng thái sở hữu và mức giá.

## Điều chỉnh phím tắt vật phẩm thành 1, 2, 3, 4

Nhằm tối ưu hóa trải nghiệm thao tác nhanh gọn trong trận chiến, toàn bộ các phím tắt của vật phẩm Cửa Hàng đã được chuẩn hóa theo hàng phím số **1, 2, 3, 4**:

### 1. Ánh xạ phím tắt chuẩn hóa
- **Phím `1` (hoặc Numpad `1`):** **Bùa Đóng Băng (`iceshroom`)** - Kích hoạt đóng băng cứng và làm chậm toàn sân 5 giây.
- **Phím `2` (hoặc Numpad `2`):** **Kẹo Chocolate (`chocolate`)** - Cho Pet Ốc Sên ăn tăng 250% tốc độ bò trong 3 phút.
- **Phím `3` (hoặc Numpad `3`):** **Xẻng Vàng / Xẻng Đào (`shovel`)** - Bật/cất xẻng trên tay để đào cây và hoàn trả 50% Mặt trời.
- **Phím `4` (hoặc Numpad `4`):** **Xe Cắt Cỏ (`lawnmower`)** - Bật/tắt chế độ chọn 1 hàng ngang để phóng xe càn quét.
*(Ghi chú: Các phím phụ cũ F, C, S, M vẫn được duy trì ngầm để đảm bảo tương thích ngược 100%)*

### 2. Đồng bộ giao diện thanh Header (Hotbar Layout)
- **Sắp xếp theo thứ tự trực quan 1 -> 4 từ trái qua phải:**
  - `[1] ❄ Bùa Đóng Băng` (X = 564)
  - `[2] 🍫 Kẹo Chocolate` (X = 620)
  - `[3] ⛏ Xẻng Vàng` (X = 676)
  - `[4] 🚜 Xe Cắt Cỏ` (X = 732)
  - `[ $ Ví Tiền Dave ]` (Góc phải)
- **Huy hiệu phím (Badge):** Góc trên bên trái mỗi nút hiển thị rõ ràng số phím tương ứng: `1`, `2`, `3`, `4`.
- **Tooltip hướng dẫn & Bong bóng tương tác:** Cập nhật đồng bộ các chỉ dẫn `[Phím 1]`, `[Phím 2]`, `[Phím 3]`, `[Phím 4]` khi rê chuột vào các nút và khi rê chuột vào Pet Ốc Sên.

## Rà soát toàn bộ dự án, khắc phục bug ẩn và tối ưu hóa hệ thống

Tiến hành rà soát toàn diện mã nguồn trò chơi (Gameplay, Audio, Graphics Rendering, Concurrency & Thread-safety), phát hiện và tối ưu triệt để các lỗi tiềm ẩn sau:

### 1. Khắc phục nguy cơ xung đột đa luồng (`ConcurrentModificationException`) giữa Swing EDT và GameThread
- **Nguyên nhân tiềm ẩn:** Trò chơi vận hành mô hình đa luồng: `GameManager.update()` chạy trên luồng phụ (`gameThread`), trong khi `GameManager.draw()` chạy trên luồng giao diện người dùng Swing EDT (`paintComponent`).
  - Danh sách `WalletManager.floatingTexts` (các con số/chữ nổi bay lên UI khi nhặt tiền, kích hoạt vật phẩm) trước đây sử dụng `ArrayList`. Khi `gameThread` gọi `add()` hoặc xóa phần tử đã hết hạn (`remove()`) cùng lúc với Swing EDT duyệt qua để vẽ, sẽ làm phát sinh ngoại lệ `ConcurrentModificationException` làm đứng hình trò chơi.
  - Tương tự, danh sách hiệu ứng hạt `ActiveLawnMower.particles` (khói xả động cơ, mảnh cỏ xanh, tia lửa) trước đây sử dụng `ArrayList`, có nguy cơ lỗi đa luồng khi vẽ xe đang chạy.
- **Giải pháp tối ưu:**
  - Chuyển `WalletManager.floatingTexts` và `ActiveLawnMower.particles` sang `CopyOnWriteArrayList`.
  - Cập nhật cơ chế dọn dẹp bằng biểu thức lambda an toàn luồng `removeIf()` thay cho `Iterator.remove()`, triệt tiêu hoàn toàn xung đột đa luồng mà không làm suy giảm FPS.

### 2. Khắc phục rò rỉ tài nguyên âm thanh (Native Sound Mixer Resource Leak) trong `AudioManager`
- **Nguyên nhân tiềm ẩn:** Trong phương thức `AudioManager.playAudioFile(String path)`, luồng âm thanh `AudioInputStream` và đối tượng `Clip` được mở và phát nhưng không có bộ lắng nghe sự kiện (`LineListener`). Trong Java Sound API, hệ điều hành chỉ cấp một số lượng hữu hạn các đường truyền trộn âm thanh (mixer lines). Nếu chơi game lâu hoặc phát nhiều hiệu ứng file WAV/AU liên tục, các đường mixer sẽ bị cạn kiệt, dẫn đến lỗi `LineUnavailableException` và mất tiếng toàn bộ game.
- **Giải pháp tối ưu:**
  - Đăng ký `clip.addLineListener(...)` bắt sự kiện `LineEvent.Type.STOP`. Ngay khi âm thanh phát xong, hệ thống tự động gọi `clip.close()` và `stream.close()`, giải phóng lập tức tài nguyên về cho hệ điều hành.

### 3. Tối ưu hóa dọn dẹp trạng thái ngắm (`cancelAllTargetingModes`) trong mọi tình huống kết thúc
- **Giải pháp tối ưu:**
  - Khi trồng cây thành công, khi không đủ Mặt trời hoặc khi click vào ô không hợp lệ: gọi tập trung `cancelAllTargetingModes()` để đảm bảo không còn trạng thái thừa hoặc xung đột với xẻng/xe cắt cỏ.
  - Khi màn chơi đạt trạng thái Chiến Thắng (`GAMEWON`) hoặc Thất Bại (`GAMEOVER`): tự động gọi `cancelAllTargetingModes()`, xóa sạch toàn bộ chùm tia ngắm và xẻng theo chuột trên màn hình tổng kết.

## Khắc phục lỗi tương tác Zombie - Cây đào bỏ & Xuất hiện Pet Ốc Sên khi mua trong trận

Đã xử lý dứt điểm 2 lỗi trực tiếp do người chơi phát hiện và mở rộng xử lý triệt để toàn bộ các lỗi mục tiêu "ma" tương tự:

### 1. Khắc phục lỗi Zombie tiếp tục nhai không khí khi đào cây bị chặn (Ghost Target / Eating Air)
- **Nguyên nhân cốt lõi:**
  - Khi người chơi dùng xẻng đào cây (`digPlant`), hệ thống gọi `plants.remove(p)` nhưng máu của cây `p.health` vẫn giữ nguyên (> 0), dẫn đến `p.isDead()` trả về `false`.
  - Zombie đang gặm cây đó vẫn giữ biến `attackTarget = p`. Trong vòng lặp `Zombie.update()` chỉ kiểm tra `if (attackTarget.isDead())`, do `isDead()` là `false` nên zombie tiếp tục kích hoạt nhịp cắn và lệnh `return;` khiến zombie đứng yên vĩnh viễn và nhai trong không khí tại ô vừa đào.
- **Giải pháp khắc phục toàn diện:**
  - Trong `GameManager.digPlant(p)`: đánh dấu sát thương hủy diệt `p.takeDamage(999999)` để `p.isDead()` lập tức thành `true`, đồng thời duyệt qua toàn bộ `gm.zombies` và đặt `z.attackTarget = null` nếu đang nhắm vào cây `p`.
  - Trong `Zombie.java` và `ShooterZombie.java`: cập nhật điều kiện kiểm tra an toàn `if (attackTarget.isDead() || (gm != null && !gm.plants.contains(attackTarget))) { attackTarget = null; }`.
  - Trong vòng lặp chính `GameManager.java`: zombie tự động hủy mục tiêu nếu cây không còn tồn tại trong `plants`, và chỉ bắt mục tiêu mới nếu cây đó còn sống `!p.isDead()`.

### 2. Khắc phục lỗi Pet Ốc Sên không xuất hiện ngay khi mua trong lúc đang chơi
- **Nguyên nhân cốt lõi:**
  - Biến `petSnail` trước đây chỉ được khởi tạo một lần duy nhất tại `resetGame()` khi bắt đầu màn chơi mới. Nếu người chơi bấm vào Shop giữa trận và mua Pet Ốc Sên, biến `hasPetSnail` được lưu vào `WalletManager` nhưng đối tượng `gm.petSnail` vẫn là `null`.
- **Giải pháp khắc phục:**
  - Trong `ShopScreen.handleClick()`: khi mua thành công Pet Snail (`buyPetSnail()`), tự động khởi tạo ngay lập tức `gm.petSnail = new PetSnail(50, gm.getMapHeight() - 60)`.
  - Trong `GameManager.closeShop()`: khi đóng cửa hàng quay lại trận đấu (`state == GameState.PLAYING`), kiểm tra nếu người chơi đã sở hữu ốc sên mà trên sân chưa có thì tự động sinh ra ngay.
  - Cơ chế tự phục hồi tại `updateCollectibles()` và `drawPlaying()`: nếu người chơi đã sở hữu ốc sên nhưng `petSnail == null`, hệ thống sẽ tự động tạo đối tượng ốc sên ngay trong frame tiếp theo.

### 3. Rà soát và khắc phục các lỗi mục tiêu "ma" tương tự (Similar Ghost Target Defects)
- **Cây bắn (Peashooter, SnowPea, SniperPlant) nhắm bắn Zombie đã chết:**
  - Trước đây trong hàm tìm kiếm mục tiêu `getClosestZombie()`, các cây đậu duyệt `gm.zombies` mà không kiểm tra `if (z.isDead()) continue;`. Khi một zombie đã bị tiêu diệt (bởi xe cắt cỏ, nổ CherryBomb hoặc đạn trước) nhưng chưa kịp gỡ khỏi danh sách ở cuối frame, các cây đậu vẫn ngắm và bắn thêm đạn vào xác chết.
  - *Đã khắc phục:* Bổ sung kiểm tra `if (z.isDead()) continue;` cho toàn bộ các lớp cây Peashooter, SnowPea và SniperPlant.
- **Đạn bắn (Projectiles) va chạm với xác Zombie hoặc Cây đã chết:**
  - Đạn đậu của người chơi trước đây va chạm với bất kỳ zombie nào trên đường bay kể cả khi zombie đã chết (`isDead() == true`), làm lãng phí đạn thay vì bay xuyên qua để bắn trúng con zombie sống phía sau. Tương tự, xương của ShooterZombie có thể va chạm với cây đã chết.
  - *Đã khắc phục:* Thêm điều kiện `!z.isDead()` và `!plant.isDead()` trong khối xử lý va chạm đạn của `GameManager`.
- **ShooterZombie bắn tầm xa vào Cây đã chết:**
  - Trong `ShooterZombie.update()`, vòng lặp tìm cây gần nhất `closestPlant` chưa bỏ qua cây đã chết (`p.isDead()`).
  - *Đã khắc phục:* Thêm điều kiện `if (p.isDead()) continue;`.
- **Chomper (Cây ngoạm) ngoạm trúng khoảng không khi Zombie bị hạ gục trước nhịp cắn:**
  - Khi Chomper đang há mồm vươn tới (tick 1 đến 12) mà con mồi bị xe cắt cỏ hoặc đạn tiêu diệt trước tick 13, Chomper sẽ nuốt không khí và chuyển sang trạng thái nhai (chewing 7 giây).
  - *Đã khắc phục:* Kiểm tra nếu `biteTarget` đã chết hoặc không còn trên sân, Chomper lập tức hủy động tác cắn và quay về trạng thái nghỉ (`idle`) ngay lập tức.

## Cập nhật cơ chế Xẻng Vàng dùng theo lượt & Tối ưu hóa hiển thị số lượng Xu lớn

Theo yêu cầu mới nhất của người chơi, hệ thống đã điều chỉnh kinh tế vật phẩm Xẻng và nâng cấp toàn diện giao diện hiển thị tiền xu:

### 1. Đổi Xẻng Vàng thành vật phẩm dùng theo lượt (Consumable Shovel Charges)
- **Giá bán trong Cửa Hàng:** Giảm từ **$ 2,500** xuống còn **$ 150 xu** cho mỗi lượt mua (`goldenShovelCount++`).
- **Cơ chế hoạt động:**
  - Mỗi lần đào một cây sẽ tiêu hao đúng **1 lượt xẻng** (`goldenShovelCount--`).
  - Hoàn trả ngay **50% số lượng Mặt trời** của cây được đào về ví năng lượng trận đấu, đồng thời hiển thị số Mặt trời hoàn và số lượt xẻng còn lại nổi trên màn hình.
  - Sau khi đào, nếu số lượt xẻng giảm về `0`, hệ thống tự động cất xẻng an toàn (`cancelAllTargetingModes()`).
  - Nếu số lượt xẻng bằng `0`, khi bấm phím tắt `[3]` hoặc click vào nút Xẻng trên thanh Header, trò chơi sẽ hiện thông báo nổi `"Hết lượt dùng Xẻng! Mua thêm trong Shop ($ 150) 🛒"` và tự động mở Cửa Hàng Dave để người chơi mua bổ sung.
- **Đồng bộ Hotbar Header:**
  - Nút Xẻng `[3]` được trang bị huy hiệu số lượng `xN` ở góc dưới bên phải, đồng bộ hoàn hảo với 3 vật phẩm còn lại: `[1] Bùa Đóng Băng (xN)`, `[2] Kẹo Chocolate (xN)`, `[3] Xẻng Vàng (xN)`, `[4] Xe Cắt Cỏ (xN)`.
  - Tooltip rê chuột cập nhật chính xác số lượt còn lại và hướng dẫn mua thêm.
- **Tương thích dữ liệu cũ:**
  - Nếu người chơi đã có file save với xẻng vĩnh viễn trước đây (`hasGoldenShovel = true`), hệ thống tự động quy đổi thành 10 lượt xẻng miễn phí để người chơi không bị thiệt thòi. Người chơi mới được tặng sẵn 3 lượt xẻng dùng thử.

### 2. Khắc phục triệt để lỗi hiển thị số lượng Xu lớn (Dynamic Bank Layout & Number Formatting)
- **Nguyên nhân lỗi trước đây:**
  - Các khung hiển thị Ví Tiền ở Main Menu (145px), In-Game Header (120px) và Shop (190px) đều có chiều rộng cố định (hardcoded).
  - Khi người chơi tích lũy số lượng xu lớn (ví dụ: hàng chục nghìn, hàng trăm nghìn hoặc hàng triệu xu), chuỗi ký tự bị tràn ra ngoài viền khung, chồng lấn lên nút Menu/Shop hoặc bị che khuất một phần.
  - Ngoài ra, chuỗi số không có dấu phẩy phân tách hàng nghìn khiến dãy số dài rất khó nhìn.
- **Giải pháp tối ưu hóa:**
  - **Khung tự động co giãn linh hoạt (Dynamic Sizing):** Sử dụng `FontMetrics` để đo chính xác chiều dài chuỗi số tiền theo thời gian thực:
    - Main Menu: `mbW = Math.max(150, textW + 54)`, neo mép phải màn hình `mbX = sw - mbW - 20`.
    - In-Game Header: `bankW = Math.max(125, textW + 42)`, tự động neo bên trái nút MENU `bankX = menuX - bankW - 12`. Nút Shop bên dưới cũng đồng bộ độ rộng và vị trí với khung tiền.
    - Shop Dave: `bankW = Math.max(190, textW + 68)`, neo mép phải `bankX = sw - bankW - 24`.
  - **Định dạng số chuẩn quốc tế:** Chuỗi số được format qua `String.format(Locale.US, "%,d", coins)` tự động chèn dấu phẩy phân tách hàng nghìn (ví dụ: `$ 150,000`, `$ 1,000,000`), giúp hiển thị rõ ràng, chuyên nghiệp.
  - **Chống tràn số học (Integer Overflow Guard):** Giới hạn tối đa an toàn `Math.min(2_000_000_000L, ...)` tránh trường hợp tràn số nguyên 32-bit thành số âm.

## Cơ chế cô lập tiến trình chơi và bảo toàn dữ liệu mặc định cho Người Chơi Mới

Nhằm giải quyết triệt để tình trạng khi người chơi/nhà phát triển chơi thử trên máy cá nhân làm thay đổi số dư xu và số lượng vật phẩm trong file save, sau đó `git push` vô tình đè dữ liệu chơi cá nhân lên repository của nhóm:

### 1. Quy định chuẩn xác dữ liệu mặc định cho Người Chơi Mới (New Player Baseline)
Toàn bộ người chơi mới khi cài đặt hoặc clone dự án lần đầu tiên sẽ có các chỉ số mặc định:
- **Tiền khởi đầu:** **100 Xu** (`coins = 100`).
- **Mỗi loại vật phẩm có sẵn đúng 2 cái:**
  - `[1] Bùa Đóng Băng:` **2 cái** (`iceShroomBlastCount = 2`).
  - `[3] Xẻng Vàng:` **2 lượt** (`goldenShovelCount = 2`).
  - `[4] Xe Cắt Cỏ Dự Phòng:` **2 cái** (`lawnMowerBackupCount = 2`).
  - `Phân bón tăng trưởng (Plant Food):` **2 cái** (`plantFoodCount = 2`).
- **Trừ Sên là KHÔNG CÓ:** `hasPetSnail = false` (phải mua trong Shop với giá $ 3,000).
- **Kẹo Chocolate KHÔNG CÓ:** `chocolateCount = 0` (phải mua trong Shop với giá $ 200).

### 2. Thiết kế kiến trúc cô lập dữ liệu tiến trình (Save Data Isolation Architecture)
- **Tách biệt dữ liệu cá nhân ra khỏi Git Repository:**
  - Đường dẫn ưu tiên hàng đầu của file save người chơi được chuyển ra ngoài thư mục dự án: `USER_SAVE_PATH = ~/.garden-defense/wallet_save.json` (nằm trong thư mục Home của người dùng trên hệ điều hành).
  - Khi bất kỳ ai chơi game, tiến trình mua sắm, tích lũy xu và vật phẩm sẽ được lưu độc lập tại đây, hoàn toàn không tạo hoặc sửa file trong thư mục git.
- **Tạo tệp mẫu chuẩn mực `saves/wallet_default.json`:**
  - Repository được bổ sung file mẫu `saves/wallet_default.json` chứa các giá trị khởi đầu chuẩn mực (100 xu, 2 mỗi loại vật phẩm, 0 sên, 0 chocolate). File này được Git theo dõi vĩnh viễn và không bao giờ bị ghi đè khi chơi game.
- **Untrack file save cục bộ khỏi Git (`git rm --cached`):**
  - File `saves/wallet_save.json` được gỡ bỏ khỏi Git index và bổ sung vào `.gitignore`. Kể cả khi chạy game ở môi trường bị hạn chế quyền user-home khiến game phải fallback lưu cục bộ, Git vẫn vĩnh viễn bỏ qua file này và không bao giờ đánh dấu `modified`.
- **Thứ tự nạp dữ liệu thông minh (`WalletManager.load()`):**
  1. `USER_SAVE_PATH` (`~/.garden-defense/wallet_save.json`): Dữ liệu cá nhân của người chơi trên máy.
  2. `SAVE_PATH` (`saves/wallet_save.json`): Dữ liệu cục bộ (nếu có).
  3. `DEFAULT_SAVE_PATH` (`saves/wallet_default.json`): Bản mẫu mặc định chuẩn của dự án.
  4. `new SaveData()`: Khởi tạo giá trị mặc định trực tiếp trong RAM.

Nhờ cơ chế này, bất kỳ ai `git clone` dự án về máy đều có trải nghiệm khởi đầu chuẩn xác 100%, và dù họ có chơi hay test đến bao nhiêu xu thì khi `git push` lên GitHub cũng không bao giờ làm ảnh hưởng đến dữ liệu người chơi mới.

## Cơ chế Countdown Hồi Chiêu Thực Vật & Cảnh Báo Zombie Xuất Hiện

Theo yêu cầu nâng cấp trải nghiệm chiến thuật và cảnh báo tình huống chiến trường:

### 1. Countdown hồi chiêu cho các cây sau khi trồng (Plant Cooldown System)
- **Thời gian hồi chiêu hợp lý, cân bằng chiến thuật cho từng loại cây:**
  - `Sunflower (Hoa hướng dương):` **7.5 giây** (`CD_SUNFLOWER = 450 frames`) - Cây kinh tế cơ bản, hồi nhanh để đảm bảo dòng tiền.
  - `Peashooter (Đậu bắn cơ bản):` **7.5 giây** (`CD_PEASHOOTER = 450 frames`) - Đơn vị hỏa lực tuyến đầu, hồi nhanh.
  - `SnowPea (Đậu băng làm chậm):` **7.5 giây** (`CD_SNOWPEA = 450 frames`) - Làm chậm và giảm tốc độ zombie, hồi nhanh.
  - `SniperPlant (Cây bắn tỉa):` **10.0 giây** (`CD_SNIPER = 600 frames`) - Sát thương 2x tầm xa, thời gian hồi trung bình.
  - `Marigold (Cúc vàng sinh tiền):` **10.0 giây** (`CD_MARIGOLD = 600 frames`) - Cây farm xu meta-game.
  - `Chomper (Cây ngoạm):` **15.0 giây** (`CD_CHOMPER = 900 frames`) - Nuốt chửng 1 zombie tức thì, hồi lâu hơn.
  - `WallNut (Khoai tây chắn đường):` **20.0 giây** (`CD_WALLNUT = 1200 frames`) - Chống chịu cực trâu (30 máu), cooldown dài để tránh lạm dụng dựng tường liên tục.
  - `CherryBomb (Quả anh đào nổ AOE):` **30.0 giây** (`CD_CHERRYBOMB = 1800 frames`) - Vũ khí dọn sân diện rộng sát thương cực lớn, yêu cầu tính toán thời điểm sử dụng khôn ngoan.
- **Giao diện đếm ngược trực quan, hiện đại:**
  - **Lớp phủ quét (Cooldown Sweep):** Lớp phủ tối đen bán trong suốt (`rgba(0, 0, 0, 185)`) che phủ thẻ bài và hạ dần xuống dưới theo tỉ lệ thời gian còn lại.
  - **Thanh quét sáng (Sweep Line):** Đường viền phát sáng ở ranh giới quét tạo cảm giác chuyển động mượt mà.
  - **Số giây đếm lùi sắc nét:** Hiển thị trực tiếp số giây còn lại (ví dụ: `7.5s`, `6.2s`, `15s`) chính giữa thẻ bài với chữ vàng sáng viền bóng đen, người chơi lập tức nắm bắt thời điểm cây sẵn sàng.
  - **Bảo vệ độc quyền:** Khi thẻ bài đang trong countdown (`currentCooldown > 0`), hệ thống chặn thao tác chọn cây và phát âm báo nhẹ.

### 2. Cảnh báo Zombie xuất hiện (Zombie Spawn Hazard Alert)
- **Biểu tượng tam giác chấm than ⚠️ (Hazard Warning Triangle):**
  - Xuất hiện ngay tại ô xuất phát của Zombie khi vừa spawn trên sân.
  - Thiết kế hình tam giác cân màu vàng rực rỡ viền đỏ cam đậm, dấu chấm than `!` nét to bo tròn nổi bật.
  - Quầng sáng cảnh báo màu đỏ cam dưới nền kết hợp hiệu ứng nhịp đập cảnh báo (pulse animation `1.0f -> 1.12f`) thu hút sự chú ý tức thì của người chơi.
  - Tự động kẹp tọa độ (clamp) đảm bảo biểu tượng luôn nằm trọn vẹn trong tầm nhìn, không bị Header hoặc mép bản đồ che khuất.
- **Chuỗi mũi tên chỉ hướng kéo dài trên cả đường đi từ đầu tới cuối của Zombie (Full-Route Marching Arrows):**
  - Tự động dựng toàn bộ lộ trình di chuyển của Zombie từ điểm xuất phát qua tất cả các waypoint trung gian đến đích cuối cùng.
  - **Đường nét đứt phát quang động (Animated Dashed Guideline):** Nối liền các waypoint với hiệu ứng vệt sáng cam đỏ mờ và nét đứt chuyển động nhịp nhàng dọc theo hành lang di chuyển.
  - **Chuỗi mũi tên đồ họa chuyển động liên tục:** Các mũi tên màu đỏ tươi viền vàng sáng sắc nét được bố trí trải đều trên toàn bộ tuyến đường (khoảng cách 44px mỗi mũi tên), liên tục trượt sóng động theo chiều hành quân của zombie, giúp người chơi thấy rõ toàn bộ lộ trình kẻ địch sẽ đi qua.
  - **Vòng tròn mục tiêu cảnh báo điểm cuối:** Điểm kết thúc tuyến đường (nơi zombie tiến vào nhà) có vòng tròn nhấp nháy báo hiệu khu vực cần bảo vệ khẩn cấp.
- **Thời gian tồn tại:**
  - Cả tam giác cảnh báo và chuỗi mũi tên kéo dài toàn tuyến hiển thị chính xác trong **2.0 giây** (`120 frames` ở 60 FPS).
  - Ở 0.35 giây cuối (`timer < 20 ticks`), toàn bộ hiệu ứng mờ dần (fade-out) nhẹ nhàng rồi tự động dọn dẹp khỏi bộ nhớ.

## Hiệu ứng rơi tay khi còn nửa máu & Rụng đầu tan biến dần khi Zombie chết

Nhằm tái hiện chân thực và sống động phong cách kinh điển của Plants vs. Zombies:

### 1. Hiệu ứng rơi cánh tay khi còn 1 nửa máu (Arm Drop at Half HP)
- **Cơ chế kích hoạt:** Khi Zombie nhận sát thương khiến lượng máu giảm xuống còn $\le 50\%$ máu tối đa (`health <= maxHp / 2`):
  - **Đứt lìa cánh tay trên thân Zombie:** Bộ diễn hoạt [Reanim.java](file:///c:/btl%20oop/New%20folder/BaoVeKhuVuon/BaoVeKhuVuon/src/main/java/com/team/gardendefense/view/renderer/Reanim.java) tự động ẩn toàn bộ các track cánh tay ngoài (`Zombie_outerarm_upper`, `Zombie_outerarm_lower`, `Zombie_outerarm_hand`), để lại khớp vai cụt chân thực trên người zombie.
  - **Mảnh cánh tay văng ra ngoài ([ZombieLimb.java](file:///c:/btl%20oop/New%20folder/BaoVeKhuVuon/BaoVeKhuVuon/src/main/java/com/team/gardendefense/model/entity/monster/ZombieLimb.java)):** Sử dụng sprite hạt gốc `ZombieArm.png`, văng lên không trung theo quỹ đạo parabol vật lý có gia tốc trọng lực, xoay tròn tự nhiên, nảy nhẹ khi chạm mặt đường và nằm lại trên sân trước khi mờ dần trong 1.5 giây. Đồng bộ chính xác tỉ lệ scale `0.55f` khớp 100% với kích thước cánh tay trên mô hình Zombie gốc.

### 2. Hiệu ứng rơi đầu và biến mất từ từ khi Zombie chết (Head Drop & Fade Out on Death)
- **Rụng đầu vật lý (Head Pop & Roll):**
  - Ngay khi Zombie hết máu (`health <= 0`), chiếc đầu rụng lìa khỏi cổ thông qua phương thức `ZombieLimb.createHead()`, sử dụng sprite hạt gốc `ZombieHead.png`.
  - Đầu zombie văng lên theo hình vòng cung, rơi xuống đất, nảy nảy vài nhịp theo quán tính và mờ dần trong 1.5 giây.
  - Đồng bộ chuẩn xác hệ số scale `ZOMBIE_SCALE = 0.55f` (từ kích thước gốc $64 \times 61$ px thu nhỏ về $35 \times 33$ px), vừa khít hoàn hảo với phần đầu trên thân Zombie gốc, không còn tình trạng đầu bị to bất thường khi rơi.
- **Thân không đầu ngã quỵ và biến mất dần (Headless Body Fall & Fade Out):**
  - Trên thân Zombie, toàn bộ các track vùng đầu và cổ (`head`, `neck`, `hair`, `tongue`, `jaw`) lập tức được ẩn đi.
  - Thân Zombie chuyển sang phân đoạn diễn hoạt chết ngã gục xuống đất (`anim_death`).
  - Độ mờ (*alpha*) của thân zombie giảm đều từ `1.0` về `0.0` trong 1.33 giây (80 frames), tạo hiệu ứng tan biến êm ái trên mặt đất thay vì biến mất đột ngột.
  - Khi thân hoàn toàn biến mất (`deathAlpha <= 0`), đối tượng mới được dọn dẹp khỏi danh sách `zombies`.
- **Bảo toàn cơ chế game & hỏa lực:**
  - Phần thưởng ánh sáng mặt trời ($+10$ ☀️) và tỉ lệ rơi xu/kim cương được trao ngay lập tức cho người chơi ngay khi zombie bắt đầu chết.
  - Các cây phòng thủ (Peashooter, SnowPea, Sniper, Chomper) và đạn đậu tự động bỏ qua xác zombie đang tan biến, đảm bảo hỏa lực luôn tập trung 100% vào các zombie sống phía sau.

## Nâng cấp Countdown Kỹ Năng Chủ Động, Cân Bằng Giá Shop & Thay Thế Đồ Họa Nút Bấm/Icon

### 1. Khắc phục nguyên nhân không chạy được code
- **Nguyên nhân cốt lõi:**
  1. Trong `GameManager.java`: `petSnail.boostTimer` là trường `private` trong `PetSnail.java`, dẫn đến lỗi biên dịch `error: boostTimer has private access in PetSnail`. Đã sửa thành phương thức getter công khai `petSnail.getBoostTimer()`.
  2. Trong `GameManager.java`: Khi tham chiếu giá xẻng vàng trong chuỗi thông báo thiếu `import com.team.gardendefense.system.ShopManager;`, dẫn đến lỗi biên dịch `error: cannot find symbol variable ShopManager`. Đã bổ sung import đầy đủ.
  3. Cải tiến `run.bat` & `run.ps1`: Tự động tìm kiếm và trỏ `JAVA_HOME` tới bộ cài đặt JDK 26 sẵn có trên máy (`C:\Program Files\Java\jdk-26.0.2`), phòng ngừa lỗi PATH hoặc stub `javapath` của Oracle bị treo trên Windows.
- **Kết quả:** Đã biên dịch sạch 100% bằng Java 11 bytecode (`--release 11`), GameLauncher khởi chạy hoàn hảo.

---

### 2. Countdown hồi chiêu cho 4 kỹ năng chủ động Hotbar
- **Cơ chế Cooldown chiến thuật:**
  - `[1] Bùa Đóng Băng (IceShroom):` **20 giây** (`CD_ICE_SHROOM = 1200 frames`). Ngăn chặn người chơi spam liên tiếp giữ quái đóng băng vĩnh viễn trên sân.
  - `[2] Kẹo Chocolate (Chocolate):` **180 giây / 3 phút** (`CD_CHOCOLATE = 10800 frames`). Bằng đúng thời gian buff tốc độ của Pet Ốc Sên, có cơ chế đồng bộ trực tiếp với `petSnail.getBoostTimer()`.
  - `[3] Xẻng Vàng (Golden Shovel):` **2.5 giây** (`CD_SHOVEL = 150 frames`) giữa các lần đào cây. Chống đào nhầm liên tục và giữ nhịp game chiến thuật.
  - `[4] Xe Cắt Cỏ Dự Phòng (Lawn Mower):` **30 giây** (`CD_LAWN_MOWER = 1800 frames`). Cân bằng vũ khí cứu nguy diện rộng 1 hàng.
- **Hiệu ứng đồ họa đếm ngược trên các nút Hotbar:**
  - Lớp phủ bóng tối quét từ trên xuống (`rgba(0, 0, 0, 185)`) bo góc mượt mà theo đúng tỉ lệ thời gian hồi chiêu còn lại.
  - Mép quét phát sáng màu trắng ngà chuyển động theo nhịp đếm lùi.
  - Hiển thị số giây còn lại sắc nét (hoặc định dạng phút:giây `MM:SS` cho kẹo) với chữ vàng sáng viền đen căn giữa nút.
  - Chặn thao tác bấm khi đang trong thời gian hồi chiêu kèm thông báo nổi (floating text) và âm thanh buzzer cảnh báo.

---

### 3. Cân bằng lại giá cả vật phẩm trong Shop Meta-game
Dựa trên chỉ số kinh tế thực tế (người chơi mới có 100 xu, mỗi màn farm được ~150 - 250 xu):
- `Pet Ốc Sên Stinky (Vĩnh viễn):` **1,500 xu** (giảm từ 3,000 xu để người chơi có thể tích lũy mua được sau vài màn chơi).
- `Xe Cắt Cỏ Càn Quét (Cứu nguy 1 hàng):` **350 xu** (giảm từ 500 xu).
- `Bùa Đóng Băng (Đóng băng toàn sân 5s):` **200 xu** (giảm từ 350 xu).
- `Kẹo Chocolate (Tăng tốc Pet 250% trong 3 phút):` **80 xu** (cân đối với lợi nhuận nhặt xu/sun do pet mang lại).
- `Xẻng Vàng (Đào hoàn 50% sun):` **100 xu / lượt** (giảm từ 150 xu).

---

### 4. Thay thế Nút bấm, Icon bằng Tài nguyên có sẵn trong images (Ưu tiên 1 & Ưu tiên 2)
- **Ưu tiên 1 (Vật phẩm Hotbar & Cửa hàng Dave):**
  - **Kẹo Chocolate:** Thay thế hoàn toàn emoji `🍫` bằng sprite kẹo thực tế `src/main/resources/assets/images/chocolate.png` trên cả nút Hotbar `[2]` và thẻ vật phẩm trong Shop.
  - **Bùa Đóng Băng:** Thay thế emoji `❄️` bằng hình ảnh đầu nấm băng xanh đặc trưng `src/main/resources/assets/reanim/IceShroom_head.png` trên cả nút Hotbar `[1]` và thẻ vật phẩm trong Shop.
  - **Pet Ốc Sên Stinky:** Thay thế emoji `🐌` bằng sprite ghép thực tế của Ốc Sên `Stinky_body.png` và `Stinky_shell.png` trong Shop Dave.
  - **Xẻng Vàng:** Thể hiện đồ họa chân thực từ `Shovel.png` trong cả Hotbar và Shop.
  - **Xe Cắt Cỏ:** Thể hiện đồ họa chân thực từ `icon_roofcleaner.png` trong cả Hotbar và Shop.
- **Ưu tiên 2 (Nút bấm đồ họa PvZ):**
  - **Mũi tên cuộn thẻ cây:** Thay thế nút vuông vẽ tay `◀` và `▶` bằng 2 sprite mũi tên đá PvZ chính thức: `zombatar_prev_button.png` và `zombatar_next_button.png` (kèm trạng thái `_highlight` khi hover và `_disabled` khi kịch thanh cuộn).
  - **Nút "QUAY LẠI":** Thay thế nút vẽ tay bằng nút đá chạm khắc phong cách PvZ `Almanac_CloseButton.png` kết hợp hiệu ứng sáng `Almanac_CloseButtonHighlight.png` tại cả màn Chọn Màn, Hướng Dẫn và Cửa Hàng Shop Dave.

### 5. Khắc phục triệt để lỗi ô vuông rỗng (Tofu Glyphs `□`)
- **Nguyên nhân:** Font mặc định `Arial` / `SansSerif` của Java Swing trên Windows không hỗ trợ các ký tự Emoji màu hoặc Emoji đặc biệt ngoài chuẩn BMP (ví dụ: `⚡`, `🛒`, `🍫`, `❄️`, `🚜`, `⛏️`, `🗺️`, `🏠`, `🔊`), dẫn đến việc hệ điều hành hiển thị các khối vuông rỗng không có ký hiệu.
- **Giải pháp hoàn thiện:**
  - **Tia sét tốc độ trên Pet Ốc Sên:** Thay thế emoji `⚡` bằng tia sét đồ họa vector vẽ trực tiếp bằng `Graphics2D.fillPolygon(...)` với hiệu ứng phát sáng vàng viền cam, vừa sắc nét vừa 100% không bị phụ thuộc font. Đổi chuỗi text thành `"SPEED MM:SS"`.
  - **Nút Shop trong trận:** Đổi `"Shop 🛒"` thành `"CỬA HÀNG"` với gradient vàng đồng phong cách nút PvZ, bỏ hoàn toàn emoji `🛒`.
  - **Thực đơn Tạm dừng (PauseDialog):** Bỏ các emoji `🛒`, `🔊`, `🗺️`, `🏠` khỏi các nút, chuyển thành text chuẩn tiếng Việt: `"CỬA HÀNG"`, `"ÂM THANH: [ BẬT ]"`, `"CHỌN MÀN KHÁC"`, `"VỀ MENU CHÍNH"`.
  - **Toàn bộ hiệu ứng hình ảnh (VFX):** Hoa tuyết rơi, tia lửa xe cắt cỏ, chỉ hướng xe cắt cỏ `◀ ◀`, và các nhãn thông báo nổi đều được chuyển sang vẽ hình học vector mượt mà hoặc text thuần túy.

### 6. Xử lý lỗi Zombie đi đè lên cây ở Màn 2 và Màn 3 (Hiệu ứng đi từ sau cây ra)
- **Hiện tượng lỗi ban đầu:**
  - Ở Màn 2 (Wave 1 tại hàng 12) và Màn 3 (Wave 1 & 2 tại hàng 9), tại khu vực mép trái nơi Zombie mới xuất hiện có cây cối lớn thuộc ảnh nền (`bgmap2.png`, `bgmap3.png`). Do toàn bộ ảnh nền được vẽ trước rồi mới vẽ Zombie, nên Zombie khi mới bước ra bị vẽ đè lên trên tán cây và thân cây, gây cảm giác phi lý.
  - Sau đó, khi tạo lớp che ban đầu, vùng che phủ bao gồm cả hàng ô cỏ ở phía trên đường đi (Hàng 11 ở Màn 2 và Hàng 8 ở Màn 3). Vì Zombie có chiều cao vượt qua 1 ô gạch, đầu của Zombie khi đi trên đường bị che khuất bất hợp lý bởi ô cỏ phía trên.
- **Giải pháp xử lý tinh chỉnh triệt để:**
  - **Tách lớp tiền cảnh (Foreground Overlay) chuẩn xác:**
    - Cắt tỉa chính xác phần thân cây và lá cây của Hàng 12 (Màn 2) và Hàng 9 (Màn 3).
    - **Loại bỏ 100% ô cỏ phía trên:** Bắt đầu tọa độ Y tiền cảnh chính xác tại mép trên của làn đường di chuyển (`y >= 11.95 * tileH` ở Màn 2 và `y >= 8.95 * tileH` ở Màn 3). Vùng ô cỏ phía trên hoàn toàn trong suốt (`Alpha = 0`).
    - Giới hạn phạm vi X chặt chẽ trong phạm vi thân cây (`x < 1.35 * tileW` ở Màn 2 và `x < 1.05 * tileW` ở Màn 3), không lấn sang các ô cỏ/đường bên cạnh.
  - **Cơ chế phân lớp đồ họa trong [Grid.java](file:///c:/btl%20oop/New%20folder/BaoVeKhuVuon/BaoVeKhuVuon/src/main/java/com/team/gardendefense/model/map/Grid.java):** Bổ sung phương thức `drawForeground(Graphics2D g)`.
  - **Thứ tự render chuẩn xác trong [GameManager.java](file:///c:/btl%20oop/New%20folder/BaoVeKhuVuon/BaoVeKhuVuon/src/main/java/com/team/gardendefense/controller/GameManager.java):**
    1. Vẽ `Grid.draw(g)` (Ảnh nền sân vườn).
    2. Vẽ Thực vật (`plants`), Zombie (`zombies`), mảnh vụn tay/đầu rơi (`zombieLimbs`), đạn bay (`projectiles`).
    3. Vẽ `Grid.drawForeground(g)` (Chỉ che phần thân cây tại tọa độ xuất hiện của Zombie, KHÔNG che đầu Zombie bởi ô cỏ phía trên).
    4. Vẽ các lớp tương tác nổi: Xe cắt cỏ (`activeMowers`), Cảnh báo Zombie xuất hiện (`spawnAlerts`), Pet Ốc sên, Ví tiền và Thanh công cụ Hotbar.
  - **Kết quả trực quan:** Khi Zombie mới xuất hiện, phần thân của Zombie nằm sau thân cây. Đầu của Zombie hoàn toàn tự nhiên nhô lên trên nền cỏ phía trên mà không bị cắt cụt hay che mất. Khi Zombie bước qua khỏi gốc cây, toàn bộ cơ thể Zombie lộ diện hoàn chỉnh ra ngoài lối đi.

### 7. Khắc phục hình ảnh Xu Bạc và Xu Vàng rơi khi Zombie chết
- **Nguyên nhân lỗi:**
  - Trong [CoinType.java](file:///c:/btl%20oop/New%20folder/BaoVeKhuVuon/BaoVeKhuVuon/src/main/java/com/team/gardendefense/model/entity/coin/CoinType.java), đường dẫn ảnh trước đây trỏ tới `coin_silver2.png` và `coin_gold2.png`. Đây thực chất là các lát cắt mỏng chỉ rộng 4 pixel (`4x41 px`) dùng trong chuỗi xoay 3D của hoạt ảnh Reanim. Khi được vẽ thành đồng xu kích thước `36x36 px`, hình ảnh bị kéo dãn méo mó, nhìn giống một vệt mỏng biến dạng thay vì đồng xu tròn.
- **Giải pháp hoàn thiện:**
  - **Bổ sung tài nguyên chuẩn vào thư mục `images`:**
    - `src/main/resources/assets/images/coin_silver.png` (`43x40 px`): Đồng xu bạc tròn đầy đủ với biểu tượng đô la chính gốc PvZ.
    - `src/main/resources/assets/images/coin_gold.png` (`45x45 px`): Đồng xu vàng tròn đầy đủ với biểu tượng đô la chính gốc PvZ.
    - `src/main/resources/assets/images/diamond.png` (`68x52 px`): Viên kim cương xanh lấp lánh chính gốc PvZ.
    - `src/main/resources/assets/images/CoinGlow.png`: Hào quang tỏa sáng của đồng xu.
  - **Cập nhật [Coin.java](file:///c:/btl%20oop/New%20folder/BaoVeKhuVuon/BaoVeKhuVuon/src/main/java/com/team/gardendefense/model/entity/coin/Coin.java):**
    - Tự động duy trì tỉ lệ khung hình chuẩn (`aspect ratio`) khi vẽ đồng xu, tránh méo góc.
    - Thêm hiệu ứng hào quang (`CoinGlow`) phát sáng nhấp nháy nhẹ nhàng trên nền cỏ/đất khi đồng xu đang nằm chờ nhặt.

### 8. Ẩn biểu tượng hình thoi (Watermark Gemini) ở góc dưới phải các bản đồ và bảng Pause
- **Nguyên nhân & Bối cảnh:**
  - Các hình ảnh nền bản đồ (`bgmap1.png`, `bgmap2.png`, `bgmap3.png`) và bảng gỗ tạm dừng (`gamepausedboard.png`) được sinh bằng mô hình AI Gemini/Imagen. Mặc định ở góc dưới cùng bên phải (`x ~ 2150..2350, y ~ 1550..1720`) đều có dập watermark hình ngôi sao/hình thoi 4 cánh `✦` màu trắng ngà của Gemini.
  - Riêng ở `bgmap4.png`, watermark này đã được che đi một cách khéo léo bằng hình ảnh một chú sinh vật màu xanh lá (cute green creature) với viền hoạt họa đậm phong cách PvZ.
- **Giải pháp xử lý:**
  - **Trích xuất & làm sạch Sprite sinh vật xanh:**
    - Trích xuất chính xác vùng nhân vật từ `bgmap4.png`, tách nền ngoại cảnh (đất đá, hàng rào) bằng thuật toán phân tích kết nối và biên độ tương phản để giữ trọn vẹn viền outline đen hoạt họa, đôi mắt long lanh và thân hình màu xanh.
    - Xuất thành tài nguyên sprite trong suốt chuẩn: `src/main/resources/assets/images/watermark_cover_creature.png` (`95x88 px`).
  - **Áp dụng che Watermark đồng bộ:**
    - Dán sinh vật xanh (kèm hiệu ứng đổ bóng mềm bên dưới để hài hòa với mặt cỏ/gỗ) đè chính xác lên tọa độ watermark hình thoi của:
      - `bgmap1.png` (Màn 1): Tọa độ `(x: 2150, y: 1545)`, kích thước `185x172 px`.
      - `bgmap2.png` (Màn 2): Tọa độ `(x: 2170, y: 1590)`, kích thước `185x172 px`.
      - `bgmap3.png` (Màn 3): Tọa độ `(x: 2170, y: 1550)`, kích thước `185x172 px`.
      - `gamepausedboard.png` (Bảng Pause): Tọa độ `(x: 1740, y: 2030)`, kích thước `130x120 px`.
    - Đồng bộ ngay lập tức vào `target/classes/assets/images/` để game hiển thị chuẩn xác khi khởi chạy.
  - **Bảo toàn 100% trật tự trước/sau (Z-order) ở Màn 2 và Màn 3:**
    - Khu vực cây cối che Zombie khi mới xuất hiện nằm ở mép TRÁI của bản đồ (`x < 250 px`), được xử lý độc lập qua lớp tiền cảnh `bgmap2_overlay.png` và `bgmap3_overlay.png` gọi bởi `Grid.drawForeground(g)`.
    - Vị trí watermark và chú sinh vật xanh nằm ở góc DƯỚI BÊN PHẢI (`x > 2150 px`, `y > 1550 px`), hoàn toàn cách biệt và không can thiệp vào các cây tiền cảnh cũng như đường đi của Zombie. Zombie vẫn đi từ sau thân cây ra một cách hoàn hảo mà không bị che mất đầu bởi cỏ.

## Cách xem thay đổi

Mở PowerShell tại thư mục dự án và chạy `.\run.bat` (hoặc `.\run.ps1`), sau đó vào màn chơi để trải nghiệm. Hệ thống tự động biên dịch với cờ tương thích `--release 11`.
