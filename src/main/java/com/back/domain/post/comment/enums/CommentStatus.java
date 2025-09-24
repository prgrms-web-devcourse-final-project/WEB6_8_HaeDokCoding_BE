package com.back.domain.post.comment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentStatus {
  PUBLIC("공개", "모든 사용자가 볼 수 있는 상태"),
  PRIVATE("비공개", "게시글 작성자와 댓글 작성자만 볼 수 있는 상태"),
  DELETED("삭제됨", "삭제 처리된 댓글 상태");

  private final String title;
  private final String description;
}
