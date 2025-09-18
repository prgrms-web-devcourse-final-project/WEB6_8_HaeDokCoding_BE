package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.enums.AlcoholBaseType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CocktailListDto {
    private Long cocktailId;
    private String cocktailName;
    private String cocktailImgUrl;

    private String alcoholStrength;
    private String cocktailType;
    private AlcoholBaseType alcoholBaseType;
}
