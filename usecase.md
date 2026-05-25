# Đặc tả Use Case – Hệ thống Đặt hàng Nhập khẩu

## 1. Danh sách Actor

| Actor | Loại | Vai trò |
|---|---|---|
| Bộ phận Bán hàng | Nội bộ | Khởi tạo yêu cầu nhập hàng, theo dõi tiến độ |
| Bộ phận Đặt hàng quốc tế | Nội bộ (chính) | Điều phối toàn bộ luồng xử lý đơn hàng |
| Site nhập khẩu | Bên ngoài | Cung cấp hàng hóa, xác nhận tồn kho và vận chuyển |
| Bộ phận Quản lý kho | Nội bộ | Tiếp nhận và đối chiếu hàng thực tế khi về |

---

## 2. Tổng quan Use Case

| Mã UC | Tên UC | Actor chính | Loại |
|---|---|---|---|
| UC002 | Tạo yêu cầu nhập hàng | Bộ phận Bán hàng | Cơ bản |
| UC003 | Theo dõi trạng thái yêu cầu | Bộ phận Bán hàng | Cơ bản |
| UC004 | Quản lý Site | Bộ phận Đặt hàng quốc tế | Phức hợp |
| UC005 | Tiếp nhận yêu cầu đơn hàng | Bộ phận Đặt hàng quốc tế | Cơ bản |
| UC006 | Truy vấn thông tin tồn kho và vận chuyển | Bộ phận Đặt hàng quốc tế | Cơ bản |
| UC007 | Xử lý yêu cầu và Tách đơn hàng | Bộ phận Đặt hàng quốc tế | Phức hợp |
| UC008 | Gửi đơn hàng | Bộ phận Đặt hàng quốc tế | Cơ bản |
| UC009 | Quản lý mặt hàng kinh doanh | Site nhập khẩu | Cơ bản |
| UC010 | Cập nhật thông tin vận chuyển | Site nhập khẩu | Cơ bản |
| UC011 | Xác nhận tồn kho | Site nhập khẩu | Cơ bản |
| UC012 | Tiếp nhận và Xác nhận đơn hàng | Site nhập khẩu | Cơ bản |
| UC013 | Xem danh sách đơn hàng | Bộ phận Quản lý kho | Cơ bản |
| UC014 | Đối chiếu và Ghi nhận nhập kho | Bộ phận Quản lý kho | Phức hợp |

---

## 3. Đặc tả Chi tiết

---

### UC002 – Tạo yêu cầu nhập hàng

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC002 |
| **Tên** | Tạo yêu cầu nhập hàng |
| **Actor chính** | Bộ phận Bán hàng |
| **Actor phụ** | — |
| **Mô tả** | Nhân viên bán hàng tạo một yêu cầu nhập hàng mới gồm danh sách mặt hàng, số lượng và ngày nhận mong muốn |
| **Tiền điều kiện** | Người dùng đã đăng nhập với vai trò Bộ phận Bán hàng |
| **Hậu điều kiện** | Yêu cầu nhập hàng được lưu vào hệ thống với trạng thái **Chờ xử lý** và sẵn sàng để Bộ phận Đặt hàng quốc tế tiếp nhận |

**Luồng sự kiện chính:**

1. Người dùng chọn chức năng "Tạo yêu cầu nhập hàng mới".
2. Hệ thống hiển thị form nhập liệu.
3. Người dùng nhập thông tin cho từng dòng mặt hàng:
   - Mã hàng (Merchandise code)
   - Số lượng (Quantity ordered)
   - Đơn vị (Unit)
   - Ngày nhận mong muốn (Desired delivery date: Year / Month / Date)
4. Người dùng thêm nhiều dòng nếu cần và nhấn "Gửi yêu cầu".
5. Hệ thống kiểm tra tính hợp lệ của dữ liệu.
6. Hệ thống lưu yêu cầu với trạng thái **Chờ xử lý**, gán mã yêu cầu duy nhất, ghi nhận ngày tạo và người tạo.
7. Hệ thống hiển thị thông báo thành công và mã yêu cầu vừa tạo.

