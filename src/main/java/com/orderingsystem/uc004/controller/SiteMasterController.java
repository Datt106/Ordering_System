package com.orderingsystem.uc004.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.uc004.boundary.dto.SiteDto;

import java.util.List;
import java.util.Optional;

/**
 * UC004 — Quản lý hồ sơ Site (Overseas): thêm/sửa/xóa master; không sửa ship/air (UC010).
 */
public class SiteMasterController {

    private final AuthService authService;
    private final SiteRepository siteRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SiteMerchandiseRepository siteMerchandiseRepository;

    public SiteMasterController() {
        this(new AuthService(), new SiteRepository(), new PurchaseOrderRepository(), new SiteMerchandiseRepository());
    }

    public SiteMasterController(
            AuthService authService,
            SiteRepository siteRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SiteMerchandiseRepository siteMerchandiseRepository
    ) {
        this.authService = authService;
        this.siteRepository = siteRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.siteMerchandiseRepository = siteMerchandiseRepository;
    }

    public SiteDto registerSite(String siteCode, String siteName, String otherInfo) {
        authService.requireRole(UserRole.OVERSEAS);
        String code = normalizeSiteCode(siteCode);
        String name = requireNonBlank(siteName, "Tên Site");
        if (siteRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã Site đã tồn tại: " + code);
        }
        Site site = new Site(code, name, blankToNull(otherInfo));
        siteRepository.save(site);
        return SiteDto.from(siteRepository.findByCode(code).orElseThrow());
    }

    public SiteDto updateMaster(String siteCode, String siteName, String otherInfo) {
        authService.requireRole(UserRole.OVERSEAS);
        String code = normalizeSiteCode(siteCode);
        String name = requireNonBlank(siteName, "Tên Site");
        if (!siteRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Site không tồn tại: " + code);
        }
        siteRepository.updateMaster(code, name, blankToNull(otherInfo));
        return SiteDto.from(siteRepository.findByCode(code).orElseThrow());
    }

    public void deleteSite(String siteCode) {
        authService.requireRole(UserRole.OVERSEAS);
        String code = normalizeSiteCode(siteCode);
        if (!siteRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Site không tồn tại: " + code);
        }
        if (purchaseOrderRepository.hasActiveOrdersForSite(code)) {
            throw new IllegalStateException("Không thể xóa Site đang có đơn hàng chưa hoàn tất.");
        }
        siteMerchandiseRepository.deleteAllBySiteCode(code);
        siteRepository.delete(code);
    }

    public Optional<SiteDto> getSite(String siteCode) {
        authService.requireRole(UserRole.OVERSEAS);
        return siteRepository.findByCode(normalizeSiteCode(siteCode)).map(SiteDto::from);
    }

    public List<SiteDto> listAllSites() {
        authService.requireRole(UserRole.OVERSEAS);
        return siteRepository.findAll().stream().map(SiteDto::from).toList();
    }

    public List<SiteDto> listActiveSites() {
        authService.requireRole(UserRole.OVERSEAS);
        return siteRepository.findAllActive().stream().map(SiteDto::from).toList();
    }

    private static String normalizeSiteCode(String siteCode) {
        if (siteCode == null || siteCode.isBlank()) {
            throw new IllegalArgumentException("Mã Site không được để trống.");
        }
        String trimmed = siteCode.trim();
        if (trimmed.length() > 32) {
            throw new IllegalArgumentException("Mã Site tối đa 32 ký tự.");
        }
        return trimmed;
    }

    private static String requireNonBlank(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldLabel + " không được để trống.");
        }
        return value.trim();
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
