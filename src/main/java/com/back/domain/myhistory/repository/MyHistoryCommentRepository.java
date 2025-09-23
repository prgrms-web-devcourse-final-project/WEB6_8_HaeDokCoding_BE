package com.back.domain.myhistory.repository;

import com.back.domain.post.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MyHistoryCommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
        select c from Comment c
         join fetch c.post p
         where c.user.id = :userId
         order by c.createdAt desc, c.id desc
    """)
    List<Comment> findMyCommentsFirstPage(@Param("userId") Long userId,
                                          Pageable pageable);

    @Query("""
        select c from Comment c
         join fetch c.post p
         where c.user.id = :userId
           and (c.createdAt < :lastCreatedAt or (c.createdAt = :lastCreatedAt and c.id < :lastId))
         order by c.createdAt desc, c.id desc
    """)
    List<Comment> findMyCommentsAfter(@Param("userId") Long userId,
                                      @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
                                      @Param("lastId") Long lastId,
                                      Pageable pageable);

    @Query("""
        select c from Comment c
         join fetch c.post p
         where c.id = :id and c.user.id = :userId
    """)
    Comment findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
