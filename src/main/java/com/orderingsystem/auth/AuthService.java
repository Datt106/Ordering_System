package com.orderingsystem.auth;

import com.orderingsystem.domain.auth.User;
import com.orderingsystem.domain.auth.UserRole;
import com.orderingsystem.infrastructure.repository.UserRepository;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService() {
        this(new UserRepository());
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Xác thực và ghi nhận phiên đăng nhập.
     *
     * @return user đã xác thực (không có mật khẩu); empty nếu sai username/password
     */
    public Optional<AuthenticatedUser> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            return Optional.empty();
        }
        AuthenticatedUser authenticated = AuthenticatedUser.from(user);
        Session.setCurrentUser(authenticated);
        return Optional.of(authenticated);
    }

    public void logout() {
        Session.clear();
    }

    public Optional<AuthenticatedUser> getCurrentUser() {
        return Session.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return Session.isLoggedIn();
    }

    public boolean hasRole(UserRole role) {
        return Session.getCurrentUser()
                .map(u -> u.role() == role)
                .orElse(false);
    }

    public void requireRole(UserRole role) {
        AuthenticatedUser user = Session.requireCurrentUser();
        if (user.role() != role) {
            throw new SecurityException("Không có quyền: cần " + role + ", hiện tại " + user.role());
        }
    }
}
