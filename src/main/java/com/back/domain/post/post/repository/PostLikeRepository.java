package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostLike;
import com.back.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
  Boolean existsByPostAndUser(Post post, User user);

  Optional<PostLike> findByPostAndUser(Post post, User user);
}
