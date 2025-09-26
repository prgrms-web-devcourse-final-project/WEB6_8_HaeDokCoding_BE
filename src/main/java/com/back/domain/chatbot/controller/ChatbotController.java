package com.back.domain.chatbot.controller;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
import com.back.domain.chatbot.dto.StepRecommendationRequestDto;
import com.back.domain.chatbot.dto.StepRecommendationResponseDto;
import com.back.domain.chatbot.entity.ChatConversation;
import com.back.domain.chatbot.service.ChatbotService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<RsData<ChatResponseDto>> sendMessage(@Valid @RequestBody ChatRequestDto requestDto) {
        try {
            ChatResponseDto response = chatbotService.sendMessage(requestDto);
            return ResponseEntity.ok(RsData.successOf(response));
        } catch (Exception e) {
            log.error("채팅 메시지 처리 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(RsData.failOf("서버 오류가 발생했습니다."));
        }
    }

    @GetMapping("/history/user/{userId}")
    public ResponseEntity<RsData<List<ChatConversation>>> getUserChatHistory(@PathVariable Long userId) {
        try {
            List<ChatConversation> history = chatbotService.getUserChatHistory(userId);
            return ResponseEntity.ok(RsData.successOf(history));
        } catch (Exception e) {
            log.error("사용자 채팅 기록 조회 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(RsData.failOf("서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/step-recommendation")
    public ResponseEntity<RsData<StepRecommendationResponseDto>> getStepRecommendation(@Valid @RequestBody StepRecommendationRequestDto requestDto) {
        try {
            StepRecommendationResponseDto response = chatbotService.getStepRecommendation(requestDto);
            return ResponseEntity.ok(RsData.successOf(response));
        } catch (Exception e) {
            log.error("단계별 추천 처리 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(RsData.failOf("서버 오류가 발생했습니다."));
        }
    }
}