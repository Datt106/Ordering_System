# Luồng Xử lý – Hệ thống Đặt hàng Nhập khẩu

## 1. Tổng quan luồng hệ thống (End-to-End Flow)

```
[Bộ phận Bán hàng]          [Bộ phận Đặt hàng QT]       [Site nhập khẩu]        [Bộ phận Quản lý Kho]
        │                            │                           │                        │
        │── Tạo yêu cầu (UC002) ────►│                           │                        │
        │                            │── Tiếp nhận (UC005) ──────┤                        │
        │                            │                           │                        │
        │                            │── Truy vấn tồn kho ───────►                        │
        │                            │       (UC006)             │── Xác nhận tồn kho ───►│
        │                            │◄─────────────────────── (UC011)                    │
        │                            │                           │                        │
        │                            │── Tách đơn (UC007) ───────┤                        │
        │                            │                           │                        │
        │                            │── Gửi đơn hàng ───────────►                        │
        │                            │       (UC008)             │── Xác nhận đơn (UC012)─┤
        │                            │                           │                        │
        │◄── Cập nhật trạng thái ─── │                    ─────────────────────────────── │
        │        (UC003)             │                    │      Đơn hàng hiển thị (UC013)│
        │                            │                           │── Hàng về: đối chiếu ──►
        │                            │                           │       (UC014)           │
        │                            │◄──────────── Báo sai lệch (nếu có) ───────────────│
```

---

## 2. Luồng chi tiết theo từng giai đoạn

---

### GIAI ĐOẠN 1 – Khởi tạo yêu cầu nhập hàng

**Actor:** Bộ phận Bán hàng  
**Use Case:** UC002

```
BẮT ĐẦU
  │
  ▼
Nhân viên bán hàng xác định nhu cầu nhập hàng
  │
  ▼
Mở form "Tạo yêu cầu nhập hàng mới"
  │
  ▼
Nhập từng dòng mặt hàng:
  [Mã hàng] [Số lượng] [Đơn vị] [Ngày nhận mong muốn]
  │
  ├── Thêm dòng mới? ──────────────────────────────────┐
  │                                                    │
  │◄───────────────────────────────────────────────────┘
  │
  ▼
Gửi yêu cầu
  │
  ├── Dữ liệu không hợp lệ? ──► Hiển thị lỗi ──► Quay lại nhập
  │
  ▼
Hệ thống lưu yêu cầu
  Trạng thái: [CHỜ XỬ LÝ]
  Gán mã yêu cầu duy nhất
  │
  ▼
Thông báo thành công
KẾT THÚC
```

**Dữ liệu đầu ra:**
```
Yêu cầu nhập hàng:
  - Mã yêu cầu: REQ-YYYYMMDD-XXX
  - Ngày tạo
  - Người tạo
  - Trạng thái: CHỜ XỬ LÝ
  - Danh sách: [Mã hàng | Số lượng | Đơn vị | Ngày nhận mong muốn]
```

---

### GIAI ĐOẠN 2 – Tiếp nhận và Truy vấn tồn kho

**Actor:** Bộ phận Đặt hàng quốc tế, Site nhập khẩu  
**Use Case:** UC005, UC006, UC011

```
BẮT ĐẦU
  │
  ▼
Bộ phận ĐH quốc tế mở "Danh sách yêu cầu chờ xử lý"
  │
  ▼
Xem chi tiết yêu cầu → Tiếp nhận
  Trạng thái: [ĐANG XỬ LÝ]
  │
  ▼
[BƯỚC TỰ ĐỘNG] Hệ thống phân tích danh sách mặt hàng
  │
  ▼
Với mỗi mặt hàng M trong yêu cầu:
  │
  ▼
  Tra cứu danh sách Site kinh doanh M
    │
    ├── Không có Site nào? ──► Ghi lỗi cho M, xử lý mặt hàng tiếp theo
    │
    ▼
  Nhóm mặt hàng theo Site (mỗi Site nhận đúng list hàng họ có)
  │
  ▼
Gửi truy vấn tồn kho tới từng Site
  [Site code | Danh sách Merchandise code cần xác nhận]
  │
  ▼
  ╔═══════════════════════════════╗
  ║  [PHÍA SITE – UC011]          ║
  ║  Nhận truy vấn                ║
  ║  Xem danh sách mặt hàng       ║
  ║  Nhập số lượng tồn kho thực tế║
  ║  (0 nếu hết hàng)             ║
  ║  Gửi phản hồi                 ║
  ╚═══════════════════════════════╝
  │
  ▼
Hệ thống nhận phản hồi và ghi vào Tệp thông tin kho:
  [Site code | Merchandise code | In-stock quantity | Unit]
  │
  ├── Chờ đủ phản hồi từ tất cả Site
  │   (Time-out: đánh dấu tồn kho = 0)
  │
  ▼
Tệp thông tin kho hoàn chỉnh → sẵn sàng cho Giai đoạn 3
KẾT THÚC
```

