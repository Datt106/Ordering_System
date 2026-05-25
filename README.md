# Ordering System (JavaFX)

Ứng dụng desktop **đặt hàng nhập khẩu** — Java 21, JavaFX 21, FXML, Hibernate 6, SQLite.

## Yêu cầu

| Thành phần | Phiên bản |
|------------|-----------|
| JDK | 21+ |
| Maven | 3.8+ |

```bash
java -version
mvn -version
```

## Cài đặt và chạy

```bash
cd Ordering_System
mvn compile
mvn javafx:run
```

Kiểm thử:

```bash
mvn test
```

Cơ sở dữ liệu demo: `data/ordering.db` (tự tạo khi chạy lần đầu).

## Tài khoản demo

| Vai trò | User | Mật khẩu |
|---------|------|----------|
| Sales | sales | sales123 |
| Đặt hàng quốc tế | overseas | overseas123 |
| Site | site01 | site123 |
| Kho | warehouse | wh123 |

## Cấu trúc dự án

```text
Ordering_System/
├── pom.xml
├── README.md
├── SRS.md, flow.md, usecase.md    # Tài liệu nghiệp vụ
└── src/
    ├── main/java/com/orderingsystem/
    │   ├── fx/                    # JavaFX: app, navigation, controllers, UX
    │   ├── uc00x/                 # Service theo use case
    │   ├── domain/                # Entity JPA
    │   ├── infrastructure/        # JPA, repository, seed
    │   └── auth/
    ├── main/resources/
    │   ├── fxml/                  # Giao diện theo vai trò
    │   ├── css/app.css
    │   └── META-INF/persistence.xml
    └── test/java/
```

## Ghi chú

- UI chính: **JavaFX + FXML** (`mvn javafx:run`).
- **UC007 tách đơn:** một yêu cầu (`request_id`) mỗi lần; mỗi mã hàng trong REQ chạy thuật toán phân bổ một lần.
- Module kho (warehouse) trên UI là placeholder; UC013/UC014 chưa triển khai đầy đủ.
