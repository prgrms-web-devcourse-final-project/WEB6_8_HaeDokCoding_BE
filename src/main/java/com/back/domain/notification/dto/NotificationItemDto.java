package com.back.domain.notification.dto;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationItemDto {
    private Long id;
    private NotificationType type;
    private Long postId;
    private String postTitle;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationItemDto from(Notification n) {
        return NotificationItemDto.builder()
                .id(n.getId())
                .type(n.getType())
                .postId(n.getPost().getId())
                .postTitle(n.getPost().getTitle())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

