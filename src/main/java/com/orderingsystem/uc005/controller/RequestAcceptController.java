package com.orderingsystem.uc005.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;

import java.util.List;
import java.util.Optional;

/**
 * UC005 — Bộ phận Đặt hàng quốc tế xem và tiếp nhận yêu cầu nhập hàng (Chờ xử lý → Đang xử lý).
 */
public class RequestAcceptController {

    private final AuthService authService;
    private final ImportRequestRepository importRequestRepository;

    public RequestAcceptController() {
        this(new AuthService(), new ImportRequestRepository());
    }

    public RequestAcceptController(AuthService authService, ImportRequestRepository importRequestRepository) {
        this.authService = authService;
        this.importRequestRepository = importRequestRepository;
    }

    /** Danh sách yêu cầu Chờ xử lý, mới nhất trước (không kèm dòng mặt hàng). */
    public List<ImportRequestDto> listPendingRequests() {
        authService.requireRole(UserRole.OVERSEAS);
        return importRequestRepository.findByStatus(RequestStatus.CHO_XU_LY).stream()
                .map(ImportRequestDto::fromSummary)
                .toList();
    }

    /** Danh sách yêu cầu Đang xử lý — dùng cho UC006 truy vấn tồn kho. */
    public List<ImportRequestDto> listProcessingRequests() {
        authService.requireRole(UserRole.OVERSEAS);
        return importRequestRepository.findByStatus(RequestStatus.DANG_XU_LY).stream()
                .map(ImportRequestDto::fromSummary)
                .toList();
    }

    /** Chi tiết yêu cầu (kèm danh sách mặt hàng) — xem trước khi tiếp nhận. */
    public Optional<ImportRequestDto> getRequest(String requestId) {
        authService.requireRole(UserRole.OVERSEAS);
        return importRequestRepository.findByIdWithItems(requireRequestId(requestId))
                .map(ImportRequestDto::from);
    }

    /** Tiếp nhận xử lý: ghi nhận người tiếp nhận và thời điểm. */
    public ImportRequestDto acceptRequest(String requestId) {
        authService.requireRole(UserRole.OVERSEAS);
        AuthenticatedUser user = Session.requireCurrentUser();
        String id = requireRequestId(requestId);
        importRequestRepository.acceptForProcessing(id, user.username());
        return ImportRequestDto.from(importRequestRepository.findByIdWithItems(id).orElseThrow());
    }

    private static String requireRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Mã yêu cầu không được để trống.");
        }
        return requestId.trim();
    }
}
