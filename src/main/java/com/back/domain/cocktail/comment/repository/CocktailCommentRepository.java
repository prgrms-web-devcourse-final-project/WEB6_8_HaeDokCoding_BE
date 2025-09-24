package com.back.domain.cocktail.comment.repository;

import com.back.domain.cocktail.comment.entity.CocktailComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CocktailCommentRepository extends JpaRepository<CocktailComment, Long> {

    List<CocktailComment> findTop10ByCocktailIdOrderByIdDesc(Long cocktailId);

    List<CocktailComment> findTop10ByCocktailIdAndIdLessThanOrderByIdDesc(Long cocktailId, Long lastId);
}
