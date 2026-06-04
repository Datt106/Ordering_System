package com.orderingsystem.uc004.controller;

import com.orderingsystem.auth.AuthService;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Kiểm thử hộp trắng cho UC004 – SiteMasterController.
 *
 * Kỹ thuật áp dụng:
 * - Phân tích đường đi điều khiển
 * - Độ đo C1 cho các nhánh if/else trong registerSite(), updateMaster(), deactivateSite(), deleteSite()
 *
 * Full class name: com.orderingsystem.uc004.controller.SiteMasterControllerWhiteBoxTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UC004 – SiteMasterController – White Box")
class SiteMasterControllerWhiteBoxTest {

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
    void registerSite_executesInsertBranch() {
        Site saved = new Site("OVS-101", "New Site", "note");
        when(siteRepository.existsByCode("OVS-101")).thenReturn(false);
        when(siteRepository.findByCode("OVS-101")).thenReturn(Optional.of(saved));

        SiteDto dto = controller.registerSite("OVS-101", "New Site", "note");

        assertEquals("OVS-101", dto.siteCode());
        verify(siteRepository).save(any(Site.class));
        verify(siteRepository).findByCode("OVS-101");
    }

    @Test
    void updateMaster_whenSiteExists_executesUpdateBranch() {
        Site updated = new Site("OVS-102", "Renamed", "other");
        when(siteRepository.existsByCode("OVS-102")).thenReturn(true);
        when(siteRepository.findByCode("OVS-102")).thenReturn(Optional.of(updated));

        SiteDto dto = controller.updateMaster("OVS-102", "Renamed", "other");

        assertEquals("Renamed", dto.siteName());
        verify(siteRepository).updateMaster("OVS-102", "Renamed", "other");
    }

    @Test
    void updateMaster_whenSiteDoesNotExist_throwsException() {
        when(siteRepository.existsByCode("OVS-103")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.updateMaster("OVS-103", "Renamed", "other"));

        assertTrue(ex.getMessage().contains("không tồn tại"));
        verify(siteRepository, never()).updateMaster(any(), any(), any());
    }

    @Test
    void deactivateSite_whenHasActiveOrders_blocksDeactivation() {
        Site site = new Site("OVS-104", "Active Site", null);
        site.setActive(true);
        when(siteRepository.findByCode("OVS-104")).thenReturn(Optional.of(site));
        when(purchaseOrderRepository.hasActiveOrdersForSite("OVS-104")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> controller.deactivateSite("OVS-104"));

        assertTrue(ex.getMessage().contains("đơn hàng chưa hoàn tất"));
        verify(siteRepository, never()).setActive(eq("OVS-104"), eq(false));
    }

    @Test
    void deactivateSite_whenNoActiveOrders_executesDeactivateBranch() {
        Site site = new Site("OVS-105", "Active Site", null);
        site.setActive(true);
        when(siteRepository.findByCode("OVS-105")).thenReturn(Optional.of(site));
        when(purchaseOrderRepository.hasActiveOrdersForSite("OVS-105")).thenReturn(false);
        when(siteRepository.findByCode("OVS-105")).thenReturn(Optional.of(new Site("OVS-105", "Active Site", null)));

        SiteDto dto = controller.deactivateSite("OVS-105");

        verify(siteRepository).setActive("OVS-105", false);
        assertEquals("OVS-105", dto.siteCode());
    }

    @Test
    void deleteSite_whenInactiveAndNoOrders_executesFullDeletePath() {
        Site site = new Site("OVS-106", "Inactive Site", null);
        site.setActive(false);
        when(siteRepository.findByCode("OVS-106")).thenReturn(Optional.of(site));
        when(purchaseOrderRepository.hasActiveOrdersForSite("OVS-106")).thenReturn(false);

        controller.deleteSite("OVS-106");

        verify(userRepository).deleteBySiteCode("OVS-106");
        verify(siteMerchandiseRepository).deleteAllBySiteCode("OVS-106");
        verify(siteRepository).delete("OVS-106");
    }

    @Test
    void deleteSite_whenActive_throwsBeforeCleanup() {
        Site site = new Site("OVS-107", "Active Site", null);
        site.setActive(true);
        when(siteRepository.findByCode("OVS-107")).thenReturn(Optional.of(site));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> controller.deleteSite("OVS-107"));

        assertTrue(ex.getMessage().contains("ngừng hoạt động"));
        verifyNoInteractions(userRepository, siteMerchandiseRepository);
    }
}
