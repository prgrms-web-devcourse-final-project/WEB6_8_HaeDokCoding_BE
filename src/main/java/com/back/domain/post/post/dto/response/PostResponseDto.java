package com.back.domain.post.post.dto.response;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.enums.PostStatus;
import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDto(
    Long postId,
    String categoryName,
    String userNickName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    PostStatus status,
    String title,
    String content,
    String imageUrl,
    List<String> tags,
    Integer likeCount,
    Integer commentCount,
    Integer viewCount
) {

  public PostResponseDto(Post post) {
    this(
        post.getId(),
        post.getCategory().getName(),
        post.getUser().getNickname(),
        post.getCreatedAt(),
        post.getUpdatedAt(),
        post.getStatus(),
        post.getTitle(),
        post.getContent(),
        post.getImageUrl(),
        post.getPostTags().stream()
            .map(postTag -> postTag.getTag().getName())
            .toList(),
        post.getLikeCount(),
        post.getCommentCount(),
        post.getViewCount()
    );
  }
}