package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.User;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.core.domain.ImportRequest;
import com.orderingsystem.core.domain.ImportRequestItem;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTest {

    private static final SiteRepository siteRepository = new SiteRepository();
    private static final UserRepository userRepository = new UserRepository();
    private static final ImportRequestRepository importRequestRepository = new ImportRequestRepository();

    @BeforeAll
    static void setUp() {
        JpaBootstrap.init();
    }

    @AfterAll
    static void tearDown() {
        JpaBootstrap.shutdown();
    }

    @Test
    void siteRepository_masterAndShipping() {
        siteRepository.save(new Site("S-REPO", "Repo Site", "info"));

        siteRepository.updateMaster("S-REPO", "Repo Site Updated", "new info");
        siteRepository.updateShipping("S-REPO", 15, 5);

        Site site = siteRepository.findByCode("S-REPO").orElseThrow();
        assertEquals("Repo Site Updated", site.getSiteName());
        assertEquals(15, site.getShipDays());
        assertEquals(ShippingStatus.DA_KHAI_BAO, site.getShippingStatus());
        assertTrue(siteRepository.findWithShippingDeclared().stream()
                .anyMatch(s -> "S-REPO".equals(s.getSiteCode())));
    }

    @Test
    void userRepository_findByUsername() {
        if (userRepository.findByUsername("overseas1").isEmpty()) {
            userRepository.save(new User("overseas1", "hash", UserRole.OVERSEAS, null));
        }

        User user = userRepository.findByUsername("overseas1").orElseThrow();
        assertEquals(UserRole.OVERSEAS, user.getRole());
        assertTrue(userRepository.existsByUsername("overseas1"));
        assertFalse(userRepository.existsByUsername("unknown"));
    }

    @Test
    void importRequestRepository_saveWithItems() {
        ImportRequest request = new ImportRequest("REQ-REPO", "sales1", "Sales");
        request.addItem(new ImportRequestItem("P001", 100, "box", LocalDate.now().plusDays(30)));
        importRequestRepository.save(request);

        ImportRequest loaded = importRequestRepository.findByIdWithItems("REQ-REPO").orElseThrow();
        assertEquals(1, loaded.getItems().size());
        assertEquals("P001", loaded.getItems().getFirst().getMerchandiseCode());

        importRequestRepository.updateStatus("REQ-REPO", RequestStatus.DANG_XU_LY, "overseas1");
        assertEquals(RequestStatus.DANG_XU_LY,
                importRequestRepository.findById("REQ-REPO").orElseThrow().getStatus());
    }

    @Test
    void importRequestRepository_findByStatus() {
        importRequestRepository.save(new ImportRequest("REQ-STATUS", "sales1", "Sales"));

        List<ImportRequest> pending = importRequestRepository.findByStatus(RequestStatus.CHO_XU_LY);
        assertTrue(pending.stream().anyMatch(r -> "REQ-STATUS".equals(r.getRequestId())));
    }
}
