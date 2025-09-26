package com.back.domain.cocktail.service;

import com.back.domain.cocktail.dto.CocktailRecommendResponseDto;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.repository.CocktailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final CocktailRepository cocktailRepository;

    public List<CocktailRecommendResponseDto> recommendRelatedCocktails(Long cocktailId, int maxSize) {
        Cocktail current = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 칵테일입니다."));

        // 3가지 조건으로 유사 칵테일 조회
        List<Cocktail> byAlcoholStrength = cocktailRepository.findByAlcoholStrengthAndIdNot(current.getAlcoholStrength(), current.getId());
        List<Cocktail> byCocktailType = cocktailRepository.findByCocktailTypeAndIdNot(current.getCocktailType(), current.getId());
        List<Cocktail> byAlcoholBase = cocktailRepository.findByAlcoholBaseTypeAndIdNot(current.getAlcoholBaseType(), current.getId());

        // 합치고 중복 제거
        Set<Cocktail> combined = new LinkedHashSet<>();
        combined.addAll(byAlcoholStrength);
        combined.addAll(byCocktailType);
        combined.addAll(byAlcoholBase);

        List<Cocktail> combinedList = new ArrayList<>(combined);
        if (combinedList.size() > maxSize) {
            combinedList = combinedList.subList(0, maxSize);
        }

        // DTO로 변환
        return combinedList.stream()
                .map(c -> new CocktailRecommendResponseDto(
                        c.getId(),
                        c.getCocktailNameKo(),
                        c.getCocktailName(),
                        c.getCocktailImgUrl(),
                        c.getAlcoholStrength().name(),
                        c.getAlcoholBaseType().name()
                ))
                .toList();
    }
}

