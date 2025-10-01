package com.back.domain.notification.controller;

import com.back.domain.notification.dto.NotificationGoResponseDto;
import com.back.domain.notification.dto.NotificationItemDto;
import com.back.domain.notification.dto.NotificationListResponseDto;
import com.back.domain.notification.dto.NotificationSettingDto;
import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.notification.service.NotificationSettingService;
import com.back.global.aspect.ResponseAspect;
import com.back.global.jwt.JwtUtil;
import com.back.global.rq.Rq;
import com.back.global.security.SecurityUser;
import java.time.LocalDateTime;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ResponseAspect.class)
@ImportAutoConfiguration(AopAutoConfiguration.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NotificationSettingService notificationSettingService;

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
    @DisplayName("Subscribe SSE")
    void subscribe_success() throws Exception {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.complete();
        given(notificationService.subscribe()).willReturn(emitter);

        MvcResult result = mockMvc.perform(get("/me/subscribe")
                        .with(withPrincipal(createPrincipal(1L)))
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());

        verify(notificationService).subscribe();
    }

    @Test
    @DisplayName("Get notifications without cursor")
    void getNotifications_noCursor() throws Exception {
        SecurityUser principal = createPrincipal(3L);

        NotificationItemDto item = NotificationItemDto.builder()
                .id(101L)
                .type(NotificationType.COMMENT)
                .postId(55L)
                .postTitle("새 댓글")
                .read(false)
                .createdAt(LocalDateTime.of(2025, 1, 2, 12, 0))
                .build();

        NotificationListResponseDto responseDto = new NotificationListResponseDto(
                List.of(item),
                false,
                null,
                null
        );

        given(notificationService.getNotifications(
                principal.getId(),
                null,
                null,
                20
        )).willReturn(responseDto);

        mockMvc.perform(get("/me/notifications")
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.items[0].id").value(101))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCreatedAt").doesNotExist());

        verify(notificationService).getNotifications(principal.getId(), null, null, 20);
    }

    @Test
    @DisplayName("Get notifications with cursor")
    void getNotifications_withCursor() throws Exception {
        SecurityUser principal = createPrincipal(5L);
        LocalDateTime cursor = LocalDateTime.of(2025, 3, 10, 9, 30, 15);

        NotificationListResponseDto responseDto = new NotificationListResponseDto(
                List.of(),
                true,
                cursor.minusHours(1),
                88L
        );

        given(notificationService.getNotifications(
                eq(principal.getId()),
                eq(cursor),
                eq(77L),
                eq(5)
        )).willReturn(responseDto);

        mockMvc.perform(get("/me/notifications")
                        .with(withPrincipal(principal))
                        .param("lastCreatedAt", cursor.toString())
                        .param("lastId", "77")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextId").value(88));

        verify(notificationService).getNotifications(principal.getId(), cursor, 77L, 5);
    }

    @Test
    @DisplayName("Get notification setting")
    void getNotificationSetting() throws Exception {
        SecurityUser principal = createPrincipal(9L);
        NotificationSettingDto dto = new NotificationSettingDto(true);
        given(notificationSettingService.getMySetting(principal.getId())).willReturn(dto);

        mockMvc.perform(get("/me/notification-setting")
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true));

        verify(notificationSettingService).getMySetting(principal.getId());
    }

    @Test
    @DisplayName("Patch notification setting")
    void patchNotificationSetting() throws Exception {
        SecurityUser principal = createPrincipal(11L);
        NotificationSettingDto dto = new NotificationSettingDto(false);
        given(notificationSettingService.setMySetting(eq(principal.getId()), eq(false))).willReturn(dto);

        mockMvc.perform(patch("/me/notification-setting")
                        .with(withPrincipal(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));

        verify(notificationSettingService).setMySetting(principal.getId(), false);
    }

    @Test
    @DisplayName("Go post link and mark read")
    void goPostLink() throws Exception {
        SecurityUser principal = createPrincipal(13L);
        NotificationGoResponseDto dto = new NotificationGoResponseDto(321L, "/posts/321");
        given(notificationService.markAsReadAndGetPostLink(eq(principal.getId()), eq(999L))).willReturn(dto);

        mockMvc.perform(post("/me/notifications/{id}", 999L)
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(321))
                .andExpect(jsonPath("$.data.postApiUrl").value("/posts/321"));

        verify(notificationService).markAsReadAndGetPostLink(principal.getId(), 999L);
    }
}