**Luồng thay thế / ngoại lệ:**

- **5a.** Mã hàng không tồn tại trong danh mục: Hệ thống hiển thị cảnh báo, yêu cầu nhập lại.
- **5b.** Số lượng ≤ 0 hoặc ngày nhận đã qua: Hệ thống hiển thị lỗi, không cho phép lưu.
- **5c.** Danh sách mặt hàng rỗng: Hệ thống không cho phép gửi, hiển thị thông báo lỗi.

---

### UC003 – Theo dõi trạng thái yêu cầu

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC003 |
| **Tên** | Theo dõi trạng thái yêu cầu |
| **Actor chính** | Bộ phận Bán hàng |
| **Actor phụ** | — |
| **Mô tả** | Nhân viên bán hàng tra cứu tiến độ xử lý của các yêu cầu nhập hàng mà bộ phận mình đã tạo |
| **Tiền điều kiện** | Người dùng đã đăng nhập; tồn tại ít nhất một yêu cầu do bộ phận tạo ra |
| **Hậu điều kiện** | Thông tin trạng thái được hiển thị (không thay đổi dữ liệu) |

**Luồng sự kiện chính:**

1. Người dùng vào mục "Danh sách yêu cầu nhập hàng".
2. Hệ thống hiển thị danh sách tất cả yêu cầu của bộ phận kèm trạng thái hiện tại.
3. Người dùng chọn một yêu cầu để xem chi tiết.
4. Hệ thống hiển thị:
   - Thông tin các mặt hàng trong yêu cầu
   - Trạng thái tổng thể của yêu cầu
   - Các đơn hàng con đã được tạo (nếu có): Site, số lượng, phương tiện vận chuyển
   - Ngày dự kiến nhận hàng

**Trạng thái yêu cầu có thể có:**

| Trạng thái | Ý nghĩa |
|---|---|
| Chờ xử lý | Chưa có Bộ phận Đặt hàng quốc tế xử lý |
| Đang xử lý | Đang truy vấn tồn kho / đang tách đơn |
| Đã tách đơn | Đơn hàng đã được phát hành tới các Site |
| Lỗi – Không đủ hàng | Không thể đáp ứng đủ số lượng yêu cầu |

---

### UC004 – Quản lý Site

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC004 |
| **Tên** | Quản lý Site |
| **Actor chính** | Bộ phận Đặt hàng quốc tế |
| **Actor phụ** | — |
| **Mô tả** | Thêm, sửa, xóa thông tin hồ sơ của các Site nhập khẩu đối tác nước ngoài |
| **Tiền điều kiện** | Người dùng đã đăng nhập với vai trò Bộ phận Đặt hàng quốc tế |
| **Hậu điều kiện** | Thông tin Site được cập nhật trong Tệp thông tin site |
| **Use case con** | UC004a – Thêm Site, UC004b – Sửa Site, UC004c – Xóa Site |

**Luồng chính – Thêm Site (UC004a):**

1. Người dùng chọn "Thêm Site mới".
2. Hệ thống hiển thị form nhập liệu.
3. Người dùng nhập: Mã Site, Tên Site, Số ngày vận chuyển đường tàu, Số ngày vận chuyển đường hàng không, Thông tin khác.
4. Người dùng xác nhận lưu.
5. Hệ thống kiểm tra mã Site chưa trùng lặp, lưu thông tin.
6. Hệ thống xác nhận thành công.

**Luồng chính – Sửa Site (UC004b):**

1. Người dùng tìm kiếm và chọn Site cần sửa.
2. Hệ thống hiển thị thông tin hiện tại.
3. Người dùng chỉnh sửa các trường cần thiết.
4. Người dùng xác nhận lưu.
5. Hệ thống cập nhật và xác nhận thành công.

**Luồng chính – Xóa Site (UC004c):**

1. Người dùng tìm kiếm và chọn Site cần xóa.
2. Hệ thống kiểm tra Site không có đơn hàng đang hoạt động.
3. Hệ thống yêu cầu xác nhận xóa.
4. Người dùng xác nhận.
5. Hệ thống xóa Site và xác nhận thành công.

