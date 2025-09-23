package com.back.domain.history.repository;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.enums.PostStatus;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<Post, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("""
        select p from Post p
         where p.user.id = :userId
           and p.status <> :deleted
         order by p.createdAt desc, p.id desc
    """)
    List<Post> findMyPostsFirstPage(@Param("userId") Long userId,
                                    @Param("deleted") PostStatus deleted,
                                    Pageable pageable);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("""
        select p from Post p
         where p.user.id = :userId
           and p.status <> :deleted
           and (p.createdAt < :lastCreatedAt or (p.createdAt = :lastCreatedAt and p.id < :lastId))
         order by p.createdAt desc, p.id desc
    """)
    List<Post> findMyPostsAfter(@Param("userId") Long userId,
                                @Param("deleted") PostStatus deleted,
                                @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
                                @Param("lastId") Long lastId,
                                Pageable pageable);
}
