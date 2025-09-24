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

        // 첫 로그인 여부에 따라 리다이렉트 분기
        String redirectUrl;

        if (securityUser.isFirstLogin()) {
            // 첫 로그인이면 welcome 페이지로
            redirectUrl = frontendUrl + "oauth/success/welcome";
            userAuthService.setFirstLoginFalse(securityUser.getId());
        } else {
            // 일반 로그인이면 메인 페이지로
            redirectUrl = frontendUrl+"oauth/success";
        }

        response.sendRedirect(redirectUrl);
    }
}