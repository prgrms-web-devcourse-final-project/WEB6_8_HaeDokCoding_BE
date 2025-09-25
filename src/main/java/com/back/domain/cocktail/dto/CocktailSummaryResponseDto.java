package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.enums.AlcoholStrength;
import lombok.Getter;

@Getter
public class CocktailSummaryResponseDto {
    private Long cocktailId;
    private String cocktailName;
    private String cocktailImgUrl;
    private AlcoholStrength alcoholStrength;

    public CocktailSummaryResponseDto(Long id, String name, String imageUrl, AlcoholStrength alcoholStrength) {
        this.cocktailId = id;
        this.cocktailName = name;
        this.cocktailImgUrl = imageUrl;
        this.alcoholStrength = alcoholStrength;
    }
}
