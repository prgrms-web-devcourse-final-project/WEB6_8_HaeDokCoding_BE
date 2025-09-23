package com.back.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationListResponseDto {
    private List<NotificationItemDto> items;
    private boolean hasNext;
    private LocalDateTime nextCreatedAt;
    private Long nextId;
}

