package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  // 최신순
  List<Post> findTop10ByOrderByIdDesc();
  List<Post> findTop10ByIdLessThanOrderByIdDesc(Long lastId);

  // 추천순
  List<Post> findTop10ByOrderByLikeCountDescIdDesc();
  List<Post> findTop10ByLikeCountLessThanOrLikeCountEqualsAndIdLessThanOrderByLikeCountDescIdDesc(
      Integer likeCount, Integer likeCount2, Long id);

  // 댓글순
  List<Post> findTop10ByOrderByCommentCountDescIdDesc();
  List<Post> findTop10ByCommentCountLessThanOrCommentCountEqualsAndIdLessThanOrderByCommentCountDescIdDesc(
      Integer commentCount, Integer commentCount2, Long id);
}
