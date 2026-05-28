package com.orderingsystem.core.domain;

public class User {

    private Long id;

    private String username;

    private String passwordHash;

    private UserRole role;

    /** Chỉ dùng khi role = SITE */
    private String siteCode;

    protected User() {
    }

    public User(String username, String passwordHash, UserRole role, String siteCode) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.siteCode = siteCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public String getSiteCode() {
        return siteCode;
    }
}
