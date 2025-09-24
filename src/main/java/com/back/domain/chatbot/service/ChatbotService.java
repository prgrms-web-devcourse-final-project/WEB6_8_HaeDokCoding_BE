package com.back.domain.chatbot.service;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
import com.back.domain.chatbot.entity.ChatConversation;
import com.back.domain.chatbot.repository.ChatConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final GeminiApiService geminiApiService;
    private final ChatConversationRepository chatConversationRepository;

    @Value("classpath:prompts/chatbot-system-prompt.txt")
    private Resource systemPromptResource;

    @Value("classpath:prompts/chatbot-response-rules.txt")
    private Resource responseRulesResource;

    @Value("${chatbot.history.max-conversation-count:5}")
    private int maxConversationCount;

    private String systemPrompt;
    private String responseRules;

    @PostConstruct
    public void init() throws IOException {
        this.systemPrompt = StreamUtils.copyToString(systemPromptResource.getInputStream(), StandardCharsets.UTF_8);
        this.responseRules = StreamUtils.copyToString(responseRulesResource.getInputStream(), StandardCharsets.UTF_8);
        log.info("챗봇 시스템 프롬프트가 로드되었습니다. (길이: {} 문자)", systemPrompt.length());
    }

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
        List<ChatConversation> recentConversations = getRecentConversations(sessionId);

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append(systemPrompt).append("\n\n");

        appendConversationHistory(contextBuilder, recentConversations);
        appendCurrentQuestion(contextBuilder, userMessage);
        appendResponseInstructions(contextBuilder);

        return contextBuilder.toString();
    }

    private List<ChatConversation> getRecentConversations(String sessionId) {
        return chatConversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private void appendConversationHistory(StringBuilder contextBuilder, List<ChatConversation> conversations) {
        if (!conversations.isEmpty()) {
            contextBuilder.append("=== 이전 대화 기록 ===\n");

            int maxHistory = Math.min(conversations.size(), maxConversationCount);
            int startIdx = Math.max(0, conversations.size() - maxHistory);

            for (int i = startIdx; i < conversations.size(); i++) {
                ChatConversation conv = conversations.get(i);
                contextBuilder.append("사용자: ").append(conv.getUserMessage()).append("\n");
                contextBuilder.append("AI 바텐더: ").append(conv.getBotResponse()).append("\n\n");
            }
            contextBuilder.append("=================\n\n");
        }
    }

    private void appendCurrentQuestion(StringBuilder contextBuilder, String userMessage) {
        contextBuilder.append("현재 사용자 질문: ").append(userMessage).append("\n\n");
    }

    private void appendResponseInstructions(StringBuilder contextBuilder) {
        contextBuilder.append(responseRules);
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