package com.back.domain.cocktail.comment.repository;

import com.back.domain.cocktail.comment.entity.CocktailComment;
import com.back.domain.post.comment.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CocktailCommentRepository extends JpaRepository<CocktailComment, Long> {

    List<CocktailComment> findTop10ByCocktailIdAndStatusInOrderByIdDesc(
            Long cocktailId, List<CommentStatus> statuses
    );

    List<CocktailComment> findTop10ByCocktailIdAndStatusInAndIdLessThanOrderByIdDesc(
            Long cocktailId, List<CommentStatus> statuses, Long lastId
    );

    boolean existsByCocktailIdAndUserIdAndStatusNot(Long cocktailId, Long id, CommentStatus status);
}
