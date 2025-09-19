package com.back.domain.mybar.dto;

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
    private String imageUrl;
    private LocalDateTime createdAt;

    public static MyBarItemResponseDto from(MyBar m) {
        return MyBarItemResponseDto.builder()
                .id(m.getId())
                .cocktailId(m.getCocktail().getCocktailId())
                .cocktailName(m.getCocktail().getCocktailName())
                .imageUrl(m.getCocktail().getCocktailImgUrl())
                .createdAt(m.getCreatedAt())
                .build();
    }
}