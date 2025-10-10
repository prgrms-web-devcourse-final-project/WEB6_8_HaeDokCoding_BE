package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.entity.Cocktail;
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
        String recipe,
        String cocktailPreview
) {
    public static CocktailDetailResponseDto from(Cocktail cocktail, List<CocktailService.IngredientDto> ingredients){
        String preview =cocktail.getCocktailStory().length() >80 ?
                cocktail.getCocktailStory().substring(0,80)+"..."
                : cocktail.getCocktailStory();

        return new CocktailDetailResponseDto(
                cocktail.getId(),
                cocktail.getCocktailName(),
                cocktail.getCocktailNameKo(),
                cocktail.getAlcoholStrength().getDescription(),
                cocktail.getCocktailType().getDescription(),
                cocktail.getAlcoholBaseType().getDescription(),
                cocktail.getCocktailImgUrl(),
                cocktail.getCocktailStory(),
                ingredients,
                cocktail.getRecipe(),
                preview
        );
    }
}
