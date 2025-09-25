package com.back.domain.cocktail.comment.dto;

import com.back.domain.post.comment.enums.CommentStatus;
import jakarta.validation.constraints.NotBlank;

public record CocktailCommentCreateRequestDto(
        CommentStatus status,
        @NotBlank(message = "내용은 필수입니다.")
        String content
) {
}
