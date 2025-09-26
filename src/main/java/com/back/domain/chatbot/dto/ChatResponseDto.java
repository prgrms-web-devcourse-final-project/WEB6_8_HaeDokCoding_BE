package com.back.domain.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {

    private String response;
    private LocalDateTime timestamp;

    // 단계별 추천 관련 필드 (선택사항)
    private StepRecommendationResponseDto stepRecommendation;

    public ChatResponseDto(String response) {
        this.response = response;
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponseDto(String response, StepRecommendationResponseDto stepRecommendation) {
        this.response = response;
        this.timestamp = LocalDateTime.now();
        this.stepRecommendation = stepRecommendation;
    }
}