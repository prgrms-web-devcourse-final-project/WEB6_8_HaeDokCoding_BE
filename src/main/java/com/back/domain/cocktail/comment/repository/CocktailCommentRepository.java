package com.back.domain.cocktail.comment.repository;

import com.back.domain.cocktail.comment.entity.CocktailComment;
import com.back.domain.post.comment.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CocktailCommentRepository extends JpaRepository<CocktailComment, Long> {

    List<CocktailComment> findTop10ByCocktailIdAndStatusOrderByIdDesc(
            Long cocktailId,
            CommentStatus status
    );

    List<CocktailComment> findTop10ByCocktailIdAndStatusAndIdLessThanOrderByIdDesc(
            Long cocktailId,
            CommentStatus status,
            Long lastId
    );

    boolean existsByCocktailIdAndUserIdAndStatusNot(Long cocktailId, Long id, CommentStatus status);
}
