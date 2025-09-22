package com.back.global.security;

import com.back.domain.user.service.UserAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserAuthService userAuthService;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        // Access Token과 Refresh Token 발급
        userAuthService.issueTokens(response, securityUser.getId(), securityUser.getEmail(), securityUser.getNickname());

        // 프론트엔드로 리다이렉트
        String redirectUrl = frontendUrl + "/oauth/success";

        response.sendRedirect(redirectUrl);
    }
}