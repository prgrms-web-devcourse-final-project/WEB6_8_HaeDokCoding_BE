package com.back.global.security;

import com.back.global.jwt.JwtUtil;
import com.back.global.rq.Rq;
import jakarta.servlet.FilterChain;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Rq rq;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private CustomAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "accessTokenExpiration", 900);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("토큰 없으면 통과")
    void noToken_Pass() throws Exception {
        when(rq.getCookieValue("accessToken", "")).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("유효한 토큰으로 인증 성공")
    void validToken_Success() throws Exception {
        String token = "valid.token";
        when(rq.getCookieValue("accessToken", "")).thenReturn(token);
        when(jwtUtil.validateAccessToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(1L);
        when(jwtUtil.getEmailFromToken(token)).thenReturn("test@test.com");
        when(jwtUtil.getNicknameFromToken(token)).thenReturn("user");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertInstanceOf(SecurityUser.class, auth.getPrincipal());
    }

    @Test
    @DisplayName("무효한 토큰은 통과")
    void invalidToken_Pass() throws Exception {
        String token = "invalid.token";
        when(rq.getCookieValue("accessToken", "")).thenReturn(token);
        when(jwtUtil.validateAccessToken(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
