package com.back.domain.myhistory.dto;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostImage;
import com.back.domain.post.post.entity.PostLike;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyHistoryLikedPostItemDto {
    private Long id;
    private String title;
    private List<String> imageUrls;
    private LocalDateTime likedAt;
    private Integer likeCount;
    private Integer commentCount;

    public static MyHistoryLikedPostItemDto from(PostLike pl) {
        Post p = pl.getPost();
        return MyHistoryLikedPostItemDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .imageUrls(p.getImages().stream()
                    .map(PostImage::getUrl)
                    .toList())
                .likedAt(pl.getCreatedAt())
                .likeCount(p.getLikeCount())
                .commentCount(p.getCommentCount())
                .build();
    }
}

