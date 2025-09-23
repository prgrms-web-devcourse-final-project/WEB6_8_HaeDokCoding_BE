package com.back.domain.history.dto;

import com.back.domain.post.post.entity.Post;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistoryPostItemDto {
    private Long id;
    private String title;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;

    public static HistoryPostItemDto from(Post p) {
        return HistoryPostItemDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .imageUrl(p.getImageUrl())
                .createdAt(p.getCreatedAt())
                .likeCount(p.getLikeCount())
                .commentCount(p.getCommentCount())
                .build();
    }
}

