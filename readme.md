Dự án Game Thủ Thành: Bảo Vệ Khu Vườn (Garden Defense)

[SYSTEM INSTRUCTION FOR ANTIGRAVITY AI]

Hello Antigravity. Đây là tài liệu mô tả kiến trúc dự án. Hãy đóng vai trò là một Senior Java Developer. Nhiệm vụ của bạn là đọc toàn bộ file này và tự động khởi tạo không gian làm việc (workspace) với cấu trúc, file và bộ khung code chuẩn xác như được yêu cầu ở phần "Nhiệm vụ thi công" phía dưới.

1. Tổng quan dự án

Thể loại: Tower Defense (thủ thành lưới giống Carrot Defense, nhân vật style giống pvz).

Nền tảng: PC (Desktop Application).

Ngôn ngữ: Java (JDK 11 trở lên).

UI Framework: Java Swing / AWT.

Build tool: Maven.

Mô hình kiến trúc: MVC (Model - View - Controller).

1. Cấu trúc thư mục yêu cầu

Tạo cấu trúc thư mục và package chính xác như sau:

BaoVeKhuVuon/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/team/gardendefense/
│   │   │       ├── main/
│   │   │       │   └── GameLauncher.java
│   │   │       ├── model/
│   │   │       │   ├── map/
│   │   │       │   │   ├── Grid.java
│   │   │       │   │   └── Tile.java
│   │   │       │   └── entity/
│   │   │       │       ├── plant/
│   │   │       │       │   ├── Plant.java (Abstract)
│   │   │       │       │   └── Peashooter.java
│   │   │       │       ├── monster/
│   │   │       │       │   └── Monster.java (Abstract)
│   │   │       │       └── projectile/
│   │   │       │           └── Projectile.java (Abstract)
│   │   │       ├── controller/
│   │   │       │   ├── GameManager.java
│   │   │       │   ├── WaveManager.java
│   │   │       │   └── InputHandler.java
│   │   │       ├── view/
│   │   │       │   ├── renderer/
│   │   │       │   │   └── GamePanel.java
│   │   │       │   └── ui/
│   │   │       │       └── HUD.java
│   │   │       └── utils/
│   │   │           └── Constants.java
│   │   └── resources/
│   │       ├── assets/
│   │       │   ├── images/
│   │       │   └── sounds/
│   │       └── data/
│   │           └── level_1.json


3. Yêu cầu Lập trình hướng đối tượng (OOP)

Các file code ban đầu cần thể hiện rõ:

Tính đóng gói (Encapsulation): Các thuộc tính (máu, sát thương, giá tiền, tọa độ) phải là private hoặc protected, có đầy đủ getter/setter.

Tính đa hình và Kế thừa (Polymorphism & Inheritance):

Tạo class trừu tượng Plant với các phương thức như shoot(), upgrade(). Các cây cụ thể như Peashooter sẽ extends Plant.

Tạo class trừu tượng Monster với các phương thức như move(), takeDamage().

4. Nhiệm vụ thi công dành cho Antigravity (CẦN THỰC THI NGAY)

Dựa vào các thông tin trên, Antigravity hãy thực hiện tuần tự các bước sau:

Khởi tạo Maven: Tạo file pom.xml cơ bản cho dự án Java.

Tạo thư mục: Dựng toàn bộ cây thư mục src/main/java... và src/main/resources... như mục 2.

Viết mã khung (Boilerplate code):

Tạo tất cả các file .java đã liệt kê.

Bên trong các file thuộc package model và controller, chỉ cần khai báo class, thuộc tính cơ bản và các phương thức rỗng (chưa cần viết logic bên trong). Mục đích là để dựng khung OOP.

Tại utils/Constants.java, định nghĩa một số hằng số cơ bản như TILE_SIZE = 64, SCREEN_WIDTH = 1024, SCREEN_HEIGHT = 768.

Code GameLauncher: Viết code hoàn chỉnh cho GameLauncher.java kết hợp với GamePanel.java để khi chạy file GameLauncher (hàm main), một cửa sổ JFrame sẽ hiển thị lên màn hình với tiêu đề "Bảo vệ khu vườn", kích thước lấy từ Constants, nền màu xanh lá cây nhạt, chưa cần vẽ gì thêm.

Cấu hình Git: Tạo file .gitignore chuẩn cho dự án Java Maven.

Vui lòng hoàn thành toàn bộ các bước trên để tôi có thể đẩy dự án lên GitHub cho nhóm.