package com.back.domain.post.comment.dto.request;

import com.back.domain.post.comment.enums.CommentStatus;

public record CommentUpdateRequestDto(
    CommentStatus status,
    String content
) {
}