package com.back.domain.chatbot.service;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
import com.back.domain.chatbot.dto.CocktailPreferenceDto;
import com.back.domain.chatbot.dto.SaveBotMessageDto;
import com.back.domain.chatbot.dto.StepRecommendationResponseDto;
import com.back.domain.chatbot.entity.ChatConversation;
import com.back.domain.chatbot.enums.MessageSender;
import com.back.domain.chatbot.enums.MessageType;
import com.back.domain.chatbot.repository.ChatConversationRepository;
import com.back.domain.cocktail.dto.CocktailSummaryResponseDto;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import com.back.domain.cocktail.repository.CocktailRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ChatModel chatModel;
    private final ChatConversationRepository chatConversationRepository;
    private final CocktailRepository cocktailRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        saveUserMessage(requestDto);

        try {
            Integer currentStep = requestDto.getCurrentStep();

            // ========== 1순위: currentStep 명시적 제어 ==========
            if (currentStep != null) {
                log.info("[EXPLICIT] currentStep={}, userId={}, mode={}",
                        currentStep, requestDto.getUserId(),
                        currentStep == 0 ? "QA" : "STEP");

                if (currentStep == 0) {
                    // 질문형 추천 선택 시 안내 메시지와 INPUT 타입 반환
                    if ("QA".equalsIgnoreCase(requestDto.getMessage()) ||
                            requestDto.getMessage().contains("질문형")) {

                        log.info("질문형 추천 시작 - userId: {}", requestDto.getUserId());

                        // 사용자 선택 메시지 저장
                        ChatConversation userChoice = ChatConversation.builder()
                                .userId(requestDto.getUserId())
                                .message("질문형 취향 찾기")
                                .sender(MessageSender.USER)
                                .createdAt(LocalDateTime.now())
                                .build();
                        chatConversationRepository.save(userChoice);

                        String guideMessage = "칵테일에 관련된 질문을 입력해주세요!";

                        ChatConversation botGuide = ChatConversation.builder()
                                .userId(requestDto.getUserId())
                                .message(guideMessage)
                                .sender(MessageSender.CHATBOT)
                                .createdAt(LocalDateTime.now())
                                .build();
                        ChatConversation savedGuide = chatConversationRepository.save(botGuide);

                        // INPUT 타입으로 반환하여 사용자 입력 유도
                        return ChatResponseDto.builder()
                                .id(savedGuide.getId())
                                .userId(requestDto.getUserId())
                                .message(guideMessage)
                                .sender(MessageSender.CHATBOT)
                                .type(MessageType.INPUT)
                                .createdAt(savedGuide.getCreatedAt())
                                .metaData(ChatResponseDto.MetaData.builder()
                                        .currentStep(0)
                                        .actionType("질문형 추천")
                                        .build())
                                .build();
                    }

                    // 실제 질문이 들어온 경우 - AI 기반 칵테일 추천
                    log.info("질문형 추천 모드 진입 - userId: {}", requestDto.getUserId());
                    return generateQARecommendation(requestDto);
                }
                else if (currentStep >= 1 && currentStep <= 4) {
                    // 단계별 추천
                    log.info("단계별 추천 모드 진입 - Step: {}, userId: {}",
                            currentStep, requestDto.getUserId());
                    return handleStepRecommendation(requestDto);
                }
                else {
                    // 유효하지 않은 step 값
                    log.warn("유효하지 않은 currentStep: {}, userId: {}", currentStep, requestDto.getUserId());
                    return createErrorResponse("잘못된 단계 정보입니다.");
                }
            }

            // ========== 2순위: 키워드 감지 (하위 호환성) ==========
            if (isStepRecommendationTrigger(requestDto.getMessage())) {
                log.info("[LEGACY] 키워드 기반 단계별 추천 감지 - userId: {}", requestDto.getUserId());
                requestDto.setCurrentStep(1);
                return handleStepRecommendation(requestDto);
            }

            // ========== 3순위: 기본 일반 대화 ==========
            log.info("[DEFAULT] 일반 대화 모드 - userId: {}", requestDto.getUserId());
            ChatConversation savedResponse = generateAIResponse(requestDto);

            return ChatResponseDto.builder()
                    .id(savedResponse.getId())
                    .userId(requestDto.getUserId())
                    .message(savedResponse.getMessage())
                    .sender(MessageSender.CHATBOT)
                    .type(MessageType.TEXT)
                    .createdAt(savedResponse.getCreatedAt())
                    .build();

        } catch (Exception e) {
            log.error("채팅 응답 생성 중 오류 발생: ", e);
            return createErrorResponse("죄송합니다. 일시적인 오류가 발생했습니다.");
        }
    }

    /**
     * 질문형 추천 - AI가 질문을 분석하여 칵테일 추천
     */
    private ChatResponseDto generateQARecommendation(ChatRequestDto requestDto) {
        String userQuestion = requestDto.getMessage();

        // 1. AI를 통해 사용자 질문 분석 및 추천 칵테일 목록 생성
        List<String> recommendedCocktailNames = analyzeCocktailRequest(userQuestion);

        // 2. DB에서 칵테일 검색 (최대 7개 검색하여 3개 선택)
        List<CocktailSummaryResponseDto> recommendations = new ArrayList<>();
        for (String cocktailName : recommendedCocktailNames) {
            if (recommendations.size() >= 3) break;

            // 칵테일 이름으로 검색
            Page<Cocktail> cocktailPage = cocktailRepository.searchWithFilters(
                    cocktailName,
                    null,
                    null,
                    null,
                    PageRequest.of(0, 1)
            );

            if (!cocktailPage.isEmpty()) {
                Cocktail cocktail = cocktailPage.getContent().get(0);
                recommendations.add(new CocktailSummaryResponseDto(
                        cocktail.getId(),
                        cocktail.getCocktailName(),
                        cocktail.getCocktailNameKo(),
                        cocktail.getCocktailImgUrl(),
                        cocktail.getAlcoholStrength().getDescription()
                ));
            }
        }

        // 3. 추천 결과가 없으면 일반 텍스트 응답
        if (recommendations.isEmpty()) {
            return generateTextResponse(requestDto, userQuestion);
        }

        // 4. AI를 통해 추천 메시지 생성
        String recommendationMessage = generateRecommendationMessage(userQuestion, recommendations);

        // 5. RESTART 옵션 추가
        List<StepRecommendationResponseDto.StepOption> restartOption = List.of(
                new StepRecommendationResponseDto.StepOption(
                        "RESTART",
                        "다시 시작하기",
                        null
                )
        );

        // 6. StepRecommendationResponseDto 생성
        StepRecommendationResponseDto stepData = new StepRecommendationResponseDto(
                0,  // 질문형은 step 0
                recommendationMessage,
                restartOption,  // RESTART 옵션 추가
                recommendations,
                true
        );

        // 7. 봇 응답 저장
        ChatConversation savedResponse = saveBotResponse(
                requestDto.getUserId(),
                recommendationMessage,
                stepData
        );

        // 8. ChatResponseDto 반환
        return ChatResponseDto.builder()
                .id(savedResponse.getId())
                .userId(requestDto.getUserId())
                .message(recommendationMessage)
                .sender(MessageSender.CHATBOT)
                .type(MessageType.CARD_LIST)
                .stepData(stepData)
                .createdAt(savedResponse.getCreatedAt())
                .metaData(ChatResponseDto.MetaData.builder()
                        .currentStep(0)
                        .actionType("질문형 추천")
                        .build())
                .build();
    }

    /**
     * AI를 통해 사용자 질문 분석하여 추천할 칵테일 이름 목록 반환
     */
    private List<String> analyzeCocktailRequest(String userQuestion) {
        String analysisPrompt = """
            사용자가 다음과 같은 칵테일 관련 질문을 했습니다:
            "%s"
            
            이 질문에 가장 적합한 칵테일을 최대 7개까지 추천해주세요.
            다음 형식으로만 응답하세요 (칵테일 이름만, 한 줄에 하나씩):
            칵테일이름1
            칵테일이름2
            칵테일이름3
            ...
            
            주의사항:
            - 영문 칵테일 이름만 작성
            - 부가 설명 없이 칵테일 이름만
            - 실제 존재하는 유명한 칵테일만 추천
            """.formatted(userQuestion);

        try {
            String response = chatClient.prompt()
                    .system("당신은 칵테일 전문가입니다. 사용자 질문에 맞는 칵테일을 추천합니다.")
                    .user(analysisPrompt)
                    .options(OpenAiChatOptions.builder()
                            .withTemperature(0.7)
                            .withMaxTokens(400)
                            .build())
                    .call()
                    .content();

            // 응답을 줄 단위로 파싱하여 칵테일 이름 목록 생성
            List<String> cocktailNames = response.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .limit(7)
                    .collect(Collectors.toList());

            log.info("AI 추천 칵테일 목록: {}", cocktailNames);
            return cocktailNames;

        } catch (Exception e) {
            log.error("칵테일 분석 중 오류: ", e);
            // 오류 시 기본 칵테일 목록 반환
            return List.of("Mojito", "Margarita", "Cosmopolitan", "Martini", "Daiquiri");
        }
    }

    /**
     * AI를 통해 추천 메시지 생성
     */
    private String generateRecommendationMessage(String userQuestion, List<CocktailSummaryResponseDto> recommendations) {
        String cocktailList = recommendations.stream()
                .map(c -> c.cocktailNameKo() != null ? c.cocktailNameKo() : c.cocktailName())
                .collect(Collectors.joining(", "));

        String messagePrompt = """
            사용자가 "%s"라고 질문했습니다.
            
            다음 칵테일들을 추천합니다: %s
            
            사용자의 질문을 반영한 친근한 추천 메시지를 100자 이내로 작성해주세요.
            '쑤리'라는 바텐더 캐릭터로 답변하며, 사용자 질문의 핵심을 언급하면서 칵테일 추천을 자연스럽게 연결하세요.
            이모지를 1-2개 포함하세요.
            """.formatted(userQuestion, cocktailList);

        try {
            String message = chatClient.prompt()
                    .system(systemPrompt)
                    .user(messagePrompt)
                    .options(OpenAiChatOptions.builder()
                            .withTemperature(0.8)
                            .withMaxTokens(150)
                            .build())
                    .call()
                    .content();

            message += "\n칵테일의 자세한 정보는 '상세보기'를 클릭해서 확인할 수 있어요.\n" +
                    "마음에 드는 칵테일은 '킵' 버튼을 눌러 나만의 Bar에 저장해보세요!";

            return message.trim();

        } catch (Exception e) {
            log.error("추천 메시지 생성 중 오류: ", e);
            return "🍹 요청하신 칵테일을 찾아봤어요! 쑤리가 엄선한 칵테일들을 추천해드릴게요." +
                    "\n\n칵테일의 자세한 정보는 '상세보기'를 클릭해서 확인할 수 있어요.\n" +
                    "마음에 드는 칵테일은 '킵' 버튼을 눌러 나만의 Bar에 저장해보세요!";
        }
    }

    /**
     * 추천할 칵테일이 없을 경우 일반 텍스트 응답 생성
     */
    private ChatResponseDto generateTextResponse(ChatRequestDto requestDto, String userQuestion) {
        ChatConversation savedResponse = generateAIResponse(requestDto);

        return ChatResponseDto.builder()
                .id(savedResponse.getId())
                .userId(requestDto.getUserId())
                .message(savedResponse.getMessage())
                .sender(MessageSender.CHATBOT)
                .type(MessageType.TEXT)
                .createdAt(savedResponse.getCreatedAt())
                .metaData(ChatResponseDto.MetaData.builder()
                        .currentStep(0)
                        .actionType("질문형 추천")
                        .build())
                .build();
    }

    private void saveUserMessage(ChatRequestDto requestDto) {
        String metadata = null;
        if (requestDto.getSelectedValue() != null) {
            try {
                metadata = objectMapper.writeValueAsString(Map.of("selectedValue", requestDto.getSelectedValue()));
            } catch (JsonProcessingException e) {
                log.error("사용자 선택 값 JSON 직렬화 실패", e);
            }
        }

        ChatConversation userMessage = ChatConversation.builder()
                .userId(requestDto.getUserId())
                .message(requestDto.getMessage())
                .sender(MessageSender.USER)
                .createdAt(LocalDateTime.now())
                .metadata(metadata)
                .build();
        chatConversationRepository.save(userMessage);
    }

    private ChatConversation saveBotResponse(Long userId, String message, Object stepData) {
        String metadata = null;
        if (stepData != null) {
            try {
                metadata = objectMapper.writeValueAsString(stepData);
            } catch (JsonProcessingException e) {
                log.error("봇 응답 메타데이터 JSON 직렬화 실패", e);
            }
        }

        ChatConversation botResponse = ChatConversation.builder()
                .userId(userId)
                .message(message)
                .sender(MessageSender.CHATBOT)
                .createdAt(LocalDateTime.now())
                .metadata(metadata)
                .build();
        return chatConversationRepository.save(botResponse);
    }

    /**
     * 대화 컨텍스트 빌드
     */
    private String buildConversationContext(List<ChatConversation> recentChats) {
        if (recentChats.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("\n\n【최근 대화 기록】\n");

        List<ChatConversation> orderedChats = new ArrayList<>(recentChats);
        orderedChats.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));

        for (ChatConversation chat : orderedChats) {
            if (chat.getSender() == MessageSender.USER) {
                context.append("사용자: ").append(chat.getMessage()).append("\n");
            } else {
                context.append("봇: ").append(chat.getMessage()).append("\n");
            }
        }
        context.append("\n위 대화를 참고하여 자연스럽게 이어지는 답변을 해주세요.\n");

        return context.toString();
    }

    @Transactional
    public ChatConversation saveConversation(ChatRequestDto requestDto, String response) {
        ChatConversation userMessage = ChatConversation.builder()
                .userId(requestDto.getUserId())
                .message(requestDto.getMessage())
                .sender(MessageSender.USER)
                .createdAt(LocalDateTime.now())
                .build();
        chatConversationRepository.save(userMessage);

        ChatConversation botResponse = ChatConversation.builder()
                .userId(requestDto.getUserId())
                .message(response)
                .sender(MessageSender.CHATBOT)
                .createdAt(LocalDateTime.now())
                .build();
        return chatConversationRepository.save(botResponse);
    }

    @Transactional(readOnly = true)
    public List<ChatResponseDto> getUserChatHistory(Long userId) {
        List<ChatConversation> history = chatConversationRepository.findByUserIdOrderByCreatedAtAsc(userId);

        return history.stream().map(conversation -> {
            ChatResponseDto.ChatResponseDtoBuilder builder = ChatResponseDto.builder()
                    .id(conversation.getId())
                    .userId(conversation.getUserId())
                    .message(conversation.getMessage())
                    .sender(conversation.getSender())
                    .createdAt(conversation.getCreatedAt());

            String metadata = conversation.getMetadata();
            if (metadata != null && !metadata.isEmpty()) {
                try {
                    if (conversation.getSender() == MessageSender.CHATBOT) {
                        StepRecommendationResponseDto stepData = objectMapper.readValue(metadata, StepRecommendationResponseDto.class);
                        builder.stepData(stepData);

                        if (stepData.getOptions() != null && !stepData.getOptions().isEmpty()) {
                            builder.type(MessageType.RADIO_OPTIONS);
                        } else if (stepData.getRecommendations() != null && !stepData.getRecommendations().isEmpty()) {
                            builder.type(MessageType.CARD_LIST);
                        } else {
                            builder.type(MessageType.TEXT);
                        }
                    } else {
                        builder.type(MessageType.TEXT);
                    }
                } catch (JsonProcessingException e) {
                    log.error("대화 기록 metadata 역직렬화 실패 [ID: {}]: {}", conversation.getId(), e.getMessage());
                    builder.type(MessageType.TEXT);
                }
            } else {
                builder.type(MessageType.TEXT);
            }
            return builder.build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public ChatConversation saveBotMessage(SaveBotMessageDto dto) {
        ChatConversation botMessage = ChatConversation.builder()
                .userId(dto.getUserId())
                .message(dto.getMessage())
                .sender(MessageSender.CHATBOT)
                .createdAt(LocalDateTime.now())
                .build();

        return chatConversationRepository.save(botMessage);
    }

    @Transactional
    public ChatResponseDto createGreetingMessage(Long userId) {
        String greetingMessage = "안녕하세요! 🍹 바텐더 '쑤리'에요.\n" +
                "취향에 맞는 칵테일을 추천해드릴게요!\n" +
                "어떤 유형으로 찾아드릴까요?";

        List<StepRecommendationResponseDto.StepOption> options = List.of(
                new StepRecommendationResponseDto.StepOption(
                        "QA",
                        "질문형 취향 찾기",
                        null
                ),
                new StepRecommendationResponseDto.StepOption(
                        "STEP",
                        "단계별 취향 찾기",
                        null
                )
        );

        StepRecommendationResponseDto stepData = new StepRecommendationResponseDto(
                0,
                greetingMessage,
                options,
                null,
                false
        );

        boolean greetingExists = chatConversationRepository.existsByUserIdAndMessage(userId, greetingMessage);

        ChatConversation savedGreeting = null;
        if (!greetingExists) {
            ChatConversation greeting = ChatConversation.builder()
                    .userId(userId)
                    .message(greetingMessage)
                    .sender(MessageSender.CHATBOT)
                    .createdAt(LocalDateTime.now())
                    .build();
            savedGreeting = chatConversationRepository.save(greeting);
            log.info("인사말 저장 완료 - userId: {}", userId);
        } else {
            log.info("이미 인사말이 존재하여 저장 생략 - userId: {}", userId);
        }

        return ChatResponseDto.builder()
                .id(savedGreeting != null ? savedGreeting.getId() : null)
                .userId(userId)
                .message(greetingMessage)
                .sender(MessageSender.CHATBOT)
                .type(MessageType.RADIO_OPTIONS)
                .stepData(stepData)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isFirstConversation(Long userId) {
        return chatConversationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).isEmpty();
    }

    private String buildSystemMessage(InternalMessageType type) {
        StringBuilder sb = new StringBuilder(systemPrompt);

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

    private String buildUserMessage(String userMessage, InternalMessageType type) {
        return userMessage + "\n\n" + responseRules;
    }

    private OpenAiChatOptions getOptionsForMessageType(InternalMessageType type) {
        return switch (type) {
            case RECIPE -> OpenAiChatOptions.builder()
                    .withTemperature(0.3)
                    .withMaxTokens(400)
                    .build();
            case RECOMMENDATION -> OpenAiChatOptions.builder()
                    .withTemperature(0.9)
                    .withMaxTokens(400)
                    .build();
            case QUESTION -> OpenAiChatOptions.builder()
                    .withTemperature(0.7)
                    .withMaxTokens(maxTokens)
                    .build();
            default -> OpenAiChatOptions.builder()
                    .withTemperature(temperature)
                    .withMaxTokens(maxTokens)
                    .build();
        };
    }

    private String postProcessResponse(String response, InternalMessageType type) {
        if (response.length() > 500) {
            response = response.substring(0, 497) + "...";
        }

        if (type == InternalMessageType.RECIPE && !response.contains("🍹")) {
            response = "🍹 " + response;
        }

        return response;
    }

    private ChatConversation generateAIResponse(ChatRequestDto requestDto) {
        log.info("Normal chat mode for userId: {}", requestDto.getUserId());

        InternalMessageType messageType = detectMessageType(requestDto.getMessage());

        List<ChatConversation> recentChats =
                chatConversationRepository.findTop20ByUserIdOrderByCreatedAtDesc(requestDto.getUserId());

        String conversationContext = buildConversationContext(recentChats);

        var promptBuilder = chatClient.prompt()
                .system(buildSystemMessage(messageType) + conversationContext)
                .user(buildUserMessage(requestDto.getMessage(), messageType));

        String response = promptBuilder
                .options(getOptionsForMessageType(messageType))
                .call()
                .content();

        response = postProcessResponse(response, messageType);

        return saveBotResponse(requestDto.getUserId(), response, null);
    }

    public ChatResponseDto createLoadingMessage() {
        return ChatResponseDto.builder()
                .message("응답을 생성하는 중...")
                .sender(MessageSender.CHATBOT)
                .type(MessageType.LOADING)
                .createdAt(LocalDateTime.now())
                .metaData(ChatResponseDto.MetaData.builder()
                        .isTyping(true)
                        .build())
                .build();
    }

    public enum InternalMessageType {
        RECIPE, RECOMMENDATION, QUESTION, CASUAL_CHAT
    }

    private InternalMessageType detectMessageType(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("레시피") || lower.contains("만드는") ||
                lower.contains("제조") || lower.contains("recipe")) {
            return InternalMessageType.RECIPE;
        } else if (lower.contains("추천") || lower.contains("어때") ||
                lower.contains("뭐가 좋") || lower.contains("recommend")) {
            return InternalMessageType.RECOMMENDATION;
        } else if (lower.contains("?") || lower.contains("뭐") ||
                lower.contains("어떻") || lower.contains("왜")) {
            return InternalMessageType.QUESTION;
        }

        return InternalMessageType.CASUAL_CHAT;
    }

    @Deprecated
    private boolean isStepRecommendationTrigger(String message) {
        log.warn("레거시 키워드 감지 사용됨. currentStep 사용 권장. message: {}", message);
        String lower = message.toLowerCase().trim();
        return lower.contains("단계별 취향 찾기");
    }

    private ChatResponseDto createErrorResponse(String errorMessage) {
        return ChatResponseDto.builder()
                .message(errorMessage)
                .sender(MessageSender.CHATBOT)
                .type(MessageType.ERROR)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ChatResponseDto handleStepRecommendation(ChatRequestDto requestDto) {
        Integer currentStep = requestDto.getCurrentStep();

        if (currentStep == 1 && "STEP".equalsIgnoreCase(requestDto.getMessage())) {
            ChatConversation userChoice = ChatConversation.builder()
                    .userId(requestDto.getUserId())
                    .message("단계별 취향 찾기")
                    .sender(MessageSender.USER)
                    .createdAt(LocalDateTime.now())
                    .build();
            chatConversationRepository.save(userChoice);
        }

        if (currentStep == null || currentStep <= 0) {
            currentStep = 1;
        }

        StepRecommendationResponseDto stepData;
        String message;
        MessageType type;

        switch (currentStep) {
            case 1:
                stepData = getAlcoholStrengthOptions();
                message = "단계별 맞춤 취향 추천을 시작합니다! 🎯\n원하시는 도수를 선택해주세요!";
                type = MessageType.RADIO_OPTIONS;
                break;

            case 2:
                // 논알콜 선택 여부에 따라 다른 옵션 제공
                boolean isNonAlcoholic = "NON_ALCOHOLIC".equals(requestDto.getSelectedAlcoholStrength());

                if (isNonAlcoholic) {
                    // 논알콜인 경우: 글라스 타입 선택
                    stepData = getCocktailTypeOptions();
                    message = "논알콜 칵테일이네요! 🥤\n어떤 스타일의 칵테일을 원하시나요?";
                } else {
                    // 알콜인 경우: 베이스 타입 선택
                    stepData = getAlcoholBaseTypeOptions(parseAlcoholStrength(requestDto.getSelectedAlcoholStrength()));
                    message = "좋은 선택이네요! \n이제 베이스가 될 술을 선택해주세요 🍸";
                }
                type = MessageType.RADIO_OPTIONS;
                break;

            case 3:
                stepData = new StepRecommendationResponseDto(
                        3,
                        null,
                        null,
                        null,
                        false
                );
                message = "좋아요! 이제 원하는 칵테일 스타일을 자유롭게 말씀해주세요 💬\n없으면 'x', 또는 '없음'을 입력해주세요!";
                type = MessageType.INPUT;
                break;

            case 4:
                // 논알콜 여부 다시 확인
                boolean isNonAlcoholicFinal = "NON_ALCOHOLIC".equals(requestDto.getSelectedAlcoholStrength());

                if (isNonAlcoholicFinal) {
                    // 논알콜: 도수와 칵테일 타입으로 검색
                    stepData = getFinalRecommendationsForNonAlcoholic(
                            parseCocktailType(requestDto.getSelectedCocktailType()),
                            requestDto.getMessage()
                    );
                } else {
                    // 알콜: 도수와 베이스 타입으로 검색
                    stepData = getFinalRecommendationsWithMessage(
                            parseAlcoholStrength(requestDto.getSelectedAlcoholStrength()),
                            parseAlcoholBaseType(requestDto.getSelectedAlcoholBaseType()),
                            requestDto.getMessage()
                    );
                }
                message = stepData.getStepTitle();
                type = MessageType.CARD_LIST;
                break;

            default:
                stepData = getAlcoholStrengthOptions();
                message = "단계별 맞춤 취향 추천을 시작합니다! 🎯";
                type = MessageType.RADIO_OPTIONS;
        }

        ChatConversation savedResponse = saveBotResponse(requestDto.getUserId(), message, stepData);

        ChatResponseDto.MetaData metaData = ChatResponseDto.MetaData.builder()
                .currentStep(currentStep)
                .totalSteps(4)
                .isTyping(type != MessageType.CARD_LIST)
                .delay(300)
                .build();

        return ChatResponseDto.builder()
                .id(savedResponse.getId())
                .userId(requestDto.getUserId())
                .message(message)
                .sender(MessageSender.CHATBOT)
                .type(type)
                .stepData(stepData)
                .metaData(metaData)
                .createdAt(savedResponse.getCreatedAt())
                .build();
    }

    private StepRecommendationResponseDto getCocktailTypeOptions() {
        List<StepRecommendationResponseDto.StepOption> options = new ArrayList<>();

        options.add(new StepRecommendationResponseDto.StepOption(
                "ALL",
                "전체",
                null
        ));

        for (CocktailType type : CocktailType.values()) {
            options.add(new StepRecommendationResponseDto.StepOption(
                    type.name(),
                    type.getDescription(),
                    null
            ));
        }

        return new StepRecommendationResponseDto(
                2,
                "어떤 스타일의 칵테일을 원하시나요?",
                options,
                null,
                false
        );
    }

    private CocktailType parseCocktailType(String value) {
        if (value == null || value.trim().isEmpty() || "ALL".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return CocktailType.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid CocktailType value: {}", value);
            return null;
        }
    }

    private AlcoholStrength parseAlcoholStrength(String value) {
        if (value == null || value.trim().isEmpty() || "ALL".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return AlcoholStrength.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid AlcoholStrength value: {}", value);
            return null;
        }
    }

    private AlcoholBaseType parseAlcoholBaseType(String value) {
        if (value == null || value.trim().isEmpty() || "ALL".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return AlcoholBaseType.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid AlcoholBaseType value: {}", value);
            return null;
        }
    }

    private StepRecommendationResponseDto getAlcoholStrengthOptions() {
        List<StepRecommendationResponseDto.StepOption> options = new ArrayList<>();

        options.add(new StepRecommendationResponseDto.StepOption(
                "ALL",
                "전체",
                null
        ));

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

        options.add(new StepRecommendationResponseDto.StepOption(
                "ALL",
                "전체",
                null
        ));

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

    private StepRecommendationResponseDto getFinalRecommendationsWithMessage(
            AlcoholStrength alcoholStrength,
            AlcoholBaseType alcoholBaseType,
            String userMessage) {

        List<CocktailSummaryResponseDto> recommendations;

        // 사용자가 의미있는 메시지를 입력한 경우 AI 기반 검색 사용
        boolean hasValidMessage = userMessage != null
                && !userMessage.trim().isEmpty()
                && !userMessage.trim().equalsIgnoreCase("x")
                && !userMessage.trim().equals("없음");

        if (hasValidMessage) {
            log.info("AI 기반 스마트 검색 시작 - 사용자 입력: {}", userMessage);

            try {
                // 1. AI로 사용자 선호도 분석
                CocktailPreferenceDto preference = analyzeUserPreference(
                        userMessage,
                        alcoholStrength,
                        alcoholBaseType
                );

                // 2. AI 분석 기반 스마트 검색
                List<Cocktail> candidates = searchCocktailsWithAI(
                        preference,
                        alcoholStrength,
                        alcoholBaseType
                );

                // 3. AI로 최종 순위 결정 (상위 3개)
                List<Cocktail> topCocktails = rankCocktailsWithAI(userMessage, candidates);

                recommendations = topCocktails.stream()
                        .map(cocktail -> new CocktailSummaryResponseDto(
                                cocktail.getId(),
                                cocktail.getCocktailName(),
                                cocktail.getCocktailNameKo(),
                                cocktail.getCocktailImgUrl(),
                                cocktail.getAlcoholStrength().getDescription()
                        ))
                        .collect(Collectors.toList());

                log.info("AI 기반 검색 완료 - 추천 칵테일: {}개", recommendations.size());

            } catch (Exception e) {
                log.error("AI 기반 검색 중 오류 발생, 기본 검색으로 폴백: ", e);
                // 오류 시 기본 키워드 검색으로 폴백
                recommendations = performBasicSearch(alcoholStrength, alcoholBaseType, userMessage);
            }
        } else {
            // 입력이 없거나 "x", "없음"인 경우 기본 필터 검색
            log.info("기본 필터 검색 사용 - 입력 없음");
            recommendations = performBasicSearch(alcoholStrength, alcoholBaseType, null);
        }

        String stepTitle = recommendations.isEmpty()
                ? "조건에 맞는 칵테일을 찾을 수 없습니다 😢"
                : "짠🎉🎉\n" +
                "칵테일의 자세한 정보는 '상세보기'를 클릭해서 확인할 수 있어요.\n" +
                "마음에 드는 칵테일은 '킵' 버튼을 눌러 나만의 Bar에 저장해보세요!";

        // RESTART 옵션 추가
        List<StepRecommendationResponseDto.StepOption> restartOption = List.of(
                new StepRecommendationResponseDto.StepOption(
                        "RESTART",
                        "다시 시작하기",
                        null
                )
        );

        return new StepRecommendationResponseDto(
                4,
                stepTitle,
                restartOption,
                recommendations,
                true
        );
    }

    //기본 키워드 검색 수행 (AI 실패 시 폴백용)
    private List<CocktailSummaryResponseDto> performBasicSearch(
            AlcoholStrength alcoholStrength,
            AlcoholBaseType alcoholBaseType,
            String keyword) {

        List<AlcoholStrength> strengths = (alcoholStrength == null) ? null : List.of(alcoholStrength);
        List<AlcoholBaseType> baseTypes = (alcoholBaseType == null) ? null : List.of(alcoholBaseType);

        Page<Cocktail> cocktailPage = cocktailRepository.searchWithFilters(
                keyword,
                strengths,
                null,
                baseTypes,
                PageRequest.of(0, 3)
        );

        return cocktailPage.getContent().stream()
                .map(cocktail -> new CocktailSummaryResponseDto(
                        cocktail.getId(),
                        cocktail.getCocktailName(),
                        cocktail.getCocktailNameKo(),
                        cocktail.getCocktailImgUrl(),
                        cocktail.getAlcoholStrength().getDescription()
                ))
                .collect(Collectors.toList());
    }
    private StepRecommendationResponseDto getFinalRecommendationsForNonAlcoholic(
            CocktailType cocktailType,
            String userMessage) {

        List<CocktailSummaryResponseDto> recommendations;

        // 사용자가 의미있는 메시지를 입력한 경우 AI 기반 검색 사용
        boolean hasValidMessage = userMessage != null
                && !userMessage.trim().isEmpty()
                && !userMessage.trim().equalsIgnoreCase("x")
                && !userMessage.trim().equals("없음");

        if (hasValidMessage) {
            log.info("논알콜 AI 기반 스마트 검색 시작 - 사용자 입력: {}", userMessage);

            try {
                // 1. AI로 사용자 선호도 분석 (논알콜 전용)
                CocktailPreferenceDto preference = analyzeUserPreference(
                        userMessage,
                        AlcoholStrength.NON_ALCOHOLIC,
                        null  // 논알콜은 베이스 타입 없음
                );

                // 2. AI 분석 기반 스마트 검색 (논알콜 필터링)
                List<Cocktail> candidates = searchCocktailsWithAIForNonAlcoholic(
                        preference,
                        cocktailType
                );

                // 3. AI로 최종 순위 결정 (상위 3개)
                List<Cocktail> topCocktails = rankCocktailsWithAI(userMessage, candidates);

                recommendations = topCocktails.stream()
                        .map(cocktail -> new CocktailSummaryResponseDto(
                                cocktail.getId(),
                                cocktail.getCocktailName(),
                                cocktail.getCocktailNameKo(),
                                cocktail.getCocktailImgUrl(),
                                cocktail.getAlcoholStrength().getDescription()
                        ))
                        .collect(Collectors.toList());

                log.info("논알콜 AI 기반 검색 완료 - 추천 칵테일: {}개", recommendations.size());

            } catch (Exception e) {
                log.error("논알콜 AI 기반 검색 중 오류 발생, 기본 검색으로 폴백: ", e);
                // 오류 시 기본 키워드 검색으로 폴백
                recommendations = performBasicSearchForNonAlcoholic(cocktailType, userMessage);
            }
        } else {
            // 입력이 없거나 "x", "없음"인 경우 기본 필터 검색
            log.info("논알콜 기본 필터 검색 사용 - 입력 없음");
            recommendations = performBasicSearchForNonAlcoholic(cocktailType, null);
        }

        String stepTitle = recommendations.isEmpty()
                ? "조건에 맞는 논알콜 칵테일을 찾을 수 없습니다 😢"
                : "짠🎉🎉 논알콜 칵테일 추천!\n" +
                "칵테일의 자세한 정보는 '상세보기'를 클릭해서 확인할 수 있어요.\n" +
                "마음에 드는 칵테일은 '킵' 버튼을 눌러 나만의 Bar에 저장해보세요!";

        // RESTART 옵션 추가
        List<StepRecommendationResponseDto.StepOption> restartOption = List.of(
                new StepRecommendationResponseDto.StepOption(
                        "RESTART",
                        "다시 시작하기",
                        null
                )
        );

        return new StepRecommendationResponseDto(
                4,
                stepTitle,
                restartOption,
                recommendations,
                true
        );
    }

    private List<Cocktail> searchCocktailsWithAIForNonAlcoholic(
            CocktailPreferenceDto preference,
            CocktailType cocktailType) {

        List<Cocktail> candidates = new ArrayList<>();
        List<AlcoholStrength> strengths = List.of(AlcoholStrength.NON_ALCOHOLIC);
        List<CocktailType> types = (cocktailType == null) ? null : List.of(cocktailType);

        // 1차: LLM이 추천한 칵테일 이름으로 직접 검색
        if (preference.getSuggestedCocktails() != null && !preference.getSuggestedCocktails().isEmpty()) {
            for (String cocktailName : preference.getSuggestedCocktails()) {
                if (candidates.size() >= 10) break;

                Page<Cocktail> page = cocktailRepository.searchWithFilters(
                        cocktailName,
                        strengths,
                        types,
                        null,  // 논알콜은 베이스 타입 없음
                        PageRequest.of(0, 1)
                );

                if (!page.isEmpty()) {
                    Cocktail found = page.getContent().get(0);
                    if (!candidates.contains(found)) {
                        candidates.add(found);
                        log.info("논알콜 1차 검색 성공: {} (추천 칵테일명)", cocktailName);
                    }
                }
            }
        }

        // 2차: 키워드 기반 검색
        if (candidates.size() < 10 && preference.getKeywords() != null && !preference.getKeywords().isEmpty()) {
            for (String keyword : preference.getKeywords()) {
                if (candidates.size() >= 10) break;

                Page<Cocktail> page = cocktailRepository.searchWithFilters(
                        keyword,
                        strengths,
                        types,
                        null,
                        PageRequest.of(0, 3)
                );

                for (Cocktail cocktail : page.getContent()) {
                    if (candidates.size() >= 10) break;
                    if (!candidates.contains(cocktail)) {
                        candidates.add(cocktail);
                        log.info("논알콜 2차 검색 성공: {} (키워드: {})", cocktail.getCocktailName(), keyword);
                    }
                }
            }
        }

        // 3차: 부족하면 기본 필터로 보충
        if (candidates.size() < 3) {
            Page<Cocktail> page = cocktailRepository.searchWithFilters(
                    null,
                    strengths,
                    types,
                    null,
                    PageRequest.of(0, 10)
            );

            for (Cocktail cocktail : page.getContent()) {
                if (candidates.size() >= 10) break;
                if (!candidates.contains(cocktail)) {
                    candidates.add(cocktail);
                    log.info("논알콜 3차 검색 성공: {} (기본 필터)", cocktail.getCocktailName());
                }
            }
        }

        log.info("논알콜 총 {}개 칵테일 후보 검색 완료", candidates.size());
        return candidates;
    }


    private List<CocktailSummaryResponseDto> performBasicSearchForNonAlcoholic(
            CocktailType cocktailType,
            String keyword) {

        List<AlcoholStrength> strengths = List.of(AlcoholStrength.NON_ALCOHOLIC);
        List<CocktailType> types = (cocktailType == null) ? null : List.of(cocktailType);

        Page<Cocktail> cocktailPage = cocktailRepository.searchWithFilters(
                keyword,
                strengths,
                types,
                null,
                PageRequest.of(0, 3)
        );

        return cocktailPage.getContent().stream()
                .map(cocktail -> new CocktailSummaryResponseDto(
                        cocktail.getId(),
                        cocktail.getCocktailName(),
                        cocktail.getCocktailNameKo(),
                        cocktail.getCocktailImgUrl(),
                        cocktail.getAlcoholStrength().getDescription()
                ))
                .collect(Collectors.toList());
    }

    private CocktailPreferenceDto analyzeUserPreference(
            String userMessage,
            AlcoholStrength alcoholStrength,
            AlcoholBaseType alcoholBaseType) {

        String strengthInfo = alcoholStrength != null
                ? alcoholStrength.getDescription()
                : "지정 안됨";
        String baseInfo = alcoholBaseType != null
                ? alcoholBaseType.getDescription()
                : "지정 안됨";

        String analysisPrompt = String.format("""
                사용자가 다음과 같은 칵테일 요구사항을 입력했습니다:
                "%s"

                추가 정보:
                - 선택된 도수: %s
                - 선택된 베이스 술: %s

                이 정보를 바탕으로 사용자가 원하는 칵테일을 찾기 위한 키워드와 추천 칵테일을 분석해주세요.

                다음 JSON 형식으로만 응답하세요 (부가 설명 없이 JSON만):
                {
                  "keywords": ["키워드1", "키워드2", "키워드3"],
                  "suggestedCocktails": ["칵테일이름1", "칵테일이름2", "칵테일이름3"],
                  "flavorProfile": "맛프로필",
                  "mood": "분위기"
                }

                주의사항:
                - keywords: 사용자 입력에서 추출한 검색 키워드 (영문, 한글 혼합 가능, 최대 7개)
                - suggestedCocktails: 실제 존재하는 유명한 칵테일 이름만 (영문, 최대 5개)
                - flavorProfile: sweet, bitter, sour, fruity 등 맛 특성 (영문 단어 1-2개)
                - mood: party, romantic, refreshing 등 분위기 (영문 단어 1개)
                - JSON 형식만 출력하고 다른 텍스트는 포함하지 마세요
                """,
                userMessage,
                strengthInfo,
                baseInfo
        );

        try {
            String response = chatClient.prompt()
                    .system("당신은 칵테일 전문 분석가입니다. 사용자 요구사항을 정확히 분석하여 JSON 형식으로만 응답합니다.")
                    .user(analysisPrompt)
                    .options(OpenAiChatOptions.builder()
                            .withTemperature(0.5)
                            .withMaxTokens(500)
                            .build())
                    .call()
                    .content();

            // JSON 파싱
            CocktailPreferenceDto preference = objectMapper.readValue(response, CocktailPreferenceDto.class);
            log.info("AI 분석 결과 - keywords: {}, suggested: {}, flavor: {}, mood: {}",
                    preference.getKeywords(),
                    preference.getSuggestedCocktails(),
                    preference.getFlavorProfile(),
                    preference.getMood());

            return preference;

        } catch (Exception e) {
            log.error("사용자 선호도 분석 중 오류: ", e);
            // 오류 시 기본값 반환
            return CocktailPreferenceDto.builder()
                    .keywords(List.of(userMessage))
                    .suggestedCocktails(List.of())
                    .flavorProfile("unknown")
                    .mood("casual")
                    .build();
        }
    }

    private List<Cocktail> searchCocktailsWithAI(
            CocktailPreferenceDto preference,
            AlcoholStrength alcoholStrength,
            AlcoholBaseType alcoholBaseType) {

        List<Cocktail> candidates = new ArrayList<>();
        List<AlcoholStrength> strengths = (alcoholStrength == null) ? null : List.of(alcoholStrength);
        List<AlcoholBaseType> baseTypes = (alcoholBaseType == null) ? null : List.of(alcoholBaseType);

        // 1차: LLM이 추천한 칵테일 이름으로 직접 검색
        if (preference.getSuggestedCocktails() != null && !preference.getSuggestedCocktails().isEmpty()) {
            for (String cocktailName : preference.getSuggestedCocktails()) {
                if (candidates.size() >= 10) break;

                Page<Cocktail> page = cocktailRepository.searchWithFilters(
                        cocktailName,
                        strengths,
                        null,
                        baseTypes,
                        PageRequest.of(0, 1)
                );

                if (!page.isEmpty()) {
                    Cocktail found = page.getContent().get(0);
                    if (!candidates.contains(found)) {
                        candidates.add(found);
                        log.info("1차 검색 성공: {} (추천 칵테일명)", cocktailName);
                    }
                }
            }
        }

        // 2차: 키워드 기반 검색
        if (candidates.size() < 10 && preference.getKeywords() != null && !preference.getKeywords().isEmpty()) {
            for (String keyword : preference.getKeywords()) {
                if (candidates.size() >= 10) break;

                Page<Cocktail> page = cocktailRepository.searchWithFilters(
                        keyword,
                        strengths,
                        null,
                        baseTypes,
                        PageRequest.of(0, 3)
                );

                for (Cocktail cocktail : page.getContent()) {
                    if (candidates.size() >= 10) break;
                    if (!candidates.contains(cocktail)) {
                        candidates.add(cocktail);
                        log.info("2차 검색 성공: {} (키워드: {})", cocktail.getCocktailName(), keyword);
                    }
                }
            }
        }

        // 3차: 부족하면 기본 필터로 보충
        if (candidates.size() < 3) {
            Page<Cocktail> page = cocktailRepository.searchWithFilters(
                    null,
                    strengths,
                    null,
                    baseTypes,
                    PageRequest.of(0, 10)
            );

            for (Cocktail cocktail : page.getContent()) {
                if (candidates.size() >= 10) break;
                if (!candidates.contains(cocktail)) {
                    candidates.add(cocktail);
                    log.info("3차 검색 성공: {} (기본 필터)", cocktail.getCocktailName());
                }
            }
        }

        log.info("총 {}개 칵테일 후보 검색 완료", candidates.size());
        return candidates;
    }

    // 상위 3개 칵테일을 AI로 재정렬
    private List<Cocktail> rankCocktailsWithAI(String userMessage, List<Cocktail> candidates) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        if (candidates.size() <= 3) {
            return candidates;
        }

        // 칵테일 후보 목록을 텍스트로 변환
        StringBuilder candidatesText = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            Cocktail c = candidates.get(i);
            candidatesText.append(String.format(
                    "%d. %s (%s) - 도수: %s, 베이스: %s\n",
                    i,
                    c.getCocktailNameKo() != null ? c.getCocktailNameKo() : c.getCocktailName(),
                    c.getCocktailName(),
                    c.getAlcoholStrength().getDescription(),
                    c.getAlcoholBaseType() != null ? c.getAlcoholBaseType().getDescription() : "없음"
            ));
        }

        String rankingPrompt = String.format("""
                사용자 요청: "%s"

                다음 칵테일 후보 중에서 사용자 요청에 가장 적합한 순서대로 3개의 인덱스만 선택해주세요:
                %s

                응답 형식 (대괄호 안에 인덱스 3개만, 예: [2,0,5]):
                [인덱스1,인덱스2,인덱스3]

                주의사항:
                - 사용자 요청과의 적합도를 최우선으로 고려
                - 정확히 3개의 인덱스만 반환
                - 다른 텍스트 없이 대괄호 형식만 출력
                """,
                userMessage,
                candidatesText.toString()
        );

        try {
            String response = chatClient.prompt()
                    .system("당신은 칵테일 추천 전문가입니다. 사용자 요청에 가장 적합한 칵테일을 선택합니다.")
                    .user(rankingPrompt)
                    .options(OpenAiChatOptions.builder()
                            .withTemperature(0.3)
                            .withMaxTokens(100)
                            .build())
                    .call()
                    .content();

            // 응답에서 인덱스 추출 (예: "[2,0,5]" → [2, 0, 5])
            String cleaned = response.replaceAll("[^0-9,]", "");
            String[] indices = cleaned.split(",");

            List<Cocktail> ranked = new ArrayList<>();
            for (String idx : indices) {
                try {
                    int index = Integer.parseInt(idx.trim());
                    if (index >= 0 && index < candidates.size()) {
                        ranked.add(candidates.get(index));
                    }
                } catch (NumberFormatException e) {
                    log.warn("인덱스 파싱 실패: {}", idx);
                }
            }

            if (ranked.size() >= 3) {
                log.info("AI 재정렬 완료: {} 개 칵테일 선택", ranked.size());
                return ranked.subList(0, 3);
            } else {
                log.warn("AI 재정렬 결과 부족, 기본 순서 사용");
                return candidates.subList(0, Math.min(3, candidates.size()));
            }

        } catch (Exception e) {
            log.error("칵테일 재정렬 중 오류: ", e);
            // 오류 시 앞에서 3개만 반환
            return candidates.subList(0, Math.min(3, candidates.size()));
        }
    }
}
