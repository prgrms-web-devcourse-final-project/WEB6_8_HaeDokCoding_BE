package com.back.domain.chatbot.service;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
import com.back.domain.chatbot.entity.ChatConversation;
import com.back.domain.chatbot.repository.ChatConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatModel chatModel;
    private final ChatConversationRepository chatConversationRepository;

    // 세션별 메모리 관리를 위한 Map
    private final Map<String, InMemoryChatMemory> sessionMemories = new HashMap<>();

    @Value("classpath:prompts/chatbot-system-prompt.txt")
    private Resource systemPromptResource;

    @Value("classpath:prompts/chatbot-response-rules.txt")
    private Resource responseRulesResource;

    @Value("${chatbot.history.max-conversation-count:5}")
    private int maxConversationCount;

    private String systemPrompt;
    private String responseRules;
    private ChatClient chatClient;

    @PostConstruct
    public void init() throws IOException {
        this.systemPrompt = StreamUtils.copyToString(
                systemPromptResource.getInputStream(),
                StandardCharsets.UTF_8
        );
        this.responseRules = StreamUtils.copyToString(
                responseRulesResource.getInputStream(),
                StandardCharsets.UTF_8
        );

        // ChatClient 초기화
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .build();

        log.info("Spring AI 챗봇 초기화 완료. 시스템 프롬프트 길이: {} 문자", systemPrompt.length());
    }

    @Transactional
    public ChatResponseDto sendMessage(ChatRequestDto requestDto) {
        String sessionId = requestDto.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        try {
            // 세션별 메모리 가져오기 또는 생성
            InMemoryChatMemory chatMemory = getOrCreateSessionMemory(sessionId);

            // 이전 대화 기록 로드
            loadConversationHistory(sessionId, chatMemory);

            // ChatClient를 사용한 응답 생성
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(buildUserMessage(requestDto.getMessage()))
                    .advisors(new MessageChatMemoryAdvisor(chatMemory))
                    .call()
                    .content();

            // 대화 저장
            saveConversation(requestDto, response, sessionId);

            return new ChatResponseDto(response, sessionId);

        } catch (Exception e) {
            log.error("채팅 응답 생성 중 오류 발생: ", e);
            return new ChatResponseDto(
                    "죄송합니다. 오류가 발생했습니다. 다시 시도해주세요.",
                    sessionId
            );
        }
    }

    private InMemoryChatMemory getOrCreateSessionMemory(String sessionId) {
        return sessionMemories.computeIfAbsent(
                sessionId,
                k -> new InMemoryChatMemory()
        );
    }

    private void loadConversationHistory(String sessionId, InMemoryChatMemory chatMemory) {
        List<ChatConversation> recentConversations =
                chatConversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        int maxHistory = Math.min(recentConversations.size(), maxConversationCount);
        int startIdx = Math.max(0, recentConversations.size() - maxHistory);

        for (int i = startIdx; i < recentConversations.size(); i++) {
            ChatConversation conv = recentConversations.get(i);
            chatMemory.add(new UserMessage(conv.getUserMessage()));
            chatMemory.add(new AssistantMessage(conv.getBotResponse()));
        }
    }

    private String buildUserMessage(String userMessage) {
        return userMessage + "\n\n" + responseRules;
    }

    private void saveConversation(ChatRequestDto requestDto, String response, String sessionId) {
        ChatConversation conversation = ChatConversation.builder()
                .userId(requestDto.getUserId())
                .userMessage(requestDto.getMessage())
                .botResponse(response)
                .sessionId(sessionId)
                .build();

        chatConversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<ChatConversation> getChatHistory(String sessionId) {
        return chatConversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional(readOnly = true)
    public List<ChatConversation> getUserChatHistory(Long userId, String sessionId) {
        return chatConversationRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
    }

    // 메모리 정리를 위한 메서드 (오래된 세션 제거)
    public void cleanupInactiveSessions() {
        // 필요시 구현: 일정 시간 이상 사용하지 않은 세션 메모리 제거
        sessionMemories.entrySet().removeIf(entry -> {
            // 구현 예: 30분 이상 비활성 세션 제거
            return false; // 실제 로직 구현 필요
        });
    }
}