---

### GIAI ĐOẠN 3 – Tách đơn hàng

**Actor:** Bộ phận Đặt hàng quốc tế  
**Use Case:** UC007

```
BẮT ĐẦU
  │
  ▼
  ┌──────────────────────────────────────────────────────┐
  │ Chọn MỘT YCNH (request_id) trạng thái Đang xử lý   │
  │ Ngày bắt đầu tính toán (StartDate) — mặc định hôm nay│
  │ Trong REQ: dòng trùng mã M → Q = SUM, D = MIN(ngày) │
  │ (Không gộp nhiều REQ — mỗi phiếu một luồng tách đơn)│
  └────────────────────────────┬─────────────────────────┘
                               │
  ▼
Đọc tồn kho của REQ đó + thông tin vận chuyển Site
  │
  ▼
Với mỗi mặt hàng M cần đặt (số lượng Q, ngày nhận D):
  │
  ▼
  ┌─────────────────────────────────────────────────────┐
  │ BƯỚC 1 – Tính ETA và Lọc Site khả dụng                          │
  │   Với mỗi Site S có tồn kho > 0 cho M:              │
  │     - Tính ngày về nếu đi tàu:  today + ship_days   │
  │     - Tính ngày về nếu đi air:  today + air_days    │
  │     - LOC: Giữ lại nếu ngày về ≤ D                       │
  └────────────────────────────┬────────────────────────┘
                               │
  ┌────────────────────────────▼────────────────────────┐
  │ BƯỚC 2 – Chọn phương án (lexicographic — đề bài)    │
  │   Cấp 1: Tối đa hóa số lượng đi TÀU (ship)          │
  │   Cấp 2: Cùng mức tàu → ít mã Site nhất (DP/ tối  │
  │           thiểu tập Site, ≤ 50 Site)              │
  │   Cấp 3: Tie-break → ưu tiên Site tồn lớn         │
  │   Thiếu Q sau tàu → bổ sung AIR, lại ít Site nhất │
  │   Mỗi Site: pool tồn chung, tổng lấy ≤ stock[S]  │
  └────────────────────────────┬────────────────────────┘
                               │
  ┌────────────────────────────▼────────────────────────┐
  │ BƯỚC 3 – Phân bổ số lượng trên tập đã chọn          │
  │   Ship trước, air sau; trong cùng mode: Site tồn  │
  │   giảm dần → allocate = min(stock còn, remaining)  │
  │   Ghi: [S | M | allocate | mode]                    │
  └────────────────────────────┬────────────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │  remaining > 0?     │
                    └──────────┬──────────┘
                        Yes   │   No
                        │     │
                        ▼     ▼
               Ghi lỗi    Đơn con cho M đã xong
            "Không đủ
             hàng cho M"
  │
  ▼
Tổng hợp tất cả đơn con ─► Hiển thị cho người dùng xác nhận
  │
  ├── Người dùng điều chỉnh thủ công (nếu cần)
  │
  ▼
Lưu các đơn con với trạng thái: [CHỜ GỬI]
KẾT THÚC
```

**Ví dụ minh họa chạy thuật toán**

**1. Bối cảnh**
- Một YCNH: `REQ-20251101-001`, trạng thái Đang xử lý, đã có tồn kho từ UC006/011.
- Ngày bắt đầu tính toán (Today): 2025-11-01.
- Trong REQ có hai dòng Tivi (T001) — hệ thống gộp **trong REQ**: Q = 150, D = 2025-11-10; và một dòng Tủ lạnh (R001): Q = 250, D = 2025-11-08.

