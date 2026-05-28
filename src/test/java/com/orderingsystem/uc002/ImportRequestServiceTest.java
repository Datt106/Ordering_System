package com.orderingsystem.uc002;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportRequestServiceTest {

    private static final AuthService authService = new AuthService();
    private static final ImportRequestService importRequestService = new ImportRequestService();

    @BeforeAll
    static void setUp() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void logout() {
        authService.logout();
    }

    @AfterAll
    static void shutDown() {
        DbManager.shutdown();
    }

    @Test
    void createImportRequest_requiresSales() {
        authService.login("overseas", "overseas123");
        assertThrows(SecurityException.class, () ->
                importRequestService.createImportRequest(List.of(sampleLine("P001"))));
    }

    @Test
    void createImportRequest_success() {
        authService.login("sales", "sales123");

        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P001", 100, "box", LocalDate.now().plusDays(14)),
                new CreateImportRequestLineInput("P002", 50, "pcs", LocalDate.now().plusDays(30))
        ));

        assertTrue(created.requestId().matches("REQ-\\d{8}-\\d{3}"));
        assertEquals("sales", created.createdBy());
        assertEquals("Sales", created.department());
        assertEquals(RequestStatus.CHO_XU_LY, created.status());
        assertEquals(2, created.items().size());
        assertEquals("P001", created.items().getFirst().merchandiseCode());
    }

    @Test
    void createImportRequest_emptyLines_fails() {
        authService.login("sales", "sales123");
        assertThrows(IllegalArgumentException.class, () ->
                importRequestService.createImportRequest(List.of()));
    }

    @Test
    void createImportRequest_unknownMerchandise_fails() {
        authService.login("sales", "sales123");
        assertThrows(IllegalArgumentException.class, () ->
                importRequestService.createImportRequest(List.of(sampleLine("UNKNOWN"))));
    }

    @Test
    void createImportRequest_invalidQuantity_fails() {
        authService.login("sales", "sales123");
        assertThrows(IllegalArgumentException.class, () ->
                importRequestService.createImportRequest(List.of(
                        new CreateImportRequestLineInput("P001", 0, "box", LocalDate.now().plusDays(7)))));
    }

    @Test
    void createImportRequest_pastDeliveryDate_fails() {
        authService.login("sales", "sales123");
        assertThrows(IllegalArgumentException.class, () ->
                importRequestService.createImportRequest(List.of(
                        new CreateImportRequestLineInput("P001", 10, "box", LocalDate.now()))));
    }

    private static CreateImportRequestLineInput sampleLine(String code) {
        return new CreateImportRequestLineInput(code, 10, "box", LocalDate.now().plusDays(7));
    }
}