**Ngoại lệ:**

- **5a (Thêm).** Mã Site đã tồn tại: Hệ thống báo lỗi trùng lặp.
- **2a (Xóa).** Site đang có đơn hàng chưa hoàn tất: Hệ thống từ chối xóa và thông báo lý do.

---

### UC005 – Tiếp nhận yêu cầu đơn hàng

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC005 |
| **Tên** | Tiếp nhận yêu cầu đơn hàng |
| **Actor chính** | Bộ phận Đặt hàng quốc tế |
| **Mô tả** | Xem và tiếp nhận các yêu cầu nhập hàng từ Bộ phận Bán hàng để bắt đầu xử lý |
| **Tiền điều kiện** | Tồn tại ít nhất một yêu cầu ở trạng thái **Chờ xử lý** |
| **Hậu điều kiện** | Yêu cầu được chuyển sang trạng thái **Đang xử lý** |

**Luồng sự kiện chính:**

1. Người dùng vào mục "Danh sách yêu cầu chờ xử lý".
2. Hệ thống hiển thị danh sách các yêu cầu với trạng thái **Chờ xử lý**, sắp xếp theo ngày tạo.
3. Người dùng xem chi tiết một yêu cầu (danh sách mặt hàng, số lượng, ngày nhận mong muốn).
4. Người dùng nhấn "Tiếp nhận xử lý".
5. Hệ thống chuyển trạng thái yêu cầu sang **Đang xử lý**, ghi nhận người tiếp nhận và thời điểm tiếp nhận.

---

### UC006 – Truy vấn thông tin tồn kho và vận chuyển

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC006 |
| **Tên** | Truy vấn thông tin tồn kho và vận chuyển |
| **Actor chính** | Bộ phận Đặt hàng quốc tế |
| **Actor phụ** | Site nhập khẩu (phản hồi qua UC011) |
| **Mô tả** | Hệ thống tìm các Site có kinh doanh mặt hàng cần đặt và gửi truy vấn tồn kho tới các Site đó |
| **Tiền điều kiện** | Yêu cầu nhập hàng đang ở trạng thái **Đang xử lý**; Tệp thông tin site đã có dữ liệu |
| **Hậu điều kiện** | Tệp thông tin kho được cập nhật với số liệu tồn kho mới nhất từ các Site phản hồi |
| **Quan hệ** | Kích hoạt UC011 (Site xác nhận tồn kho) |

**Luồng sự kiện chính:**

1. Hệ thống đọc danh sách mặt hàng từ yêu cầu đang xử lý.
2. Với mỗi mặt hàng, hệ thống tra cứu danh sách Site có kinh doanh mặt hàng đó.
3. Hệ thống nhóm các mặt hàng theo Site (mỗi Site nhận đúng danh sách mặt hàng họ kinh doanh).
4. Hệ thống gửi truy vấn tồn kho tới từng Site.
5. Người dùng chờ phản hồi từ các Site (UC011).
6. Khi nhận được phản hồi, hệ thống ghi vào Tệp thông tin kho: `[Site code | Merchandise code | In-stock quantity | Unit]`.
7. Hệ thống thông báo đã nhận đủ phản hồi từ tất cả Site.

**Ngoại lệ:**

- **2a.** Không có Site nào kinh doanh một mặt hàng cụ thể: Hệ thống ghi nhận lỗi cho mặt hàng đó, tiếp tục xử lý các mặt hàng còn lại.
- **7a.** Một Site không phản hồi trong thời gian quy định: Hệ thống đánh dấu tồn kho = 0 cho mặt hàng đó tại Site đó.

---

### UC007 – Xử lý yêu cầu và Tách đơn hàng

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC007 |
| **Tên** | Xử lý yêu cầu và Tách đơn hàng |
| **Actor chính** | Bộ phận Đặt hàng quốc tế |
| **Mô tả** | Dựa trên tồn kho và số ngày vận chuyển, quyết định nhập bao nhiêu từ Site nào (từng mặt hàng độc lập); ưu tiên tàu → tồn lớn → ít Site; tạo đơn hàng con |
| **Tiền điều kiện** | Tệp thông tin kho đã được cập nhật đầy đủ (UC006 + UC011 hoàn tất) |
| **Hậu điều kiện** | Tạo ra danh sách đơn hàng con sẵn sàng để gửi (UC008); hoặc ghi nhận lỗi nếu không đủ hàng |
| **Quan hệ** | `<<include>>` UC006; `<<extend>>` UC008 |

