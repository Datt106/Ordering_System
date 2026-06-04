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
 * Kiểm thử hộp trắng (White-Box Testing) cho phương thức
 * {@link RequestCreateController#createImportRequest(List)}.
 *
 * Kỹ thuật áp dụng: Độ đo phủ nhánh C1 (Branch Coverage / Decision Coverage)
 *
 * Phân tích các nhánh (branch) trong createImportRequest() và toEntityItem():
 *
 * [B1]  lines == null               → true (ném lỗi) / false (tiếp tục)
 * [B2]  lines.isEmpty()             → true (ném lỗi) / false (tiếp tục)
 * [B3]  code == null || isBlank()   → true (ném lỗi) / false (tiếp tục)
 * [B4]  trimmed.length() > 64       → true (ném lỗi) / false (tiếp tục)
 * [B5]  !catalog.existsByCode(code) → true (ném lỗi) / false (tiếp tục)
 * [B6]  qty <= 0                    → true (ném lỗi) / false (tiếp tục)
 * [B7]  unit == null || isBlank()   → true (ném lỗi) / false (tiếp tục)
 * [B8]  unit.length() > 32          → true (ném lỗi) / false (tiếp tục)
 * [B9]  deliveryDate == null        → true (ném lỗi) / false (tiếp tục)
 * [B10] !deliveryDate.isAfter(now)  → true (ném lỗi) / false (tiếp tục)
 * [B11] sequence > 999 (generateRequestId) → true (ném lỗi) / false (tiếp tục)
 *
 * Mỗi test dưới đây kích hoạt một nhánh cụ thể (true hoặc false).
 * Tổng cộng 11 nhánh, thiết kế 11 test case để đạt C1 = 100%.
 *
 * Full class name: com.orderingsystem.uc002.controller.RequestCreateControllerWhiteBoxTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC002 – createImportRequest – Kiểm thử Hộp Trắng (C1 Branch Coverage)")
class RequestCreateControllerWhiteBoxTest {

    @Mock
    private AuthService authService;

    @Mock
    private ImportRequestRepository importRequestRepository;

    @Mock
    private MerchandiseCatalogRepository merchandiseCatalogRepository;

    private RequestCreateController controller;

    @BeforeEach
    void setUp() {
        controller = new RequestCreateController(
                authService,
                importRequestRepository,
                merchandiseCatalogRepository
        );
        AuthenticatedUser salesUser = new AuthenticatedUser(1L, "sales_user", UserRole.SALES, null);
        Session.setCurrentUser(salesUser);
        doNothing().when(authService).requireRole(UserRole.SALES);
    }

    @AfterEach
    void tearDown() {
        Session.clear();
    }

    // ================================================================
    // TC-WB-01: [B1-true] lines == null
    // Nhánh: điều kiện lines == null → TRUE → ném IllegalArgumentException
    // ================================================================
    @Test
    @DisplayName("TC-WB-01: [B1-true] lines == null → ném IllegalArgumentException")
    void tc_wb_01_branch_B1_true_nullLines() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(null));
    }

    // ================================================================
    // TC-WB-02: [B2-true] lines.isEmpty()
    // Nhánh: điều kiện lines.isEmpty() → TRUE → ném IllegalArgumentException
    // (B1 đã false: lines != null nhưng isEmpty)
    // ================================================================
    @Test
    @DisplayName("TC-WB-02: [B2-true] lines rỗng → ném IllegalArgumentException")
    void tc_wb_02_branch_B2_true_emptyLines() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of()));
    }

    // ================================================================
    // TC-WB-03: [B3-true] code == null
    // Nhánh: code == null → TRUE → ném IllegalArgumentException
    // ================================================================
    @Test
    @DisplayName("TC-WB-03: [B3-true] mã hàng null → ném IllegalArgumentException")
    void tc_wb_03_branch_B3_true_nullCode() {
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                null, 5, "pcs", LocalDate.now().plusDays(3));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-WB-04: [B4-true] trimmed.length() > 64
    // Nhánh: code dài 65 ký tự → trimmed.length() > 64 == TRUE → ném lỗi
    // (B3 đã false: code không null/blank)
    // ================================================================
    @Test
    @DisplayName("TC-WB-04: [B4-true] mã hàng > 64 ký tự → ném IllegalArgumentException")
    void tc_wb_04_branch_B4_true_codeTooLong() {
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "A".repeat(65), 5, "pcs", LocalDate.now().plusDays(3));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
        assertTrue(ex.getMessage().contains("64 ký tự"));
    }

    // ================================================================
    // TC-WB-05: [B5-true] !existsByCode(code)
    // Nhánh: mã hàng không tồn tại trong catalog → TRUE → ném lỗi
    // (B3, B4 đã false: code hợp lệ, đúng độ dài)
    // ================================================================
    @Test
    @DisplayName("TC-WB-05: [B5-true] mã hàng không tồn tại catalog → ném IllegalArgumentException")
    void tc_wb_05_branch_B5_true_codeNotInCatalog() {
        when(merchandiseCatalogRepository.existsByCode("MCH-X")).thenReturn(false);
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-X", 5, "pcs", LocalDate.now().plusDays(3));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-WB-06: [B6-true] qty <= 0
    // Nhánh: qty = 0 → TRUE → ném IllegalArgumentException
    // (B3..B5 đã false: code tồn tại trong catalog)
    // ================================================================
    @Test
    @DisplayName("TC-WB-06: [B6-true] qty = 0 → ném IllegalArgumentException")
    void tc_wb_06_branch_B6_true_qtyZero() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 0, "pcs", LocalDate.now().plusDays(3));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-WB-07: [B7-true] unit == null
    // Nhánh: unit null → TRUE → ném IllegalArgumentException
    // (B3..B6 đã false)
    // ================================================================
    @Test
    @DisplayName("TC-WB-07: [B7-true] đơn vị null → ném IllegalArgumentException")
    void tc_wb_07_branch_B7_true_nullUnit() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 5, null, LocalDate.now().plusDays(3));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-WB-08: [B8-true] unit.length() > 32
    // Nhánh: unit dài 33 ký tự → trimmed.length() > 32 == TRUE → ném lỗi
    // (B7 đã false: unit không null/blank)
    // ================================================================
    @Test
    @DisplayName("TC-WB-08: [B8-true] đơn vị > 32 ký tự → ném IllegalArgumentException")
    void tc_wb_08_branch_B8_true_unitTooLong() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 5, "U".repeat(33), LocalDate.now().plusDays(3));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-WB-09: [B9-true] deliveryDate == null
    // Nhánh: deliveryDate null → TRUE → ném IllegalArgumentException
    // (B3..B8 đã false)
    // ================================================================
    @Test
    @DisplayName("TC-WB-09: [B9-true] ngày giao null → ném IllegalArgumentException")
    void tc_wb_09_branch_B9_true_nullDeliveryDate() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 5, "pcs", null);
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-WB-10: [B10-true] !deliveryDate.isAfter(now)
    // Nhánh: deliveryDate = today → !isAfter(now) == TRUE → ném lỗi
    // (B9 đã false: deliveryDate không null)
    // ================================================================
    @Test
    @DisplayName("TC-WB-10: [B10-true] ngày giao = hôm nay → ném IllegalArgumentException")
    void tc_wb_10_branch_B10_true_deliveryDateNotAfterToday() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);
        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 5, "pcs", LocalDate.now());
        assertThrows(IllegalArgumentException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-WB-11: [B11-true] sequence > 999 trong generateRequestId()
    // Nhánh: đã có 999 yêu cầu trong ngày → sequence = 1000 > 999 → ném IllegalStateException
    // (Tất cả B1..B10 đã false: dữ liệu input hoàn toàn hợp lệ)
    // ================================================================
    @Test
    @DisplayName("TC-WB-11: [B11-true] vượt 999 yêu cầu trong ngày → ném IllegalStateException")
    void tc_wb_11_branch_B11_true_dailyLimitExceeded() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);
        // Trả về 999 → sequence = 1000 → vượt giới hạn
        when(importRequestRepository.countByRequestIdPrefix(anyString())).thenReturn(999);

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 5, "pcs", LocalDate.now().plusDays(3));
        assertThrows(IllegalStateException.class,
                () -> controller.createImportRequest(List.of(line)));
    }

    // ================================================================
    // TC-WB-12: Tất cả nhánh FALSE → luồng thành công (Happy Path)
    // Mục đích: bao phủ nhánh FALSE của tất cả các điều kiện (B1..B11 đều false)
    // Mong đợi: trả về ImportRequestDto hợp lệ, gọi save() đúng 1 lần
    // ================================================================
    @Test
    @DisplayName("TC-WB-12: Tất cả nhánh false (luồng thành công) → trả về DTO")
    void tc_wb_12_allBranchesFalse_happyPath() {
        when(merchandiseCatalogRepository.existsByCode("MCH-001")).thenReturn(true);
        when(importRequestRepository.countByRequestIdPrefix(anyString())).thenReturn(0); // sequence = 1 ≤ 999
        doNothing().when(importRequestRepository).save(any(ImportRequest.class));

        ImportRequest savedReq = new ImportRequest("REQ-20260604-001", "sales_user", "Sales");
        when(importRequestRepository.findByIdWithItems(anyString()))
                .thenReturn(Optional.of(savedReq));

        CreateImportRequestLineInput line = new CreateImportRequestLineInput(
                "MCH-001", 1, "pcs", LocalDate.now().plusDays(1));
        ImportRequestDto result = controller.createImportRequest(List.of(line));

        assertNotNull(result);
        assertEquals(RequestStatus.CHO_XU_LY, result.status());
        assertEquals("REQ-20260604-001", result.requestId());
        verify(importRequestRepository, times(1)).save(any(ImportRequest.class));
        verify(importRequestRepository, times(1)).findByIdWithItems(anyString());
    }
}
