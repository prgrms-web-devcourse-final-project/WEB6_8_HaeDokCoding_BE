package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.enums.PostStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  // 최신순 (카테고리 없음)
  List<Post> findTop10ByStatusNotOrderByIdDesc(PostStatus status);
  List<Post> findTop10ByStatusNotAndIdLessThanOrderByIdDesc(PostStatus status, Long lastId);

  // 추천순 (카테고리 없음)
  List<Post> findTop10ByStatusNotOrderByLikeCountDescIdDesc(PostStatus status);
  List<Post> findTop10ByStatusNotAndLikeCountLessThanOrLikeCountEqualsAndIdLessThanOrderByLikeCountDescIdDesc(
      PostStatus status, Integer likeCount, Integer likeCount2, Long id);

  // 댓글순 (카테고리 없음)
  List<Post> findTop10ByStatusNotOrderByCommentCountDescIdDesc(PostStatus status);
  List<Post> findTop10ByStatusNotAndCommentCountLessThanOrCommentCountEqualsAndIdLessThanOrderByCommentCountDescIdDesc(
      PostStatus status, Integer commentCount, Integer commentCount2, Long id);

  // 최신순 (카테고리)
  List<Post> findTop10ByCategoryIdAndStatusNotOrderByIdDesc(Long categoryId, PostStatus status);
  List<Post> findTop10ByCategoryIdAndStatusNotAndIdLessThanOrderByIdDesc(Long categoryId, PostStatus status, Long id);

  // 추천순 (카테고리)
  List<Post> findTop10ByCategoryIdAndStatusNotOrderByLikeCountDescIdDesc(Long categoryId, PostStatus status);
  List<Post> findTop10ByCategoryIdAndStatusNotAndLikeCountLessThanOrLikeCountEqualsAndIdLessThanOrderByLikeCountDescIdDesc(
      Long categoryId, PostStatus status, Integer likeCount, Integer likeCountEquals, Long id);

  // 댓글순 (카테고리)
  List<Post> findTop10ByCategoryIdAndStatusNotOrderByCommentCountDescIdDesc(Long categoryId, PostStatus status);
  List<Post> findTop10ByCategoryIdAndStatusNotAndCommentCountLessThanOrCommentCountEqualsAndIdLessThanOrderByCommentCountDescIdDesc(
      Long categoryId, PostStatus status, Integer commentCount, Integer commentCountEquals, Long id);
}

