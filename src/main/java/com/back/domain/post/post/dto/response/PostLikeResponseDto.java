package com.back.domain.post.post.dto.response;

import com.back.domain.post.post.entity.PostLike;
import com.back.domain.post.post.enums.PostLikeStatus;

public record PostLikeResponseDto(
    PostLikeStatus status
) {

  public PostLikeResponseDto(PostLike postLike) {
    this(
        postLike.getStatus()
    );
  }
}