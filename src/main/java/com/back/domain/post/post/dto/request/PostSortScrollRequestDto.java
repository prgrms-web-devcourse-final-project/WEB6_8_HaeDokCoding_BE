package com.back.domain.post.post.dto.request;

import com.back.domain.post.post.enums.PostSortStatus;

public record PostSortScrollRequestDto(
    Long categoryId,
    Long lastId,
    Integer lastLikeCount,
    Integer lastCommentCount,
    PostSortStatus postSortStatus
) {
}