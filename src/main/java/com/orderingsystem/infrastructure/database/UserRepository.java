package com.orderingsystem.infrastructure.database;

import com.orderingsystem.core.domain.User;
import com.orderingsystem.core.domain.UserRole;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository {

    public void save(User user) {
        if (user.getId() == null) {
            inJdbcTransaction(connection -> {
                executeUpdate(connection,
                        "INSERT INTO users (username, password_hash, role, site_code) VALUES (?, ?, ?, ?)",
                        bind(user.getUsername(), user.getPasswordHash(), user.getRole().name(), user.getSiteCode()));
                Long id = executeQuery(connection, "SELECT last_insert_rowid()", null, rs -> {
                    rs.next();
                    return rs.getLong(1);
                });
                user.setId(id);
                return null;
            });
            return;
        }
        inJdbcTransaction(connection -> executeUpdate(connection,
                "UPDATE users SET username = ?, password_hash = ?, role = ?, site_code = ? WHERE id = ?",
                bind(user.getUsername(), user.getPasswordHash(), user.getRole().name(), user.getSiteCode(), user.getId())));
    }

    public Optional<User> findByUsername(String username) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM users WHERE username = ? LIMIT 1",
                bind(username),
                rs -> rs.next() ? Optional.of(mapUser(rs)) : Optional.empty()));
    }

    public Optional<User> findById(Long id) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM users WHERE id = ?",
                bind(id),
                rs -> rs.next() ? Optional.of(mapUser(rs)) : Optional.empty()));
    }

    public List<User> findByRole(UserRole role) {
        return jdbcQuery(connection -> executeQuery(connection,
                "SELECT * FROM users WHERE role = ? ORDER BY username",
                bind(role.name()),
                rs -> {
                    List<User> users = new ArrayList<>();
                    while (rs.next()) {
                        users.add(mapUser(rs));
                    }
                    return users;
                }));
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("username"),
                rs.getString("password_hash"),
                JdbcSupport.getUserRole(rs, "role"),
                rs.getString("site_code"));
        user.setId(rs.getLong("id"));
        return user;
    }
}
