package com.orderingsystem.auth;

import com.orderingsystem.core.domain.UserRole;

import java.util.Optional;

/**
 * Phiên đăng nhập trong bộ nhớ (desktop app, một process).
 */
public final class Session {

    private static AuthenticatedUser currentUser;

    private Session() {
    }

    public static void setCurrentUser(AuthenticatedUser user) {
        currentUser = user;
    }

    public static Optional<AuthenticatedUser> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static AuthenticatedUser requireCurrentUser() {
        if (currentUser == null) {
            throw new IllegalStateException("Chưa đăng nhập.");
        }
        return currentUser;
    }

    public static UserRole requireRole() {
        return requireCurrentUser().role();
    }

    public static String getUsername() {
        return requireCurrentUser().username();
    }

    public static Optional<String> getSiteCode() {
        return Optional.ofNullable(currentUser).map(AuthenticatedUser::siteCode);
    }

    public static void clear() {
        currentUser = null;
    }
}