**Luồng sự kiện chính:**

1. Định kỳ theo tần suất vận hành của bộ phận, người dùng truy cập vào Không gian làm việc (Workspace) và thiết lập các tham số vận hành trên thanh công cụ cấu hình:
   - **Ngày bắt đầu tính toán (Calculation Start Date):** Mặc định hiển thị ngày hệ thống hiện tại (`Current_Date`), cho phép người dùng tùy chỉnh bằng bộ chọn ngày (Date Picker).
   - **Khung thời gian gom (Time Window):** Nhập số ngày gộp đơn thông qua ô nhập số hoặc danh sách chọn sẵn.
2. Người dùng khởi động chức năng tách đơn cho yêu cầu đang xử lý.
3. Hệ thống tự động gộp số lượng của các mặt hàng trùng mã từ các yêu cầu đã chọn để tạo thành Tổng nhu cầu. Hệ thống xác định **Ngày nhận đích (Target Date)** của lô hàng bằng **Ngày sớm nhất** trong các yêu cầu được gộp.
4. Với từng mặt hàng, hệ thống thực hiện thuật toán phân bổ (xem chi tiết bên dưới).
5. Hệ thống tổng hợp kết quả: danh sách `[Site | Mã hàng | Số lượng | Phương tiện]`.
6. Hệ thống hiển thị kết quả tách đơn cho người dùng xem xét.
7. Người dùng xác nhận hoặc điều chỉnh thủ công nếu cần.
8. Hệ thống lưu các đơn hàng con với trạng thái **Chờ gửi**.

**Nguyên tắc (theo đề bài):**

- Xử lý **từng mặt hàng độc lập**.
- Chỉ xét Site **đáp ứng ngày nhận** (`ETA ≤ Ngày nhận đích`).
- Một Site không đủ hàng → **được phép** lấy từ nhiều Site; tổng lấy từ một Site không vượt tồn kho thực tế tại Site đó.
- Không đủ tổng tồn khả dụng → **báo lỗi** (không tách đơn cho mặt hàng đó).
- Chọn Site theo **thứ tự ưu tiên giảm dần** (so sánh **từng cấp**, cấp sau chỉ áp dụng khi cấp trước **bằng nhau**):
  1. **Ưu tiên phương tiện tàu** hơn hàng không (tối đa hóa số lượng đi tàu trong phương án).
  2. **Ưu tiên Site có tồn kho lớn** (tie-break khi cùng số Site và cùng mức dùng tàu/bay).
  3. **Số Site được chọn nhỏ nhất có thể** (đếm theo **mã Site**, không đếm số dòng đơn).

**Thuật toán phân bổ (cho từng mặt hàng):**

