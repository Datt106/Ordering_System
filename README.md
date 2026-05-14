# Ordering JavaFX

Ứng dụng desktop mẫu xây trên **JavaFX 21** và **Maven**, dùng **FXML** tách giao diện khỏi mã Java.

---

## Yêu cầu hệ thống

| Thành phần | Phiên bản gợi ý |
|------------|-----------------|
| **JDK** | 21 trở lên (trùng `maven.compiler.release` trong `pom.xml`) |
| **Maven** | 3.8+ |
| **Hệ điều hành** | Windows, macOS hoặc Linux (JavaFX có artifact theo nền tảng; plugin sẽ xử lý khi chạy qua Maven) |

Kiểm tra nhanh:

```bash
java -version
mvn -version
```

---

## Cài đặt

1. **Clone hoặc tải mã nguồn** về máy.

2. **Vào thư mục dự án** (thư mục chứa `pom.xml`):

   ```bash
   cd Ordering_System
   ```

3. **Tải dependency và biên dịch** (lần đầu có thể mất vài phút do Maven tải OpenJFX từ Maven Central):

   ```bash
   mvn compile
   ```

Không cần cài JavaFX riêng: thư viện được khai báo trong `pom.xml` (`org.openjfx:javafx-controls`, `javafx-fxml`).

---

## Chạy ứng dụng

```bash
mvn javafx:run
```

Lệnh dùng plugin **`org.openjfx:javafx-maven-plugin`** (phiên bản trong `pom.xml`) và lớp chính `com.orderingsystem.fx.OrderingFxApp`.

---

## Đóng gói (tùy chọn)

```bash
mvn package
```

Artifact nằm tại `target/ordering-javafx-1.0.0-SNAPSHOT.jar`. Chạy JAR thuần với JavaFX thường cần thêm tùy chọn module path hoặc công cụ như `jlink` / bản phân phối đóng gói (GraalVM Native Image, jpackage, v.v.) — trong quá trình phát triển nên ưu tiên `mvn javafx:run`.

---

## Cấu trúc thư mục

Tổ chức theo chuẩn Maven, tách **mã nguồn**, **tài nguyên** (FXML, CSS) và **cấu hình build**.

```text
Ordering_System/
├── pom.xml                          # Maven: JavaFX, compiler, javafx-maven-plugin
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/orderingsystem/fx/
    │   │   ├── OrderingFxApp.java           # Điểm vào: javafx.application.Application
    │   │   ├── framework/
    │   │   │   └── FxmlLoaderFactory.java   # Tiện ích nạp FXML từ classpath
    │   │   └── ui/main/
    │   │       └── MainController.java      # Controller gắn với MainView.fxml
    │   └── resources/
    │       ├── fxml/
    │       │   └── MainView.fxml            # Khai báo giao diện + fx:controller
    │       └── css/
    │           └── app.css                  # Kiểu giao diện toàn cục
    └── test/java/                           # (Có thể bổ sung test sau)
```

### Ý nghĩa nhanh

| Đường dẫn | Vai trò |
|-----------|---------|
| `OrderingFxApp` | Khởi tạo `Stage`, `Scene`, gắn stylesheet, gọi nạp FXML. |
| `framework/` | Các lớp hỗ trợ dùng lại (ví dụ nạp FXML); có thể mở rộng điều hướng, theme. |
| `ui/.../MainController` | Logic điều khiển màn hình chính; liên kết với `fx:id` trong FXML. |
| `resources/fxml/` | Giao diện khai báo (layout, control). |
| `resources/css/` | Giao diện trình bày (màu sắc, font, v.v.). |

---

## Ghi chú kỹ thuật

- **Phiên bản JavaFX** và **plugin** được quản lý trong `pom.xml` (`javafx.version`, `javafx.maven.plugin.version`). Plugin `javafx-maven-plugin` trên Maven Central hiện dùng bản **0.0.8** (không dùng bản chưa tồn tại như `0.0.9`).
- Nếu đổi **JDK**, cập nhật `maven.compiler.release` cho khớp (JavaFX 21 tương thích JDK 21).

---

## Tài liệu tham khảo

- [OpenJFX](https://openjfx.io/)
- [JavaFX Maven plugin (OpenJFX)](https://github.com/openjfx/javafx-maven-plugin)