**CHI TIẾT TRACE LOG THUẬT TOÁN PHÂN BỔ**

**Mặt hàng T001 (trong REQ)**

```
Mặt hàng: T001 (Tivi), cần 150 đơn vị, ngày nhận đích: 2025-11-10
Today: 2025-11-01

Site | Tồn kho | Tàu (ngày) | Ngày về tàu | Air (ngày) | Ngày về air | Đáp ứng?
-----|---------|------------|-------------|------------|-------------|----------
 S01 |   150   |     5      |  2025-11-06 |     2      |  2025-11-03 |  Tàu ✓
 S02 |    80   |    10      |  2025-11-11 |     4      |  2025-11-05 |  Air ✓ (Tàu trễ)

Bước 2 (chọn phương án):
  Chỉ cần tàu: S01 ship đủ 150 → 1 Site, qty_ship = 150 = Q (tối đa tàu, ít Site nhất).

Bước 3 (phân bổ):
  → S01 | ship | 150

Đơn con tạo ra:
  [S01 | T001 | 150 | unit | ship delivery]
```

**Mặt hàng R001 (trong REQ)**

```
Mặt hàng: R001 (Tủ lạnh), cần 250 đơn vị, ngày nhận đích: 2025-11-08
Today: 2025-11-01

Site | Tồn kho | Tàu (ngày) | Ngày về tàu | Air (ngày) | Ngày về air | Đáp ứng?
-----|---------|------------|-------------|------------|-------------|----------
 S01 |   100   |     5      |  2025-11-06 |     2      |  2025-11-03 |  Tàu ✓
 S03 |   200   |    12      |  2025-11-13 |     6      |  2025-11-07 |  Air ✓ (Tàu trễ)

Bước 2 (chọn phương án):
  Tàu: chỉ S01 ship (100). Cần thêm 150 → bổ sung air: S03 air (200) — tối thiểu 2 Site
  (không thể 1 Site vì S03 tồn 200 < 250). qty_ship tối đa = 100; số Site = 2.

Bước 3 (phân bổ):
  → S01 ship 100, S03 air 150

Đơn con tạo ra:
  [S01 | R001 | 100 | unit | ship delivery]
  [S03 | R001 | 150 | unit | air delivery]
```

**Lô minh họa — ưu tiên tàu cao hơn “ít Site” (cùng đủ Q)**

```
Mặt hàng: X99, cần 100, ngày nhận đích: đủ cho cả tàu và bay
Today: 2025-11-01

Site | Tồn | Tàu đáp ứng? | Bay đáp ứng?
-----|-----|--------------|-------------
 S1  | 100 | Không        | Có
 S2  |  60 | Có           | Có
 S3  |  60 | Có           | Có

So sánh phương án:
  A) S1 bay 100        → 1 Site, toàn bay (qty_ship = 0)
  B) S2 tàu 60 + S3 tàu 40 → 2 Site, qty_ship = 100

Theo đề bài (tàu → tồn lớn → ít Site): chọn B — cấp 1 (tàu) thắng dù nhiều Site hơn A.
Greedy “sắp xếp tàu rồi lấy lần lượt” cũng ra B nếu S2, S3 tồn bằng nhau; nhưng
greedy KHÔNG đảm bảo ít Site trong mọi trường hợp → bước 2b bắt buộc tìm tập Site tối thiểu.
```

YCNH khác (ví dụ máy giặt ngày nhận muộn) được tách đơn trong **lần chọn REQ khác**, không gộp chung với REQ trên.

---

### GIAI ĐOẠN 4 – Gửi đơn và Site xác nhận

**Actor:** Bộ phận Đặt hàng quốc tế, Site nhập khẩu  
**Use Case:** UC008, UC012