```
INPUT: merchandise M, total_quantity Q, target_delivery_date D,
       calculation_start_date StartDate

BƯỚC 1 – Tính ETA & lọc phương án (Site, mode):
  Với mỗi Site S có in_stock[S] > 0 cho M:
    ETA_Ship = StartDate + ship_days[S]
    ETA_Air  = StartDate + air_days[S]
    Nếu ETA_Ship ≤ D → thêm phương án (S, ship), capacity = in_stock[S]
    Nếu ETA_Air  ≤ D → thêm phương án (S, air),  capacity = in_stock[S]
  (Cùng một Site dùng chung một pool tồn: tổng số lượng lấy từ S ≤ in_stock[S].)

BƯỚC 2 – Chọn phương án tối ưu theo thứ tự ưu tiên (lexicographic):

  2a. Tối đa hóa lượng hàng đi TÀU:
      Trên tập phương án mode = ship, tìm phân bổ (có thể nhiều Site)
      sao cho tổng qty_ship càng lớn càng tốt (tối đa min(Q, tổng tồn tàu-eligible)).

  2b. Trong các phương án có cùng qty_ship tối đa, chọn phương án có
      **số Site (mã Site) nhỏ nhất** vẫn đạt được qty_ship đó
      (bài toán tập Site tối thiểu — có thể dùng DP vì tối đa ~50 Site).

  2c. Nếu qty_ship < Q: bổ sung phương án AIR cho phần còn thiếu
      (remaining = Q − qty_ship), lại áp dụng **ít Site nhất** trên pool
      tồn còn lại của từng Site; chỉ dùng air khi cần.

  2d. Tie-break (cùng qty_ship, cùng số Site): ưu tiên tập Site có
      in_stock lớn hơn (sắp xếp Site theo tồn giảm dần khi phân bổ
      trong từng phương án).

  → Kết quả bước 2: tập các dòng [Site | mode | qty] thỏa Q và
    thỏa thứ tự ưu tiên đề bài.

BƯỚC 3 – Phân bổ số lượng cụ thể trên tập Site đã chọn:
  Với mỗi mode (ship trước, air sau), sắp Site theo in_stock giảm dần:
    allocate = min(tồn còn lại tại S, remaining)
    remaining -= allocate
    Ghi đơn con: [S | M | allocate | mode]

BƯỚC 4 – Kiểm tra:
  Nếu không tồn tại phương án đủ Q sau bước 2 → lỗi "Không đủ hàng cho M"
  Nếu không có (S, mode) nào thỏa ETA ≤ D → lỗi "Không có Site đáp ứng ngày nhận"
```

**Lưu ý:** Không dùng “sắp xếp rồi greedy một lần” thay cho bước 2b–2c — cách đó có thể vi phạm tiêu chí **ít Site nhất** hoặc **tối đa tàu** (xem ví dụ trong `flow.md`).

**Ngoại lệ:**

- **1a.** Không có Site nào đáp ứng ngày nhận mong muốn: Hệ thống thông báo lỗi, đề xuất nới rộng ngày nhận.
- **3a.** Tổng tồn kho < số lượng cần: Hệ thống ghi nhận lỗi "Không đủ hàng", báo cáo số lượng thiếu.

---

### UC008 – Gửi đơn hàng

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC008 |
| **Tên** | Gửi đơn hàng |
| **Actor chính** | Bộ phận Đặt hàng quốc tế |
| **Actor phụ** | Site nhập khẩu (nhận đơn qua UC012) |
| **Mô tả** | Phát hành chính thức các đơn hàng con tới các Site và thông báo cho Bộ phận Quản lý kho |
| **Tiền điều kiện** | Các đơn hàng con đã được tạo và xác nhận ở UC007 (trạng thái **Chờ gửi**) |
| **Hậu điều kiện** | Đơn hàng được gửi tới Site (trạng thái **Đã gửi**); thông tin đơn hàng hiển thị cho Bộ phận Quản lý kho |
| **Quan hệ** | Kích hoạt UC012 (Site tiếp nhận đơn) |

**Luồng sự kiện chính:**

1. Người dùng chọn "Gửi đơn hàng" cho yêu cầu đã được tách đơn.
2. Hệ thống hiển thị tóm tắt các đơn hàng con sắp gửi.
3. Người dùng xác nhận gửi.
4. Hệ thống gửi thông tin tới từng Site theo định dạng: `[Site code | Merchandise code | Quantity ordered | Unit | Delivery means]`.
5. Hệ thống cập nhật trạng thái các đơn hàng con thành **Đã gửi**.
6. Hệ thống cập nhật trạng thái yêu cầu gốc thành **Đã tách đơn**.
7. Thông tin đơn hàng được lưu và trở nên hiển thị với Bộ phận Quản lý kho (UC013).

---

