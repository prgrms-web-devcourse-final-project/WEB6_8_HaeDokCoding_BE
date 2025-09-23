package com.back.domain.chatbot.service;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
import com.back.domain.chatbot.entity.ChatConversation;
import com.back.domain.chatbot.repository.ChatConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final GeminiApiService geminiApiService;
    private final ChatConversationRepository chatConversationRepository;

    @Transactional
    public ChatResponseDto sendMessage(ChatRequestDto requestDto) {
        String sessionId = requestDto.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        try {
            String contextualMessage = buildContextualMessage(requestDto.getMessage(), sessionId);

            String botResponse = geminiApiService.generateResponse(contextualMessage).block();

            ChatConversation conversation = ChatConversation.builder()
                    .userId(requestDto.getUserId())
                    .userMessage(requestDto.getMessage())
                    .botResponse(botResponse)
                    .sessionId(sessionId)
                    .build();

            chatConversationRepository.save(conversation);

            return new ChatResponseDto(botResponse, sessionId);

        } catch (Exception e) {
            log.error("채팅 응답 생성 중 오류 발생: ", e);
            return new ChatResponseDto("죄송합니다. 오류가 발생했습니다. 다시 시도해주세요.", sessionId);
        }
    }

    private String buildContextualMessage(String userMessage, String sessionId) {
        List<ChatConversation> recentConversations = chatConversationRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        if (recentConversations.isEmpty()) {
            return "당신은 칵테일 전문 챗봇입니다. 칵테일에 관련된 질문에 친근하고 도움이 되는 답변을 해주세요. 질문: " + userMessage;
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("당신은 칵테일 전문 챗봇입니다. 다음은 이전 대화 내용입니다:\n\n");

        int maxHistory = Math.min(recentConversations.size(), 5);
        for (int i = Math.max(0, recentConversations.size() - maxHistory); i < recentConversations.size(); i++) {
            ChatConversation conv = recentConversations.get(i);
            contextBuilder.append("사용자: ").append(conv.getUserMessage()).append("\n");
            contextBuilder.append("챗봇: ").append(conv.getBotResponse()).append("\n\n");
        }

        contextBuilder.append("새로운 질문: ").append(userMessage);
        contextBuilder.append("\n\n이전 대화 맥락을 고려하여 친근하고 도움이 되는 답변을 해주세요.");

        return contextBuilder.toString();
    }

    @Transactional(readOnly = true)
    public List<ChatConversation> getChatHistory(String sessionId) {
        return chatConversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional(readOnly = true)
    public List<ChatConversation> getUserChatHistory(Long userId, String sessionId) {
        return chatConversationRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
    }
}