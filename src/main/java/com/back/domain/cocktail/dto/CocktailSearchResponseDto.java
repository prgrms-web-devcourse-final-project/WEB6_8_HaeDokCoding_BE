package com.back.domain.cocktail.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CocktailSearchResponseDto {

    private long cocktailId;
    private String cocktailName;
    private String cocktailNameKo;
    private String alcoholStrength;
    private String cocktailType;
    private String alcoholBaseType;
    private String cocktailImgUrl;
    private String cocktailStory;

    public CocktailSearchResponseDto(long cocktailId, String cocktailName, String cocktailNameKo,
                                     String alcoholStrength, String cocktailType,
                                     String alcoholBaseType, String cocktailImgUrl,
                                     String cocktailStory) {
        this.cocktailId = cocktailId;
        this.cocktailName = cocktailName;
        this.cocktailNameKo = cocktailNameKo;
        this.alcoholStrength = alcoholStrength;
        this.cocktailType = cocktailType;
        this.alcoholBaseType = alcoholBaseType;
        this.cocktailImgUrl = cocktailImgUrl;
        this.cocktailStory = cocktailStory;
    }
}