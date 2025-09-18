package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.entity.Cocktail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CocktailResponseDto {

    private long cocktailId;
    private String cocktailName;
    private Cocktail.AlcoholStrength alcoholStrength;
    private String cocktailStory;
    private Cocktail.CocktailType cocktailType;
    private String ingredient;
    private String recipe;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}