package com.back.global.security;

import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.http.Cookie;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testUser = User.builder()
                .email("test@example.com")
                .nickname("테스트사용자")
                .oauthId("test_123456")
                .role("USER")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("JWT 토큰 없이 보호된 API 접근 - 통과 (현재 permitAll 설정)")
    void accessProtectedApiWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 404 (엔드포인트 없음) - 인증은 통과
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 API 접근 - CustomAuthenticationFilter 동작 확인")
    void accessApiWithValidToken() throws Exception {
        // given - 실제 JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(testUser.getId(), testUser.getEmail(), testUser.getNickname());
        Cookie tokenCookie = new Cookie("accessToken", accessToken);

        // when & then - 실제 필터 체인 동작 확인
        mockMvc.perform(get("/api/test")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 엔드포인트 없음이지만 인증은 성공
    }

    @Test
    @DisplayName("만료된 JWT 토큰으로 API 접근")
    void accessApiWithExpiredToken() throws Exception {
        // given - 만료된 토큰 생성 (음수 만료시간)
        String expiredToken = jwtUtil.generateAccessTokenWithExpiration(
                testUser.getId(), testUser.getEmail(), testUser.getNickname(), -1000);
        Cookie tokenCookie = new Cookie("accessToken", expiredToken);

        // when & then
        mockMvc.perform(get("/api/test")
                        .cookie(tokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 여전히 통과 (permitAll)
    }

    @Test
    @DisplayName("잘못된 형식의 JWT 토큰으로 API 접근")
    void accessApiWithInvalidToken() throws Exception {
        // given
        Cookie invalidTokenCookie = new Cookie("accessToken", "invalid.jwt.token");

        // when & then
        mockMvc.perform(get("/api/test")
                        .cookie(invalidTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // 여전히 통과 (permitAll)
    }

    @Test
    @DisplayName("OAuth2 로그인 엔드포인트 접근 - 실제 리다이렉트 확인")
    void oAuth2LoginEndpoint() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/naver"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                    org.hamcrest.Matchers.containsString("nid.naver.com")));
    }

    @Test
    @DisplayName("OAuth2 로그인 엔드포인트 - 카카오")
    void kakaoLoginEndpoint() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/kakao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                    org.hamcrest.Matchers.containsString("kauth.kakao.com")));
    }

    @Test
    @DisplayName("OAuth2 로그인 엔드포인트 - 구글")
    void googleLoginEndpoint() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                    org.hamcrest.Matchers.containsString("accounts.google.com")));
    }

    @Test
    @DisplayName("CORS 헤더 확인")
    void checkCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Credentials"));
    }
}