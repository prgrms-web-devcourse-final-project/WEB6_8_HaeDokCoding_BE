package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.entity.Cocktail;

public record CocktailSearchResponseDto (

    Long cocktailId,
    String cocktailName,
    String cocktailNameKo,
    String alcoholStrength,
    String cocktailType,
    String alcoholBaseType,
    String cocktailImgUrl,
    String cocktailStory,
    String cocktailPreview
){
    public static CocktailSearchResponseDto from(Cocktail cocktail){
        String preview =cocktail.getCocktailStory().length() >80 ?
                cocktail.getCocktailStory().substring(0,80)+"..."
                : cocktail.getCocktailStory();

        return new CocktailSearchResponseDto(
                cocktail.getId(),
                cocktail.getCocktailName(),
                cocktail.getCocktailNameKo(),
                cocktail.getAlcoholStrength().getDescription(),
                cocktail.getCocktailType().getDescription(),
                cocktail.getAlcoholBaseType().getDescription(),
                cocktail.getCocktailImgUrl(),
                cocktail.getCocktailStory(),
                preview
        );
    }
}