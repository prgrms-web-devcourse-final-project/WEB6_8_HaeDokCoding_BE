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
    /**
     * @deprecated currentStep 필드를 사용하세요. 이 필드는 하위 호환성을 위해 유지됩니다.
     */
    @Deprecated
    private boolean isStepRecommendation = false;

    private Integer currentStep;
    // "ALL" 처리를 위해 스텝 2개 String으로 변경
    private String selectedAlcoholStrength;
    private String selectedAlcoholBaseType;
    // selectedCocktailType 삭제

    // Step 3에서 사용자가 입력한 칵테일 스타일 (검색 키워드로 사용)
    private String userStyleInput;
}