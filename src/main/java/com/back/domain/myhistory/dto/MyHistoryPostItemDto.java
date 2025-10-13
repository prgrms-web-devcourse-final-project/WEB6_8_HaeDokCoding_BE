package com.back.domain.myhistory.dto;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostImage;
import com.back.domain.post.post.enums.PostStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyHistoryPostItemDto {
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

    public static MyHistoryPostItemDto from(Post p) {
        String categoryName = p.getCategory() != null ? p.getCategory().getName() : null;
        String userNickName = p.getUser() != null ? p.getUser().getNickname() : null;
        return MyHistoryPostItemDto.builder()
                .id(p.getId())
                .postId(p.getId())
                .categoryName(categoryName)
                .userNickName(userNickName)
                .title(p.getTitle())
                .content(p.getContent())
                .imageUrls(p.getImages().stream()
                        .map(PostImage::getUrl)
                        .toList())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .status(p.getStatus())
                .likeCount(p.getLikeCount())
                .commentCount(p.getCommentCount())
                .viewCount(p.getViewCount())
                .build();
    }
}