### UC009 – Quản lý mặt hàng kinh doanh

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC009 |
| **Tên** | Quản lý mặt hàng kinh doanh |
| **Actor chính** | Site nhập khẩu |
| **Mô tả** | Site khai báo và cập nhật danh sách các mặt hàng mà họ có thể cung cấp |
| **Tiền điều kiện** | Site đã được đăng ký trong hệ thống (UC004); đại diện Site đã đăng nhập |
| **Hậu điều kiện** | Danh sách mặt hàng kinh doanh của Site được cập nhật trong hệ thống |

**Luồng sự kiện chính:**

1. Đại diện Site vào mục "Quản lý danh mục mặt hàng".
2. Hệ thống hiển thị danh sách mặt hàng hiện tại của Site.
3. Site có thể:
   - **Thêm** mặt hàng mới bằng cách nhập mã hàng (phải tồn tại trong danh mục chuẩn).
   - **Xóa** mặt hàng khỏi danh sách kinh doanh của mình.
4. Site xác nhận lưu thay đổi.
5. Hệ thống cập nhật và thông báo thành công.

---

### UC010 – Cập nhật thông tin vận chuyển

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC010 |
| **Tên** | Cập nhật thông tin vận chuyển |
| **Actor chính** | Site nhập khẩu |
| **Mô tả** | Site tự cập nhật số ngày vận chuyển dự kiến theo đường tàu và đường hàng không |
| **Tiền điều kiện** | Site đã đăng nhập |
| **Hậu điều kiện** | Tệp thông tin site được cập nhật với thông tin vận chuyển mới |

**Luồng sự kiện chính:**

1. Đại diện Site vào mục "Thông tin vận chuyển".
2. Hệ thống hiển thị thông tin vận chuyển hiện tại: số ngày tàu, số ngày hàng không.
3. Site nhập giá trị mới cho một hoặc cả hai phương tiện.
4. Site xác nhận lưu.
5. Hệ thống kiểm tra giá trị hợp lệ (số nguyên dương) và lưu vào Tệp thông tin site.

---

### UC011 – Xác nhận tồn kho

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC011 |
| **Tên** | Xác nhận tồn kho |
| **Actor chính** | Site nhập khẩu |
| **Mô tả** | Site phản hồi truy vấn từ UC006, xác nhận số lượng hàng thực tế còn trong kho |
| **Tiền điều kiện** | Site nhận được truy vấn tồn kho từ Bộ phận Đặt hàng quốc tế (UC006) |
| **Hậu điều kiện** | Tệp thông tin kho được cập nhật; UC006 tiếp tục xử lý |

**Luồng sự kiện chính:**

1. Site nhận được thông báo truy vấn tồn kho trong hệ thống.
2. Hệ thống hiển thị danh sách mặt hàng cần xác nhận.
3. Với từng mặt hàng, Site nhập số lượng tồn kho thực tế (0 nếu không còn hàng).
4. Site xác nhận gửi phản hồi.
5. Hệ thống lưu phản hồi vào Tệp thông tin kho: `[Site code | Merchandise code | In-stock quantity | Unit]`.

---

### UC012 – Tiếp nhận và Xác nhận đơn hàng

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC012 |
| **Tên** | Tiếp nhận và Xác nhận đơn hàng |
| **Actor chính** | Site nhập khẩu |
| **Mô tả** | Site nhận đơn hàng từ hệ thống, xem xét và xác nhận để chuẩn bị hàng hóa |
| **Tiền điều kiện** | Đơn hàng đã được gửi bởi Bộ phận Đặt hàng quốc tế (UC008) |
| **Hậu điều kiện** | Đơn hàng được Site xác nhận; trạng thái đơn chuyển sang **Đã xác nhận** |

**Luồng sự kiện chính:**

1. Site nhận thông báo có đơn hàng mới.
2. Hệ thống hiển thị chi tiết đơn: `[Mã hàng | Số lượng | Đơn vị | Phương tiện vận chuyển]`.
3. Site xem xét chi tiết đơn hàng.
4. Site xác nhận đơn hàng.
5. Hệ thống cập nhật trạng thái đơn thành **Đã xác nhận**, ghi nhận thời điểm xác nhận.

**Ngoại lệ:**

