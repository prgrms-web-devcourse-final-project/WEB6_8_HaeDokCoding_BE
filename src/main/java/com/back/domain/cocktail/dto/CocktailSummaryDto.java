package com.back.domain.cocktail.dto;

import lombok.Getter;

@Getter
public class CocktailSummaryDto {
    private Long cocktailId;
    private String cocktailName;
    private String cocktailImgUrl;

    public CocktailSummaryDto(Long id, String name, String imageUrl) {
        this.cocktailId = id;
        this.cocktailName = name;
        this.cocktailImgUrl = imageUrl;
    }
}
