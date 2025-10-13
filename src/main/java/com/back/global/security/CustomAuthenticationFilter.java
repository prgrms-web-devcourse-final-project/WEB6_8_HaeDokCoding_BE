package com.back.global.security;

import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import com.back.global.jwt.JwtUtil;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import com.back.global.standard.util.Ut;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final Rq rq;

    @Value("${custom.accessToken.expirationSeconds}")
    private int accessTokenExpiration;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            work(request, response, filterChain);
        } catch (ServiceException e) {
            RsData<Void> rsData = e.getRsData();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(rsData.code());
            String jsonResponse = Ut.json.toString(rsData);
            if (jsonResponse == null) {
                jsonResponse = "{\"resultCode\":\"" + rsData.code() + "\",\"msg\":\"" + rsData.message() + "\"}";
            }
            response.getWriter().write(jsonResponse);
        } catch (Exception e) {
            throw e;
        }
    }

    private void work(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        log.debug("===== Authentication Filter Start =====");
        log.debug("Request: {} {}", method, uri);

        String accessToken = null;

        // 1. 먼저 Authorization 헤더에서 토큰 가져오기 시도
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
            log.debug("Token found in Authorization header");
        }

        // 2. Authorization 헤더에 없으면 쿠키에서 가져오기
        if (accessToken == null || accessToken.isBlank()) {
            accessToken = rq.getCookieValue("accessToken", "");
            if (!accessToken.isBlank()) {
                log.debug("Token found in Cookie");
            }
        }

        boolean isAccessTokenExists = !accessToken.isBlank();

        if (!isAccessTokenExists) {
            log.debug("No access token found - proceeding without authentication");
            filterChain.doFilter(request, response);
            return;
        }

        User user = null;

        // accessToken 검증
        if (jwtUtil.validateAccessToken(accessToken)) {
            log.debug("Access token is valid");

            try {
                Long userId = jwtUtil.getUserIdFromToken(accessToken);
                String email = jwtUtil.getEmailFromToken(accessToken);
                String nickname = jwtUtil.getNicknameFromToken(accessToken);

                if (userId != null) {
                    user = User.builder()
                            .id(userId)
                            .email(email)
                            .nickname(nickname)
                            .role("USER")
                            .build();

                    log.debug("User extracted - ID: {}, Email: {}, Nickname: {}", userId, email, nickname);
                } else {
                    log.warn("User ID is null in token");
                }
            } catch (Exception e) {
                log.error("Error extracting user info from token", e);
            }
        } else {
            log.warn("Access token validation failed - token is expired or invalid");
            // 만료된 토큰은 인증 실패 처리 (user는 null로 유지)
        }

        // user가 null이면 인증 실패
        if (user == null) {
            log.warn("Authentication failed - user is null");
            filterChain.doFilter(request, response);
            return;
        }

        // SecurityContext에 인증 정보 저장
        try {
            UserDetails userDetails = new SecurityUser(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.isFirstLogin(),
                    user.getAuthorities(),
                    Map.of() // JWT 인증에서는 빈 attributes
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("✅ Authentication SUCCESS - User ID: {}, Nickname: {}", user.getId(), user.getNickname());
            log.debug("===== Authentication Filter End =====");
        } catch (Exception e) {
            log.error("Error setting authentication in SecurityContext", e);
        }

        filterChain.doFilter(request, response);
    }
}