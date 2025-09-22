package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;

import java.time.LocalDateTime;

public class CocktailDetailDto {
    private Long cocktailId;
    private String cocktailName;
    private String ingredient;
    private AlcoholStrength alcoholStrength;
    private CocktailType cocktailType;
    private AlcoholBaseType alcoholBaseType;
    private String cocktailImgUrl;
    private String cocktailStory;
    private LocalDateTime createdAt;

    public CocktailDetailDto(Cocktail c) {
        this.cocktailId = c.getCocktailId();
        this.cocktailName = c.getCocktailName();
        this.ingredient = c.getIngredient();
        this.alcoholStrength = c.getAlcoholStrength();
        this.cocktailType = c.getCocktailType();
        this.alcoholBaseType = c.getAlcoholBaseType();
        this.cocktailImgUrl = c.getCocktailImgUrl();
        this.cocktailStory = c.getCocktailStory();
        this.createdAt = c.getCreatedAt();
    }
}
