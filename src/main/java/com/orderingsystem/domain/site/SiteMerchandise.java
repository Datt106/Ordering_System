package com.orderingsystem.domain.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "site_merchandise",
        uniqueConstraints = @UniqueConstraint(columnNames = {"site_code", "merchandise_code"})
)
public class SiteMerchandise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_code", nullable = false)
    private Site site;

    @Column(name = "merchandise_code", nullable = false, length = 64)
    private String merchandiseCode;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected SiteMerchandise() {
    }

    public SiteMerchandise(Site site, String merchandiseCode) {
        this.site = site;
        this.merchandiseCode = merchandiseCode;
    }

    public Long getId() {
        return id;
    }

    public Site getSite() {
        return site;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }
}
