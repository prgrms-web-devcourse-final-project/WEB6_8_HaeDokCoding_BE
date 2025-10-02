package com.back.domain.chatbot.dto;

import com.back.domain.chatbot.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDto {

    private String message;                    // 텍스트 메시지
    private MessageType type;                  // 메시지 표시 타입
    private LocalDateTime timestamp;

    // 단계별 추천 관련 데이터 (type이 RADIO_OPTIONS 또는 CARD_LIST일 때 사용)
    private StepRecommendationResponseDto stepData;

    // 추가 메타데이터
    private MetaData metaData;

    // 생성자들
    public ChatResponseDto(String message) {
        this.message = message;
        this.type = MessageType.TEXT;
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponseDto(String message, StepRecommendationResponseDto stepData) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.stepData = stepData;

        // stepData 내용에 따라 type 자동 설정
        if (stepData != null) {
            if (stepData.getOptions() != null && !stepData.getOptions().isEmpty()) {
                this.type = MessageType.RADIO_OPTIONS;
            } else if (stepData.getRecommendations() != null && !stepData.getRecommendations().isEmpty()) {
                this.type = MessageType.CARD_LIST;
            } else {
                this.type = MessageType.TEXT;
            }
        } else {
            this.type = MessageType.TEXT;
        }
    }

    // 메타데이터 내부 클래스
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetaData {
        private Integer currentStep;     // 현재 단계 (단계별 추천)
        private Integer totalSteps;      // 전체 단계 수
        private Boolean isTyping;        // 타이핑 애니메이션 표시 여부
        private Integer delay;           // 메시지 표시 지연 시간(ms)
        private String actionType;       // 버튼 액션 타입
    }
}