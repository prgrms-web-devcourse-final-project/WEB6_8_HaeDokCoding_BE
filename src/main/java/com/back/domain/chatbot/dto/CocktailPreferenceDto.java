package com.back.domain.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CocktailPreferenceDto {  // 칵테일 선호 정보

    //추출된 키워드 목록 (영문, 한글 혼합 가능)
    private List<String> keywords;
    //칵테일 이름 추천
    private List<String> suggestedCocktails;
    //추출된 맛 프로필
    private String flavorProfile;
    //추출된 무드/상황
    private String mood;
}
