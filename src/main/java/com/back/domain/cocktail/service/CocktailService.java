package com.back.domain.cocktail.service;

import com.back.domain.cocktail.dto.CocktailSummaryDto;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.repository.CocktailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CocktailService {

    private final CocktailRepository cocktailRepository;

    private static final int DEFAULT_SIZE = 20;

    @Transactional(readOnly = true)
    public Cocktail getCocktailById(Long id) {
        return cocktailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found. id=" + id));
    }

    // 칵테일 무한스크롤 조회
    @Transactional(readOnly = true)
    public List<CocktailSummaryDto> getCocktails(Long lastId, Integer size) { // 무한스크롤 조회, 클라이언트 쪽에서 lastId와 size 정보를 받음.(스크롤 이벤트)
        int fetchSize = (size != null) ? size : DEFAULT_SIZE;

        List<Cocktail> cocktails;
        if (lastId == null) {
            // 첫 요청 → 최신 데이터부터
            cocktails = cocktailRepository.findAllByOrderByCocktailIdDesc(PageRequest.of(0, fetchSize));
        } else {
            // 무한스크롤 → 마지막 ID보다 작은 데이터 조회
            cocktails = cocktailRepository.findByCocktailIdLessThanOrderByCocktailIdDesc(lastId, PageRequest.of(0, fetchSize));
        }

        return cocktails.stream()
                .map(c -> new CocktailSummaryDto(c.getCocktailId(), c.getCocktailName(), c.getCocktailImgUrl()))
                .collect(Collectors.toList());
    }

    // 칵테일 검색기능
    public List<Cocktail> cocktailSearch(String keyword) {
        // cockTailName, ingredient이 하나만 있을 수도 있고 둘 다 있을 수도 있음
        if (keyword == null || keyword.trim().isEmpty()) {
            // 아무 검색어 없으면 전체 반환 처리
            return cocktailRepository.findAll();
        } else {
            // 이름 또는 재료 둘 중 하나라도 매칭되면 결과 반환
            return cocktailRepository.findByCocktailNameContainingIgnoreCaseOrIngredientContainingIgnoreCase(keyword, keyword);
        }
    }
}
