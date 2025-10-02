package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.entity.Cocktail;

public record CocktailSummaryResponseDto(
        Long cocktailId,
        String cocktailName,
        String cocktailNameKo,
        String cocktailImgUrl,
        String alcoholStrength // Enum 대신 String
) {
    public CocktailSummaryResponseDto(Cocktail cocktail) {
        this(
                cocktail.getId(),
                cocktail.getCocktailName(),
                cocktail.getCocktailNameKo(),
                cocktail.getCocktailImgUrl(),
                cocktail.getAlcoholStrength().getDescription() // 설명으로 변환
        );
    }
}