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

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetySetting {
        private String category;
        private String threshold;

        // 카테고리 상수
        public static final String HARM_CATEGORY_HARASSMENT = "HARM_CATEGORY_HARASSMENT";
        public static final String HARM_CATEGORY_HATE_SPEECH = "HARM_CATEGORY_HATE_SPEECH";
        public static final String HARM_CATEGORY_SEXUALLY_EXPLICIT = "HARM_CATEGORY_SEXUALLY_EXPLICIT";
        public static final String HARM_CATEGORY_DANGEROUS_CONTENT = "HARM_CATEGORY_DANGEROUS_CONTENT";

        // 임계값 상수
        public static final String BLOCK_NONE = "BLOCK_NONE";           // 차단 안함
        public static final String BLOCK_LOW_AND_ABOVE = "BLOCK_LOW_AND_ABOVE";  // 낮음 이상 차단
        public static final String BLOCK_MEDIUM_AND_ABOVE = "BLOCK_MEDIUM_AND_ABOVE"; // 중간 이상 차단
        public static final String BLOCK_HIGH = "BLOCK_ONLY_HIGH";      // 높음만 차단
    }

    // 기본 생성자 - 간단한 텍스트만
    public GeminiRequestDto(String message) {
        this.contents = List.of(new Content(message));
    }

    // 파라미터 설정 포함 생성자
    public GeminiRequestDto(String message, GenerationConfig config) {
        this.contents = List.of(new Content(message));
        this.generationConfig = config;
    }

    // 전체 설정 포함 생성자
    public GeminiRequestDto(String message, GenerationConfig config, List<SafetySetting> safetySettings) {
        this.contents = List.of(new Content(message));
        this.generationConfig = config;
        this.safetySettings = safetySettings;
    }

    public static GeminiRequestDto createForCocktailChat(String message, boolean isDetailedResponse) {
        GenerationConfig config = GenerationConfig.builder()
                .temperature(0.8)  // 적당한 창의성
                .topP(0.95)        // 상위 95% 토큰 고려
                .topK(40)          // 상위 40개 토큰
                .maxOutputTokens(isDetailedResponse ? 300 : 150)  // 상세 답변 vs 간단 답변
                .candidateCount(1)  // 하나의 응답만
                .stopSequences(List.of("끝.", "이상입니다."))  // 종료 구문
                .build();

        // 안전 설정 (칵테일 관련이므로 비교적 관대하게)
        List<SafetySetting> safetySettings = List.of(
                SafetySetting.builder()
                        .category(SafetySetting.HARM_CATEGORY_HARASSMENT)
                        .threshold(SafetySetting.BLOCK_MEDIUM_AND_ABOVE)
                        .build(),
                SafetySetting.builder()
                        .category(SafetySetting.HARM_CATEGORY_HATE_SPEECH)
                        .threshold(SafetySetting.BLOCK_MEDIUM_AND_ABOVE)
                        .build(),
                SafetySetting.builder()
                        .category(SafetySetting.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                        .threshold(SafetySetting.BLOCK_MEDIUM_AND_ABOVE)
                        .build(),
                SafetySetting.builder()
                        .category(SafetySetting.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .threshold(SafetySetting.BLOCK_LOW_AND_ABOVE)  // 음주 관련이므로 더 엄격
                        .build()
        );

        return new GeminiRequestDto(message, config, safetySettings);
    }

    // 간결한 답변용 프리셋
    public static GeminiRequestDto createBriefResponse(String message) {
        GenerationConfig config = GenerationConfig.builder()
                .temperature(0.5)   // 더 일관된 답변
                .topP(0.8)         // 더 집중된 선택
                .topK(20)          // 적은 선택지
                .maxOutputTokens(100)  // 짧은 답변
                .candidateCount(1)
                .build();

        return new GeminiRequestDto(message, config);
    }

    // 창의적 답변용 프리셋
    public static GeminiRequestDto createCreativeResponse(String message) {
        GenerationConfig config = GenerationConfig.builder()
                .temperature(1.2)   // 높은 창의성
                .topP(0.98)        // 더 다양한 선택
                .topK(40)          // 많은 선택지
                .maxOutputTokens(500)  // 긴 답변 허용
                .candidateCount(1)
                .build();

        return new GeminiRequestDto(message, config);
    }
}