package com.back.domain.post.comment.entity;

import com.back.domain.post.comment.enums.CommentStatus;
import com.back.domain.post.post.entity.Post;
import com.back.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment {
  // 각 댓글을 구분하는 유일한 번호
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  // 해당 댓글이 작성된 게시글의 고유 식별자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  // 해당 댓글을 작성한 유저의 고유 식별자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  // 댓글 작성 날짜
  @CreatedDate
  private LocalDateTime createdAt;

  // 댓글 수정 날짜
  @LastModifiedDate
  private LocalDateTime updatedAt;

  // 댓글 게시 상태 (기본값: 공개)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private CommentStatus status = CommentStatus.PUBLIC;

  // 댓글 내용
  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  public void updateStatus(CommentStatus status) {
    this.status = status;
  }

  public void updateContent(String content) {
    this.content = content;
  }
}
