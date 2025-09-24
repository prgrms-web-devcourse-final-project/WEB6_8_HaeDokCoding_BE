package com.back.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationGoResponseDto {
    private Long postId;
    private String postApiUrl;
}

