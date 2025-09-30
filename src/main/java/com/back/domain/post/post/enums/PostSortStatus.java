package com.back.domain.post.post.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostSortStatus {
  LATEST("최신순", "작성 날짜가 가장 최신인 게시글 순서부터"),
  COMMENTS("댓글순", "게시글에 달린 댓글이 많은 순서부터"),
  POPULAR("인기순", "추천수가 많은 순서부터");

  private final String title;
  private final String description;
}
