package com.back.domain.mybar.controller;

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
    @DisplayName("Get my bar list - first page")
    void getMyBarList_withoutCursor() throws Exception {
        SecurityUser principal = createPrincipal(1L);
        LocalDateTime keptAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime createdAt = keptAt.minusDays(1);

        MyBarItemResponseDto item = MyBarItemResponseDto.builder()
                .id(3L)
                .cocktailId(10L)
                .cocktailName("Margarita")
                .imageUrl("https://example.com/margarita.jpg")
                .createdAt(createdAt)
                .keptAt(keptAt)
                .build();

        MyBarListResponseDto responseDto = new MyBarListResponseDto(
                List.of(item),
                true,
                keptAt.minusMinutes(5),
                2L
        );

        given(myBarService.getMyBar(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(20)
        )).willReturn(responseDto);

        mockMvc.perform(get("/me/bar")
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.items[0].id").value(3L))
                .andExpect(jsonPath("$.data.items[0].cocktailId").value(10L))
                .andExpect(jsonPath("$.data.items[0].cocktailName").value("Margarita"))
                .andExpect(jsonPath("$.data.items[0].imageUrl").value("https://example.com/margarita.jpg"))
                .andExpect(jsonPath("$.data.items[0].createdAt").value(ISO_WITH_SECONDS.format(createdAt)))
                .andExpect(jsonPath("$.data.items[0].keptAt").value(ISO_WITH_SECONDS.format(keptAt)))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextKeptAt").value(ISO_WITH_SECONDS.format(keptAt.minusMinutes(5))))
                .andExpect(jsonPath("$.data.nextId").value(2L));

        verify(myBarService).getMyBar(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(20)
        );
    }

    @Test
    @DisplayName("Get my bar list - next page")
    void getMyBarList_withCursor() throws Exception {
        SecurityUser principal = createPrincipal(7L);
        LocalDateTime cursorKeptAt = LocalDateTime.of(2025, 2, 10, 9, 30, 15);
        LocalDateTime itemKeptAt = cursorKeptAt.minusMinutes(1);
        LocalDateTime itemCreatedAt = itemKeptAt.minusDays(2);

        MyBarItemResponseDto item = MyBarItemResponseDto.builder()
                .id(20L)
                .cocktailId(33L)
                .cocktailName("Negroni")
                .imageUrl("https://example.com/negroni.jpg")
                .createdAt(itemCreatedAt)
                .keptAt(itemKeptAt)
                .build();

        MyBarListResponseDto responseDto = new MyBarListResponseDto(
                List.of(item),
                false,
                null,
                null
        );

        given(myBarService.getMyBar(
                eq(principal.getId()),
                eq(cursorKeptAt),
                eq(99L),
                eq(5)
        )).willReturn(responseDto);

        mockMvc.perform(get("/me/bar")
                        .with(withPrincipal(principal))
                        .param("lastKeptAt", cursorKeptAt.toString())
                        .param("lastId", "99")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.items[0].id").value(20L))
                .andExpect(jsonPath("$.data.items[0].cocktailName").value("Negroni"))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextKeptAt").doesNotExist());

        verify(myBarService).getMyBar(
                eq(principal.getId()),
                eq(cursorKeptAt),
                eq(99L),
                eq(5)
        );
    }

    @Test
    @DisplayName("Keep cocktail")
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
    @DisplayName("Unkeep cocktail")
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