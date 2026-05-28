package com.orderingsystem.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Singleton — quản lý kết nối JDBC tới PostgreSQL (không dùng DataSource pool).
 * <p>
 * Cấu hình qua system properties (ưu tiên) hoặc giá trị mặc định:
 * <ul>
 *   <li>{@code ordering.db.url} — jdbc:postgresql://localhost:5432/ordering_system</li>
 *   <li>{@code ordering.db.user} — ordering</li>
 *   <li>{@code ordering.db.password} — ordering</li>
 * </ul>
 * Gọi {@link #init()} một lần khi khởi động app; {@link #shutdown()} khi thoát.
 */
public final class DbManager {

    private static final String DEFAULT_URL =
            "jdbc:postgresql://localhost:5432/ordering_system";
    private static final String DEFAULT_USER = "ordering";
    private static final String DEFAULT_PASSWORD = "ordering";

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

    /** Khởi tạo singleton và nạp driver PostgreSQL. */
    public static void init() {
        if (initialized) {
            return;
        }
        synchronized (DbManager.class) {
            if (initialized) {
                return;
            }
            loadDriver();
            String url = System.getProperty(PROP_URL, DEFAULT_URL);
            String user = System.getProperty(PROP_USER, DEFAULT_USER);
            String password = System.getProperty(PROP_PASSWORD, DEFAULT_PASSWORD);
            instance = new DbManager(url, user, password);
            initialized = true;
        }
    }

    public static void init(String jdbcUrl, String username, String password) {
        Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        synchronized (DbManager.class) {
            loadDriver();
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

    /**
     * Mở một {@link Connection} mới (caller phải {@link Connection#close()}).
     * Mặc định {@code autoCommit = true}; {@link BaseRepository} tắt khi cần transaction.
     */
    public static Connection getConnection() throws SQLException {
        ensureInitialized();
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

    private static void ensureInitialized() {
        if (!initialized || instance == null) {
            throw new IllegalStateException("Gọi DbManager.init() trước khi dùng database.");
        }
    }

    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Thiếu PostgreSQL JDBC driver. Thêm dependency org.postgresql:postgresql vào pom.xml.",
                    e
            );
        }
    }
}
