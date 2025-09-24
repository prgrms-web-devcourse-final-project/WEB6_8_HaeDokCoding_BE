package com.back.domain.post.post.entity;

import com.back.domain.post.category.entity.Category;
import com.back.domain.post.post.enums.PostStatus;
import com.back.domain.user.entity.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "post")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post {

  // 각 게시글을 구분하는 유일한 번호
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  // 해당 게시글의 카테고리 고유 식별자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  // 해당 게시글을 작성한 유저의 고유 식별자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  // 게시글 작성 날짜
  @CreatedDate
  private LocalDateTime createdAt;

  // 게시글 수정 날짜
  @LastModifiedDate
  private LocalDateTime updatedAt;

  // 게시글 게시 상태 (기본값: 공개)
  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PostStatus status = PostStatus.PUBLIC;

  // 게시글 제목
  @Column(name = "title", nullable = false, length = 100)
  private String title;

  // 게시글 내용
  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  // 게시글 이미지 URL
  @Column(name = "image_url")
  private String imageUrl;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PostTag> postTags = new ArrayList<>();

  // 게시글 추천 수 (기본값: 0)
  @Builder.Default
  @Column(name = "like_count", nullable = false)
  private Integer likeCount = 0;

  // 게시글 댓글 수
  @Column(name = "comment_count")
  private Integer commentCount;

  // 게시글 조회 수
  @Column(name = "view_count")
  private Integer viewCount;

  public void updateCategory(Category category) {
    this.category = category;
  }

  public void updateStatus(PostStatus status) {
    this.status = status;
  }

  public void updateTitle(String title) {
    this.title = title;
  }

  public void updateContent(String content) {
    this.content = content;
  }

  public void updateImage(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void addTag(Tag tag) {
    PostTag postTag = PostTag.create(this, tag);
    this.postTags.add(postTag);
  }

  public void clearTags() {
    this.postTags.clear();
  }
}
