package com.back.global.security;

import com.back.domain.user.service.UserService;
import com.back.global.jwt.JwtUtil;
import com.back.global.rq.Rq;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final Rq rq;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        // Access Token 생성
        String accessToken = jwtUtil.generateAccessToken(securityUser.getId(), securityUser.getEmail());

        // 쿠키에 토큰 저장
        rq.setCrossDomainCookie("accessToken", accessToken, (int) TimeUnit.MINUTES.toSeconds(20));

        // 프론트엔드로 리다이렉트
        String redirectUrl = frontendUrl + "/oauth/success";

        response.sendRedirect(redirectUrl);
    }
}