package com.back.domain.cocktail.comment.dto;

import com.back.domain.post.comment.enums.CommentStatus;

public record CocktailCommentUpdateRequestDto(
        CommentStatus status,
        String content
) {
}
