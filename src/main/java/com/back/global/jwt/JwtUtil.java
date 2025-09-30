package com.back.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final String cookieDomain;
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public JwtUtil(@Value("${custom.jwt.secretKey}") String secretKey,
                   @Value("${custom.accessToken.expirationSeconds}") long accessTokenExpiration,
                   @Value("${custom.site.cookieDomain}") String cookieDomain) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration * 1000;
        this.cookieDomain = cookieDomain;
    }

    public String generateAccessToken(Long userId, String email, String nickname) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("nickname", nickname)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey) // javax.crypto.SecretKey 타입
                .compact();
    }

    // 테스트용: 커스텀 만료시간으로 토큰 생성
    public String generateAccessTokenWithExpiration(Long userId, String email, String nickname, long customExpirationMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + customExpirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("nickname", nickname)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }


    public void addAccessTokenToCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발환경에서는 false, 프로덕션에서는 true
        cookie.setPath("/");
        cookie.setDomain(cookieDomain);
        cookie.setMaxAge((int) (accessTokenExpiration / 1000));
        response.addCookie(cookie);
    }


    public void removeAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setDomain(cookieDomain);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public String getEmailFromToken(String token) {
        return parseToken(token).get("email").toString();
    }

    public String getNicknameFromToken(String token) {
        return parseToken(token).get("nickname").toString();
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setDomain(cookieDomain);
        cookie.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(cookie);
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        log.debug("받은 쿠키 개수: {}", cookies != null ? cookies.length : 0);

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.debug("쿠키 확인 - 이름: {}, 값: {}", cookie.getName(), cookie.getValue());
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    log.debug("RefreshToken 쿠키 발견: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        log.debug("RefreshToken 쿠키를 찾을 수 없음");
        return null;
    }

    public void removeRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setDomain(cookieDomain);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }


}