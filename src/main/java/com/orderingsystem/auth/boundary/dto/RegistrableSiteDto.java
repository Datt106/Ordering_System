package com.orderingsystem.auth.boundary.dto;

/**
 * Site đã có trong master (UC004) và chưa có tài khoản đăng nhập — có thể tự đăng ký.
 */
public record RegistrableSiteDto(String siteCode, String siteName) {
}
