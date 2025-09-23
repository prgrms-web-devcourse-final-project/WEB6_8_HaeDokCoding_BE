package com.back.domain.cocktail.dto;

import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CocktailSearchRequestDto {

    private String keyword; // 검색 키워드

    private List<AlcoholStrength> alcoholStrengths;

    private List<CocktailType> cocktailTypes;

    private List<AlcoholBaseType> alcoholBaseTypes;

    // 페이징/정렬 추가하고 싶으면 여기 옵션 추가
    @Min(0)
    private Integer page;     // 0-based 페이지 번호
    @Max(100)
    private Integer size;     // 페이지 사이즈

    // 생성자
    public CocktailSearchRequestDto(
            String keyword,
            List<AlcoholStrength> alcoholStrengths,
            List<CocktailType> cocktailTypes,
            List<AlcoholBaseType> alcoholBaseTypes,
            Integer page,
            Integer size
    ) {
        this.keyword = keyword;
        this.alcoholStrengths = alcoholStrengths;
        this.cocktailTypes = cocktailTypes;
        this.alcoholBaseTypes = alcoholBaseTypes;
        this.page = page;
        this.size = size;
    }
}
