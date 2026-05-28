package com.orderingsystem.infrastructure.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Singleton — quản lý kết nối JDBC (SQLite mặc định cho dev/demo).
 * <p>
 * Cấu hình qua system properties:
 * <ul>
 *   <li>{@code ordering.db.url} — mặc định {@code jdbc:sqlite:data/ordering.db}</li>
 *   <li>{@code ordering.db.user} / {@code ordering.db.password} — thường để trống với SQLite</li>
 * </ul>
 */
public final class DbManager {

    private static final String DEFAULT_URL = "jdbc:sqlite:data/ordering.db";
    private static final String PROP_URL = "ordering.db.url";
    private static final String PROP_USER = "ordering.db.user";
    private static final String PROP_PASSWORD = "ordering.db.password";

    private static volatile DbManager instance;
    private static volatile boolean initialized;

    private final String jdbcUrl;
    private final String username;
    private final String password;

    private DbManager(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    /** Khởi tạo singleton, tạo thư mục {@code data/} nếu dùng SQLite file. */
    public static void init() {
        if (initialized) {
            return;
        }
        synchronized (DbManager.class) {
            if (initialized) {
                return;
            }
            String url = System.getProperty(PROP_URL, DEFAULT_URL);
            ensureSqliteParentDirectory(url);
            loadDriver(url);
            String user = System.getProperty(PROP_USER, "");
            String password = System.getProperty(PROP_PASSWORD, "");
            instance = new DbManager(url, user, password);
            initialized = true;
        }
    }

    public static void init(String jdbcUrl, String username, String password) {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        synchronized (DbManager.class) {
            ensureSqliteParentDirectory(jdbcUrl);
            loadDriver(jdbcUrl);
            instance = new DbManager(jdbcUrl, username, password);
            initialized = true;
        }
    }

    public static void shutdown() {
        synchronized (DbManager.class) {
            instance = null;
            initialized = false;
        }
    }

    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        if (instance.username == null || instance.username.isBlank()) {
            return DriverManager.getConnection(instance.jdbcUrl);
        }
        Connection connection = DriverManager.getConnection(
                instance.jdbcUrl,
                instance.username,
                instance.password
        );
        connection.setAutoCommit(true);
        return connection;
    }

    public static String configuredUrl() {
        ensureInitialized();
        return instance.jdbcUrl;
    }

    public static Path databasePath() {
        String url = configuredUrl();
        if (url.startsWith("jdbc:sqlite:")) {
            String path = url.substring("jdbc:sqlite:".length());
            return Path.of(path).toAbsolutePath();
        }
        return Path.of(url);
    }

    private static void ensureInitialized() {
        if (!initialized || instance == null) {
            throw new IllegalStateException("Gọi DbManager.init() trước khi dùng database.");
        }
    }

    private static void ensureSqliteParentDirectory(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:sqlite:")) {
            return;
        }
        String file = jdbcUrl.substring("jdbc:sqlite:".length());
        if (file.isBlank() || ":memory:".equals(file)) {
            return;
        }
        try {
            Path parent = Path.of(file).getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Không tạo được thư mục chứa file SQLite.", ex);
        }
    }

    private static void loadDriver(String jdbcUrl) {
        try {
            if (jdbcUrl.startsWith("jdbc:sqlite:")) {
                Class.forName("org.sqlite.JDBC");
            } else if (jdbcUrl.startsWith("jdbc:postgresql:")) {
                Class.forName("org.postgresql.Driver");
            } else {
                throw new IllegalArgumentException("Chỉ hỗ trợ SQLite hoặc PostgreSQL URL: " + jdbcUrl);
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Thiếu JDBC driver cho URL: " + jdbcUrl, ex);
        }
    }
}
