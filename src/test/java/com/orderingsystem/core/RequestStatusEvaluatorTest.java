package com.orderingsystem.core;

import com.orderingsystem.auth.AuthService;
import com.orderingsystem.core.domain.RequestStatus;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.ImportRequestRepository;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import com.orderingsystem.uc002.ImportRequestService;
import com.orderingsystem.uc002.boundary.dto.CreateImportRequestLineInput;
import com.orderingsystem.uc002.boundary.dto.ImportRequestDto;
import com.orderingsystem.uc005.ImportRequestAcceptanceService;
import com.orderingsystem.uc006.InventoryQueryService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestStatusEvaluatorTest {

    private static final AuthService authService = new AuthService();
    private static final ImportRequestService importRequestService = new ImportRequestService();
    private static final ImportRequestAcceptanceService acceptanceService = new ImportRequestAcceptanceService();
    private static final InventoryQueryService inventoryQueryService = new InventoryQueryService();
    private static final ImportRequestRepository importRequestRepository = new ImportRequestRepository();

    @BeforeAll
    static void initDatabase() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void logout() {
        authService.logout();
    }

    @AfterAll
    static void shutdown() {
        DbManager.shutdown();
    }

    @Test
    void dispatchWithNoEligibleSite_marksRequestAsLoi() {
        authService.login("sales", "sales123");
        ImportRequestDto created = importRequestService.createImportRequest(List.of(
                new CreateImportRequestLineInput("P002", 10, "pcs", LocalDate.now().plusDays(14))
        ));
        authService.logout();

        authService.login("overseas", "overseas123");
        acceptanceService.acceptRequest(created.requestId());
        inventoryQueryService.dispatchInventoryQueries(created.requestId());

        assertEquals(
                RequestStatus.LOI,
                importRequestRepository.findById(created.requestId()).orElseThrow().getStatus()
        );
    }
}
