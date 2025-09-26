package com.back.domain.chatbot.dto;

import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRequestDto {

    @NotBlank(message = "메시지는 필수입니다.")
    private String message;

    private Long userId;

    // 단계별 추천 관련 필드들
    private boolean isStepRecommendation = false;
    private Integer currentStep;
    private AlcoholStrength selectedAlcoholStrength;
    private AlcoholBaseType selectedAlcoholBaseType;
    private CocktailType selectedCocktailType;
}