```
BẮT ĐẦU
  │
  ▼
Bộ phận ĐH quốc tế xem tóm tắt đơn hàng sắp gửi
  │
  ▼
Xác nhận gửi
  │
  ▼
[Với mỗi Site được chọn]
  Hệ thống gửi thông tin:
  [Site code | Merchandise code | Quantity | Unit | Delivery means]
  │
  ▼
  ╔════════════════════════════════════╗
  ║  [PHÍA SITE – UC012]               ║
  ║  Nhận thông báo đơn hàng mới       ║
  ║  Xem chi tiết đơn                  ║
  ║  Xác nhận đơn hàng                 ║
  ║    └─ Từ chối? → Gửi lý do         ║
  ╚════════════════════════════════════╝
  │
  ├── SITE XÁC NHẬN: 
  │      Cập nhật trạng thái Đơn con: [ĐÃ XÁC NHẬN] 
  │      Cập nhật trạng thái Yêu cầu gốc: [ĐÃ TÁCH ĐƠN]
  │      Thông tin truyền tới Bộ phận Quản lý kho (UC013)
  │
  └── SITE TỪ CHỐI (Gửi kèm lý do lỗi kho, đứt gãy cung ứng...):
         Cập nhật trạng thái Đơn con: [BỊ TỪ CHỐI - ĐỎ]
         │
         ▼
         Nhân viên BP Đặt hàng nhận thông báo và nhấn "Tái xử lý"
         │
         ▼
         Hệ thống thu hồi khối lượng hàng bị từ chối 
         Quay ngược lại GIAI ĐOẠN 3 (Chạy lại thuật toán cho lượng hàng này)
  │
  ▼
KẾT THÚC
```

---

### GIAI ĐOẠN 5 – Tiếp nhận hàng và Đối chiếu nhập kho

**Actor:** Bộ phận Quản lý kho  
**Use Case:** UC013, UC014

```
BẮT ĐẦU
  │
  ▼
Nhân viên kho xem "Danh sách đơn hàng sắp về" (UC013)
  [Lọc theo ngày về dự kiến, Site, mã hàng]
  │
  ▼
Hàng hóa về đến kho
  │
  ▼
Nhân viên chọn đơn hàng để đối chiếu (UC014)
  │
  ▼
Hệ thống hiển thị:
  [Mã hàng | Số lượng đặt | Đơn vị | Nguồn Site]
  │
  ▼
Nhân viên nhập số lượng thực tế nhận được cho từng mặt hàng
  │
  ▼
Hệ thống so sánh:
  Sai lệch = Số lượng thực tế − Số lượng đặt
  │
  ├── Không có sai lệch (tất cả = 0):
  │   Trạng thái đơn: [ĐÃ NHẬP KHO]
  │
  └── Có sai lệch ít nhất 1 mặt hàng:
      Trạng thái đơn: [NHẬP KHO CÓ SAI LỆCH]
      Lưu chi tiết: [Mã hàng | Đặt | Thực tế | Sai lệch]
      Gửi thông báo → Bộ phận Đặt hàng quốc tế
  │
  ▼
KẾT THÚC
```

---

## 3. Luồng quản trị dữ liệu (Actor: Site nhập khẩu)

Các luồng này không phụ thuộc vào một yêu cầu nhập hàng cụ thể mà chạy độc lập khi có thay đổi từ phía Site.

### 3.1 Cập nhật danh mục mặt hàng kinh doanh (UC009)

```
Site phát hiện thay đổi danh mục
  │
  ▼
Đăng nhập hệ thống → Vào "Quản lý danh mục mặt hàng"
  │
  ├── Thêm mặt hàng:
  │     Nhập mã hàng (phải có trong danh mục chuẩn) → Lưu
  │
  └── Xóa mặt hàng:
        Chọn mặt hàng → Xác nhận xóa → Lưu
  │
  ▼
Hệ thống cập nhật danh sách mặt hàng của Site
→ Tự động có hiệu lực cho các truy vấn tiếp theo (UC006)
```

### 3.2 Cập nhật thông tin vận chuyển (UC010)

```
Site có thay đổi lịch vận chuyển
  │
  ▼
Đăng nhập hệ thống → Vào "Thông tin vận chuyển"
  │
  ▼
Cập nhật: [Số ngày tàu] và/hoặc [Số ngày hàng không]
  │
  ▼
Lưu → Tệp thông tin site cập nhật
→ Tự động áp dụng cho thuật toán tách đơn (UC007) trong lần sau
```

### 3.3 Quản lý hồ sơ Site (UC004)

