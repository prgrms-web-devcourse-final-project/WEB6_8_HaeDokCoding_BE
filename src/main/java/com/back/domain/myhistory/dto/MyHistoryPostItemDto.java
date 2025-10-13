package com.back.domain.myhistory.dto;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyHistoryPostItemDto {
    private Long id;
    private String title;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;

    public static MyHistoryPostItemDto from(Post p) {
        return MyHistoryPostItemDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .imageUrls(p.getImages().stream()
                    .map(PostImage::getUrl)
                    .toList())
                .createdAt(p.getCreatedAt())
                .likeCount(p.getLikeCount())
                .commentCount(p.getCommentCount())
                .build();
    }
}

