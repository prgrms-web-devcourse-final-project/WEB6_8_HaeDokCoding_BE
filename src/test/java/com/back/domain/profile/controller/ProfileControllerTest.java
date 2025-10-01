package com.back.domain.profile.controller;

import com.back.domain.profile.dto.ProfileResponseDto;
import com.back.domain.profile.dto.ProfileUpdateRequestDto;
import com.back.domain.profile.service.ProfileService;
import com.back.domain.user.service.UserService;
import com.back.global.aspect.ResponseAspect;
import com.back.global.jwt.JwtUtil;
import com.back.global.rq.Rq;
import com.back.global.security.SecurityUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ResponseAspect.class)
@ImportAutoConfiguration(AopAutoConfiguration.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private Rq rq;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private SecurityUser createPrincipal(Long userId) {
        return new SecurityUser(
                userId,
                "user" + userId + "@example.com",
                "user" + userId,
                false,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of()
        );
    }

    private UsernamePasswordAuthenticationToken authenticated(SecurityUser principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private RequestPostProcessor withPrincipal(SecurityUser principal) {
        return request -> {
            UsernamePasswordAuthenticationToken authentication = authenticated(principal);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.setUserPrincipal(authentication);
            request.getSession(true)
                    .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            return request;
        };
    }

    @Test
    @DisplayName("Get profile - success")
    void getProfile_success() throws Exception {
        SecurityUser principal = createPrincipal(10L);

        ProfileResponseDto responseDto = ProfileResponseDto.builder()
                .id(principal.getId())
                .nickname(principal.getNickname())
                .email(principal.getEmail())
                .abvDegree(12.5)
                .abvLevel(2)
                .abvLabel("12.5%")
                .myPostCount(7L)
                .myCommentCount(14L)
                .myLikedPostCount(5L)
                .myKeepCount(9L)
                .build();

        given(profileService.getProfile(principal.getId())).willReturn(responseDto);

        mockMvc.perform(get("/me/profile")
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(principal.getId()))
                .andExpect(jsonPath("$.data.nickname").value(principal.getNickname()))
                .andExpect(jsonPath("$.data.email").value(principal.getEmail()))
                .andExpect(jsonPath("$.data.abvDegree").value(12.5))
                .andExpect(jsonPath("$.data.abvLevel").value(2))
                .andExpect(jsonPath("$.data.abvLabel").value("12.5%"))
                .andExpect(jsonPath("$.data.myPostCount").value(7))
                .andExpect(jsonPath("$.data.myCommentCount").value(14))
                .andExpect(jsonPath("$.data.myLikedPostCount").value(5))
                .andExpect(jsonPath("$.data.myKeepCount").value(9));

        verify(profileService).getProfile(principal.getId());
    }

    @Test
    @DisplayName("Patch profile nickname - success")
    void patchNickname_success() throws Exception {
        SecurityUser principal = createPrincipal(22L);
        String payload = "{\"nickname\":\"newNick\"}";

        ProfileResponseDto responseDto = ProfileResponseDto.builder()
                .id(principal.getId())
                .nickname("newNick")
                .email(principal.getEmail())
                .abvDegree(20.0)
                .abvLevel(4)
                .abvLabel("20.0%")
                .myPostCount(11L)
                .myCommentCount(8L)
                .myLikedPostCount(3L)
                .myKeepCount(15L)
                .build();

        given(profileService.updateProfile(eq(principal.getId()), any(ProfileUpdateRequestDto.class)))
                .willReturn(responseDto);

        mockMvc.perform(patch("/me/profile")
                        .with(withPrincipal(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.nickname").value("newNick"))
                .andExpect(jsonPath("$.data.myKeepCount").value(15));

        ArgumentCaptor<ProfileUpdateRequestDto> captor = ArgumentCaptor.forClass(ProfileUpdateRequestDto.class);
        verify(profileService).updateProfile(eq(principal.getId()), captor.capture());
        ProfileUpdateRequestDto captured = captor.getValue();
        assertThat(captured.getNickname()).isEqualTo("newNick");
    }
}