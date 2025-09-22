package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
  // 태그 이름으로 Tag 엔티티를 찾기 위한 메서드
  Optional<Tag> findByName(String name);
}
