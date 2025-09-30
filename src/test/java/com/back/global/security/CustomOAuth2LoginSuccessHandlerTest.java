package com.back.global.security;

import com.back.domain.user.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2LoginSuccessHandlerTest {

    @Mock
    private UserAuthService userAuthService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityUser securityUser;

    @InjectMocks
    private CustomOAuth2LoginSuccessHandler handler;

    private final String frontendUrl = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "frontendUrl", frontendUrl);
    }


    @Test
    @DisplayName("첫 사용자는 first-user 페이지로 리다이렉트")
    void redirectToFirstUser() throws Exception {
        // given
        Long userId = 1L;
        String email = "test@example.com";
        String nickname = "testUser";

        when(authentication.getPrincipal()).thenReturn(securityUser);
        when(securityUser.getId()).thenReturn(userId);
        when(securityUser.getEmail()).thenReturn(email);
        when(securityUser.getNickname()).thenReturn(nickname);
        when(securityUser.isFirstLogin()).thenReturn(true);

        // when
        handler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(userAuthService).issueTokens(response, userId, email, nickname);
        verify(userAuthService).setFirstLoginFalse(userId);
        verify(response).sendRedirect(frontendUrl + "/login/user/first-user");
    }

    @Test
    @DisplayName("기존 사용자는 success 페이지로 리다이렉트")
    void redirectToSuccess() throws Exception {
        // given
        Long userId = 1L;
        String email = "test@example.com";
        String nickname = "testUser";

        when(authentication.getPrincipal()).thenReturn(securityUser);
        when(securityUser.getId()).thenReturn(userId);
        when(securityUser.getEmail()).thenReturn(email);
        when(securityUser.getNickname()).thenReturn(nickname);
        when(securityUser.isFirstLogin()).thenReturn(false);

        // when
        handler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(userAuthService).issueTokens(response, userId, email, nickname);
        verify(userAuthService, never()).setFirstLoginFalse(userId);
        verify(response).sendRedirect(frontendUrl + "/login/user/success");
    }

    @Test
    @DisplayName("토큰 발급이 정상적으로 호출되는지 확인")
    void tokenIssuance() throws Exception {
        // given
        Long userId = 2L;
        String email = "user@test.com";
        String nickname = "nickname";

        when(authentication.getPrincipal()).thenReturn(securityUser);
        when(securityUser.getId()).thenReturn(userId);
        when(securityUser.getEmail()).thenReturn(email);
        when(securityUser.getNickname()).thenReturn(nickname);
        when(securityUser.isFirstLogin()).thenReturn(false);

        // when
        handler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(userAuthService).issueTokens(response, userId, email, nickname);
    }
}
