package com.back.domain.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LLM이 사용자 입력을 분석하여 추출한 칵테일 선호도 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CocktailPreferenceDto {

    /**
     * 추출된 키워드 목록 (영문, 한글 혼합 가능)
     * 예: ["sweet", "fruity", "달콤한", "tropical"]
     */
    private List<String> keywords;

    /**
     * LLM이 추천한 칵테일 이름 목록
     * 예: ["Mojito", "Daiquiri", "Pina Colada"]
     */
    private List<String> suggestedCocktails;

    /**
     * 추출된 맛 프로필
     * 예: "sweet_fruity", "bitter_strong", "refreshing"
     */
    private String flavorProfile;

    /**
     * 추출된 분위기/상황
     * 예: "party", "date", "relaxing"
     */
    private String mood;
}
