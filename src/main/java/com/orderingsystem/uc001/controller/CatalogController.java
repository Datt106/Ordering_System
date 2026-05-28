package com.orderingsystem.uc001.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.auth.AuthenticatedUser;
import com.orderingsystem.auth.Session;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.StandardMerchandise;
import com.orderingsystem.infrastructure.database.MerchandiseCatalogRepository;
import com.orderingsystem.uc001.boundary.dto.StandardMerchandiseDto;

import java.util.List;
import java.util.Set;

/**
 * Quản lý danh mục mặt hàng chuẩn — Sales duy trì mã + tên/mô tả;
 * Site / Overseas tra cứu (read-only) khi chọn mặt hàng kinh doanh hoặc xử lý yêu cầu.
 */
public class CatalogController {

    private static final Set<UserRole> CATALOG_READERS = Set.of(
            UserRole.SALES, UserRole.SITE, UserRole.OVERSEAS);

    private final AuthService authService;
    private final MerchandiseCatalogRepository merchandiseCatalogRepository;

    public CatalogController() {
        this(new AuthService(), new MerchandiseCatalogRepository());
    }

    public CatalogController(
            AuthService authService,
            MerchandiseCatalogRepository merchandiseCatalogRepository
    ) {
        this.authService = authService;
        this.merchandiseCatalogRepository = merchandiseCatalogRepository;
    }

    /** Sales — quản lý đầy đủ. */
    public List<StandardMerchandiseDto> listAll() {
        authService.requireRole(UserRole.SALES);
        return listCatalogEntries();
    }

    /**
     * Site / Overseas / Sales — xem danh mục chuẩn (mã + tên + mô tả) để chọn mặt hàng.
     */
    public List<StandardMerchandiseDto> listCatalogForBrowsing() {
        requireCatalogReader();
        return listCatalogEntries();
    }

    public StandardMerchandiseDto registerMerchandise(
            String merchandiseCode,
            String merchandiseName,
            String description
    ) {
        authService.requireRole(UserRole.SALES);
        String code = normalizeMerchandiseCode(merchandiseCode);
        String name = requireNonBlank(merchandiseName, "Tên mặt hàng");
        if (merchandiseCatalogRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã hàng đã tồn tại trong danh mục chuẩn: " + code);
        }
        merchandiseCatalogRepository.save(new StandardMerchandise(code, name, blankToNull(description)));
        return StandardMerchandiseDto.from(
                merchandiseCatalogRepository.findByCode(code).orElseThrow());
    }

    public StandardMerchandiseDto updateMerchandise(
            String merchandiseCode,
            String merchandiseName,
            String description
    ) {
        authService.requireRole(UserRole.SALES);
        String code = normalizeMerchandiseCode(merchandiseCode);
        String name = requireNonBlank(merchandiseName, "Tên mặt hàng");
        merchandiseCatalogRepository.updateInfo(code, name, blankToNull(description));
        return StandardMerchandiseDto.from(merchandiseCatalogRepository.findByCode(code).orElseThrow());
    }

    public void deleteMerchandise(String merchandiseCode) {
        authService.requireRole(UserRole.SALES);
        String code = normalizeMerchandiseCode(merchandiseCode);
        if (!merchandiseCatalogRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã hàng không tồn tại trong danh mục chuẩn: " + code);
        }
        if (merchandiseCatalogRepository.isReferenced(code)) {
            throw new IllegalStateException(
                    "Không thể xóa mã hàng đang được dùng trong yêu cầu, Site hoặc đơn hàng: " + code);
        }
        merchandiseCatalogRepository.deleteByCode(code);
    }

    private List<StandardMerchandiseDto> listCatalogEntries() {
        return merchandiseCatalogRepository.findAllOrderByCode().stream()
                .map(StandardMerchandiseDto::from)
                .toList();
    }

    private void requireCatalogReader() {
        AuthenticatedUser user = Session.requireCurrentUser();
        if (!CATALOG_READERS.contains(user.role())) {
            throw new SecurityException("Không có quyền xem danh mục mặt hàng chuẩn.");
        }
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
