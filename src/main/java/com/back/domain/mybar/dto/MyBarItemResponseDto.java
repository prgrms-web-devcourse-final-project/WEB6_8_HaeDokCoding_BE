package com.back.domain.mybar.dto;

import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.mybar.entity.MyBar;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyBarItemResponseDto {
    private Long id;
    private Long cocktailId;
    private String cocktailName;
    private String cocktailNameKo; // 칵테일의 한글 표기 이름
    private AlcoholStrength alcoholStrength; // 도수 레이블로 쓰이는 알코올 강도
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime keptAt;

    public static MyBarItemResponseDto from(MyBar m) {
        return MyBarItemResponseDto.builder()
                .id(m.getId())
                .cocktailId(m.getCocktail().getId())
                .cocktailName(m.getCocktail().getCocktailName())
                .cocktailNameKo(m.getCocktail().getCocktailNameKo())
                .alcoholStrength(m.getCocktail().getAlcoholStrength())
                .imageUrl(m.getCocktail().getCocktailImgUrl())
                .createdAt(m.getCreatedAt())
                .keptAt(m.getKeptAt())
                .build();
    }
}