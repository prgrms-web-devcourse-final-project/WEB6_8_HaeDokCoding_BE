package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.entity.Cocktail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CocktailRequestDto {

    @NotBlank
    private String cocktailName;

    @NotNull
    private Cocktail.AlcoholStrength alcoholStrength;

    private String cocktailStory;
    private Cocktail.CocktailType cocktailType;
    private String ingredient;
    private String recipe;
    private String imageUrl;

}
