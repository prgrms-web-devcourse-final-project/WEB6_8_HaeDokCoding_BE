package com.back.domain.post.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {
  PUBLIC("공개", "모든 사용자가 볼 수 있는 상태"),
  PRIVATE("비공개", "작성자만 볼 수 있는 상태"),
  DELETED("삭제됨", "삭제 처리된 게시글 상태");

  private final String title;
  private final String description;
}
