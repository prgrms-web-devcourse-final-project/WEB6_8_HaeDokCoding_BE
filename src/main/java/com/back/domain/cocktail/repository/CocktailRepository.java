package com.back.domain.cocktail.repository;

import com.back.domain.cocktail.entity.Cocktail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {

    // 첫 요청 → 최신순(내림차순)으로 정렬해서 가져오기
    List<Cocktail> findAllByOrderByCocktailIdDesc(Pageable pageable);

    // 무한스크롤 → lastId보다 작은 ID들 가져오기
    List<Cocktail> findByCocktailIdLessThanOrderByCocktailIdDesc(Long lastId, Pageable pageable);
}
