package com.back.domain.chatbot.service;

import com.back.domain.chatbot.dto.GeminiRequestDto;
import com.back.domain.chatbot.dto.GeminiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiApiService {

    private final WebClient geminiWebClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    // 기본 응답
    public Mono<String> generateResponse(String userMessage) {
        GeminiRequestDto requestDto = new GeminiRequestDto(userMessage);

        return callGeminiApi(requestDto);
    }

    // 간단한 응답 생성 (100 토큰)
    public Mono<String> generateBriefResponse(String userMessage) {
        GeminiRequestDto requestDto = GeminiRequestDto.createBriefResponse(userMessage);

        return callGeminiApi(requestDto);
    }

    // 상세한 응답 생성 (200 토큰, 구조화된 답변)
    public Mono<String> generateDetailedResponse(String userMessage) {
        GeminiRequestDto requestDto = GeminiRequestDto.createForCocktailChat(userMessage, true);

        return callGeminiApi(requestDto);
    }

    // 창의적인 응답 생성 (500 토큰, 높은 창의성)
    public Mono<String> generateCreativeResponse(String userMessage) {
        GeminiRequestDto requestDto = GeminiRequestDto.createCreativeResponse(userMessage);

        return callGeminiApi(requestDto);
    }

    // 사용자 지정 파라미터로 응답 생성 ( 커스텀 )
    public Mono<String> generateCustomResponse(
            String userMessage,
            Double temperature,
            Integer maxTokens,
            Double topP,
            Integer topK
    ) {
        GeminiRequestDto.GenerationConfig config = GeminiRequestDto.GenerationConfig.builder()
                .temperature(temperature != null ? temperature : 0.8)
                .maxOutputTokens(maxTokens != null ? maxTokens : 200)
                .topP(topP != null ? topP : 0.95)
                .topK(topK != null ? topK : 40)
                .candidateCount(1)
                .build();

        GeminiRequestDto requestDto = new GeminiRequestDto(userMessage, config);

        return callGeminiApi(requestDto);
    }

    // 메시지 유형에 따른 최적화된 응답 생성
    public Mono<String> generateOptimizedResponse(String userMessage, MessageType messageType) {
        GeminiRequestDto requestDto = switch (messageType) {
            case RECIPE -> createRecipeRequest(userMessage);
            case RECOMMENDATION -> createRecommendationRequest(userMessage);
            case QUESTION -> createQuestionRequest(userMessage);
            case CASUAL_CHAT -> createCasualChatRequest(userMessage);
            default -> new GeminiRequestDto(userMessage);
        };

        return callGeminiApi(requestDto);
    }

    // 레시피 요청 (구조화된 답변 필요)
    private GeminiRequestDto createRecipeRequest(String message) {
        GeminiRequestDto.GenerationConfig config = GeminiRequestDto.GenerationConfig.builder()
                .temperature(0.3)  // 낮은 temperature로 정확성 향상
                .topP(0.8)
                .topK(20)
                .maxOutputTokens(400)  // 레시피는 좀 더 길게
                .candidateCount(1)
                .build();

        return new GeminiRequestDto(message, config);
    }

    // 추천 요청 (적당한 창의성)
    private GeminiRequestDto createRecommendationRequest(String message) {
        GeminiRequestDto.GenerationConfig config = GeminiRequestDto.GenerationConfig.builder()
                .temperature(0.9)  // 다양한 추천을 위해 높게
                .topP(0.95)
                .topK(40)
                .maxOutputTokens(250)
                .candidateCount(1)
                .build();

        return new GeminiRequestDto(message, config);
    }

    // 일반 질문 (균형잡힌 설정)
    private GeminiRequestDto createQuestionRequest(String message) {
        GeminiRequestDto.GenerationConfig config = GeminiRequestDto.GenerationConfig.builder()
                .temperature(0.7)
                .topP(0.9)
                .topK(30)
                .maxOutputTokens(200)
                .candidateCount(1)
                .build();

        return new GeminiRequestDto(message, config);
    }

    // 캐주얼한 대화 (자연스러움 중시)
    private GeminiRequestDto createCasualChatRequest(String message) {
        GeminiRequestDto.GenerationConfig config = GeminiRequestDto.GenerationConfig.builder()
                .temperature(1.0)
                .topP(0.95)
                .topK(40)
                .maxOutputTokens(150)
                .candidateCount(1)
                .build();

        return new GeminiRequestDto(message, config);
    }

    // Gemini API 호출 공통 메서드
    private Mono<String> callGeminiApi(GeminiRequestDto requestDto) {
        log.debug("Gemini API 호출 - Temperature: {}, MaxTokens: {}",
                requestDto.getGenerationConfig() != null ? requestDto.getGenerationConfig().getTemperature() : "default",
                requestDto.getGenerationConfig() != null ? requestDto.getGenerationConfig().getMaxOutputTokens() : "default"
        );

        return geminiWebClient.post()
                .uri("?key=" + apiKey)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(GeminiResponseDto.class)
                .map(GeminiResponseDto::getGeneratedText)
                .doOnSuccess(response -> log.debug("응답 길이: {} 글자", response != null ? response.length() : 0))
                .doOnError(error -> log.error("Gemini API 호출 실패: ", error))
                .onErrorReturn("죄송합니다. 현재 응답을 생성할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    // 유형 ENUM 정의
    public enum MessageType {
        RECIPE,         // 레시피 요청
        RECOMMENDATION, // 추천 요청
        QUESTION,       // 일반 질문
        CASUAL_CHAT,    // 캐주얼 대화
        UNKNOWN         // 분류 불가
    }

    // 메시지 유형 감지 (간단한 키워드 기반)
    public static MessageType detectMessageType(String message) {
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("레시피") || lowerMessage.contains("만드는") ||
                lowerMessage.contains("제조") || lowerMessage.contains("recipe")) {
            return MessageType.RECIPE;
        } else if (lowerMessage.contains("추천") || lowerMessage.contains("어때") ||
                lowerMessage.contains("뭐가 좋") || lowerMessage.contains("recommend")) {
            return MessageType.RECOMMENDATION;
        } else if (lowerMessage.contains("?") || lowerMessage.contains("뭐") ||
                lowerMessage.contains("어떻") || lowerMessage.contains("왜")) {
            return MessageType.QUESTION;
        } else {
            return MessageType.CASUAL_CHAT;
        }
    }
}