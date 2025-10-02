package com.back.domain.cocktail.comment.dto;

import com.back.domain.cocktail.comment.entity.CocktailComment;
import com.back.domain.post.comment.enums.CommentStatus;

import java.time.LocalDateTime;

public record CocktailCommentResponseDto(
        Long commentId,
        Long cocktailId,
        String userNickName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CommentStatus status,
        String content
) {
    public CocktailCommentResponseDto(CocktailComment cocktailcomment) {
        this(
                cocktailcomment.getId(),
                cocktailcomment.getCocktail().getId(),
                cocktailcomment.getUser().getNickname(),
                cocktailcomment.getCreatedAt(),
                cocktailcomment.getUpdatedAt(),
                cocktailcomment.getStatus(),
                cocktailcomment.getContent()
        );
    }
}
