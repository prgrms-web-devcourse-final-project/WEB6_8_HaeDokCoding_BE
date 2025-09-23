package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
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
    private AlcoholStrength alcoholStrength;
    private String cocktailStory;
    private CocktailType cocktailType;
    private AlcoholBaseType alcoholBaseType;
    private String ingredient;
    private String recipe;
    private String cocktailImgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CocktailResponseDto(long cocktailId, String cocktailName,
                               AlcoholStrength alcoholStrength, CocktailType cocktailType,
                               AlcoholBaseType alcoholBaseType, String cocktailImgUrl,
                               String cocktailStory, LocalDateTime createdAt) {
        this.cocktailId = cocktailId;
        this.cocktailName = cocktailName;
        this.alcoholStrength = alcoholStrength;
        this.cocktailType = cocktailType;
        this.alcoholBaseType = alcoholBaseType;
        this.cocktailImgUrl = cocktailImgUrl;
        this.cocktailStory = cocktailStory;
        this.createdAt = createdAt;
    }
}