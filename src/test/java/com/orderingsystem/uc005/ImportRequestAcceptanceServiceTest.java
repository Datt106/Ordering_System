package com.orderingsystem.uc005;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.domain.request.RequestStatus;
import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc002.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.dto.ImportRequestDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportRequestAcceptanceServiceTest {

    private static final AuthService authService = new AuthService();
    private static final ImportRequestService importRequestService = new ImportRequestService();
    private static final ImportRequestAcceptanceService acceptanceService =
            new ImportRequestAcceptanceService();

    @BeforeAll
    static void setUp() {
        JpaBootstrap.init();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void logout() {
        authService.logout();
    }

    @AfterAll
    static void shutDown() {
        JpaBootstrap.shutdown();
    }

    @Test
    void listAndAccept_requiresOverseas() {
        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 20, "box", LocalDate.now().plusDays(10))));
        authService.logout();

        authService.login("sales", "sales123");
        assertThrows(SecurityException.class, acceptanceService::listPendingRequests);
        authService.logout();

        authService.login("overseas", "overseas123");
        assertTrue(acceptanceService.listPendingRequests().stream()
                .anyMatch(r -> r.requestId().equals(created.requestId())));

        ImportRequestDto detail = acceptanceService.getRequest(created.requestId()).orElseThrow();
        assertEquals(1, detail.items().size());
        assertEquals("P001", detail.items().getFirst().merchandiseCode());

        ImportRequestDto accepted = acceptanceService.acceptRequest(created.requestId());
        assertEquals(RequestStatus.DANG_XU_LY, accepted.status());
        assertEquals("overseas", accepted.processedBy());
        assertTrue(accepted.processedAt() != null);

        assertThrows(IllegalStateException.class, () ->
                acceptanceService.acceptRequest(created.requestId()));
    }
}
