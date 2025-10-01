package com.back.domain.cocktail.dto;

public record CocktailDetailResponseDto(
        Long cocktailId,
        String cocktailName,
        String cocktailNameKo,
        String alcoholStrength,
        String cocktailType,
        String alcoholBaseType,
        String cocktailImgUrl,
        String cocktailStory,
        String ingredient,
        String recipe
) {
}
