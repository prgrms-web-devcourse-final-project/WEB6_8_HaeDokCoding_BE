package com.back.domain.post.post.entity;

import com.back.domain.post.post.enums.PostImageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostImage {

  // 각 이미지를 구분하는 유일한 번호
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  // 해당 이미지가 첨부된 게시글의 고유 식별자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  // 이미지 파일 명
  @Column(name = "file_name")
  private String fileName;

  // 이미지 URL
  @Column(name = "url")
  private String url;

  // 이미지 삭제 상태 (기본값: 게시)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PostImageStatus status = PostImageStatus.POSTED;

  // 이미지 순서
  @Column(name = "sort_order")
  private Integer sortOrder;

  public void updatePost(Post post) {
    this.post = post;
  }

  public void updateSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }
}
