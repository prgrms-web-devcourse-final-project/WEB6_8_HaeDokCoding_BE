package com.back.domain.notification.controller;

import com.back.domain.notification.dto.NotificationGoResponseDto;
import com.back.domain.notification.dto.NotificationListResponseDto;
import com.back.domain.notification.dto.NotificationSettingDto;
import com.back.domain.notification.dto.NotificationSettingUpdateRequestDto;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.notification.service.NotificationSettingService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    /**
     * 알림 API 컨트롤러.
     * 알림 목록 조회, 읽음 처리 후 이동 정보, 알림 설정 조회/변경, SSE 구독을 제공합니다.
     */

    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;

    // SSE 연결
    // produces = "text/event-stream": 응답 형식이 SSE임을 명시
    /**
     * 알림 SSE 구독
     *
     * @return SSE 스트림 핸들러(SseEmitter)
     */
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    @Operation(summary = "알림 SSE 구독", description = "Server-Sent Events로 실시간 알림 스트림 구독")
    public SseEmitter subscribe() {
        return notificationService.subscribe();
    }

    /**
     * 알림 목록 조회(무한스크롤)
     *
     * @param userId 인증된 사용자 ID
     * @param lastCreatedAt 이전 페이지 마지막 createdAt (옵션)
     * @param lastId 이전 페이지 마지막 id (옵션)
     * @param limit 페이지 크기(1~100)
     * @return 알림 아이템 목록과 다음 페이지 커서
     */
    @GetMapping("/notifications")
    @Operation(summary = "알림 목록 조회", description = "무한스크롤(nextCreatedAt, nextId) 기반 최신순 조회. limit 1~100")
    public RsData<NotificationListResponseDto> getNotifications(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        NotificationListResponseDto body = notificationService.getNotifications(userId, lastCreatedAt, lastId, limit);
        return RsData.successOf(body);
    }

    /**
     * 알림 설정 조회
     *
     * @param userId 인증된 사용자 ID
     * @return enabled 상태 (없으면 기본 true)
     */
    @GetMapping("/notification-setting")
    @Operation(summary = "알림 설정 조회", description = "사용자 알림 on/off 상태 조회. 미생성 시 기본 true 반환")
    public RsData<NotificationSettingDto> getMyNotificationSetting(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        NotificationSettingDto body = notificationSettingService.getMySetting(userId);
        return RsData.successOf(body);
    }

    /**
     * 알림 설정 변경(멱등)
     *
     * @param userId 인증된 사용자 ID
     * @param req enabled true/false
     * @return 변경된 enabled 상태
     */
    @PatchMapping("/notification-setting")
    @Operation(summary = "알림 설정 변경", description = "enabled 값을 true/false로 설정(멱등)")
    public RsData<NotificationSettingDto> setMyNotificationSetting(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody NotificationSettingUpdateRequestDto req
    ) {
        NotificationSettingDto body = notificationSettingService.setMySetting(userId, req.enabled());
        return RsData.successOf(body);
    }

    /**
     * 알림 읽음 처리 후 이동 정보 반환
     *
     * @param userId 인증된 사용자 ID
     * @param notificationId 알림 ID
     * @return 게시글 ID와 게시글 API URL
     */
    @PostMapping("/notifications/{id}")
    @Operation(summary = "읽음 처리 후 이동 정보", description = "알림을 읽음 처리하고 해당 게시글 ID와 API URL 반환")
    public RsData<NotificationGoResponseDto> goPostLink(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable("id") Long notificationId
    ) {
        var body = notificationService.markAsReadAndGetPostLink(userId, notificationId);
        return RsData.successOf(body);
    }
}
