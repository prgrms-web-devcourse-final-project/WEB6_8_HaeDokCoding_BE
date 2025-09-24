package com.back.domain.profile.repository;

import com.back.domain.post.comment.enums.CommentStatus;
import com.back.domain.post.post.enums.PostLikeStatus;
import com.back.domain.post.post.enums.PostStatus;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileQueryRepository extends JpaRepository<User, Long> {

    @Query("""
        select count(p) from Post p
         where p.user.id = :userId
           and p.status <> :deleted
    """)
    long countMyPosts(@Param("userId") Long userId, @Param("deleted") PostStatus deleted);

    @Query("""
        select count(c) from Comment c
         where c.user.id = :userId
           and c.status <> :deleted
    """)
    long countMyComments(@Param("userId") Long userId, @Param("deleted") CommentStatus deleted);

    @Query("""
        select count(pl) from PostLike pl
         where pl.user.id = :userId
           and pl.status = :like
    """)
    long countMyLikedPosts(@Param("userId") Long userId, @Param("like") PostLikeStatus like);
}