```
Bộ phận ĐH quốc tế có đối tác mới / thay đổi / ngừng hợp tác
  │
  ├── Thêm Site mới:
  │     Nhập [Mã Site | Tên | Số ngày tàu | Số ngày air | Thông tin khác]
  │     Kiểm tra mã không trùng → Lưu vào Tệp thông tin site
  │
  ├── Sửa Site:
  │     Tìm Site → Chỉnh sửa thông tin → Lưu
  │
  └── Xóa Site:
        Kiểm tra không có đơn hàng đang hoạt động
        → Xác nhận → Xóa khỏi hệ thống
```

---

## 4. Luồng ngoại lệ và xử lý lỗi

| Tình huống | Phát sinh tại | Xử lý |
|---|---|---|
| Mã hàng không tồn tại trong danh mục | UC002 | Báo lỗi, yêu cầu nhập lại |
| Ngày nhận mong muốn đã qua | UC002 | Báo lỗi, không cho lưu |
| Không có Site nào kinh doanh mặt hàng | UC006 | Ghi lỗi cho mặt hàng đó, tiếp tục xử lý mặt hàng khác |
| Site không phản hồi truy vấn tồn kho | UC011 | Time-out → đánh dấu tồn kho = 0 |
| Tổng tồn kho < số lượng cần đặt | UC007 | Ghi lỗi "Không đủ hàng", thông báo cho Bộ phận Bán hàng |
| Không có Site nào đáp ứng ngày nhận | UC007 | Thông báo lỗi, đề xuất nới rộng ngày nhận |
| Site từ chối đơn hàng | UC012 | Thông báo Bộ phận ĐH quốc tế, cần xử lý lại UC007 |
| Hàng về không đủ số lượng | UC014 | Ghi sai lệch, thông báo Bộ phận ĐH quốc tế |
| Xóa Site đang có đơn hàng hoạt động | UC004 | Hệ thống từ chối xóa, hiển thị lý do |

---

## 5. Trạng thái của các đối tượng chính

### 5.1 Trạng thái Yêu cầu nhập hàng

```
[CHỜ XỬ LÝ]
    │
    ▼ (UC005 – Tiếp nhận)
[ĐANG XỬ LÝ]
    │
    ├─── (UC007 – Lỗi không đủ hàng) ───► [LỖI – KHÔNG ĐỦ HÀNG]
    │
    ▼ (UC008 – Gửi thành công)
[ĐÃ TÁCH ĐƠN]
```

### 5.2 Trạng thái Đơn hàng con

```
[CHỜ GỬI]
    │
    ▼ (UC008 – Gửi đi)
[ĐÃ GỬI]
    │
    ├─── (UC012 – Site từ chối) ─────────► [TỪ CHỐI] ──(Tái xử lý)──► QUAY LẠI CHẠY THUẬT TOÁN
    │
    ▼ (UC012 – Site xác nhận)
[ĐÃ XÁC NHẬN]
    │
    ▼ (UC014 – Nhập kho không sai lệch)
[ĐÃ NHẬP KHO]
    │
    hoặc
    │
    ▼ (UC014 – Nhập kho có sai lệch)
[NHẬP KHO CÓ SAI LỆCH]
```

---

## 6. Dữ liệu lưu trữ chính trong hệ thống

### Tệp thông tin site (Site Information File)

| Trường | Kiểu | Mô tả |
|---|---|---|
| Site code | String | Mã định danh Site |
| Import site name | String | Tên Site |
| Number of days for delivery by ship | Integer | Số ngày vận chuyển đường tàu |
| Number of days for delivery by air | Integer | Số ngày vận chuyển đường hàng không |
| Other information | String | Thông tin bổ sung |

### Tệp thông tin kho (Inventory Information File)

| Trường | Kiểu | Mô tả |
|---|---|---|
| Site code | String | Mã Site phản hồi |
| Merchandise code | String | Mã mặt hàng |
| In-stock quantity | Integer | Số lượng tồn kho tại thời điểm truy vấn |
| Unit | String | Đơn vị tính |

### Đơn hàng xuất (Purchase Order)

| Trường | Kiểu | Mô tả |
|---|---|---|
| Site code | String | Site được đặt hàng |
| Merchandise code | String | Mã mặt hàng |
| Quantity ordered | Integer | Số lượng đặt |
| Unit | String | Đơn vị tính |
| Delivery means | Enum | "ship delivery" hoặc "air delivery" |
