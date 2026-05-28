package com.orderingsystem.uc002.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.MerchandiseCatalogRepository;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * UC002 — Bộ phận Bán hàng tạo yêu cầu nhập hàng (trạng thái Chờ xử lý).
 */
public class RequestCreateController {

    private static final String SALES_DEPARTMENT = "Sales";
    private static final DateTimeFormatter REQUEST_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;
    private final MerchandiseCatalogRepository merchandiseCatalogRepository;

    public RequestCreateController() {
        this(new AuthService(), new ImportRequestRepository(), new MerchandiseCatalogRepository());
    }

    public RequestCreateController(
            AuthService authService,
            ImportRequestRepository importRequestRepository,
            MerchandiseCatalogRepository merchandiseCatalogRepository
    ) {
        this.authService = authService;
        this.importRequestRepository = importRequestRepository;
        this.merchandiseCatalogRepository = merchandiseCatalogRepository;
    }

    /**
     * Tạo yêu cầu mới với một hoặc nhiều dòng mặt hàng.
     */
    public ImportRequestDto createImportRequest(List<CreateImportRequestLineInput> lines) {
        authService.requireRole(UserRole.SALES);
        AuthenticatedUser user = Session.requireCurrentUser();

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Danh sách mặt hàng không được rỗng.");
        }

        List<ImportRequestItem> items = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            items.add(toEntityItem(lines.get(i), i + 1));
        }

        String requestId = generateRequestId();
        ImportRequest request = new ImportRequest(requestId, user.username(), SALES_DEPARTMENT);
        for (ImportRequestItem item : items) {
            request.addItem(item);
        }
        importRequestRepository.save(request);

        return ImportRequestDto.from(
                importRequestRepository.findByIdWithItems(requestId).orElseThrow());
    }

    private ImportRequestItem toEntityItem(CreateImportRequestLineInput line, int lineNumber) {
        String code = normalizeMerchandiseCode(line.merchandiseCode(), lineNumber);
        if (!merchandiseCatalogRepository.existsByCode(code)) {
            throw new IllegalArgumentException(
                    "Mã hàng không tồn tại trong danh mục chuẩn: " + code);
        }
        int qty = line.quantityOrdered();
        if (qty <= 0) {
            throw new IllegalArgumentException("Số lượng phải là số nguyên dương (dòng " + lineNumber + ").");
        }
        String unit = requireNonBlank(line.unit(), "Đơn vị", lineNumber);
        LocalDate deliveryDate = line.desiredDeliveryDate();
        if (deliveryDate == null) {
            throw new IllegalArgumentException("Ngày nhận mong muốn không được để trống (dòng " + lineNumber + ").");
        }
        if (!deliveryDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày nhận mong muốn phải sau ngày hiện tại (dòng " + lineNumber + ").");
        }
        return new ImportRequestItem(code, qty, unit, deliveryDate);
    }

    private String generateRequestId() {
        String datePart = LocalDate.now().format(REQUEST_DATE);
        String prefix = "REQ-" + datePart + "-";
        int sequence = importRequestRepository.countByRequestIdPrefix(prefix) + 1;
        if (sequence > 999) {
            throw new IllegalStateException("Đã vượt giới hạn số yêu cầu trong ngày.");
        }
        return prefix + String.format("%03d", sequence);
    }

    private static String normalizeMerchandiseCode(String code, int lineNumber) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Mã hàng không được để trống (dòng " + lineNumber + ").");
        }
        String trimmed = code.trim();
        if (trimmed.length() > 64) {
            throw new IllegalArgumentException("Mã hàng tối đa 64 ký tự (dòng " + lineNumber + ").");
        }
        return trimmed;
    }

    private static String requireNonBlank(String value, String fieldLabel, int lineNumber) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldLabel + " không được để trống (dòng " + lineNumber + ").");
        }
        String trimmed = value.trim();
        if (trimmed.length() > 32) {
            throw new IllegalArgumentException(fieldLabel + " tối đa 32 ký tự (dòng " + lineNumber + ").");
        }
        return trimmed;
    }
}
