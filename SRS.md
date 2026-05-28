# Đặc tả Yêu cầu Phần mềm (SRS)
## Hệ thống Đặt hàng Nhập khẩu – Import Order Management System

---

| Thông tin tài liệu | |
|---|---|
| **Tên dự án** | Hệ thống phần mềm đặt hàng nhập khẩu |
| **Tên tài liệu** | Đặc tả Yêu cầu Phần mềm (SRS) |
| **Phiên bản** | 1.0 |
| **Trạng thái** | Bản thảo |
| **Ngày tạo** | 2025 |

---

## Mục lục

1. [Giới thiệu](#1-giới-thiệu)
2. [Mô tả tổng quan hệ thống](#2-mô-tả-tổng-quan-hệ-thống)
3. [Các bên liên quan và người dùng](#3-các-bên-liên-quan-và-người-dùng)
4. [Yêu cầu chức năng](#4-yêu-cầu-chức-năng)
5. [Yêu cầu phi chức năng](#5-yêu-cầu-phi-chức-năng)
6. [Mô hình dữ liệu](#6-mô-hình-dữ-liệu)
7. [Giao diện hệ thống](#7-giao-diện-hệ-thống)
8. [Ràng buộc và giả định](#8-ràng-buộc-và-giả-định)
9. [Phụ lục](#9-phụ-lục)

---

## 1. Giới thiệu

### 1.1 Mục đích tài liệu

Tài liệu này là Đặc tả Yêu cầu Phần mềm (Software Requirements Specification – SRS) cho **Hệ thống Đặt hàng Nhập khẩu** của một công ty kinh doanh hàng nhập ngoại. Tài liệu mô tả đầy đủ các yêu cầu chức năng, yêu cầu phi chức năng, ràng buộc thiết kế và mô hình dữ liệu của hệ thống.

Tài liệu này hướng đến các đối tượng đọc sau:
- **Nhóm phát triển phần mềm**: làm căn cứ thiết kế và lập trình.
- **Nhóm kiểm thử**: làm căn cứ xây dựng test case.
- **Người dùng cuối và quản lý dự án**: xác nhận phạm vi và yêu cầu.

### 1.2 Phạm vi hệ thống

Hệ thống Đặt hàng Nhập khẩu (sau đây gọi tắt là **hệ thống**) được xây dựng nhằm **tin học hóa toàn bộ quy trình đặt hàng nhập khẩu** của công ty, bao gồm:

- Tiếp nhận và quản lý yêu cầu nhập hàng từ Bộ phận Bán hàng.
- Truy vấn thông tin tồn kho và vận chuyển từ các Site nhập khẩu nước ngoài.
- Tự động phân bổ và tách đơn hàng theo thuật toán đề bài: ưu tiên tàu → Site tồn lớn → ít Site.
- Phát hành đơn hàng tới các Site đối tác.
- Cung cấp thông tin đơn hàng cho Bộ phận Quản lý kho phục vụ đối chiếu khi hàng về.

**Ngoài phạm vi:**
- Hệ thống quản lý kho nội bộ (WMS) của Bộ phận Quản lý kho.
- Hệ thống kế toán, thanh toán quốc tế.
- Quy trình thông quan, hải quan.

### 1.3 Định nghĩa và thuật ngữ

| Thuật ngữ | Định nghĩa |
|---|---|
| **Site nhập khẩu** | Nhà cung cấp / đối tác nước ngoài có kết nối với hệ thống. Hiện tại có 50 Site trên toàn thế giới |
| **Yêu cầu nhập hàng** | Danh sách mặt hàng cần đặt do Bộ phận Bán hàng tạo ra |
| **Đơn hàng con** | Đơn hàng được tạo ra cho một Site cụ thể sau khi tách đơn |
| **Tệp thông tin site** | Cơ sở dữ liệu lưu thông tin vận chuyển của từng Site |
| **Tệp thông tin kho** | Dữ liệu tồn kho tạm thời thu thập từ phản hồi của các Site cho mỗi yêu cầu |
| **Tách đơn** | Quá trình phân bổ số lượng một mặt hàng cho nhiều Site khác nhau |
| **Delivery means** | Phương tiện vận chuyển: "ship delivery" (đường tàu) hoặc "air delivery" (đường hàng không) |
| **Ngày nhận mong muốn** | Ngày Bộ phận Bán hàng muốn nhận được hàng tại kho |
| **SRS** | Software Requirements Specification – Đặc tả Yêu cầu Phần mềm |
| **UC** | Use Case – Tình huống sử dụng |

### 1.4 Tài liệu tham chiếu

- Mô tả hiện trạng hệ thống: *Project_BTL – HeThongDatHangNhapKhau*
- Danh sách Use Case đã xác nhận (UC002 – UC014)
- Tài liệu đặc tả Use Case chi tiết (`usecase.md`)
- Tài liệu luồng xử lý hệ thống (`flow.md`)

---

## 2. Mô tả tổng quan hệ thống

### 2.1 Bối cảnh hệ thống

Công ty kinh doanh hàng nhập ngoại hiện đang vận hành quy trình đặt hàng nhập khẩu **hoàn toàn thủ công**. Các bộ phận liên lạc với nhau qua email, điện thoại và giấy tờ, dẫn đến nhiều vấn đề:

- Thông tin tồn kho và vận chuyển dễ bị lỗi thời hoặc thất lạc.
- Việc chọn Site và tách đơn phụ thuộc hoàn toàn vào kinh nghiệm cá nhân, thiếu nhất quán.
- Không có cơ chế theo dõi trạng thái đơn hàng xuyên suốt từ đầu đến cuối.
- Bộ phận Quản lý kho thường không có thông tin đầy đủ trước khi hàng về.

Hệ thống phần mềm được xây dựng để **số hóa và tự động hóa** toàn bộ quy trình trên, kết nối 4 nhóm người dùng chính trên một nền tảng thống nhất.

### 2.2 Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────┐
│                  HỆ THỐNG ĐẶT HÀNG NHẬP KHẨU                │
│                                                             │
│  ┌──────────────┐   ┌──────────────────┐   ┌────────────┐   │
│  │  Module      │   │  Module Đặt hàng │   │  Module    │   │
│  │  Bán hàng    │   │  quốc tế         │   │  Kho       │   │
│  │  (UC002,003) │   │  (UC004–UC008)   │   │  (UC013,14)│   │
│  └──────┬───────┘   └────────┬─────────┘   └─────┬──────┘   │
│         │                    │                   │          │
│         └────────────────────┼───────────────────┘          │
│                              │                              │
│                    ┌─────────▼─────────┐                    │
│                    │  Cơ sở dữ liệu    │                    │
│                    │  trung tâm        │                    │
│                    └───────────────────┘                    │
└─────────────────────────────┬───────────────────────────────┘
                              │ Kết nối mạng / API
              ┌───────────────▼────────────────┐
              │   SITE NHẬP KHẨU (50 Site)     │
              │   Module Site (UC009–UC012)    │
              └────────────────────────────────┘
```

### 2.3 Các chức năng chính

Hệ thống cung cấp 6 nhóm chức năng chính:

| STT | Nhóm chức năng | Mô tả |
|---|---|---|
| 1 | Quản lý yêu cầu nhập hàng | Tạo, theo dõi trạng thái yêu cầu |
| 2 | Quản lý đối tác Site | Thêm/sửa/xóa Site, mặt hàng, thông tin vận chuyển |
| 3 | Truy vấn tồn kho | Gửi truy vấn và thu thập phản hồi tồn kho từ Site |
| 4 | Xử lý và tách đơn | Thuật toán phân bổ: tàu → tồn lớn → ít Site (theo đề bài) |
| 5 | Phát hành đơn hàng | Gửi đơn tới Site, Site xác nhận |
| 6 | Quản lý nhập kho | Xem đơn hàng sắp về, đối chiếu và ghi nhận sai lệch |

### 2.4 Ràng buộc vận hành

- Hệ thống phải hỗ trợ kết nối đồng thời với **tối đa 50 Site** nhập khẩu nước ngoài.
- Tại cùng một thời điểm, Bộ phận Bán hàng có thể tồn tại **nhiều yêu cầu nhập hàng** đang xử lý song song.
- Mỗi Site kinh doanh **nhiều mặt hàng** khác nhau; các Site khác nhau có thể cùng kinh doanh một số mặt hàng giống nhau.

---

## 3. Các bên liên quan và người dùng

### 3.1 Bộ phận Bán hàng (Sales Department)

| Thuộc tính | Mô tả |
|---|---|
| **Vai trò** | Tác nhân nội bộ – Người khởi tạo đầu vào cho hệ thống |
| **Trách nhiệm** | Tạo yêu cầu nhập hàng, theo dõi tiến độ xử lý |
| **Quyền truy cập** | Module Bán hàng: UC002, UC003 |
| **Đặc điểm** | Không cần hiểu nghiệp vụ đặt hàng quốc tế; giao diện cần đơn giản, trực quan |
| **Kỳ vọng** | Biết được trạng thái yêu cầu và khi nào hàng sẽ về |

### 3.2 Bộ phận Đặt hàng quốc tế (Overseas Order Placement Department)

| Thuộc tính | Mô tả |
|---|---|
| **Vai trò** | Tác nhân nội bộ chính – Điều phối toàn bộ quy trình |
| **Trách nhiệm** | Tiếp nhận yêu cầu, quản lý Site, chạy tách đơn, phát hành đơn hàng |
| **Quyền truy cập** | Module Đặt hàng quốc tế: UC004, UC005, UC006, UC007, UC008 |
| **Đặc điểm** | Người dùng có nghiệp vụ chuyên sâu; cần công cụ hiệu quả và rõ ràng |
| **Kỳ vọng** | Thuật toán tách đơn tự động giảm thiểu công việc thủ công, tránh sai sót |

### 3.3 Site nhập khẩu (Overseas Import Sites)

| Thuộc tính | Mô tả |
|---|---|
| **Vai trò** | Tác nhân bên ngoài – Nhà cung cấp / đối tác nước ngoài |
| **Trách nhiệm** | Khai báo mặt hàng, cập nhật vận chuyển, xác nhận tồn kho, xác nhận đơn hàng |
| **Quyền truy cập** | Module Site: UC009, UC010, UC011, UC012 |
| **Đặc điểm** | Người dùng nước ngoài; giao diện cần hỗ trợ đa ngôn ngữ (tối thiểu tiếng Anh) |
| **Kỳ vọng** | Thao tác đơn giản, nhanh chóng; thông báo rõ ràng khi có truy vấn hoặc đơn mới |

### 3.4 Bộ phận Quản lý kho (Warehouse Management Department)

| Thuộc tính | Mô tả |
|---|---|
| **Vai trò** | Tác nhân nội bộ – Người dùng cuối cùng trong quy trình |
| **Trách nhiệm** | Xem thông tin đơn hàng sắp về, đối chiếu hàng thực tế, ghi nhận sai lệch |
| **Quyền truy cập** | Module Kho: UC013, UC014 |
| **Đặc điểm** | Chỉ tương tác với hệ thống quốc tế để đọc thông tin đơn hàng; hệ thống WMS riêng nằm ngoài phạm vi |
| **Kỳ vọng** | Thông tin đơn hàng chính xác, dễ tra cứu; cảnh báo kịp thời khi có sai lệch |

---

## 4. Yêu cầu chức năng

### 4.1 Module Bán hàng

#### FR-01: Tạo yêu cầu nhập hàng (UC002)

| Mã | Yêu cầu |
|---|---|
| FR-01.1 | Hệ thống phải cho phép nhân viên Bộ phận Bán hàng tạo một yêu cầu nhập hàng mới |
| FR-01.2 | Mỗi yêu cầu bao gồm một hoặc nhiều dòng mặt hàng; mỗi dòng có: Mã hàng, Số lượng, Đơn vị, Ngày nhận mong muốn (Năm/Tháng/Ngày) |
| FR-01.3 | Hệ thống phải kiểm tra mã hàng có tồn tại trong danh mục chuẩn; nếu không, hiển thị lỗi |
| FR-01.4 | Hệ thống phải kiểm tra số lượng là số nguyên dương; ngày nhận mong muốn phải là ngày trong tương lai |
| FR-01.5 | Hệ thống phải tự động gán mã yêu cầu duy nhất, ghi nhận ngày tạo và người tạo |
| FR-01.6 | Sau khi tạo thành công, yêu cầu có trạng thái **Chờ xử lý** |
| FR-01.7 | Hệ thống phải hiển thị thông báo thành công kèm mã yêu cầu vừa tạo |

#### FR-02: Theo dõi trạng thái yêu cầu (UC003)

| Mã | Yêu cầu |
|---|---|
| FR-02.1 | Hệ thống phải hiển thị danh sách tất cả yêu cầu nhập hàng của Bộ phận Bán hàng |
| FR-02.2 | Danh sách phải bao gồm: mã yêu cầu, ngày tạo, số mặt hàng, trạng thái hiện tại |
| FR-02.3 | Người dùng có thể xem chi tiết từng yêu cầu: danh sách mặt hàng, các đơn hàng con đã tạo, Site được chọn, phương tiện vận chuyển |
| FR-02.4 | Hệ thống phải hỗ trợ lọc danh sách theo trạng thái và ngày tạo |
| FR-02.5 | Hệ thống phải hỗ trợ 4 trạng thái: **Chờ xử lý**, **Đang xử lý**, **Đã tách đơn**, **Lỗi – Không đủ hàng** |

---

### 4.2 Module Đặt hàng quốc tế

#### FR-03: Quản lý Site (UC004)

| Mã | Yêu cầu |
|---|---|
| FR-03.1 | Hệ thống phải cho phép thêm Site mới với các thông tin: Mã Site, Tên Site, Số ngày vận chuyển đường tàu, Số ngày vận chuyển đường hàng không, Thông tin khác |
| FR-03.2 | Mã Site phải là duy nhất trong hệ thống; hệ thống phải kiểm tra và từ chối nếu trùng |
| FR-03.3 | Hệ thống phải cho phép sửa thông tin Site hiện có |
| FR-03.4 | Hệ thống phải cho phép xóa Site; tuy nhiên phải từ chối xóa nếu Site đang có đơn hàng chưa hoàn tất |
| FR-03.5 | Hệ thống phải hỗ trợ tìm kiếm Site theo mã và tên |

#### FR-04: Tiếp nhận yêu cầu đơn hàng (UC005)

| Mã | Yêu cầu |
|---|---|
| FR-04.1 | Hệ thống phải hiển thị danh sách các yêu cầu nhập hàng đang ở trạng thái **Chờ xử lý** |
| FR-04.2 | Người dùng có thể xem chi tiết yêu cầu trước khi tiếp nhận |
| FR-04.3 | Khi tiếp nhận, hệ thống chuyển trạng thái yêu cầu sang **Đang xử lý**, ghi nhận người tiếp nhận và thời điểm |

#### FR-05: Truy vấn thông tin tồn kho và vận chuyển (UC006)

| Mã | Yêu cầu |
|---|---|
| FR-05.1 | Hệ thống phải tự động xác định các Site có kinh doanh ít nhất một mặt hàng trong yêu cầu đang xử lý |
| FR-05.2 | Hệ thống phải nhóm danh sách mặt hàng theo Site: mỗi Site chỉ nhận danh sách mặt hàng họ kinh doanh |
| FR-05.3 | Hệ thống phải gửi truy vấn tồn kho tới từng Site tìm được |
| FR-05.4 | Hệ thống phải ghi nhận phản hồi tồn kho từ các Site vào Tệp thông tin kho theo định dạng: `[Site code | Merchandise code | In-stock quantity | Unit]` |
| FR-05.5 | Nếu một Site không phản hồi trong thời gian quy định (time-out), hệ thống phải đánh dấu tồn kho = 0 cho tất cả mặt hàng tại Site đó |
| FR-05.6 | Nếu không có Site nào kinh doanh một mặt hàng cụ thể, hệ thống phải ghi nhận lỗi cho mặt hàng đó và tiếp tục xử lý các mặt hàng còn lại |

#### FR-06: Xử lý yêu cầu và Tách đơn hàng (UC007)

| Mã | Yêu cầu |
|---|---|
| FR-06.1 | Mỗi lần tách đơn xử lý **một** yêu cầu (`request_id`). Trong REQ, các dòng trùng mã hàng được cộng dồn; **Ngày nhận đích** = ngày sớm nhất trong các dòng đó. Không gộp nhiều REQ trong phiên bản hiện tại. |
| FR-06.2 | Hệ thống phải **xử lý từng mặt hàng độc lập**. Nếu một Site không đủ số lượng, được phép nhập từ **nhiều Site**; tổng lấy từ một Site không vượt tồn kho tại Site đó. |
| FR-06.3 | Với mỗi mặt hàng, `ETA = Ngày chốt đơn + Số ngày vận chuyển` (riêng cho tàu và hàng không). Chỉ giữ phương án `(Site, mode)` có `ETA ≤ Ngày nhận đích`. |
| FR-06.4 | Chọn Site theo thứ tự ưu tiên đề bài (mức sau chỉ khi mức trước ngang nhau): **(1) Tàu hơn hàng không** (lấy tối đa số lượng đi tàu) → **(2) Site có tồn kho lớn** → **(3) Số Site được chọn ít nhất** (đếm theo mã Site). |
| FR-06.5 | Phân bổ số lượng trên tập Site đã chọn: ưu tiên lấy từ Site tồn lớn trước trong cùng phương tiện; mỗi Site một pool tồn dùng chung cho tàu/bay. |
| FR-06.6 | Nếu tổng tồn khả dụng (sau lọc ETA) < số lượng yêu cầu → lỗi **"Không đủ hàng"** kèm số lượng thiếu. |
| FR-06.7 | Nếu không có `(Site, mode)` nào thỏa ETA → lỗi không đáp ứng ngày nhận, đề xuất nới ngày. |
| FR-06.8 | Hệ thống phải hiển thị kết quả tách đơn để người dùng xem xét trước khi xác nhận |
| FR-06.9 | Người dùng có thể điều chỉnh thủ công kết quả tách đơn trước khi xác nhận |

#### FR-07: Gửi đơn hàng (UC008)

| Mã | Yêu cầu |
|---|---|
| FR-07.1 | Hệ thống phải gửi thông tin đơn hàng tới từng Site được chọn theo định dạng: `[Site code | Merchandise code | Quantity ordered | Unit | Delivery means]` |
| FR-07.2 | Giá trị Delivery means phải là "ship delivery" hoặc "air delivery" |
| FR-07.3 | Sau khi gửi, hệ thống cập nhật trạng thái các đơn con thành **Đã gửi** và yêu cầu gốc thành **Đã tách đơn** |
| FR-07.4 | Thông tin đơn hàng đã gửi phải ngay lập tức hiển thị cho Bộ phận Quản lý kho (UC013) |

---

### 4.3 Module Site nhập khẩu

#### FR-08: Quản lý mặt hàng kinh doanh (UC009)

| Mã | Yêu cầu |
|---|---|
| FR-08.1 | Hệ thống phải cho phép đại diện Site xem danh sách mặt hàng hiện đang kinh doanh |
| FR-08.2 | Hệ thống phải cho phép Site thêm mặt hàng vào danh sách kinh doanh; mã hàng phải tồn tại trong danh mục chuẩn |
| FR-08.3 | Hệ thống phải cho phép Site xóa mặt hàng khỏi danh sách kinh doanh của mình |
| FR-08.4 | Thay đổi danh mục mặt hàng của Site có hiệu lực ngay trong các truy vấn tiếp theo |

#### FR-09: Cập nhật thông tin vận chuyển (UC010)

| Mã | Yêu cầu |
|---|---|
| FR-09.1 | Hệ thống phải cho phép Site tự cập nhật số ngày vận chuyển đường tàu và đường hàng không |
| FR-09.2 | Hệ thống phải kiểm tra giá trị nhập là số nguyên dương |
| FR-09.3 | Thông tin vận chuyển mới có hiệu lực ngay trong các lần tách đơn tiếp theo |

#### FR-10: Xác nhận tồn kho (UC011)

| Mã | Yêu cầu |
|---|---|
| FR-10.1 | Hệ thống phải thông báo cho Site khi có truy vấn tồn kho mới |
| FR-10.2 | Hệ thống phải hiển thị danh sách mặt hàng cần xác nhận tồn kho |
| FR-10.3 | Site phải nhập số lượng tồn kho thực tế cho từng mặt hàng (0 nếu hết hàng) |
| FR-10.4 | Hệ thống phải lưu phản hồi vào Tệp thông tin kho và thông báo cho UC006 tiếp tục xử lý |

#### FR-11: Tiếp nhận và Xác nhận đơn hàng (UC012)

| Mã | Yêu cầu |
|---|---|
| FR-11.1 | Hệ thống phải thông báo cho Site khi có đơn hàng mới được gửi đến |
| FR-11.2 | Hệ thống phải hiển thị chi tiết đơn hàng: mã hàng, số lượng, đơn vị, phương tiện vận chuyển |
| FR-11.3 | Site có thể xác nhận hoặc từ chối đơn hàng |
| FR-11.4 | Nếu từ chối, Site phải nhập lý do; hệ thống phải thông báo cho Bộ phận Đặt hàng quốc tế |
| FR-11.5 | Khi xác nhận, hệ thống cập nhật trạng thái đơn thành **Đã xác nhận** kèm thời điểm xác nhận |

---

### 4.4 Module Quản lý kho

#### FR-12: Xem danh sách đơn hàng (UC013)

| Mã | Yêu cầu |
|---|---|
| FR-12.1 | Hệ thống phải hiển thị danh sách các đơn hàng ở trạng thái **Đã gửi** hoặc **Đã xác nhận** |
| FR-12.2 | Thông tin hiển thị gồm: Site, mã hàng, số lượng đặt, đơn vị, phương tiện vận chuyển, ngày dự kiến về |
| FR-12.3 | Hệ thống phải hỗ trợ lọc theo Site, mã hàng và ngày về dự kiến |
| FR-12.4 | Đây là chức năng chỉ đọc; không cho phép chỉnh sửa thông tin đơn hàng |

#### FR-13: Đối chiếu và Ghi nhận nhập kho (UC014)

| Mã | Yêu cầu |
|---|---|
| FR-13.1 | Hệ thống phải cho phép nhân viên kho chọn một đơn hàng để bắt đầu đối chiếu |
| FR-13.2 | Hệ thống phải hiển thị danh sách mặt hàng với số lượng đặt để so sánh |
| FR-13.3 | Nhân viên kho nhập số lượng thực tế nhận được cho từng mặt hàng |
| FR-13.4 | Hệ thống phải tự động tính sai lệch: `sai lệch = số lượng thực tế − số lượng đặt` |
| FR-13.5 | Nếu không có sai lệch: cập nhật trạng thái đơn thành **Đã nhập kho** |
| FR-13.6 | Nếu có sai lệch: cập nhật trạng thái thành **Nhập kho có sai lệch**, lưu chi tiết sai lệch, gửi thông báo tới Bộ phận Đặt hàng quốc tế |

---

## 5. Yêu cầu phi chức năng

### 5.1 Hiệu năng (Performance)

| Mã | Yêu cầu |
|---|---|
| NFR-P01 | Thời gian phản hồi của các thao tác đọc/hiển thị danh sách không vượt quá **3 giây** trong điều kiện bình thường |
| NFR-P02 | Thời gian phản hồi của các thao tác tạo/lưu dữ liệu không vượt quá **5 giây** |
| NFR-P03 | Thuật toán tách đơn phải hoàn thành trong vòng **10 giây** cho một yêu cầu có tối đa 20 mặt hàng và 50 Site |
| NFR-P04 | Hệ thống phải hỗ trợ tối thiểu **20 người dùng đồng thời** mà không suy giảm hiệu năng đáng kể |

### 5.2 Bảo mật (Security)

| Mã | Yêu cầu |
|---|---|
| NFR-S01 | Hệ thống phải yêu cầu xác thực người dùng (đăng nhập) trước khi truy cập bất kỳ chức năng nào |
| NFR-S02 | Hệ thống phải phân quyền truy cập theo vai trò; mỗi vai trò chỉ truy cập được module tương ứng |
| NFR-S03 | Mật khẩu người dùng phải được mã hóa khi lưu trữ (tối thiểu sử dụng hashing với salt) |
| NFR-S04 | Hệ thống phải ghi audit log cho các thao tác quan trọng: tạo yêu cầu, gửi đơn, đối chiếu kho |
| NFR-S05 | Phiên làm việc (session) phải tự động hết hạn sau **30 phút** không hoạt động |

### 5.3 Khả năng sử dụng (Usability)

| Mã | Yêu cầu |
|---|---|
| NFR-U01 | Giao diện phải hỗ trợ tối thiểu **hai ngôn ngữ**: Tiếng Việt (cho người dùng nội bộ) và Tiếng Anh (cho đại diện Site nước ngoài) |
| NFR-U02 | Các thông báo lỗi phải rõ ràng, chỉ định chính xác trường nào bị lỗi và lý do |
| NFR-U03 | Giao diện phải hiển thị trạng thái của yêu cầu và đơn hàng ở dạng nhãn màu trực quan |
| NFR-U04 | Người dùng mới có thể thực hiện được các thao tác cơ bản sau tối đa **2 giờ** làm quen |

### 5.4 Độ tin cậy (Reliability)

| Mã | Yêu cầu |
|---|---|
| NFR-R01 | Hệ thống phải có tỷ lệ uptime tối thiểu **99%** trong giờ hành chính |
| NFR-R02 | Hệ thống phải tự động lưu dữ liệu trong quá trình người dùng nhập liệu để tránh mất dữ liệu khi lỗi mạng |
| NFR-R03 | Trong trường hợp thuật toán tách đơn gặp lỗi, hệ thống phải rollback và không lưu kết quả dở dang |

### 5.5 Khả năng bảo trì (Maintainability)

| Mã | Yêu cầu |
|---|---|
| NFR-M01 | Mã nguồn phải tuân thủ coding convention được thống nhất trong nhóm |
| NFR-M02 | Hệ thống phải được thiết kế theo kiến trúc phân lớp (layered architecture) để dễ bảo trì |
| NFR-M03 | Các tham số cấu hình (time-out truy vấn tồn kho, số Site tối đa...) phải được lưu ở file cấu hình, không hard-code |

---

## 6. Mô hình dữ liệu

### 6.1 Các thực thể chính

#### ImportRequest (Yêu cầu nhập hàng)

| Thuộc tính | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| requestId | String | ✓ | Mã yêu cầu, định dạng REQ-YYYYMMDD-XXX |
| createdDate | DateTime | ✓ | Ngày giờ tạo yêu cầu |
| createdBy | String | ✓ | Mã nhân viên tạo yêu cầu |
| department | String | ✓ | Bộ phận tạo yêu cầu |
| status | Enum | ✓ | CHO_XU_LY / DANG_XU_LY / DA_TACH_DON / LOI |
| processedBy | String | | Mã nhân viên tiếp nhận xử lý |
| processedDate | DateTime | | Ngày giờ tiếp nhận |

#### ImportRequestItem (Dòng mặt hàng trong yêu cầu)

| Thuộc tính | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| requestId | String | ✓ | FK → ImportRequest |
| merchandiseCode | String | ✓ | Mã hàng hóa |
| quantityOrdered | Integer | ✓ | Số lượng yêu cầu (> 0) |
| unit | String | ✓ | Đơn vị tính |
| desiredDeliveryDate | Date | ✓ | Ngày nhận mong muốn |
| itemStatus | Enum | ✓ | OK / LOI_KHONG_DU_HANG / KHONG_CO_SITE |

#### Site (Thông tin site nhập khẩu)

| Thuộc tính | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| siteCode | String | ✓ | Mã Site, duy nhất |
| siteName | String | ✓ | Tên Site |
| shipDays | Integer | ✓ | Số ngày vận chuyển đường tàu (> 0) |
| airDays | Integer | ✓ | Số ngày vận chuyển đường hàng không (> 0) |
| otherInfo | String | | Thông tin bổ sung |
| isActive | Boolean | ✓ | Trạng thái hoạt động |

#### SiteMerchandise (Mặt hàng Site kinh doanh)

| Thuộc tính | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| siteCode | String | ✓ | FK → Site |
| merchandiseCode | String | ✓ | Mã hàng hóa |
| updatedDate | DateTime | ✓ | Ngày cập nhật gần nhất |

#### InventoryQuery (Tệp thông tin kho – kết quả truy vấn)

| Thuộc tính | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| queryId | String | ✓ | Mã truy vấn |
| requestId | String | ✓ | FK → ImportRequest |
| siteCode | String | ✓ | FK → Site |
| merchandiseCode | String | ✓ | Mã hàng hóa |
| inStockQuantity | Integer | ✓ | Số lượng tồn kho (≥ 0) |
| unit | String | ✓ | Đơn vị tính |
| respondedAt | DateTime | | Thời điểm phản hồi (null nếu time-out) |

#### PurchaseOrder (Đơn hàng con)

| Thuộc tính | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| orderId | String | ✓ | Mã đơn hàng con, duy nhất |
| requestId | String | ✓ | FK → ImportRequest |
| siteCode | String | ✓ | FK → Site |
| merchandiseCode | String | ✓ | Mã hàng hóa |
| quantityOrdered | Integer | ✓ | Số lượng đặt |
| unit | String | ✓ | Đơn vị tính |
| deliveryMeans | Enum | ✓ | SHIP / AIR |
| status | Enum | ✓ | CHO_GUI / DA_GUI / DA_XAC_NHAN / TU_CHOI / DA_NHAP_KHO / SAI_LECH |
| sentAt | DateTime | | Thời điểm gửi đơn |
| confirmedAt | DateTime | | Thời điểm Site xác nhận |
| actualQuantity | Integer | | Số lượng thực tế nhận được |
| quantityDiff | Integer | | Sai lệch = actualQuantity − quantityOrdered |

### 6.2 Quan hệ giữa các thực thể

```
ImportRequest 1 ──── N  ImportRequestItem
ImportRequest 1 ──── N  InventoryQuery
ImportRequest 1 ──── N  PurchaseOrder
Site          1 ──── N  SiteMerchandise
Site          1 ──── N  InventoryQuery
Site          1 ──── N  PurchaseOrder
```

---

## 7. Giao diện hệ thống

### 7.1 Giao diện người dùng (GUI)

- Hệ thống được triển khai dưới dạng **ứng dụng web** truy cập qua trình duyệt.
- Giao diện phải **responsive**, hỗ trợ màn hình desktop (tối thiểu 1280×720).
- Mỗi vai trò người dùng sau khi đăng nhập sẽ thấy **menu điều hướng phù hợp** với quyền hạn của mình.
- Các form nhập liệu phải có validation ngay trên giao diện trước khi gửi về server.

### 7.2 Giao diện với Site nhập khẩu

- Site truy cập hệ thống qua cùng nền tảng web, với tài khoản riêng cho từng Site.
- Hệ thống phải gửi **thông báo trong ứng dụng** (in-app notification) cho Site khi:
  - Có truy vấn tồn kho mới (UC011).
  - Có đơn hàng mới được gửi đến (UC012).

### 7.3 Giao diện phần mềm nội bộ

- Hệ thống **không** tích hợp trực tiếp với WMS của Bộ phận Quản lý kho trong phạm vi dự án này.
- Dữ liệu đối chiếu nhập kho (UC014) được lưu trong hệ thống hiện tại; việc đồng bộ sang WMS là bước mở rộng tương lai.

---

## 8. Ràng buộc và giả định

### 8.1 Ràng buộc

| STT | Ràng buộc |
|---|---|
| C01 | Hệ thống phải hỗ trợ **tối đa 50 Site** nhập khẩu kết nối đồng thời |
| C02 | Định dạng dữ liệu đầu ra gửi cho Site phải tuân đúng: `[Site code \| Merchandise code \| Quantity ordered \| Unit \| Delivery means]` |
| C03 | Giá trị Delivery means chỉ được là "ship delivery" hoặc "air delivery" |
| C04 | Tệp thông tin site và Tệp thông tin kho phải tuân đúng định dạng quy định trong phần mô tả hiện trạng |
| C05 | Thuật toán tách đơn (UC007) phải tuân đề bài: **(1) tàu trước bay** → **(2) ưu tiên Site tồn lớn** → **(3) ít Site nhất có thể**. Không thay bằng cách “sắp xếp một lần rồi lấy tuần tự” (chi tiết trong `usecase.md` / `flow.md`). |

### 8.2 Giả định

| STT | Giả định |
|---|---|
| A01 | Tất cả 50 Site đã có kết nối internet ổn định để tương tác với hệ thống |
| A02 | Mỗi Site có ít nhất một đại diện được ủy quyền sử dụng hệ thống |
| A03 | Danh mục mặt hàng chuẩn (master catalogue) đã tồn tại và được duy trì bởi quản trị viên hệ thống |
| A04 | Thông tin số ngày vận chuyển do Site cung cấp là chính xác và được cập nhật kịp thời |
| A05 | Bộ phận Bán hàng và Bộ phận Đặt hàng quốc tế làm việc trong cùng múi giờ; Site nước ngoài có thể ở múi giờ khác nhau |

---

## 9. Phụ lục

### 9.1 Bảng trạng thái đầy đủ

#### Trạng thái Yêu cầu nhập hàng

| Trạng thái | Giá trị | Mô tả | Chuyển tiếp từ |
|---|---|---|---|
| Chờ xử lý | CHO_XU_LY | Mới tạo, chưa được tiếp nhận | (khởi tạo) |
| Đang xử lý | DANG_XU_LY | Đang truy vấn tồn kho / tách đơn | CHO_XU_LY |
| Đã tách đơn | DA_TACH_DON | Đơn hàng đã được gửi tới Site | DANG_XU_LY |
| Lỗi – Không đủ hàng | LOI | Không thể đáp ứng số lượng yêu cầu | DANG_XU_LY |

#### Trạng thái Đơn hàng con

| Trạng thái | Giá trị | Mô tả |
|---|---|---|
| Chờ gửi | CHO_GUI | Đã tạo, chưa phát hành |
| Đã gửi | DA_GUI | Đã phát hành tới Site |
| Đã xác nhận | DA_XAC_NHAN | Site đã xác nhận nhận đơn |
| Từ chối | TU_CHOI | Site từ chối thực hiện |
| Đã nhập kho | DA_NHAP_KHO | Đối chiếu hoàn tất, không sai lệch |
| Nhập kho có sai lệch | SAI_LECH | Đối chiếu hoàn tất, phát hiện sai lệch |

### 9.2 Bảng mã lỗi hệ thống

| Mã lỗi | Mô tả | Hành động hệ thống |
|---|---|---|
| ERR-001 | Mã hàng không tồn tại trong danh mục | Từ chối lưu, hiển thị thông báo |
| ERR-002 | Số lượng không hợp lệ (≤ 0) | Từ chối lưu, hiển thị thông báo |
| ERR-003 | Ngày nhận mong muốn đã qua | Từ chối lưu, hiển thị thông báo |
| ERR-004 | Không có Site nào kinh doanh mặt hàng | Ghi nhận lỗi cho mặt hàng, tiếp tục xử lý |
| ERR-005 | Site không phản hồi truy vấn (time-out) | Đánh dấu tồn kho = 0, tiếp tục |
| ERR-006 | Không đủ tồn kho để đáp ứng yêu cầu | Ghi lỗi, thông báo người dùng |
| ERR-007 | Không có Site đáp ứng ngày nhận mong muốn | Ghi lỗi, đề xuất nới ngày |
| ERR-008 | Mã Site đã tồn tại khi thêm mới | Từ chối, hiển thị thông báo |
| ERR-009 | Xóa Site đang có đơn hàng hoạt động | Từ chối, hiển thị lý do |

### 9.3 Luồng xử lý chính tóm tắt

```
UC002          UC005          UC006 + UC011      UC007           UC008          UC013 + UC014
Tạo yêu cầu → Tiếp nhận   → Truy vấn tồn kho → Tách đơn     → Gửi đơn     → Nhập kho
[Bán hàng]    [ĐH Quốc tế]  [ĐH QT + Site]     [ĐH Quốc tế]  [ĐH QT + Site] [Quản lý kho]
```
