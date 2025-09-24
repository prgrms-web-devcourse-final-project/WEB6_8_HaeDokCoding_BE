package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CocktailDetailResponseDto {
    private Long cocktailId;
    private String cocktailName;
    private AlcoholStrength alcoholStrength;
    private CocktailType cocktailType;
    private AlcoholBaseType alcoholBaseType;
    private String cocktailImgUrl;
    private String cocktailStory;
    private String ingredient;
    private String recipe;

    public CocktailDetailResponseDto(
            long cocktailId, String cocktailName,
            AlcoholStrength alcoholStrength, CocktailType cocktailType,
            AlcoholBaseType alcoholBaseType, String cocktailImgUrl,
            String cocktailStory, String ingredient,
            String recipe
    ) {
        this.cocktailId = cocktailId;
        this.cocktailName = cocktailName;
        this.alcoholStrength = alcoholStrength;
        this.cocktailType = cocktailType;
        this.alcoholBaseType = alcoholBaseType;
        this.cocktailImgUrl = cocktailImgUrl;
        this.cocktailStory = cocktailStory;
        this.ingredient = ingredient;
        this.recipe = recipe;
    }

    public CocktailDetailResponseDto(Cocktail cocktail) {
        this.cocktailId = cocktail.getId();
        this.cocktailName = cocktail.getCocktailName();
        this.alcoholStrength = cocktail.getAlcoholStrength();
        this.cocktailType = cocktail.getCocktailType();
        this.alcoholBaseType = cocktail.getAlcoholBaseType();
        this.cocktailImgUrl = cocktail.getCocktailImgUrl();
        this.cocktailStory = cocktail.getCocktailStory();
        this.ingredient = cocktail.getIngredient();
        this.recipe = cocktail.getRecipe();
    }
}
