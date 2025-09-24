package com.back.domain.post.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostLikeStatus {
  NONE("비추천", "해당 게시글에 추천을 아직 누르지 않은 상태"),
  LIKE("추천", "해당 게시글에 추천을 누른 상태");

  private final String title;
  private final String description;
}
