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

    // 단계별 추천 시작을 위한 필드 (선택사항)
    private boolean startStepRecommendation = false;
}