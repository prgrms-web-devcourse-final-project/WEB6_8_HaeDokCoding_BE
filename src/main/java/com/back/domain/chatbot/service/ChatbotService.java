package com.back.domain.chatbot.service;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
import com.back.domain.chatbot.entity.ChatConversation;
import com.back.domain.chatbot.repository.ChatConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.openai.OpenAiChatOptions;
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
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatModel chatModel;
    private final ChatConversationRepository chatConversationRepository;

    // 세션별 메모리 관리 (Thread-Safe)
    private final ConcurrentHashMap<String, InMemoryChatMemory> sessionMemories = new ConcurrentHashMap<>();

    @Value("classpath:prompts/chatbot-system-prompt.txt")
    private Resource systemPromptResource;

    @Value("classpath:prompts/chatbot-response-rules.txt")
    private Resource responseRulesResource;

    @Value("${chatbot.history.max-conversation-count:5}")
    private int maxConversationCount;

    @Value("${spring.ai.openai.chat.options.temperature:0.8}")
    private Double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens:300}")
    private Integer maxTokens;

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

        // ChatClient 고급 설정
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultOptions(OpenAiChatOptions.builder()
                        .withTemperature(temperature)
                        .withMaxTokens(maxTokens)
                        .build())
                .build();

        log.info("Spring AI 챗봇 초기화 완료. Temperature: {}, MaxTokens: {}", temperature, maxTokens);
    }

    @Transactional
    public ChatResponseDto sendMessage(ChatRequestDto requestDto) {
        String sessionId = ensureSessionId(requestDto.getSessionId());

        try {
            // 메시지 타입 감지
            MessageType messageType = detectMessageType(requestDto.getMessage());

            // 세션별 메모리 가져오기
            InMemoryChatMemory chatMemory = getOrCreateSessionMemory(sessionId);

            // 이전 대화 기록 로드
            loadConversationHistory(sessionId, chatMemory);

            // ChatClient 빌더 생성
            var promptBuilder = chatClient.prompt()
                    .system(buildSystemMessage(messageType))
                    .user(buildUserMessage(requestDto.getMessage(), messageType))
                    .advisors(new MessageChatMemoryAdvisor(chatMemory));

            // RAG 기능은 향후 구현 예정 (Vector DB 설정 필요)

            // 응답 생성
            String response = promptBuilder
                    .options(getOptionsForMessageType(messageType))
                    .call()
                    .content();

            // 응답 후처리
            response = postProcessResponse(response, messageType);

            // 대화 저장
            saveConversation(requestDto, response, sessionId);

            return new ChatResponseDto(response, sessionId);

        } catch (Exception e) {
            log.error("채팅 응답 생성 중 오류 발생: ", e);
            return handleError(sessionId, e);
        }
    }

    private String ensureSessionId(String sessionId) {
        return (sessionId == null || sessionId.isEmpty())
                ? UUID.randomUUID().toString()
                : sessionId;
    }

    private InMemoryChatMemory getOrCreateSessionMemory(String sessionId) {
        return sessionMemories.computeIfAbsent(
                sessionId,
                k -> new InMemoryChatMemory()
        );
    }

    private void loadConversationHistory(String sessionId, InMemoryChatMemory chatMemory) {
        List<ChatConversation> conversations =
                chatConversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        // 최근 N개의 대화만 메모리에 로드
        String sessionIdForMemory = sessionId;
        conversations.stream()
                .skip(Math.max(0, conversations.size() - maxConversationCount))
                .forEach(conv -> {
                    chatMemory.add(sessionIdForMemory, new UserMessage(conv.getUserMessage()));
                    chatMemory.add(sessionIdForMemory, new AssistantMessage(conv.getBotResponse()));
                });
    }

    private String buildSystemMessage(MessageType type) {
        StringBuilder sb = new StringBuilder(systemPrompt);

        // 메시지 타입별 추가 지시사항
        switch (type) {
            case RECIPE:
                sb.append("\n\n【레시피 답변 모드】정확한 재료 비율과 제조 순서를 강조하세요.");
                break;
            case RECOMMENDATION:
                sb.append("\n\n【추천 모드】다양한 선택지와 각각의 특징을 설명하세요.");
                break;
            case QUESTION:
                sb.append("\n\n【질문 답변 모드】정확하고 신뢰할 수 있는 정보를 제공하세요.");
                break;
            default:
                break;
        }

        return sb.toString();
    }

    private String buildUserMessage(String userMessage, MessageType type) {
        return userMessage + "\n\n" + responseRules;
    }

    private OpenAiChatOptions getOptionsForMessageType(MessageType type) {
        return switch (type) {
            case RECIPE -> OpenAiChatOptions.builder()
                    .withTemperature(0.3)  // 정확성 중시
                    .withMaxTokens(400)     // 레시피는 길게
                    .build();
            case RECOMMENDATION -> OpenAiChatOptions.builder()
                    .withTemperature(0.9)  // 다양성 중시
                    .withMaxTokens(250)
                    .build();
            case QUESTION -> OpenAiChatOptions.builder()
                    .withTemperature(0.7)  // 균형
                    .withMaxTokens(200)
                    .build();
            default -> OpenAiChatOptions.builder()
                    .withTemperature(temperature)
                    .withMaxTokens(maxTokens)
                    .build();
        };
    }


    private String postProcessResponse(String response, MessageType type) {
        // 응답 길이 제한 확인
        if (response.length() > 500) {
            response = response.substring(0, 497) + "...";
        }

        // 이모지 추가 (타입별)
        if (type == MessageType.RECIPE && !response.contains("🍹")) {
            response = "🍹 " + response;
        }

        return response;
    }

    private void saveConversation(ChatRequestDto requestDto, String response, String sessionId) {
        ChatConversation conversation = ChatConversation.builder()
                .userId(requestDto.getUserId())
                .userMessage(requestDto.getMessage())
                .botResponse(response)
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();

        chatConversationRepository.save(conversation);
    }

    private ChatResponseDto handleError(String sessionId, Exception e) {
        String errorMessage = "죄송합니다. 잠시 후 다시 시도해주세요.";

        if (e.getMessage().contains("rate limit")) {
            errorMessage = "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
        } else if (e.getMessage().contains("timeout")) {
            errorMessage = "응답 시간이 초과되었습니다. 다시 시도해주세요.";
        }

        return new ChatResponseDto(errorMessage, sessionId);
    }

    public enum MessageType {
        RECIPE, RECOMMENDATION, QUESTION, CASUAL_CHAT
    }

    private MessageType detectMessageType(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("레시피") || lower.contains("만드는") ||
                lower.contains("제조") || lower.contains("recipe")) {
            return MessageType.RECIPE;
        } else if (lower.contains("추천") || lower.contains("어때") ||
                lower.contains("뭐가 좋") || lower.contains("recommend")) {
            return MessageType.RECOMMENDATION;
        } else if (lower.contains("?") || lower.contains("뭐") ||
                lower.contains("어떻") || lower.contains("왜")) {
            return MessageType.QUESTION;
        }

        return MessageType.CASUAL_CHAT;
    }

    @Transactional(readOnly = true)
    public List<ChatConversation> getChatHistory(String sessionId) {
        return chatConversationRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional(readOnly = true)
    public List<ChatConversation> getUserChatHistory(Long userId, String sessionId) {
        return chatConversationRepository.findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
    }

    // 정기적인 메모리 정리 (스케줄러로 호출)
    public void cleanupInactiveSessions() {
        long thirtyMinutesAgo = System.currentTimeMillis() - (30 * 60 * 1000);

        sessionMemories.entrySet().removeIf(entry -> {
            // 실제로는 마지막 사용 시간을 추적해야 함
            return false;
        });

        log.info("세션 메모리 정리 완료. 현재 활성 세션: {}", sessionMemories.size());
    }
}

