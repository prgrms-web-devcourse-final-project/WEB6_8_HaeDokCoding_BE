package com.back.domain.chatbot.dto;

import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StepRecommendationRequestDto {

    private Long userId;
    private AlcoholStrength alcoholStrength;  // 1단계: 도수
    private AlcoholBaseType alcoholBaseType;  // 2단계: 베이스 술
    private CocktailType cocktailType;        // 3단계: 샷 잔
    private Integer step;                     // 현재 단계 (1: 도수, 2: 베이스술, 3: 샷잔, 4: 최종추천)
}