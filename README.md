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
    └── test/java/
```

## Luồng demo (gợi ý)

1. **sales** — Danh mục → Tạo yêu cầu → Theo dõi  
2. **overseas** — Tiếp nhận → Truy vấn tồn kho → Tách đơn → Gửi đơn  
3. **site01** — Vận chuyển → Mặt hàng KD → Xác nhận tồn kho → Tiếp nhận đơn  
4. **warehouse** — Danh sách đơn → Đối chiếu nhập kho  

## Ghi chú

- UI: **JavaFX + FXML**; FE gọi `app.uc001()` … `app.uc014()` (controller boundary từng UC).
- **UC007:** một `request_id` mỗi lần tách; mỗi mã hàng trong REQ chạy thuật toán phân bổ một lần.
