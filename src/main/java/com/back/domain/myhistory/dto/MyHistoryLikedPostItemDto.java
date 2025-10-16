package com.back.domain.myhistory.dto;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostImage;
import com.back.domain.post.post.entity.PostLike;
import com.back.domain.post.post.enums.PostStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyHistoryLikedPostItemDto {
    private Long id;
    private Long postId;
    private String categoryName;
    private String userNickName;
    private String title;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PostStatus status;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private LocalDateTime likedAt;

    public static MyHistoryLikedPostItemDto from(PostLike pl) {
        Post p = pl.getPost();
        List<String> imageUrls = p.getImages() == null
                ? List.of()
                : p.getImages().stream()
                .map(PostImage::getUrl)
                .toList();

        return MyHistoryLikedPostItemDto.builder()
                .id(pl.getId())
                .postId(p.getId())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .userNickName(p.getUser() != null ? p.getUser().getNickname() : null)
                .title(p.getTitle())
                .content(p.getContent())
                .imageUrls(imageUrls)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .status(p.getStatus())
                .likeCount(p.getLikeCount())
                .commentCount(p.getCommentCount())
                .viewCount(p.getViewCount())
                .likedAt(pl.getCreatedAt())
                .build();
    }
}

