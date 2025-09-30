package com.back.domain.chatbot.dto;

import com.back.domain.cocktail.dto.CocktailSummaryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepRecommendationResponseDto {

    private Integer currentStep;              // 현재 단계
    private String stepTitle;                 // 단계 제목 (예: "원하시는 도수를 선택해주세요!")
    private List<StepOption> options;         // 선택 옵션들
    private List<CocktailSummaryResponseDto> recommendations; // 최종 추천 칵테일 (4단계에서만)
    private boolean isCompleted;              // 추천이 완료되었는지 여부

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepOption {
        private String value;      // enum 값 (예: "NON_ALCOHOLIC")
        private String label;      // 화면에 표시될 텍스트 (예: "논알콜 (0%)")
        private String description; // 부가 설명 (선택사항)
    }
}