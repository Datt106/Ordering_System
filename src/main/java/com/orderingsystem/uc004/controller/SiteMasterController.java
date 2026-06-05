package com.orderingsystem.uc004.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.infrastructure.database.UserRepository;
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
    private final UserRepository userRepository;

    public SiteMasterController() {
        this(
                new AuthService(),
                new SiteRepository(),
                new PurchaseOrderRepository(),
                new SiteMerchandiseRepository(),
                new UserRepository()
        );
    }

    public SiteMasterController(
            AuthService authService,
            SiteRepository siteRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SiteMerchandiseRepository siteMerchandiseRepository,
            UserRepository userRepository
    ) {
        this.authService = authService;
        this.siteRepository = siteRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.siteMerchandiseRepository = siteMerchandiseRepository;
        this.userRepository = userRepository;
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

    public SiteDto deactivateSite(String siteCode) {
        authService.requireRole(UserRole.OVERSEAS);
        String code = normalizeSiteCode(siteCode);
        Site site = siteRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Site không tồn tại: " + code));
        if (!site.isActive()) {
            throw new IllegalStateException("Site đã ở trạng thái ngừng hoạt động.");
        }
        if (purchaseOrderRepository.hasActiveOrdersForSite(code)) {
            throw new IllegalStateException("Không thể ngừng hoạt động khi Site còn đơn hàng chưa hoàn tất.");
        }
        siteRepository.setActive(code, false);
        return SiteDto.from(siteRepository.findByCode(code).orElseThrow());
    }

    public SiteDto activateSite(String siteCode) {
        authService.requireRole(UserRole.OVERSEAS);
        String code = normalizeSiteCode(siteCode);
        if (!siteRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Site không tồn tại: " + code);
        }
        siteRepository.setActive(code, true);
        return SiteDto.from(siteRepository.findByCode(code).orElseThrow());
    }

    /** Xóa hẳn hồ sơ Site (chỉ khi đã ngừng hoạt động, không còn đơn đang chạy). */
    public void deleteSite(String siteCode) {
        authService.requireRole(UserRole.OVERSEAS);
        String code = normalizeSiteCode(siteCode);
        Site site = siteRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Site không tồn tại: " + code));
        if (site.isActive()) {
            throw new IllegalStateException(
                    "Chỉ xóa Site đã ngừng hoạt động. Dùng \"Ngừng hoạt động\" trước khi xóa hẳn.");
        }
        if (purchaseOrderRepository.hasActiveOrdersForSite(code)) {
            throw new IllegalStateException("Không thể xóa Site đang có đơn hàng chưa hoàn tất.");
        }
        userRepository.deleteBySiteCode(code);
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

    /**
     * Tìm Site theo từ khóa (mã, tên, thông tin khác) và trạng thái hoạt động.
     *
     * @param keyword    null/blank = không lọc theo chữ
     * @param activeOnly true = hoạt động, false = ngừng, null = tất cả
     */
    public List<SiteDto> searchSites(String keyword, Boolean activeOnly) {
        authService.requireRole(UserRole.OVERSEAS);
        return siteRepository.search(keyword, activeOnly).stream().map(SiteDto::from).toList();
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
