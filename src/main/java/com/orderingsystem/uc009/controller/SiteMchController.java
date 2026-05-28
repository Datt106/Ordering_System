package com.orderingsystem.uc009.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.core.domain.SiteMerchandise;
import com.orderingsystem.infrastructure.database.MerchandiseCatalogRepository;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.uc009.boundary.dto.SiteMerchandiseDto;

import java.util.List;

/**
 * UC009 — Site quản lý danh sách mặt hàng kinh doanh (thêm/xóa, mã phải có trong danh mục chuẩn).
 */
public class SiteMchController {

    private final AuthService authService;
    private final SiteRepository siteRepository;
    private final SiteMerchandiseRepository siteMerchandiseRepository;
    private final MerchandiseCatalogRepository merchandiseCatalogRepository;

    public SiteMchController() {
        this(
                new AuthService(),
                new SiteRepository(),
                new SiteMerchandiseRepository(),
                new MerchandiseCatalogRepository()
        );
    }

    public SiteMchController(
            AuthService authService,
            SiteRepository siteRepository,
            SiteMerchandiseRepository siteMerchandiseRepository,
            MerchandiseCatalogRepository merchandiseCatalogRepository
    ) {
        this.authService = authService;
        this.siteRepository = siteRepository;
        this.siteMerchandiseRepository = siteMerchandiseRepository;
        this.merchandiseCatalogRepository = merchandiseCatalogRepository;
    }

    /** Danh sách mặt hàng Site đang đăng nhập đang kinh doanh. */
    public List<SiteMerchandiseDto> listMyMerchandise() {
        authService.requireRole(UserRole.SITE);
        String siteCode = requireSiteCode();
        return siteMerchandiseRepository.findBySiteCode(siteCode).stream()
                .map(entry -> toDto(entry, siteCode))
                .toList();
    }

    /** Thêm mặt hàng vào danh sách kinh doanh của Site hiện tại. */
    public SiteMerchandiseDto addMerchandise(String merchandiseCode) {
        authService.requireRole(UserRole.SITE);
        String siteCode = requireSiteCode();
        String code = normalizeMerchandiseCode(merchandiseCode);

        if (!siteRepository.existsByCode(siteCode)) {
            throw new IllegalArgumentException("Site không tồn tại: " + siteCode);
        }
        if (!merchandiseCatalogRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã hàng không tồn tại trong danh mục chuẩn: " + code);
        }
        if (siteMerchandiseRepository.findBySiteAndMerchandise(siteCode, code).isPresent()) {
            throw new IllegalArgumentException("Mặt hàng đã có trong danh sách kinh doanh: " + code);
        }

        Site site = siteRepository.findByCode(siteCode).orElseThrow();
        SiteMerchandise created = siteMerchandiseRepository.createLink(site, code);
        return toDto(created, siteCode);
    }

    /** Xóa mặt hàng khỏi danh sách kinh doanh của Site hiện tại. */
    public void removeMerchandise(String merchandiseCode) {
        authService.requireRole(UserRole.SITE);
        String siteCode = requireSiteCode();
        String code = normalizeMerchandiseCode(merchandiseCode);

        SiteMerchandise entry = siteMerchandiseRepository.findBySiteAndMerchandise(siteCode, code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mặt hàng không có trong danh sách kinh doanh: " + code));
        siteMerchandiseRepository.deleteById(entry.getId());
    }

    private String requireSiteCode() {
        AuthenticatedUser user = Session.requireCurrentUser();
        String siteCode = user.siteCode();
        if (siteCode == null || siteCode.isBlank()) {
            throw new IllegalStateException("Tài khoản Site chưa gắn mã Site.");
        }
        return siteCode.trim();
    }

    private SiteMerchandiseDto toDto(SiteMerchandise entry, String siteCode) {
        return merchandiseCatalogRepository.findByCode(entry.getMerchandiseCode())
                .map(catalog -> SiteMerchandiseDto.from(
                        entry,
                        siteCode,
                        catalog.getMerchandiseName(),
                        catalog.getDescription()))
                .orElseGet(() -> SiteMerchandiseDto.from(
                        entry, siteCode, entry.getMerchandiseCode(), null));
    }

    private static String normalizeMerchandiseCode(String merchandiseCode) {
        if (merchandiseCode == null || merchandiseCode.isBlank()) {
            throw new IllegalArgumentException("Mã hàng không được để trống.");
        }
        String trimmed = merchandiseCode.trim();
        if (trimmed.length() > 64) {
            throw new IllegalArgumentException("Mã hàng tối đa 64 ký tự.");
        }
        return trimmed;
    }
}
