package com.back.domain.notification.controller;

import com.back.domain.notification.dto.NotificationGoResponseDto;
import com.back.domain.notification.dto.NotificationListResponseDto;
import com.back.domain.notification.dto.NotificationSettingDto;
import com.back.domain.notification.dto.NotificationSettingUpdateRequestDto;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.notification.service.NotificationSettingService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;

    // SSE 연결
    // produces = "text/event-stream": 응답 형식이 SSE임을 명시
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe() {
        return notificationService.subscribe();
    }

    @GetMapping("/notifications")
    public RsData<NotificationListResponseDto> getNotifications(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        NotificationListResponseDto body = notificationService.getNotifications(userId, lastCreatedAt, lastId, limit);
        return RsData.successOf(body);
    }

    @GetMapping("/notification-setting")
    public RsData<NotificationSettingDto> getMyNotificationSetting(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        NotificationSettingDto body = notificationSettingService.getMySetting(userId);
        return RsData.successOf(body);
    }

    @PatchMapping("/notification-setting")
    public RsData<NotificationSettingDto> setMyNotificationSetting(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody NotificationSettingUpdateRequestDto req
    ) {
        NotificationSettingDto body = notificationSettingService.setMySetting(userId, req.enabled());
        return RsData.successOf(body);
    }

    @PostMapping("/notifications/{id}")
    public RsData<NotificationGoResponseDto> goPostLink(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable("id") Long notificationId
    ) {
        var body = notificationService.markAsReadAndGetPostLink(userId, notificationId);
        return RsData.successOf(body);
    }
}
