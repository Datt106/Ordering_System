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
  │ BƯỚC 0 – Gom lô (Batch Aggregation / Time Window)    │
  │ - Lọc các YCNH đang chờ xử lý theo Khung thời gian.  │
  │ - Gộp các YCNH có cùng Mã mặt hàng (M).              │
  │ - Tổng cầu (Q) = SUM(Số lượng các YCNH).             │
  │ - Ngày đích (D) = MIN(Ngày mong muốn) (Earliest Date)│
  └────────────────────────────┬─────────────────────────┘
                               │
  ▼
Đọc Tệp thông tin kho + Tệp thông tin site (số ngày vận chuyển)
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
  │ BƯỚC 2 – Sắp xếp ưu tiên                            │
  │   Ưu tiên 1: Tàu (ship) trước Hàng không (air)      │
  │   Ưu tiên 2: Tồn kho lớn hơn trước                  │
  └────────────────────────────┬────────────────────────┘
                               │
  ┌────────────────────────────▼────────────────────────┐
  │ BƯỚC 3 – Phân bổ số lượng (Greedy)                  │
  │   remaining = Q                                     │
  │   For Site S theo thứ tự ưu tiên:                   │
  │     allocate = min(stock[S], remaining)             │
  │     remaining -= allocate                           │
  │     Ghi: [S | M | allocate | mode]                  │
  │     If remaining == 0: STOP                         │
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

**1. Bối cảnh & Cấu hình đầu vào**
Ngày bắt đầu tính toán (Today): 2025-11-01
Khung thời gian gom: 14 ngày (Hệ thống chỉ lấy các đơn có ngày mong muốn từ 2025-11-01 đến 2025-11-15).

**2. Danh sách Yêu cầu nhập hàng (YCNH) chờ xử lý**
YCNH #1: Tivi (T001), 100 cái, ngày nhận: 2025-11-10
YCNH #2: Tivi (T001), 50 cái, ngày nhận: 2025-11-12
YCNH #3: Tủ lạnh (R001), 250 cái, ngày nhận: 2025-11-08
YCNH #4: Máy giặt (W001), 50 cái, ngày nhận: 2025-11-20 (Bị bỏ qua vì > 2025-11-15, chờ đợt gom sau).

**3. Kết quả Gộp đơn hàng (Batching)**

Lô 1 (T001): Cần 150 đơn vị (100+50). Ngày đích = MIN(10, 12) = 2025-11-10
Lô 2 (R001): Cần 250 đơn vị. Ngày đích = 2025-11-08

**CHI TIẾT TRACE LOG THUẬT TOÁN PHÂN BỔ**

**Lô hàng 1**

```
Mặt hàng: T001 (Tivi), cần 150 đơn vị, ngày nhận đích: 2025-11-10
Today: 2025-11-01

Site | Tồn kho | Tàu (ngày) | Ngày về tàu | Air (ngày) | Ngày về air | Đáp ứng?
-----|---------|------------|-------------|------------|-------------|----------
 S01 |   150   |     5      |  2025-11-06 |     2      |  2025-11-03 |  Tàu ✓
 S02 |    80   |    10      |  2025-11-11 |     4      |  2025-11-05 |  Air ✓ (Tàu trễ)

Sau sắp xếp (ưu tiên tàu, tồn kho lớn):
  1. S01 – tàu – 150 đơn vị
  2. S02 – air – 80 đơn vị

Phân bổ:
  remaining = 150
  → S01: allocate = min(150, 150) = 150, remaining = 0  ✓

Đơn con tạo ra:
  [S01 | T001 | 150 | unit | ship delivery]
```

**Lô hàng 2**

```
Mặt hàng: R001 (Tủ lạnh), cần 250 đơn vị, ngày nhận đích: 2025-11-08
Today: 2025-11-01

Site | Tồn kho | Tàu (ngày) | Ngày về tàu | Air (ngày) | Ngày về air | Đáp ứng?
-----|---------|------------|-------------|------------|-------------|----------
 S01 |   100   |     5      |  2025-11-06 |     2      |  2025-11-03 |  Tàu ✓
 S03 |   200   |    12      |  2025-11-13 |     6      |  2025-11-07 |  Air ✓ (Tàu trễ)

Sau sắp xếp (ưu tiên tàu, tồn kho lớn):
  1. S01 – tàu – 100 đơn vị
  2. S03 – air – 200 đơn vị

Phân bổ:
  remaining = 250
  → S01: allocate = min(100, 250) = 100, remaining = 150
  → S03: allocate = min(200, 150) = 150, remaining = 0  ✓

Đơn con tạo ra:
  [S01 | R001 | 100 | unit | ship delivery]
  [S03 | R001 | 150 | unit | air delivery]
```
**4. Đợt chạy thuật toán tiếp theo**
Ngày bắt đầu tính toán (Today): 2025-11-08
Khung thời gian gom: 14 ngày (Hệ thống chỉ lấy các đơn có ngày mong muốn từ 2025-11-08 đến 2025-11-23).

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
