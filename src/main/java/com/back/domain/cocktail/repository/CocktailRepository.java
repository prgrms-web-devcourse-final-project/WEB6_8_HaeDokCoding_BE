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

@Repository
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {

    // 전체조회 : 최신순
    List<Cocktail> findAllByOrderByIdDesc(Pageable pageable);
    List<Cocktail> findByIdLessThanOrderByIdDesc(Long lastId, Pageable pageable);

    // 전체 조회: keepsCount 기준 내림차순
    @Query("""
        SELECT c FROM Cocktail c
        LEFT JOIN MyBar m ON m.cocktail = c AND m.status = 'ACTIVE'
        GROUP BY c.id
        ORDER BY COUNT(m) DESC, c.id DESC
    """)
    List<Cocktail> findAllOrderByKeepCountDesc(Pageable pageable);

    // 무한스크롤 조회: lastKeepCount 이하
    @Query("""
    SELECT c FROM Cocktail c
    LEFT JOIN MyBar m ON m.cocktail = c AND m.status = 'ACTIVE'
    GROUP BY c.id
    HAVING COUNT(m) < :lastKeepCount OR (COUNT(m) = :lastKeepCount AND c.id < :lastId)
    ORDER BY COUNT(m) DESC, c.id DESC
""")
    List<Cocktail> findByKeepCountLessThanOrderByKeepCountDesc(
            @Param("lastKeepCount") Long lastKeepCount,
            @Param("lastId") Long lastId,
            Pageable pageable
    );

    // 댓글순
    @Query("SELECT c FROM Cocktail c " +
        "LEFT JOIN CocktailComment cm ON cm.cocktail = c " +
        "GROUP BY c.id " +
        "ORDER BY COUNT(cm) DESC, c.id DESC")
    List<Cocktail> findAllOrderByCommentsCountDesc(Pageable pageable);

    @Query("""
        SELECT c FROM Cocktail c
        LEFT JOIN CocktailComment cm ON cm.cocktail = c
        GROUP BY c.id
        HAVING COUNT(cm) < :lastCommentsCount OR (COUNT(cm) = :lastCommentsCount AND c.id < :lastId)
        ORDER BY COUNT(cm) DESC, c.id DESC
    """)
    List<Cocktail> findByCommentsCountLessThanOrderByCommentsCountDesc(
            @Param("lastCommentsCount") Long lastCommentsCount,
            @Param("lastId") Long lastId,
            Pageable pageable
    );

    //검색, 필터
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

    List<Cocktail> findByAlcoholStrengthAndAlcoholBaseTypeAndIdNot(
            AlcoholStrength alcoholStrength,
            AlcoholBaseType alcoholBaseType,
            Long id
    );
}
