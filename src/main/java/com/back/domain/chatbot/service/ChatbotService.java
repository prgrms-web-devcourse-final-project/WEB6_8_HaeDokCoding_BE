package com.back.domain.chatbot.service;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
import com.back.domain.chatbot.dto.StepRecommendationResponseDto;
import com.back.domain.chatbot.entity.ChatConversation;
import com.back.domain.chatbot.repository.ChatConversationRepository;
import com.back.domain.cocktail.dto.CocktailSummaryResponseDto;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import com.back.domain.cocktail.repository.CocktailRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatModel chatModel;
    private final ChatConversationRepository chatConversationRepository;
    private final CocktailRepository cocktailRepository;


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
        try {
            // 단계별 추천 모드 확인 (currentStep이 있으면 무조건 단계별 추천 모드)
            if (requestDto.isStepRecommendation() ||
                requestDto.getCurrentStep() != null ||
                isStepRecommendationTrigger(requestDto.getMessage())) {
                log.info("Recommendation chat mode for userId: {}", requestDto.getUserId());
                return handleStepRecommendation(requestDto);
            }

            log.info("Normal chat mode for userId: {}", requestDto.getUserId());

            // 메시지 타입 감지
            MessageType messageType = detectMessageType(requestDto.getMessage());

            // 최근 대화 기록 조회 (최신 5개)
            List<ChatConversation> recentChats =
                chatConversationRepository.findTop5ByUserIdOrderByCreatedAtDesc(requestDto.getUserId());

            // 대화 히스토리를 시간순으로 정렬 (오래된 것부터)
            Collections.reverse(recentChats);

            // 대화 컨텍스트 생성
            String conversationContext = buildConversationContext(recentChats);

            // ChatClient 빌더 생성
            var promptBuilder = chatClient.prompt()
                    .system(buildSystemMessage(messageType) + conversationContext)
                    .user(buildUserMessage(requestDto.getMessage(), messageType));

            // RAG 기능은 향후 구현 예정 (Vector DB 설정 필요)

            // 응답 생성
            String response = promptBuilder
                    .options(getOptionsForMessageType(messageType))
                    .call()
                    .content();

            // 응답 후처리
            response = postProcessResponse(response, messageType);

            // 대화 저장 (sessionId 없이)
            saveConversation(requestDto, response);

            return new ChatResponseDto(response);

        } catch (Exception e) {
            log.error("채팅 응답 생성 중 오류 발생: ", e);
            return handleError(e);
        }
    }


    private String buildConversationContext(List<ChatConversation> recentChats) {
        if (recentChats.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("\n\n【최근 대화 기록】\n");
        for (ChatConversation chat : recentChats) {
            context.append("사용자: ").append(chat.getUserMessage()).append("\n");
            context.append("봇: ").append(chat.getBotResponse()).append("\n\n");
        }
        context.append("위 대화를 참고하여 자연스럽게 이어지는 답변을 해주세요.\n");

        return context.toString();
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

    private void saveConversation(ChatRequestDto requestDto, String response) {
        ChatConversation conversation = ChatConversation.builder()
                .userId(requestDto.getUserId())
                .userMessage(requestDto.getMessage())
                .botResponse(response)
                .createdAt(LocalDateTime.now())
                .build();

        chatConversationRepository.save(conversation);
    }

    private ChatResponseDto handleError(Exception e) {
        String errorMessage = "죄송합니다. 잠시 후 다시 시도해주세요.";

        if (e.getMessage().contains("rate limit")) {
            errorMessage = "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
        } else if (e.getMessage().contains("timeout")) {
            errorMessage = "응답 시간이 초과되었습니다. 다시 시도해주세요.";
        }

        return new ChatResponseDto(errorMessage);
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

    // 단계별 추천 시작 키워드 감지
    private boolean isStepRecommendationTrigger(String message) {
        String lower = message.toLowerCase().trim();
        return lower.contains("단계별 추천");
    }

    // 단계별 추천 처리 통합 메서드
    private ChatResponseDto handleStepRecommendation(ChatRequestDto requestDto) {
        Integer currentStep = requestDto.getCurrentStep();

        // 단계가 지정되지 않았거나 첫 시작인 경우
        if (currentStep == null || currentStep <= 0) {
            currentStep = 1;
        }

        StepRecommendationResponseDto stepRecommendation;
        String chatResponse;

        switch (currentStep) {
            case 1:
                stepRecommendation = getAlcoholStrengthOptions();
                chatResponse = "단계별 맞춤 추천을 시작합니다! 🎯\n원하시는 도수를 선택해주세요!";
                break;
            case 2:
                stepRecommendation = getAlcoholBaseTypeOptions(requestDto.getSelectedAlcoholStrength());
                chatResponse = "좋은 선택이네요! 이제 베이스가 될 술을 선택해주세요 🍸";
                break;
            case 3:
                stepRecommendation = getCocktailTypeOptions(requestDto.getSelectedAlcoholStrength(), requestDto.getSelectedAlcoholBaseType());
                chatResponse = "완벽해요! 마지막으로 어떤 스타일로 즐기실 건가요? 🥃";
                break;
            case 4:
                stepRecommendation = getFinalRecommendations(
                    requestDto.getSelectedAlcoholStrength(),
                    requestDto.getSelectedAlcoholBaseType(),
                    requestDto.getSelectedCocktailType()
                );
                chatResponse = stepRecommendation.getStepTitle();
                break;
            default:
                stepRecommendation = getAlcoholStrengthOptions();
                chatResponse = "단계별 맞춤 추천을 시작합니다! 🎯";
        }

        // 대화 기록 저장
        saveConversation(requestDto, chatResponse);

        return new ChatResponseDto(chatResponse, stepRecommendation);
    }

    @Transactional(readOnly = true)
    public List<ChatConversation> getUserChatHistory(Long userId) {
        return chatConversationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
    }


    private StepRecommendationResponseDto getAlcoholStrengthOptions() {
        List<StepRecommendationResponseDto.StepOption> options = new ArrayList<>();

        for (AlcoholStrength strength : AlcoholStrength.values()) {
            options.add(new StepRecommendationResponseDto.StepOption(
                strength.name(),
                strength.getDescription(),
                null
            ));
        }

        return new StepRecommendationResponseDto(
            1,
            "원하시는 도수를 선택해주세요!",
            options,
            null,
            false
        );
    }

    private StepRecommendationResponseDto getAlcoholBaseTypeOptions(AlcoholStrength alcoholStrength) {
        List<StepRecommendationResponseDto.StepOption> options = new ArrayList<>();

        for (AlcoholBaseType baseType : AlcoholBaseType.values()) {
            options.add(new StepRecommendationResponseDto.StepOption(
                baseType.name(),
                baseType.getDescription(),
                null
            ));
        }

        return new StepRecommendationResponseDto(
            2,
            "베이스가 될 술을 선택해주세요!",
            options,
            null,
            false
        );
    }

    private StepRecommendationResponseDto getCocktailTypeOptions(AlcoholStrength alcoholStrength, AlcoholBaseType alcoholBaseType) {
        List<StepRecommendationResponseDto.StepOption> options = new ArrayList<>();

        for (CocktailType cocktailType : CocktailType.values()) {
            options.add(new StepRecommendationResponseDto.StepOption(
                cocktailType.name(),
                cocktailType.getDescription(),
                null
            ));
        }

        return new StepRecommendationResponseDto(
            3,
            "어떤 종류의 잔으로 드시겠어요?",
            options,
            null,
            false
        );
    }

    private StepRecommendationResponseDto getFinalRecommendations(
            AlcoholStrength alcoholStrength,
            AlcoholBaseType alcoholBaseType,
            CocktailType cocktailType) {
        // 필터링 조건에 맞는 칵테일 검색
        List<AlcoholStrength> strengths = List.of(alcoholStrength);
        List<AlcoholBaseType> baseTypes = List.of(alcoholBaseType);
        List<CocktailType> cocktailTypes = List.of(cocktailType);

        Page<Cocktail> cocktailPage = cocktailRepository.searchWithFilters(
            null, // 키워드 없음
            strengths,
            cocktailTypes,
            baseTypes,
            PageRequest.of(0, 5) // 최대 5개 추천
        );

        List<CocktailSummaryResponseDto> recommendations = cocktailPage.getContent().stream()
            .map(cocktail -> new CocktailSummaryResponseDto(
                cocktail.getId(),
                cocktail.getCocktailName(),
                cocktail.getCocktailImgUrl(),
                cocktail.getAlcoholStrength()
            ))
            .collect(Collectors.toList());

        String stepTitle = recommendations.isEmpty()
            ? "조건에 맞는 칵테일을 찾을 수 없습니다 😢"
            : "당신을 위한 맞춤 칵테일 추천입니다! 🍹";

        return new StepRecommendationResponseDto(
            4,
            stepTitle,
            null,
            recommendations,
            true
        );
    }

}

