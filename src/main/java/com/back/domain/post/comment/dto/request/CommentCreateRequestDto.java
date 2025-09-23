package com.back.domain.post.comment.dto.request;

import com.back.domain.post.comment.enums.CommentStatus;
import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequestDto(
    CommentStatus status,
    @NotBlank (message = "내용은 필수입니다.")
    String content
) {
}