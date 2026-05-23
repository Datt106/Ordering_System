package com.orderingsystem.uc011;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.domain.auth.UserRole;
import com.orderingsystem.domain.inventory.InventoryQuery;
import com.orderingsystem.infrastructure.repository.InventoryQueryRepository;
import com.orderingsystem.uc006.dto.InventoryQueryDto;

import java.time.Instant;
import java.util.List;

/**
 * UC011 — Site xác nhận tồn kho cho các truy vấn đang chờ (phản hồi UC006).
 */
public class SiteInventoryResponseService {

    private final AuthService authService;
    private final InventoryQueryRepository inventoryQueryRepository;

    public SiteInventoryResponseService() {
        this(new AuthService(), new InventoryQueryRepository());
    }

    public SiteInventoryResponseService(
            AuthService authService,
            InventoryQueryRepository inventoryQueryRepository
    ) {
        this.authService = authService;
        this.inventoryQueryRepository = inventoryQueryRepository;
    }

    /** Các dòng truy vấn chờ Site hiện tại phản hồi. */
    public List<InventoryQueryDto> listMyPendingQueries() {
        authService.requireRole(UserRole.SITE);
        String siteCode = requireSiteCode();
        return inventoryQueryRepository.findPendingBySiteCode(siteCode).stream()
                .map(InventoryQueryDto::from)
                .toList();
    }

    /**
     * Ghi nhận số lượng tồn kho cho một truy vấn thuộc Site đang đăng nhập.
     */
    public InventoryQueryDto respond(String queryId, int inStockQuantity) {
        authService.requireRole(UserRole.SITE);
        String siteCode = requireSiteCode();
        String id = requireQueryId(queryId);

        if (inStockQuantity < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
        }

        InventoryQuery query = inventoryQueryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Truy vấn không tồn tại: " + id));

        if (!siteCode.equals(query.getSiteCode())) {
            throw new SecurityException("Truy vấn không thuộc Site của bạn.");
        }
        if (!query.isPending()) {
            throw new IllegalStateException("Truy vấn đã được phản hồi trước đó.");
        }

        query.setInStockQuantity(inStockQuantity);
        query.setRespondedAt(Instant.now());
        inventoryQueryRepository.save(query);
        return InventoryQueryDto.from(query);
    }

    private String requireSiteCode() {
        AuthenticatedUser user = Session.requireCurrentUser();
        String siteCode = user.siteCode();
        if (siteCode == null || siteCode.isBlank()) {
            throw new IllegalStateException("Tài khoản Site chưa gắn mã Site.");
        }
        return siteCode.trim();
    }

    private static String requireQueryId(String queryId) {
        if (queryId == null || queryId.isBlank()) {
            throw new IllegalArgumentException("Mã truy vấn không được để trống.");
        }
        return queryId.trim();
    }
}
