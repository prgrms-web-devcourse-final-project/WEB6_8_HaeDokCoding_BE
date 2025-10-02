package com.back.domain.cocktail.comment.repository;

import com.back.domain.cocktail.comment.entity.CocktailComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CocktailCommentRepository extends JpaRepository<CocktailComment, Long> {

    List<CocktailComment> findTop10ByCocktailIdOrderByIdDesc(Long cocktailId);

    List<CocktailComment> findTop10ByCocktailIdAndIdLessThanOrderByIdDesc(Long cocktailId, Long lastId);

    boolean existsByCocktailIdAndUserId(Long cocktailId, Long id);
}