- **4a.** Site không thể thực hiện đơn: Site có thể gửi thông báo từ chối kèm lý do. Hệ thống thông báo cho Bộ phận Đặt hàng quốc tế để xử lý lại.

---

### UC013 – Xem danh sách đơn hàng

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC013 |
| **Tên** | Xem danh sách đơn hàng |
| **Actor chính** | Bộ phận Quản lý kho |
| **Mô tả** | Nhân viên kho tra cứu các đơn hàng đã được gửi ra quốc tế để chuẩn bị kế hoạch tiếp nhận |
| **Tiền điều kiện** | Có ít nhất một đơn hàng ở trạng thái **Đã gửi** hoặc **Đã xác nhận** |
| **Hậu điều kiện** | Thông tin được hiển thị (chỉ đọc, không thay đổi dữ liệu) |

**Luồng sự kiện chính:**

1. Người dùng vào mục "Danh sách đơn hàng sắp về".
2. Hệ thống hiển thị danh sách đơn hàng kèm: Site, mã hàng, số lượng, phương tiện, ngày dự kiến về.
3. Người dùng có thể lọc theo Site, mã hàng, ngày về dự kiến.
4. Người dùng chọn đơn để xem chi tiết.

---

### UC014 – Đối chiếu và Ghi nhận nhập kho

| Trường | Nội dung |
|---|---|
| **Mã UC** | UC014 |
| **Tên** | Đối chiếu và Ghi nhận nhập kho |
| **Actor chính** | Bộ phận Quản lý kho |
| **Mô tả** | Nhân viên kho đối chiếu hàng thực tế với đơn hàng đã đặt, ghi nhận sai lệch vào hệ thống |
| **Tiền điều kiện** | Hàng hóa đã về; đơn hàng tương ứng ở trạng thái **Đã gửi** hoặc **Đã xác nhận** |
| **Hậu điều kiện** | Đơn hàng được cập nhật trạng thái **Đã nhập kho** hoặc **Nhập kho có sai lệch**; Bộ phận Đặt hàng quốc tế được thông báo nếu có sai lệch |

**Luồng sự kiện chính:**

1. Nhân viên kho chọn đơn hàng đang chờ đối chiếu.
2. Hệ thống hiển thị danh sách mặt hàng đã đặt với số lượng dự kiến.
3. Nhân viên nhập số lượng thực tế nhận được cho từng mặt hàng.
4. Hệ thống so sánh số lượng thực tế với số lượng đặt, tính toán sai lệch.
5. Nếu không có sai lệch: Hệ thống cập nhật trạng thái thành **Đã nhập kho**.
6. Nếu có sai lệch: Hệ thống đánh dấu **Nhập kho có sai lệch**, lưu chi tiết sai lệch, gửi thông báo tới Bộ phận Đặt hàng quốc tế.

**Ngoại lệ:**

- **3a.** Số lượng thực tế = 0 cho một mặt hàng: Hệ thống ghi nhận là "Không nhận được hàng" cho mặt hàng đó.

---

## 4. Quan hệ giữa các Use Case

```
UC002 ──────────────────────────────► UC005 (Bán hàng tạo → Đặt hàng tiếp nhận)
UC005 ──── <<include>> ──────────────► UC006 (Tiếp nhận → Truy vấn tồn kho)
UC006 ──── kích hoạt ────────────────► UC011 (Truy vấn → Site xác nhận)
UC006 ──── <<include>> ──────────────► UC007 (Tồn kho sẵn sàng → Tách đơn)
UC007 ──── <<include>> ──────────────► UC008 (Tách đơn → Gửi đơn)
UC008 ──── kích hoạt ────────────────► UC012 (Gửi đơn → Site xác nhận đơn)
UC008 ──── cung cấp dữ liệu ─────────► UC013 (Đơn gửi → Kho xem đơn)
UC013 ──── <<include>> ──────────────► UC014 (Xem đơn → Đối chiếu)
UC004 ──── <<extend>> ───────────────► UC009 (Quản lý Site mở rộng quản lý mặt hàng)
UC004 ──── <<extend>> ───────────────► UC010 (Quản lý Site mở rộng cập nhật vận chuyển)
```
