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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final Rq rq;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("Processing request for " + request.getRequestURI());

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

        // SecurityConfig에서 permitAll()로 설정된 경로들은 필터를 통과시키기
        if (
            //추후 로그인 필요한 api 추가 설정
                uri.startsWith("/h2-console") ||
                        uri.startsWith("/api/login/oauth2/") ||
                        (method.equals("GET") && uri.equals("api/~~")) ||
                        (method.equals("POST") && uri.equals("/api/user/login"))

        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // 인증 필수 URL 확인
        boolean requiresAuth = uri.startsWith("/api/");

        // 인증이 필요하지 않은 URL이면 그냥 통과
        if (!requiresAuth) {
            filterChain.doFilter(request, response);
            return;
        }

        // 쿠키에서 accessToken 가져오기
        String accessToken = rq.getCookieValue("accessToken", "");

        logger.debug("accessToken : " + accessToken);

        boolean isAccessTokenExists = !accessToken.isBlank();

        if (!isAccessTokenExists) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = null;
        boolean isAccessTokenValid = false;

        // accessToken 검증
        if (isAccessTokenExists) {
            if (jwtUtil.validateToken(accessToken)) {
                Long userId = jwtUtil.getUserIdFromToken(accessToken);
                String email = jwtUtil.getEmailFromToken(accessToken);
                String nickname = jwtUtil.getNicknameFromToken(accessToken);

                user = User.builder()
                        .id(userId)
                        .email(email)
                        .nickname(nickname)
                        .role("USER")
                        .build();
                isAccessTokenValid = true;
            }
        }

        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // accessToken이 만료됐으면 새로 발급
        if (isAccessTokenExists && !isAccessTokenValid) {
            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
            rq.setCrossDomainCookie("accessToken", newAccessToken, 60 * 20);
        }

        // SecurityContext에 인증 정보 저장
        UserDetails userDetails = new SecurityUser(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAuthorities(),
                Map.of() // JWT 인증에서는 빈 attributes
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities()
        );
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}