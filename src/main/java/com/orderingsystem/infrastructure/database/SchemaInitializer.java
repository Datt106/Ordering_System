package com.orderingsystem.infrastructure.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Áp dụng DDL từ {@code schema.sql} khi khởi động ứng dụng. */
public final class SchemaInitializer {

    private SchemaInitializer() {
    }

    public static void apply() {
        try (Connection connection = DbManager.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : loadStatements()) {
                statement.execute(sql);
            }
        } catch (SQLException ex) {
            throw BaseRepository.wrapSQLException(ex);
        }
    }

    private static List<String> loadStatements() {
        try (InputStream input = SchemaInitializer.class.getResourceAsStream("/schema.sql")) {
            if (input == null) {
                throw new IllegalStateException("Không tìm thấy schema.sql trên classpath.");
            }
            String script = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                    .lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");
            List<String> statements = new ArrayList<>();
            for (String part : script.split(";")) {
                String sql = part.trim();
                if (!sql.isEmpty()) {
                    statements.add(sql);
                }
            }
            return statements;
        } catch (IOException ex) {
            throw new IllegalStateException("Không đọc được schema.sql", ex);
        }
    }
}
