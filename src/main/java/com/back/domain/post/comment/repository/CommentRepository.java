package com.back.domain.post.comment.repository;

import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.comment.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  // 첫 페이지 (lastId == null)
  List<Comment> findTop10ByPostIdAndStatusNotOrderByIdDesc(Long postId, CommentStatus status);

  // 무한스크롤 (lastId != null)
  List<Comment> findTop10ByPostIdAndIdLessThanAndStatusNotOrderByIdDesc(Long postId, Long id, CommentStatus status);
}
