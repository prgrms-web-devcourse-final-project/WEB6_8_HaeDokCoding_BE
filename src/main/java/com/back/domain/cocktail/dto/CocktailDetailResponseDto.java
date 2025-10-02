package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.service.CocktailService;

import java.util.List;

public record CocktailDetailResponseDto(
        Long cocktailId,
        String cocktailName,
        String cocktailNameKo,
        String alcoholStrength,
        String cocktailType,
        String alcoholBaseType,
        String cocktailImgUrl,
        String cocktailStory,
        List<CocktailService.IngredientDto> ingredient,
        String recipe
) {
}
