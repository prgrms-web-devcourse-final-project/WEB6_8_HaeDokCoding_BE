package com.back.domain.chatbot.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class GeminiRequestDto {

    private List<Content> contents;
    private GenerationConfig generationConfig;
    private List<SafetySetting> safetySettings;

    @Getter
    @Setter
    public static class Content {
        private List<Part> parts;

        public Content(String text) {
            this.parts = List.of(new Part(text));
        }
    }

    @Getter
    @Setter
    public static class Part {
        private String text;

        public Part(String text) {
            this.text = text;
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig { // 생성 설정
        /**
         * Temperature (0.0 ~ 2.0)
         * - 낮을수록 (0.0): 일관되고 예측 가능한 응답
         * - 높을수록 (2.0): 창의적이고 다양한 응답
         * - 권장값: 0.7 ~ 1.0
         */
        private Double temperature;

        /**
         * Top-P (0.0 ~ 1.0)
         * - 누적 확률 임계값
         * - 0.95 = 상위 95% 확률의 토큰만 고려
         * - 낮을수록 더 집중된 응답
         */
        private Double topP;

        /**
         * Top-K (1 ~ 40)
         * - 고려할 토큰의 최대 개수
         * - 40 = 상위 40개 토큰만 고려
         * - 낮을수록 더 결정적인 응답
         */
        private Integer topK;

        /**
         * Max Output Tokens
         * - 응답의 최대 토큰 수 (출력 길이 제한)
         * - Gemini 1.5 Flash: 최대 8192 토큰
         * - Gemini 1.5 Pro: 최대 8192 토큰
         * - 한글 1글자 ≈ 1-2 토큰, 영어 3-4글자 ≈ 1 토큰
         */
        private Integer maxOutputTokens;

        /**
         * Stop Sequences
         * - 이 문자열을 만나면 생성 중단
         * - 예: ["끝", "END", "\n\n"]
         */
        private List<String> stopSequences;

        /**
         * Candidate Count (1 ~ 8)
         * - 생성할 응답 후보의 개수
         * - 여러 개 생성 후 최적 선택 가능
         */
        private Integer candidateCount;
    }

    public GeminiRequestDto(String message) {
        this.contents = List.of(new Content(message));
    }
}