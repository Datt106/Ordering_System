package com.orderingsystem.auth;

import com.orderingsystem.auth.boundary.dto.RegistrableSiteDto;
import com.orderingsystem.core.domain.User;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.infrastructure.database.UserRepository;

import java.util.List;

/**
 * Site tự đăng ký tài khoản sau khi Overseas đã thêm hồ sơ Site (UC004).
 * Sales / Overseas / Kho dùng tài khoản do hệ thống cấp sẵn (seed).
 */
public class SiteRegistrationService {

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    public SiteRegistrationService() {
        this(new UserRepository(), new SiteRepository());
    }

    public SiteRegistrationService(UserRepository userRepository, SiteRepository siteRepository) {
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
    }

    /** Site đang hoạt động, chưa có user SITE gắn mã đó. */
    public List<RegistrableSiteDto> listRegistrableSites() {
        return siteRepository.findAllActive().stream()
                .filter(site -> !userRepository.existsBySiteCode(site.getSiteCode()))
                .map(site -> new RegistrableSiteDto(site.getSiteCode(), site.getSiteName()))
                .toList();
    }

    public User registerSiteAccount(String siteCode, String username, String password) {
        String code = normalizeSiteCode(siteCode);
        String login = requireUsername(username);
        requirePassword(password);

        var site = siteRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mã Site không tồn tại. Liên hệ Bộ phận Đặt hàng quốc tế để được thêm vào danh sách đối tác."));
        if (!site.isActive()) {
            throw new IllegalStateException("Site đã ngừng hoạt động — không thể đăng ký tài khoản.");
        }
        if (userRepository.existsBySiteCode(code)) {
            throw new IllegalStateException("Site này đã có tài khoản. Hãy đăng nhập hoặc liên hệ quản trị.");
        }
        if (userRepository.existsByUsername(login)) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng — chọn tên khác.");
        }

        User user = new User(login, PasswordHasher.hash(password), UserRole.SITE, code);
        userRepository.save(user);
        return user;
    }

    private static String normalizeSiteCode(String siteCode) {
        if (siteCode == null || siteCode.isBlank()) {
            throw new IllegalArgumentException("Chọn hoặc nhập mã Site.");
        }
        String trimmed = siteCode.trim();
        if (trimmed.length() > 32) {
            throw new IllegalArgumentException("Mã Site tối đa 32 ký tự.");
        }
        return trimmed;
    }

    private static String requireUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        String trimmed = username.trim();
        if (trimmed.length() < MIN_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Tên đăng nhập tối thiểu " + MIN_USERNAME_LENGTH + " ký tự.");
        }
        if (trimmed.contains(" ")) {
            throw new IllegalArgumentException("Tên đăng nhập không được chứa khoảng trắng.");
        }
        return trimmed;
    }

    private static void requirePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Mật khẩu tối thiểu " + MIN_PASSWORD_LENGTH + " ký tự.");
        }
    }
}
