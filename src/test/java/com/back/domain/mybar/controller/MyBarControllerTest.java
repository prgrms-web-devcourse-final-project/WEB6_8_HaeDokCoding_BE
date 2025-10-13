package com.back.domain.mybar.controller;

import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.mybar.dto.MyBarIdResponseDto;
import com.back.domain.mybar.dto.MyBarItemResponseDto;
import com.back.domain.mybar.dto.MyBarListResponseDto;
import com.back.domain.mybar.service.MyBarService;
import com.back.global.aspect.ResponseAspect;
import com.back.global.jwt.JwtUtil;
import com.back.global.rq.Rq;
import com.back.global.security.SecurityUser;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyBarController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ResponseAspect.class)
@ImportAutoConfiguration(AopAutoConfiguration.class)
class MyBarControllerTest {

    private static final DateTimeFormatter ISO_WITH_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyBarService myBarService;

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
    @DisplayName("경량 내 바 목록을 조회한다")
    void getMyBarIds() throws Exception {
        SecurityUser principal = createPrincipal(5L);

        List<MyBarIdResponseDto> response = List.of(
                MyBarIdResponseDto.builder()
                        .id(123L)
                        .cocktailId(1L)
                        .keptAt(LocalDateTime.of(2025, 10, 10, 12, 0))
                        .build(),
                MyBarIdResponseDto.builder()
                        .id(124L)
                        .cocktailId(5L)
                        .keptAt(LocalDateTime.of(2025, 10, 9, 15, 30))
                        .build()
        );

        given(myBarService.getMyBarIds(principal.getId())).willReturn(response);

        mockMvc.perform(get("/me/bar")
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(123L))
                .andExpect(jsonPath("$.data[0].cocktailId").value(1L))
                .andExpect(jsonPath("$.data[0].keptAt").value("2025-10-10T12:00:00"))
                .andExpect(jsonPath("$.data[1].cocktailId").value(5L));

        verify(myBarService).getMyBarIds(principal.getId());
    }

    @Test
    @DisplayName("상세 내 바 목록을 조회한다")
    void getMyBarDetail() throws Exception {
        SecurityUser principal = createPrincipal(9L);
        LocalDateTime keptAt = LocalDateTime.of(2025, 10, 1, 10, 0);
        LocalDateTime createdAt = keptAt.minusDays(1);

        MyBarItemResponseDto item = MyBarItemResponseDto.builder()
                .id(123L)
                .cocktailId(1L)
                .cocktailName("Mojito")
                .cocktailNameKo("모히또")
                .alcoholStrength(AlcoholStrength.MEDIUM)
                .imageUrl("https://example.com/mojito.jpg")
                .createdAt(createdAt)
                .keptAt(keptAt)
                .build();

        MyBarListResponseDto response = new MyBarListResponseDto(
                List.of(item),
                false,
                null,
                null
        );

        given(myBarService.getMyBarDetail(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(50)
        )).willReturn(response);

        mockMvc.perform(get("/me/bar/detail")
                        .with(withPrincipal(principal))
                        .param("limit", "50")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.items[0].cocktailName").value("Mojito"))
                .andExpect(jsonPath("$.data.items[0].cocktailNameKo").value("모히또"))
                .andExpect(jsonPath("$.data.items[0].keptAt").value(ISO_WITH_SECONDS.format(keptAt)))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextKeptAt").doesNotExist());

        verify(myBarService).getMyBarDetail(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(50)
        );
    }

    @Test
    @DisplayName("킵 추가")
    void keepCocktail() throws Exception {
        SecurityUser principal = createPrincipal(11L);
        Long cocktailId = 42L;

        willDoNothing().given(myBarService).keep(principal.getId(), cocktailId);

        mockMvc.perform(post("/me/bar/{cocktailId}/keep", cocktailId)
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("kept"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(myBarService).keep(principal.getId(), cocktailId);
    }

    @Test
    @DisplayName("킵 해제")
    void unkeepCocktail() throws Exception {
        SecurityUser principal = createPrincipal(12L);
        Long cocktailId = 77L;

        willDoNothing().given(myBarService).unkeep(principal.getId(), cocktailId);

        mockMvc.perform(delete("/me/bar/{cocktailId}/keep", cocktailId)
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("deleted"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(myBarService).unkeep(principal.getId(), cocktailId);
    }
}
