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

    private String selectedValue; // 예: "NON_ALCOHOLIC"
    // 단계별 추천 관련 필드들
    /**
     * @deprecated currentStep 필드를 사용하세요. 이 필드는 하위 호환성을 위해 유지됩니다.
     */
    @Deprecated
    private boolean isStepRecommendation = false;

    private Integer currentStep;
    // "ALL" 처리를 위해 스텝 2개 String으로 변경
    private String selectedAlcoholStrength;
    private String selectedAlcoholBaseType;
    private String selectedCocktailType;

}