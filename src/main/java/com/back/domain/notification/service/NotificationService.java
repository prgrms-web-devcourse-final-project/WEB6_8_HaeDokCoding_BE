package com.back.domain.notification.service;

import com.back.domain.notification.dto.NotificationGoResponseDto;
import com.back.domain.notification.dto.NotificationItemDto;
import com.back.domain.notification.dto.NotificationListResponseDto;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponseDto getNotifications(Long userId, LocalDateTime lastCreatedAt, Long lastId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int fetchSize = safeLimit + 1;

        List<Notification> rows;
        if (lastCreatedAt == null || lastId == null) {
            rows = notificationRepository.findMyNotificationsFirstPage(userId, PageRequest.of(0, fetchSize));
        } else {
            rows = notificationRepository.findMyNotificationsAfter(userId, lastCreatedAt, lastId, PageRequest.of(0, fetchSize));
        }

        boolean hasNext = rows.size() > safeLimit;
        if (hasNext) rows = rows.subList(0, safeLimit);

        List<NotificationItemDto> items = new ArrayList<>();
        for (Notification n : rows) items.add(NotificationItemDto.from(n));

        LocalDateTime nextCreatedAt = null;
        Long nextId = null;
        if (hasNext && !rows.isEmpty()) {
            Notification last = rows.get(rows.size() - 1);
            nextCreatedAt = last.getCreatedAt();
            nextId = last.getId();
        }

        return new NotificationListResponseDto(items, hasNext, nextCreatedAt, nextId);
    }

    @Transactional
    public NotificationGoResponseDto markAsReadAndGetPostLink(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId);
        if (notification == null) {
            throw new ServiceException(404, "알림을 찾을 수 없습니다.");
        }
        if (!notification.isRead()) {
            notification.markRead();
        }
        Long postId = notification.getPost().getId();
        String apiUrl = "/api/posts/" + postId;
        return new NotificationGoResponseDto(postId, apiUrl);
    }
}
