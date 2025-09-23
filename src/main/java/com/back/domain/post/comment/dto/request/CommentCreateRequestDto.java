package com.back.domain.post.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequestDto(
    Long postId,
    @NotBlank (message = "내용은 필수입니다.")
    String content
) {
}