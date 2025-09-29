package com.back.domain.cocktail.dto;

public record CocktailRecommendResponseDto(
        Long id,                  // 상세페이지 이동용 ID
        String cocktailNameKo,    // 한글 이름
        String cocktailName,      // 영문 이름
        String cocktailImgUrl,    // 이미지 URL (썸네일)
        String alcoholStrength,   // 도수 (라이트/미디엄/스트롱 등)
        String alcoholBaseType    // 베이스 주종 (진, 럼, 보드카 등)
) {
}

