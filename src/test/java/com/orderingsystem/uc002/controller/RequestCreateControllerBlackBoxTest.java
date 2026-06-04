package com.orderingsystem.uc002.controller;

import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.MerchandiseCatalogRepository;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử hộp đen (Black-Box Testing) cho phương thức
 * {@link RequestCreateController#createImportRequest(List)}.
 *
 * Kỹ thuật áp dụng:
 *   - Phân lớp tương đương (Equivalence Partitioning)
 *   - Phân tích giá trị biên (Boundary Value Analysis)
 *
 * Full class name: com.orderingsystem.uc002.controller.RequestCreateControllerBlackBoxTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC002 – createImportRequest – Kiểm thử Hộp Đen")
class RequestCreateControllerBlackBoxTest {

    @Mock
    private AuthService authService;

    @Mock
    private ImportRequestRepository importRequestRepository;

    @Mock
    private MerchandiseCatalogRepository merchandiseCatalogRepository;

    private RequestCreateController controller;

    // ------------------------------------------------------------------ setup
    @BeforeEach
    void setUp() {
        controller = new RequestCreateController(
                authService,
                importRequestRepository,
                merchandiseCatalogRepository
        );
        // Đăng nhập session mặc định với vai trò SALES
        AuthenticatedUser salesUser = new AuthenticatedUser(1L, "sales_user", UserRole.SALES, null);
        Session.setCurrentUser(salesUser);
        // authService.requireRole không ném ngoại lệ (vai trò đúng)
        doNothing().when(authService).requireRole(UserRole.SALES);
    }

    @AfterEach
    void tearDown() {
        Session.clear();
    }

    // ================================================================
    // TC-BB-01: Danh sách đầu vào là null
    // Phân lớp: input không hợp lệ (null)
    // Mong đợi: ném IllegalArgumentException
    // ================================================================
    @Test
    @DisplayName("TC-BB-01: Danh sách null → ném IllegalArgumentException")
    void tc_bb_01_nullLines_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.createImportRequest(null)
        );
        assertTrue(ex.getMessage().contains("rỗng"),
                "Thông báo lỗi phải đề cập đến 'rỗng'");
    }

    // ================================================================
    // TC-BB-02: Danh sách rỗng (không có dòng mặt hàng)
    // Phân lớp: input không hợp lệ (empty list)
    // Mong đợi: ném IllegalArgumentException
    // ================================================================
    @Test
    @DisplayName("TC-BB-02: Danh sách rỗng → ném IllegalArgumentException")
    void tc_bb_02_emptyLines_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of())
        );
        assertTrue(ex.getMessage().contains("rỗng"));
    }

    // ================================================================
    // TC-BB-03: Mã hàng là null
    // Phân lớp: mã hàng không hợp lệ (null)
    // Mong đợi: ném IllegalArgumentException chứa "Mã hàng không được để trống"
    // ================================================================
    @Test
    @DisplayName("TC-BB-03: Mã hàng null → ném IllegalArgumentException")
    void tc_bb_03_nullMerchandiseCode_throwsException() {
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                null, 10, "pcs", LocalDate.now().plusDays(5)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line))
        );
        assertTrue(ex.getMessage().contains("Mã hàng không được để trống"));
    }

    // ================================================================
    // TC-BB-04: Mã hàng là chuỗi rỗng/khoảng trắng
    // Phân lớp: mã hàng không hợp lệ (blank)
    // Mong đợi: ném IllegalArgumentException
    // ================================================================
    @Test
    @DisplayName("TC-BB-04: Mã hàng blank → ném IllegalArgumentException")
    void tc_bb_04_blankMerchandiseCode_throwsException() {
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "   ", 10, "pcs", LocalDate.now().plusDays(5)
        );
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-BB-05: Mã hàng dài hơn 64 ký tự (Boundary: vượt giới hạn trên)
    // Phân tích giá trị biên: length = 65
    // Mong đợi: ném IllegalArgumentException chứa "64 ký tự"
    // ================================================================
    @Test
    @DisplayName("TC-BB-05: Mã hàng > 64 ký tự → ném IllegalArgumentException")
    void tc_bb_05_merchandiseCodeTooLong_throwsException() {
        String longCode = "A".repeat(65);
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                longCode, 10, "pcs", LocalDate.now().plusDays(5)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line))
        );
        assertTrue(ex.getMessage().contains("64 ký tự"));
    }

    // ================================================================
    // TC-BB-06: Mã hàng không tồn tại trong danh mục chuẩn
    // Phân lớp: mã hàng hợp lệ về format nhưng không tồn tại
    // Mong đợi: ném IllegalArgumentException chứa "không tồn tại trong danh mục"
    // ================================================================
    @Test
    @DisplayName("TC-BB-06: Mã hàng không tồn tại trong danh mục → ném IllegalArgumentException")
    void tc_bb_06_merchandiseCodeNotInCatalog_throwsException() {
        when(merchandiseCatalogRepository.existsByCode("MCH-999")).thenReturn(false);

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-999", 10, "pcs", LocalDate.now().plusDays(5)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line))
        );
        assertTrue(ex.getMessage().contains("không tồn tại trong danh mục chuẩn"));
    }

    // ================================================================
    // TC-BB-07: Số lượng bằng 0 (Boundary: giới hạn dưới không hợp lệ)
    // Phân tích giá trị biên: qty = 0
    // Mong đợi: ném IllegalArgumentException chứa "số nguyên dương"
    // ================================================================
    @Test
    @DisplayName("TC-BB-07: Số lượng = 0 → ném IllegalArgumentException")
    void tc_bb_07_quantityZero_throwsException() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 0, "pcs", LocalDate.now().plusDays(5)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line))
        );
        assertTrue(ex.getMessage().contains("số nguyên dương"));
    }

    // ================================================================
    // TC-BB-08: Số lượng âm
    // Phân lớp: số lượng không hợp lệ (< 0)
    // Mong đợi: ném IllegalArgumentException
    // ================================================================
    @Test
    @DisplayName("TC-BB-08: Số lượng âm → ném IllegalArgumentException")
    void tc_bb_08_quantityNegative_throwsException() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", -5, "pcs", LocalDate.now().plusDays(5)
        );
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-BB-09: Ngày nhận là ngày hôm nay (Boundary: không hợp lệ, phải SAU hôm nay)
    // Phân tích giá trị biên: date = LocalDate.now()
    // Mong đợi: ném IllegalArgumentException
    // ================================================================
    @Test
    @DisplayName("TC-BB-09: Ngày nhận = hôm nay → ném IllegalArgumentException")
    void tc_bb_09_deliveryDateToday_throwsException() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 10, "pcs", LocalDate.now()
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line))
        );
        assertTrue(ex.getMessage().contains("phải sau ngày hiện tại"));
    }

    // ================================================================
    // TC-BB-10: Ngày nhận là ngày hôm qua (quá khứ)
    // Phân lớp: ngày nhận không hợp lệ (quá khứ)
    // Mong đợi: ném IllegalArgumentException
    // ================================================================
    @Test
    @DisplayName("TC-BB-10: Ngày nhận là quá khứ → ném IllegalArgumentException")
    void tc_bb_10_deliveryDateInPast_throwsException() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 10, "pcs", LocalDate.now().minusDays(1)
        );
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-BB-11: Đơn vị là null
    // Phân lớp: đơn vị không hợp lệ (null)
    // Mong đợi: ném IllegalArgumentException
    // ================================================================
    @Test
    @DisplayName("TC-BB-11: Đơn vị null → ném IllegalArgumentException")
    void tc_bb_11_nullUnit_throwsException() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 10, null, LocalDate.now().plusDays(5)
        );
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-BB-12: Đơn vị dài hơn 32 ký tự (Boundary: vượt giới hạn trên)
    // Phân tích giá trị biên: unit.length = 33
    // Mong đợi: ném IllegalArgumentException chứa "32 ký tự"
    // ================================================================
    @Test
    @DisplayName("TC-BB-12: Đơn vị > 32 ký tự → ném IllegalArgumentException")
    void tc_bb_12_unitTooLong_throwsException() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);

        String longUnit = "U".repeat(33);
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 10, longUnit, LocalDate.now().plusDays(5)
        );
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line))
        );
        assertTrue(ex.getMessage().contains("32 ký tự"));
    }

    // ================================================================
    // TC-BB-13: Tất cả dữ liệu hợp lệ – một dòng mặt hàng (luồng chính)
    // Phân lớp: input hợp lệ
    // Mong đợi: trả về ImportRequestDto với status CHO_XU_LY, requestId không null
    // ================================================================
    @Test
    @DisplayName("TC-BB-13: Dữ liệu hợp lệ 1 dòng → tạo yêu cầu thành công")
    void tc_bb_13_validSingleLine_returnsDto() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);
        when(importRequestRepository.countByRequestIdPrefix(anyString())).thenReturn(0);
        doNothing().when(importRequestRepository).save(any(ImportRequest.class));

        ImportRequest savedRequest = new ImportRequest("REQ-20260604-001", "sales_user", "Sales");
        when(importRequestRepository.findByIdWithItems(anyString()))
                .thenReturn(Optional.of(savedRequest));

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 1, "pcs", LocalDate.now().plusDays(1)
        );
        ImportRequestDto result = controller.createImportRequest(List.of(line));

        assertNotNull(result);
        assertNotNull(result.requestId());
        assertEquals(RequestStatus.CHO_XU_LY, result.status());
        assertEquals("sales_user", result.createdBy());
    }

    // ================================================================
    // TC-BB-14: Nhiều dòng mặt hàng hợp lệ
    // Phân lớp: input hợp lệ (multiple lines)
    // Mong đợi: tạo thành công, số lượng items = 2
    // ================================================================
    @Test
    @DisplayName("TC-BB-14: Dữ liệu hợp lệ 2 dòng → tạo yêu cầu thành công")
    void tc_bb_14_validMultipleLines_returnsDto() {
        when(merchandiseCatalogRepository.existsByCode(anyString())).thenReturn(true);
        when(importRequestRepository.countByRequestIdPrefix(anyString())).thenReturn(0);
        doNothing().when(importRequestRepository).save(any(ImportRequest.class));

        ImportRequest savedRequest = new ImportRequest("REQ-20260604-001", "sales_user", "Sales");
        when(importRequestRepository.findByIdWithItems(anyString()))
                .thenReturn(Optional.of(savedRequest));

        List<CreateImportRequestLineInput> lines = List.of(
                new CreateImportRequestLineInput("MCH-001", 5, "pcs", LocalDate.now().plusDays(3)),
                new CreateImportRequestLineInput("MCH-002", 10, "box", LocalDate.now().plusDays(7))
        );
        ImportRequestDto result = controller.createImportRequest(lines);

        assertNotNull(result);
        verify(importRequestRepository, times(1)).save(any(ImportRequest.class));
    }

    // ================================================================
    // TC-BB-15: Mã hàng đúng 64 ký tự (Boundary: giới hạn trên hợp lệ)
    // Phân tích giá trị biên: code.length = 64
    // Mong đợi: không ném exception về độ dài mã hàng
    // ================================================================
    @Test
    @DisplayName("TC-BB-15: Mã hàng đúng 64 ký tự → không lỗi về độ dài")
    void tc_bb_15_merchandiseCodeExactly64Chars_valid() {
        String code64 = "A".repeat(64);
        when(merchandiseCatalogRepository.existsByCode(code64)).thenReturn(true);
        when(importRequestRepository.countByRequestIdPrefix(anyString())).thenReturn(0);
        doNothing().when(importRequestRepository).save(any(ImportRequest.class));

        ImportRequest savedRequest = new ImportRequest("REQ-20260604-001", "sales_user", "Sales");
        when(importRequestRepository.findByIdWithItems(anyString()))
                .thenReturn(Optional.of(savedRequest));

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                code64, 5, "pcs", LocalDate.now().plusDays(5)
        );
        assertDoesNotThrow(() -> controller.createImportRequest(List.of(line)));
    }
}
