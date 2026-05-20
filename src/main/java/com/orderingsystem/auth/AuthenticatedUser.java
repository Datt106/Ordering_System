package com.orderingsystem.auth;

import com.orderingsystem.domain.auth.User;
import com.orderingsystem.domain.auth.UserRole;

/**
 * Thông tin user sau đăng nhập — không chứa mật khẩu (an toàn cho Session / UI).
 */
public record AuthenticatedUser(
        Long id,
        String username,
        UserRole role,
        String siteCode
) {
    public static AuthenticatedUser from(User user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getSiteCode()
        );
    }
}
