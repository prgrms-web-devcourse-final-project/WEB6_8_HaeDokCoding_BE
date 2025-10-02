package com.back.domain.chatbot.dto;

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
    private String selectedAlcoholStrength;  // "ALL" 처리를 위해 스텝 3개 String으로 변경
    private String selectedAlcoholBaseType;
    private String selectedCocktailType;
}