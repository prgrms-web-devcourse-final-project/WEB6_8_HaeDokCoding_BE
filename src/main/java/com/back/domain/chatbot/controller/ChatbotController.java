package com.back.domain.chatbot.controller;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
import com.back.domain.chatbot.dto.SaveBotMessageDto;
import com.back.domain.chatbot.entity.ChatConversation;
import com.back.domain.chatbot.service.ChatbotService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "채팅 메시지 보내기", description = "자율형 대화 및 단계별 추천 두가지 모드 지원")
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
    @Operation(summary = "유저 대화 히스토리", description = "사용자 채팅 기록 조회")
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

    @PostMapping("/bot-message")
    @Operation(summary = "봇 메시지 저장", description = "FE에서 생성한 봇 메시지(인사말 등)를 DB에 저장")
    public ResponseEntity<RsData<ChatConversation>> saveBotMessage(@Valid @RequestBody SaveBotMessageDto requestDto) {
        try {
            ChatConversation savedMessage = chatbotService.saveBotMessage(requestDto);
            return ResponseEntity.ok(RsData.successOf(savedMessage));
        } catch (Exception e) {
            log.error("봇 메시지 저장 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(RsData.failOf("서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/greeting/{userId}")
    @Operation(summary = "인사말 생성", description = "사용자가 채팅을 시작할 때 기본 인사말을 생성하고 저장")
    public ResponseEntity<RsData<ChatResponseDto>> createGreeting(@PathVariable Long userId) {
        try {
            ChatResponseDto greeting = chatbotService.createGreetingMessage(userId);
            return ResponseEntity.ok(RsData.successOf(greeting));
        } catch (Exception e) {
            log.error("인사말 생성 중 오류 발생: ", e);
            return ResponseEntity.internalServerError()
                    .body(RsData.failOf("서버 오류가 발생했습니다."));
        }
    }

}