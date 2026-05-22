package com.orderingsystem.infrastructure.seed;

import com.orderingsystem.auth.PasswordHasher;
import com.orderingsystem.domain.auth.User;
import com.orderingsystem.domain.auth.UserRole;
import com.orderingsystem.domain.catalog.StandardMerchandise;
import com.orderingsystem.domain.site.Site;
import com.orderingsystem.infrastructure.repository.MerchandiseCatalogRepository;
import com.orderingsystem.infrastructure.repository.SiteRepository;
import com.orderingsystem.infrastructure.repository.UserRepository;

import java.util.List;

/**
 * Tạo dữ liệu demo khi DB còn trống (user + Site mẫu cho tài khoản Site).
 */
public final class DatabaseSeeder {

    public static final String DEMO_SITE_CODE = "S01";

    public static final List<String> DEMO_MERCHANDISE_CODES = List.of("P001", "P002", "P003");

    private static final String[][] DEMO_CATALOG = {
            {"P001", "Precision Bearing", "Industrial precision bearing"},
            {"P002", "Hydraulic Pump", "Standard hydraulic pump unit"},
            {"P003", "Control Valve", "Flow control valve assembly"},
    };

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final MerchandiseCatalogRepository merchandiseCatalogRepository;

    public DatabaseSeeder() {
        this(new UserRepository(), new SiteRepository(), new MerchandiseCatalogRepository());
    }

    public DatabaseSeeder(
            UserRepository userRepository,
            SiteRepository siteRepository,
            MerchandiseCatalogRepository merchandiseCatalogRepository
    ) {
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
        this.merchandiseCatalogRepository = merchandiseCatalogRepository;
    }

    /** Idempotent — gọi mỗi lần app khởi động. */
    public void seedDemoData() {
        seedStandardCatalog();
        if (userRepository.existsByUsername("sales")) {
            return;
        }
        seedDemoSite();
        seedDemoUsers();
    }

    private void seedStandardCatalog() {
        for (String[] row : DEMO_CATALOG) {
            String code = row[0];
            String name = row[1];
            String description = row[2];
            if (!merchandiseCatalogRepository.existsByCode(code)) {
                merchandiseCatalogRepository.save(new StandardMerchandise(code, name, description));
            } else {
                merchandiseCatalogRepository.updateInfo(code, name, description);
            }
        }
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
