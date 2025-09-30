package com.back.global.rq;

import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserService;
import com.back.global.security.SecurityUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final UserService userService;

    @Value("${custom.cookie.secure}")
    private boolean cookieSecure;

    @Value("${custom.cookie.same}")
    private String cookieSameSite;

    @Value("${custom.site.cookieDomain}")
    private String cookieDomain;


    public User getActor() {
        return Optional.ofNullable(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                )
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof SecurityUser)
                .map(principal -> {
                    SecurityUser securityUser = (SecurityUser) principal;
                    // 권한에서 ROLE_ 접두사를 제거하여 ADMIN/USER로 변환
                    String role = securityUser.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .filter(auth -> auth.startsWith("ROLE_"))
                            .map(auth -> auth.substring(5)) // "ROLE_" 제거
                            .findFirst()
                            .orElse("USER");
                    return User.builder()
                            .id(securityUser.getId())
                            .email(securityUser.getEmail())
                            .nickname(securityUser.getNickname())
                            .role(role)
                            .build();
                })
                .orElse(null);
    }

    public String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    public void setHeader(String name, String value) {

        if (value.isBlank()) {
            req.removeAttribute(name);
        } else {
            resp.setHeader(name, value);
        }
    }

    public String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(req.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(cookies)
                                        .filter(cookie -> cookie.getName().equals(name))
                                        .map(Cookie::getValue)
                                        .filter(value -> !value.isBlank())
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCrossDomainCookie(String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .maxAge(maxAge)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .domain(cookieDomain)
                .httpOnly(true)
                .build();
        resp.addHeader("Set-Cookie", cookie.toString());
    }

    public void deleteCrossDomainCookie(String name) {
        setCrossDomainCookie(name, "", 0);
    }

    @SneakyThrows
    public void sendRedirect(String url) {
        resp.sendRedirect(url);
    }

    public User getActorFromDb() {
        User actor = getActor();
        if(actor == null) return null;
        return userService.findById(actor.getId());
    }
}