package com.orderingsystem.auth;

import com.orderingsystem.core.domain.UserRole;
import com.orderingsystem.infrastructure.database.DbManager;
import com.orderingsystem.infrastructure.database.SchemaInitializer;
import com.orderingsystem.infrastructure.seed.DatabaseSeeder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private static final AuthService authService = new AuthService();

    @BeforeAll
    static void setUp() {
        DbManager.init("jdbc:sqlite:data/test-ordering.db", "", "");
        SchemaInitializer.apply();
        new DatabaseSeeder().seedDemoData();
    }

    @AfterEach
    void tearDownSession() {
        authService.logout();
    }

    @AfterAll
    static void shutDown() {
        DbManager.shutdown();
    }

    @Test
    void login_success_setsSession() {
        Optional<AuthenticatedUser> result = authService.login("sales", "sales123");

        assertTrue(result.isPresent());
        assertEquals("sales", result.get().username());
        assertEquals(UserRole.SALES, result.get().role());
        assertTrue(authService.isLoggedIn());
        assertEquals(UserRole.SALES, Session.requireRole());
    }

    @Test
    void login_siteUser_hasSiteCode() {
        Optional<AuthenticatedUser> result = authService.login("site01", "site123");

        assertTrue(result.isPresent());
        assertEquals(UserRole.SITE, result.get().role());
        assertEquals(DatabaseSeeder.DEMO_SITE_CODE, result.get().siteCode());
    }

    @Test
    void login_wrongPassword_fails() {
        assertTrue(authService.login("sales", "wrong").isEmpty());
        assertFalse(authService.isLoggedIn());
    }

    @Test
    void login_unknownUser_fails() {
        assertTrue(authService.login("nobody", "x").isEmpty());
    }

    @Test
    void logout_clearsSession() {
        authService.login("overseas", "overseas123");
        authService.logout();
        assertFalse(authService.isLoggedIn());
    }

    @Test
    void requireRole_throwsWhenWrongRole() {
        authService.login("sales", "sales123");
        assertThrows(SecurityException.class, () -> authService.requireRole(UserRole.OVERSEAS));
    }
}
