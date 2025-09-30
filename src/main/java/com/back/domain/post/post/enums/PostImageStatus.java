package com.back.domain.post.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostImageStatus {
  POSTED("게시", "이미지가 해당 게시물에 게시된 상태"),
  DELETED("삭제", "이미지가 삭제 처리된 상태");

  private final String title;
  private final String description;
}
