package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  // 첫 페이지 (최신순, id 기준)
  List<Post> findTop10ByOrderByIdDesc();

  // 이후 페이지 (lastId보다 작은 id들 중 최신순)
  List<Post> findTop10ByIdLessThanOrderByIdDesc(Long lastId);
}
