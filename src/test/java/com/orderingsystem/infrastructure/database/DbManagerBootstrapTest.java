package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.Site;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbManagerBootstrapTest {

    private static final SiteRepository siteRepository = new SiteRepository();

    @BeforeAll
    static void setUp() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
    }

    @AfterAll
    static void tearDown() {
        DbManager.shutdown();
    }

    @Test
    void createsDatabaseAndPersistsSite() {
        siteRepository.save(new Site("S-TEST", "Test Import Site", "demo"));
        Site loaded = siteRepository.findByCode("S-TEST").orElseThrow();
        assertEquals("Test Import Site", loaded.getSiteName());
        assertTrue(DbManager.databasePath().toString().endsWith("test-ordering.db"));
    }
}
