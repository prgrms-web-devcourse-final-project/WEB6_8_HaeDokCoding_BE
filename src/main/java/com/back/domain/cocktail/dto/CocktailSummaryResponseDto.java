package com.back.domain.cocktail.dto;

import lombok.Getter;

@Getter
public class CocktailSummaryResponseDto {
    private Long cocktailId;
    private String cocktailName;
    private String cocktailImgUrl;

    public CocktailSummaryResponseDto(Long id, String name, String imageUrl) {
        this.cocktailId = id;
        this.cocktailName = name;
        this.cocktailImgUrl = imageUrl;
    }
}
