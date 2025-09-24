package com.back.domain.myhistory.repository;

import com.back.domain.post.post.entity.PostLike;
import com.back.domain.post.post.enums.PostLikeStatus;
import com.back.domain.post.post.enums.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MyHistoryLikedPostRepository extends JpaRepository<PostLike, Long> {

    @Query("""
        select pl from PostLike pl
         join fetch pl.post p
         where pl.user.id = :userId
           and pl.status = :like
           and p.status <> :deleted
         order by pl.createdAt desc, pl.id desc
    """)
    List<PostLike> findMyLikedPostsFirstPage(@Param("userId") Long userId,
                                             @Param("like") PostLikeStatus like,
                                             @Param("deleted") PostStatus deleted,
                                             Pageable pageable);

    @Query("""
        select pl from PostLike pl
         join fetch pl.post p
         where pl.user.id = :userId
           and pl.status = :like
           and p.status <> :deleted
           and (pl.createdAt < :lastCreatedAt or (pl.createdAt = :lastCreatedAt and pl.id < :lastId))
         order by pl.createdAt desc, pl.id desc
    """)
    List<PostLike> findMyLikedPostsAfter(@Param("userId") Long userId,
                                         @Param("like") PostLikeStatus like,
                                         @Param("deleted") PostStatus deleted,
                                         @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
                                         @Param("lastId") Long lastId,
                                         Pageable pageable);
}

