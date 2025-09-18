package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
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
    private AlcoholStrength alcoholStrength;

    private String cocktailStory;
    private CocktailType cocktailType;
    private AlcoholBaseType alcoholBaseType;
    private String ingredient;
    private String recipe;
    private String cocktailImgUrl;
}
