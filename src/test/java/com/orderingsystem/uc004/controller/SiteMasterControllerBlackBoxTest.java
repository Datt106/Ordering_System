package com.orderingsystem.uc004.controller;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.ShippingStatus;
import com.orderingsystem.core.domain.Site;
import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.PurchaseOrderRepository;
import com.orderingsystem.infrastructure.database.SiteMerchandiseRepository;
import com.orderingsystem.infrastructure.database.SiteRepository;
import com.orderingsystem.infrastructure.database.UserRepository;
import com.orderingsystem.uc004.boundary.dto.SiteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử hộp đen cho UC004 – SiteMasterController.
 *
 * Kỹ thuật áp dụng:
 * - Chọn giá trị đại diện cho các lớp tương đương hợp lệ / không hợp lệ
 * - Phân tích giá trị biên cho mã Site (rỗng, > 32 ký tự)
 * - Kiểm thử theo đầu vào/đầu ra quan sát được, không phụ thuộc cấu trúc nội bộ
 *
 * Full class name: com.orderingsystem.uc004.controller.SiteMasterControllerBlackBoxTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC004 – SiteMasterController – Black Box")
class SiteMasterControllerBlackBoxTest {

    @Mock
    private AuthService authService;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SiteMerchandiseRepository siteMerchandiseRepository;

    @Mock
    private UserRepository userRepository;

    private SiteMasterController controller;

    @BeforeEach
    void setUp() {
        controller = new SiteMasterController(authService, siteRepository, purchaseOrderRepository, siteMerchandiseRepository, userRepository);
        doNothing().when(authService).requireRole(UserRole.OVERSEAS);
    }

    @Test
    void registerSite_withValidData_returnsCreatedSite() {
        Site site = new Site("OVS-001", "Overseas Site", "note");
        site.setShippingStatus(ShippingStatus.CHUA_KHAI_BAO);
        site.setShippingUpdatedAt(Instant.parse("2026-06-04T10:15:30Z"));

        when(siteRepository.existsByCode("OVS-001")).thenReturn(false);
        when(siteRepository.findByCode("OVS-001")).thenReturn(Optional.of(site));

        SiteDto dto = controller.registerSite("OVS-001", "Overseas Site", "note");

        assertEquals("OVS-001", dto.siteCode());
        assertEquals("Overseas Site", dto.siteName());
        assertEquals("note", dto.otherInfo());
        assertTrue(dto.active());
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    void registerSite_whenCodeAlreadyExists_throwsException() {
        when(siteRepository.existsByCode("OVS-001")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.registerSite("OVS-001", "Overseas Site", "note"));

        assertTrue(ex.getMessage().contains("đã tồn tại"));
        verify(siteRepository, never()).save(any());
    }

    @Test
    void registerSite_withBlankCode_rejectsInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.registerSite("   ", "Overseas Site", "note"));

        assertTrue(ex.getMessage().contains("không được để trống"));
        verifyNoInteractions(siteRepository);
    }

    @Test
    void registerSite_withTooLongCode_rejectsInput() {
        String longCode = "A".repeat(33);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.registerSite(longCode, "Overseas Site", "note"));

        assertTrue(ex.getMessage().contains("32 ký tự"));
        verifyNoInteractions(siteRepository);
    }

    @Test
    void listActiveSites_returnsMappedDtos() {
        Site site = new Site("OVS-002", "Active Site", null);
        site.setShippingStatus(ShippingStatus.DA_KHAI_BAO);
        when(siteRepository.findAllActive()).thenReturn(List.of(site));

        List<SiteDto> result = controller.listActiveSites();

        assertEquals(1, result.size());
        assertEquals("OVS-002", result.get(0).siteCode());
        assertEquals("Active Site", result.get(0).siteName());
        verify(siteRepository).findAllActive();
    }

    @Test
    void getSite_whenExists_returnsOptionalDto() {
        Site site = new Site("OVS-003", "Lookup Site", "info");
        when(siteRepository.findByCode(eq("OVS-003"))).thenReturn(Optional.of(site));

        Optional<SiteDto> result = controller.getSite("OVS-003");

        assertTrue(result.isPresent());
        assertEquals("OVS-003", result.orElseThrow().siteCode());
    }
}
