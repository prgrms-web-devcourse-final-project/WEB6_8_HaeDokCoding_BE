package com.back.domain.cocktail.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CocktailShareResponseDto {
    private String url;
    private String title;
    private String imageUrl;
}
