package com.back.domain.post.comment.repository;

import com.back.domain.post.comment.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findTop10ByPostIdOrderByIdDesc(Long postId);

  List<Comment> findTop10ByPostIdAndIdLessThanOrderByIdDesc(Long postId, Long lastId);
}
