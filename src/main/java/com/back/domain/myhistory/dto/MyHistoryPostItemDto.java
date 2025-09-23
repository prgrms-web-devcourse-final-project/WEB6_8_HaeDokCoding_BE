package com.back.domain.myhistory.dto;

import com.back.domain.post.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyHistoryPostItemDto {
    private Long id;
    private String title;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;

    public static MyHistoryPostItemDto from(Post p) {
        return MyHistoryPostItemDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .imageUrl(p.getImageUrl())
                .createdAt(p.getCreatedAt())
                .likeCount(p.getLikeCount())
                .commentCount(p.getCommentCount())
                .build();
    }
}

