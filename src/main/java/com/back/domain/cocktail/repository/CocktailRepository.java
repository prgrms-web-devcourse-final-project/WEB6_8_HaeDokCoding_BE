package com.back.domain.cocktail.repository;

import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.List;

@Repository
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {

    // 첫 요청 → 최신순(내림차순)으로 정렬해서 가져오기
    List<Cocktail> findAllByOrderByCocktailIdDesc(Pageable pageable);

    // 무한스크롤 → lastId보다 작은 ID들 가져오기
    List<Cocktail> findByCocktailIdLessThanOrderByCocktailIdDesc(Long lastId, Pageable pageable);

    List<Cocktail> findByCocktailNameContainingIgnoreCaseOrIngredientContainingIgnoreCase(String cocktailName, String ingredient);

    @Query("SELECT c FROM Cocktail c " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "       LOWER(c.cocktailName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(c.ingredient) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            "  AND (:strengths IS NULL OR c.alcoholStrength IN :strengths) " + // 알코올 도수 필터를 담당
            "  AND (:types IS NULL OR c.cocktailType IN :types) " +            // 칵테일 타입 필터를 담당
            "  AND (:bases IS NULL OR c.alcoholBaseType IN :bases) ")  // 알코올 베이스 필터를 담당
    Page<Cocktail> searchWithFilters(@Param("keyword") String keyword,
                                     @Param("strengths") List<AlcoholStrength> strengths,
                                     @Param("types") List<CocktailType> types,
                                     @Param("bases") List<AlcoholBaseType> bases,
                                     Pageable pageable);
}
