package com.back.domain.post.post.entity;

import com.back.domain.post.category.entity.Category;
import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.post.enums.PostStatus;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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

  // Post → Comment = 1:N
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  // Post → PostImage = 1:N
  @Builder.Default
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("sortOrder ASC") // 조회 시 순서대로 정렬
  private List<PostImage> images = new ArrayList<>();

  // 게시글 동영상 URL
  @Column(name = "video_url")
  private String videoUrl;

  @Builder.Default
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PostTag> postTags = new ArrayList<>();

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PostLike> postLikes = new ArrayList<>();

  // 게시글 추천 수 (기본값: 0)
  @Builder.Default
  @Column(name = "like_count", nullable = false)
  private Integer likeCount = 0;

  // 게시글 댓글 수 (기본값: 0)
  @Builder.Default
  @Column(name = "comment_count", nullable = false)
  private Integer commentCount = 0;

  // 게시글 조회 수 (기본값: 0)
  @Builder.Default
  @Column(name = "view_count", nullable = false)
  private Integer viewCount = 0;

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

  public void addImage(PostImage image) {
    if (this.images == null) {
      this.images = new ArrayList<>();
    }
    this.images.add(image);
    image.updatePost(this); // 양방향 관계 유지
  }


  public void updateImages(List<PostImage> images) {
    this.images.clear();
    for (PostImage i : images) {
      i.updatePost(this);
      this.images.add(i);
    }
  }

  public void updateVideo(String videoUrl) {
    this.videoUrl = videoUrl;
  }

  public void addTag(Tag tag) {
    PostTag postTag = PostTag.create(this, tag);
    this.postTags.add(postTag);
  }

  public void clearTags() {
    this.postTags.clear();
  }

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    this.likeCount--;
  }

  public void increaseCommentCount() {
    this.commentCount++;
  }

  public void decreaseCommentCount() {
    if (this.commentCount > 0) {
      this.commentCount--;
    }
  }

  public void increaseViewCount() {
    this.viewCount++;
  }
}
