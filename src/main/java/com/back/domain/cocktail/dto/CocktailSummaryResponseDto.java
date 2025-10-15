package com.back.domain.cocktail.dto;

public record CocktailSummaryResponseDto(
        Long cocktailId,
        String cocktailName,
        String cocktailNameKo,
        String cocktailImgUrl,
        String alcoholStrength,
        Long keepCount,
        Long commentCount
) {

   //5개 필드만 사용하는 경우 (keepCount, commentCount 기본값 0)

    public CocktailSummaryResponseDto(Long cocktailId,
                                      String cocktailName,
                                      String cocktailNameKo,
                                      String cocktailImgUrl,
                                      String alcoholStrength) {
        this(cocktailId, cocktailName, cocktailNameKo, cocktailImgUrl, alcoholStrength, 0L, 0L);
    }
}