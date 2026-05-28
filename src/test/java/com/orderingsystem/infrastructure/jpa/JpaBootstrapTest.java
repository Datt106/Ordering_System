package com.orderingsystem.infrastructure.jpa;

import com.orderingsystem.core.domain.Site;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaBootstrapTest {

    @BeforeAll
    static void setUp() {
        JpaBootstrap.init();
    }

    @AfterAll
    static void tearDown() {
        JpaBootstrap.shutdown();
    }

    @Test
    void createsDatabaseAndPersistsSite() {
        EntityManager em = JpaBootstrap.openEntityManager();
        try {
            em.getTransaction().begin();
            Site existing = em.find(Site.class, "S-TEST");
            if (existing != null) {
                em.remove(existing);
            }
            Site site = new Site("S-TEST", "Test Import Site", "demo");
            em.persist(site);
            em.getTransaction().commit();

            Site loaded = em.find(Site.class, "S-TEST");
            assertEquals("Test Import Site", loaded.getSiteName());
        } finally {
            em.close();
        }

        assertTrue(JpaBootstrap.databasePath().toString().endsWith("ordering.db"));
    }
}
