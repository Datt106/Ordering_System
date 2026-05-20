package com.orderingsystem.infrastructure.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Khởi tạo Hibernate / SQLite một lần cho toàn ứng dụng desktop.
 */
public final class JpaBootstrap {

    private static final String PU_NAME = "orderingPU";
    private static EntityManagerFactory entityManagerFactory;

    private JpaBootstrap() {
    }

    public static void init() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            return;
        }
        try {
            Files.createDirectories(Path.of("data"));
        } catch (IOException e) {
            throw new IllegalStateException("Không tạo được thư mục data/", e);
        }
        entityManagerFactory = Persistence.createEntityManagerFactory(PU_NAME);
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        ensureInitialized();
        return entityManagerFactory;
    }

    public static EntityManager openEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        entityManagerFactory = null;
    }

    public static Path databasePath() {
        return Path.of("data", "ordering.db").toAbsolutePath();
    }

    private static void ensureInitialized() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            throw new IllegalStateException("Gọi JpaBootstrap.init() trước khi dùng database.");
        }
    }
}
