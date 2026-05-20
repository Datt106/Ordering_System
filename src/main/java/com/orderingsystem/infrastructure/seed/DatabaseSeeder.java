package com.orderingsystem.infrastructure.seed;

import com.orderingsystem.auth.PasswordHasher;
import com.orderingsystem.domain.auth.User;
import com.orderingsystem.domain.auth.UserRole;
import com.orderingsystem.domain.site.Site;
import com.orderingsystem.infrastructure.repository.SiteRepository;
import com.orderingsystem.infrastructure.repository.UserRepository;

/**
 * Tạo dữ liệu demo khi DB còn trống (user + Site mẫu cho tài khoản Site).
 */
public final class DatabaseSeeder {

    public static final String DEMO_SITE_CODE = "S01";

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    public DatabaseSeeder() {
        this(new UserRepository(), new SiteRepository());
    }

    public DatabaseSeeder(UserRepository userRepository, SiteRepository siteRepository) {
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
    }

    /** Idempotent — gọi mỗi lần app khởi động. */
    public void seedDemoData() {
        if (userRepository.existsByUsername("sales")) {
            return;
        }
        seedDemoSite();
        seedDemoUsers();
    }

    private void seedDemoSite() {
        if (!siteRepository.existsByCode(DEMO_SITE_CODE)) {
            siteRepository.save(new Site(DEMO_SITE_CODE, "Demo Import Site Tokyo", "Seed data"));
        }
    }

    private void seedDemoUsers() {
        userRepository.save(new User("sales", PasswordHasher.hash("sales123"), UserRole.SALES, null));
        userRepository.save(new User("overseas", PasswordHasher.hash("overseas123"), UserRole.OVERSEAS, null));
        userRepository.save(new User("site01", PasswordHasher.hash("site123"), UserRole.SITE, DEMO_SITE_CODE));
        userRepository.save(new User("warehouse", PasswordHasher.hash("wh123"), UserRole.WAREHOUSE, null));
    }
}
