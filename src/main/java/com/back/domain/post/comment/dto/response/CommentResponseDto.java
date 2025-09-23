package com.back.domain.post.comment.dto.response;

import com.back.domain.post.comment.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponseDto(
    Long commentId,
    Long postId,
    String userNickName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String content
) {

  public CommentResponseDto(Comment comment) {
    this(
        comment.getId(),
        comment.getPost().getId(),
        comment.getUser().getNickname(),
        comment.getCreatedAt(),
        comment.getUpdatedAt(),
        comment.getContent()
    );
  }
}