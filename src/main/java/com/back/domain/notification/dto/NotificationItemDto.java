package com.back.domain.notification.dto;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.enums.NotificationType;
import com.back.domain.post.post.entity.PostImage;
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
    private String postCategoryName;
    private String postThumbnailUrl;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationItemDto from(Notification n) {
        String categoryName = n.getPost().getCategory() != null ? n.getPost().getCategory().getName() : null;
        String thumbnailUrl = n.getPost().getImages().stream()
                .map(PostImage::getUrl)
                .findFirst()
                .orElse(null);

        return NotificationItemDto.builder()
                .id(n.getId())
                .type(n.getType())
                .postId(n.getPost().getId())
                .postTitle(n.getPost().getTitle())
                .postCategoryName(categoryName)
                .postThumbnailUrl(thumbnailUrl)
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
