package com.back.domain.cocktail.service;

import com.back.domain.cocktail.dto.CocktailRecommendResponseDto;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.repository.CocktailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final CocktailRepository cocktailRepository;

    public List<CocktailRecommendResponseDto> recommendRelatedCocktails(Long cocktailId, int maxSize) {
        Cocktail current = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 칵테일입니다."));

       // 알콜 강도와 베이스 타이이 같은 칵테일만 조회
        List<Cocktail> related = cocktailRepository
                .findByAlcoholStrengthAndAlcoholBaseTypeAndIdNot(
                        current.getAlcoholStrength(),
                        current.getAlcoholBaseType(),
                        current.getId()
                );

        if (related.size() > maxSize) {
            related = related.subList(0, maxSize);
        }

        // DTO로 변환
        return related.stream()
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

