package com.orderingsystem.uc010.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.uc004.boundary.dto.SiteDto;

/**
 * UC010 — Site cập nhật số ngày vận chuyển (tàu / hàng không) cho chính Site của user.
 */
public class SiteShipController {

    private final AuthService authService;
    private final SiteRepository siteRepository;

    public SiteShipController() {
        this(new AuthService(), new SiteRepository());
    }

    public SiteShipController(AuthService authService, SiteRepository siteRepository) {
        this.authService = authService;
        this.siteRepository = siteRepository;
    }

    /**
     * Cập nhật lead time cho Site đang đăng nhập (role SITE, siteCode trên user).
     */
    public SiteDto updateMyShipping(int shipDays, int airDays) {
        authService.requireRole(UserRole.SITE);
        AuthenticatedUser user = Session.requireCurrentUser();
        String siteCode = user.siteCode();
        if (siteCode == null || siteCode.isBlank()) {
            throw new IllegalStateException("Tài khoản Site chưa gắn mã Site.");
        }
        validatePositiveDays(shipDays, airDays);
        if (!siteRepository.existsByCode(siteCode)) {
            throw new IllegalArgumentException("Site không tồn tại: " + siteCode);
        }
        siteRepository.updateShipping(siteCode, shipDays, airDays);
        return SiteDto.from(siteRepository.findByCode(siteCode).orElseThrow());
    }

    /** Site xem thông tin vận chuyển + hồ sơ read-only của mình. */
    public SiteDto getMySite() {
        authService.requireRole(UserRole.SITE);
        String siteCode = Session.requireCurrentUser().siteCode();
        if (siteCode == null || siteCode.isBlank()) {
            throw new IllegalStateException("Tài khoản Site chưa gắn mã Site.");
        }
        return SiteDto.from(siteRepository.findByCode(siteCode)
                .orElseThrow(() -> new IllegalArgumentException("Site không tồn tại: " + siteCode)));
    }

    private static void validatePositiveDays(int shipDays, int airDays) {
        if (shipDays <= 0 || airDays <= 0) {
            throw new IllegalArgumentException("Số ngày vận chuyển phải là số nguyên dương.");
        }
    }
}
