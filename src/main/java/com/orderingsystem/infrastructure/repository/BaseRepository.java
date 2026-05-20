package com.orderingsystem.infrastructure.repository;

import com.orderingsystem.infrastructure.jpa.JpaBootstrap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.function.Consumer;
import java.util.function.Function;

abstract class BaseRepository {

    protected void inTransaction(Consumer<EntityManager> work) {
        EntityManager em = JpaBootstrap.openEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            work.accept(em);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    protected <T> T inTransaction(Function<EntityManager, T> work) {
        EntityManager em = JpaBootstrap.openEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    protected <T> T query(Function<EntityManager, T> work) {
        EntityManager em = JpaBootstrap.openEntityManager();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }
}
