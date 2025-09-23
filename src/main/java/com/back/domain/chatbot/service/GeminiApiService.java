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

    public Mono<String> generateResponse(String userMessage) {
        GeminiRequestDto requestDto = new GeminiRequestDto(userMessage);

        return geminiWebClient.post()
                .uri("?key=" + apiKey)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(GeminiResponseDto.class)
                .map(GeminiResponseDto::getGeneratedText)
                .doOnError(error -> log.error("Gemini API 호출 실패: ", error))
                .onErrorReturn("죄송합니다. 현재 응답을 생성할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }
}