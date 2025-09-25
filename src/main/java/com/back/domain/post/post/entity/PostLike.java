package com.back.domain.post.post.entity;

import com.back.domain.post.post.enums.PostLikeStatus;
import com.back.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
// 같은 사용자(user_id)가 같은 게시글(post_id)을 중복 추천하지 못하도록 DB 레벨에서 보장.
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostLike {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  // 추천 생성 날짜
  @CreatedDate
  private LocalDateTime createdAt;

  // 추천 상태 (기본값: 비추천)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PostLikeStatus status = PostLikeStatus.NONE;

  public void updateStatus(PostLikeStatus status) {
    this.status = status;
  }
}
