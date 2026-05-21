package com.orderingsystem.uc009;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.domain.auth.UserRole;
import com.orderingsystem.domain.site.Site;
import com.orderingsystem.domain.site.SiteMerchandise;
import com.orderingsystem.infrastructure.repository.MerchandiseCatalogRepository;
import com.orderingsystem.infrastructure.repository.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.repository.SiteRepository;
import com.orderingsystem.uc009.dto.SiteMerchandiseDto;

import java.util.List;

/**
 * UC009 — Site quản lý danh sách mặt hàng kinh doanh (thêm/xóa, mã phải có trong danh mục chuẩn).
 */
public class SiteMerchandiseService {

    private final AuthService authService;
    private final SiteRepository siteRepository;
    private final SiteMerchandiseRepository siteMerchandiseRepository;
    private final MerchandiseCatalogRepository merchandiseCatalogRepository;

    public SiteMerchandiseService() {
        this(
                new AuthService(),
                new SiteRepository(),
                new SiteMerchandiseRepository(),
                new MerchandiseCatalogRepository()
        );
    }

    public SiteMerchandiseService(
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
                .map(entry -> SiteMerchandiseDto.from(entry, siteCode))
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
        return SiteMerchandiseDto.from(created, siteCode);
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
