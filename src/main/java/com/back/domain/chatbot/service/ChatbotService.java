package com.back.domain.chatbot.service;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // 로딩 메시지 상수
    private static final String RECOMMENDATION_LOADING_MESSAGE =
            "당신에게 어울리는 칵테일은? 두구❤️두구💛두구💚두구💙두구💜두구🖤두구🤍두구🤎";

    // 처리 완료 플래그 키워드
    private static final String PROCESS_STEP_RECOMMENDATION = "PROCESS_STEP_RECOMMENDATION";
    private static final String PROCESS_QA_RECOMMENDATION = "PROCESS_QA_RECOMMENDATION";

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
                        /*
                        String guideMessage = "좋아요! 질문형 추천을 시작할게요 🎯\n" +
                                "칵테일에 관련된 질문을 자유롭게 입력해주세요!\n" +
                                "예시: 달콤한 칵테일 추천해줘, 파티용 칵테일이 필요해, 초보자용 칵테일 알려줘";
                         */

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

                    // 실제 질문이 들어온 경우 - 먼저 로딩 메시지 반환
                    if (requestDto.getMessage() != null && !requestDto.getMessage().trim().isEmpty()) {
                        // 로딩 메시지인지 확인 (두구두구 메시지 이후의 실제 처리 요청)
                        if (requestDto.getMessage().contains("PROCESS_RECOMMENDATION")) {
                            log.info("질문형 추천 실제 처리 - userId: {}", requestDto.getUserId());
                            return generateAIResponseWithContext(requestDto, "질문형 추천");
                        }

                        // 사용자 질문 저장
                        ChatConversation userQuestion = ChatConversation.builder()
                                .userId(requestDto.getUserId())
                                .message(requestDto.getMessage())
                                .sender(MessageSender.USER)
                                .createdAt(LocalDateTime.now())
                                .build();
                        chatConversationRepository.save(userQuestion);

                        // 고정 로딩 메시지
                        String loadingMessage = "당신에게 어울리는 칵테일은?\n 두구❤️두구💛두구💚두구💙두구💜두구🖤두구🤍두구🤎";

                        ChatConversation loadingBot = ChatConversation.builder()
                                .userId(requestDto.getUserId())
                                .message(loadingMessage)
                                .sender(MessageSender.CHATBOT)
                                .createdAt(LocalDateTime.now())
                                .build();
                        ChatConversation savedLoading = chatConversationRepository.save(loadingBot);

                        // 로딩 메시지 반환 (FE에서 이후 자동으로 실제 추천 요청)
                        return ChatResponseDto.builder()
                                .id(savedLoading.getId())
                                .userId(requestDto.getUserId())
                                .message(loadingMessage)
                                .sender(MessageSender.CHATBOT)
                                .type(MessageType.LOADING)
                                .createdAt(savedLoading.getCreatedAt())
                                .metaData(ChatResponseDto.MetaData.builder()
                                        .currentStep(0)
                                        .actionType("LOADING_QA")
                                        .isTyping(true)
                                        .delay(2000) // 2초 후 자동 요청
                                        .build())
                                .build();
                    }
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

    // ============ 수정된 메서드들 ============

    /**
     * 대화 컨텍스트 빌드 - 변경사항: sender로 구분하여 대화 재구성
     */
    private String buildConversationContext(List<ChatConversation> recentChats) {
        if (recentChats.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder("\n\n【최근 대화 기록】\n");

        // 시간 역순으로 정렬된 리스트를 시간순으로 재정렬
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

    /**
     * 대화 저장 - 변경사항: 사용자 메시지와 봇 응답을 각각 별도로 저장
     * @return 저장된 봇 응답 엔티티 (id 포함)
     */
    @Transactional
    public ChatConversation saveConversation(ChatRequestDto requestDto, String response) {
        // 1. 사용자 메시지 저장
        ChatConversation userMessage = ChatConversation.builder()
                .userId(requestDto.getUserId())
                .message(requestDto.getMessage())
                .sender(MessageSender.USER)
                .createdAt(LocalDateTime.now())
                .build();
        chatConversationRepository.save(userMessage);

        // 2. 봇 응답 저장
        ChatConversation botResponse = ChatConversation.builder()
                .userId(requestDto.getUserId())
                .message(response)
                .sender(MessageSender.CHATBOT)
                .createdAt(LocalDateTime.now())
                .build();
        return chatConversationRepository.save(botResponse);
    }

    /**
     * 사용자 채팅 기록 조회 - 변경사항: sender 구분 없이 모든 메시지 시간순으로 조회
     */
    @Transactional(readOnly = true)
    public List<ChatConversation> getUserChatHistory(Long userId) {
        return chatConversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * FE에서 생성한 봇 메시지를 DB에 저장
     * 예: 인사말, 안내 메시지, 에러 메시지 등
     */
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

    /**
     * 기본 인사말 생성 및 저장
     * 채팅 시작 시 호출하여 인사말을 DB에 저장
     * 이미 동일한 인사말이 존재하면 중복 저장하지 않음
     * MessageType.RADIO_OPTIONS와 options 데이터를 포함한 ChatResponseDto 반환
     */
    @Transactional
    public ChatResponseDto createGreetingMessage(Long userId) {
        String greetingMessage = "안녕하세요! 🍹 바텐더 '쑤리'에요.\n" +
                "취향에 맞는 칵테일을 추천해드릴게요!\n" +
                "어떤 유형으로 찾아드릴까요?";

        // 선택 옵션 생성
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

        // StepRecommendationResponseDto 생성
        StepRecommendationResponseDto stepData = new StepRecommendationResponseDto(
                0,  // 인사말은 step 0
                greetingMessage,
                options,
                null,
                false
        );

        // 중복 확인: 동일한 인사말이 이미 존재하는지 확인
        boolean greetingExists = chatConversationRepository.existsByUserIdAndMessage(userId, greetingMessage);

        ChatConversation savedGreeting = null;
        // 중복되지 않을 경우에만 DB에 저장
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

        // ChatResponseDto 반환 (요청된 형식에 맞춰 id, userId, sender, type, createdAt 포함)
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

    /**
     * 사용자의 첫 대화 여부 확인
     * 첫 대화인 경우 인사말 자동 생성에 활용 가능
     */
    @Transactional(readOnly = true)
    public boolean isFirstConversation(Long userId) {
        return chatConversationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).isEmpty();
    }

    // ============ 기존 메서드들 (변경 없음) ============

    private String buildSystemMessage(InternalMessageType type) {
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

    private String buildUserMessage(String userMessage, InternalMessageType type) {
        return userMessage + "\n\n" + responseRules;
    }

    private OpenAiChatOptions getOptionsForMessageType(InternalMessageType type) {
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

    private String postProcessResponse(String response, InternalMessageType type) {
        // 응답 길이 제한 확인
        if (response.length() > 500) {
            response = response.substring(0, 497) + "...";
        }

        // 이모지 추가 (타입별)
        if (type == InternalMessageType.RECIPE && !response.contains("🍹")) {
            response = "🍹 " + response;
        }

        return response;
    }

    /**
     * AI 응답 생성
     * @return 저장된 봇 응답 엔티티 (id 포함)
     */
    private ChatConversation generateAIResponse(ChatRequestDto requestDto) {
        log.info("Normal chat mode for userId: {}", requestDto.getUserId());

        // 메시지 타입 감지 (내부 enum 사용)
        InternalMessageType messageType = detectMessageType(requestDto.getMessage());

        // 최근 대화 기록 조회 (최신 20개 메시지 - USER와 CHATBOT 메시지 모두 포함)
        List<ChatConversation> recentChats =
                chatConversationRepository.findTop20ByUserIdOrderByCreatedAtDesc(requestDto.getUserId());

        // 대화 컨텍스트 생성
        String conversationContext = buildConversationContext(recentChats);

        // ChatClient 빌더 생성
        var promptBuilder = chatClient.prompt()
                .system(buildSystemMessage(messageType) + conversationContext)
                .user(buildUserMessage(requestDto.getMessage(), messageType));

        // 응답 생성
        String response = promptBuilder
                .options(getOptionsForMessageType(messageType))
                .call()
                .content();

        // 응답 후처리
        response = postProcessResponse(response, messageType);

        // 대화 저장 - 사용자 메시지와 봇 응답을 각각 저장하고 저장된 봇 응답 반환
        return saveConversation(requestDto, response);
    }

    /**
     * 로딩 메시지 생성
     */
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

    /**
     * 단계별 추천 시작 키워드 감지 (레거시 지원)
     * @deprecated currentStep 명시적 전달 방식을 사용하세요. 이 메서드는 하위 호환성을 위해 유지됩니다.
     */
    @Deprecated
    private boolean isStepRecommendationTrigger(String message) {
        log.warn("레거시 키워드 감지 사용됨. currentStep 사용 권장. message: {}", message);
        String lower = message.toLowerCase().trim();
        return lower.contains("단계별 취향 찾기");
    }

    /**
     * 질문형 추천 전용 AI 응답 생성
     * 일반 대화와 구분하여 추천에 특화된 응답 생성
     */
    private ChatResponseDto generateAIResponseWithContext(ChatRequestDto requestDto, String mode) {
        ChatConversation savedResponse = generateAIResponse(requestDto);

        return ChatResponseDto.builder()
                .id(savedResponse.getId())
                .userId(requestDto.getUserId())
                .message(savedResponse.getMessage())
                .sender(MessageSender.CHATBOT)
                .type(MessageType.TEXT)
                .createdAt(savedResponse.getCreatedAt())
                .metaData(ChatResponseDto.MetaData.builder()
                        .actionType(mode)
                        .currentStep(0)
                        .totalSteps(0)
                        .build())
                .build();
    }

    /**
     * 에러 응답 생성
     */
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

        // 단계별 추천 선택 시 처리
        if (currentStep == 1 && "STEP".equalsIgnoreCase(requestDto.getMessage())) {
            // 사용자 선택 메시지 저장
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
                stepData = getAlcoholBaseTypeOptions(parseAlcoholStrength(requestDto.getSelectedAlcoholStrength()));
                message = "좋은 선택이네요! 이제 베이스가 될 술을 선택해주세요 🍸";
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
                // Step 4에서 로딩 메시지 처리
                if (!"PROCESS_STEP_RECOMMENDATION".equals(requestDto.getMessage())) {
                    // 사용자 입력 저장 (Step 3의 답변)
                    if (requestDto.getMessage() != null && !requestDto.getMessage().trim().isEmpty()) {
                        ChatConversation userInput = ChatConversation.builder()
                                .userId(requestDto.getUserId())
                                .message(requestDto.getMessage())
                                .sender(MessageSender.USER)
                                .createdAt(LocalDateTime.now())
                                .build();
                        chatConversationRepository.save(userInput);
                    }

                    // 고정 로딩 메시지
                    String loadingMessage = "당신에게 어울리는 칵테일은?\n 두구❤️두구💛두구💚두구💙두구💜두구🖤두구🤍두구🤎";

                    ChatConversation loadingBot = ChatConversation.builder()
                            .userId(requestDto.getUserId())
                            .message(loadingMessage)
                            .sender(MessageSender.CHATBOT)
                            .createdAt(LocalDateTime.now())
                            .build();
                    ChatConversation savedLoading = chatConversationRepository.save(loadingBot);

                    // 로딩 메시지 반환
                    return ChatResponseDto.builder()
                            .id(savedLoading.getId())
                            .userId(requestDto.getUserId())
                            .message(loadingMessage)
                            .sender(MessageSender.CHATBOT)
                            .type(MessageType.LOADING)
                            .createdAt(savedLoading.getCreatedAt())
                            .metaData(ChatResponseDto.MetaData.builder()
                                    .currentStep(4)
                                    .totalSteps(4)
                                    .actionType("LOADING_STEP")
                                    .isTyping(true)
                                    .delay(2000) // 2초 후 자동 요청
                                    .build())
                            .stepData(new StepRecommendationResponseDto(
                                    4,
                                    null,
                                    null,
                                    null,
                                    false
                            ))
                            .build();
                }

                // 실제 추천 처리
                stepData = getFinalRecommendationsWithMessage(
                        parseAlcoholStrength(requestDto.getSelectedAlcoholStrength()),
                        parseAlcoholBaseType(requestDto.getSelectedAlcoholBaseType()),
                        requestDto.getMessage()
                );
                message = stepData.getStepTitle();
                type = MessageType.CARD_LIST;
                break;

            default:
                stepData = getAlcoholStrengthOptions();
                message = "단계별 맞춤 취향 추천을 시작합니다! 🎯";
                type = MessageType.RADIO_OPTIONS;
        }

        // 봇 응답 저장
        ChatConversation botResponse = ChatConversation.builder()
                .userId(requestDto.getUserId())
                .message(message)
                .sender(MessageSender.CHATBOT)
                .createdAt(LocalDateTime.now())
                .build();
        ChatConversation savedResponse = chatConversationRepository.save(botResponse);

        // 메타데이터 포함
        ChatResponseDto.MetaData metaData = ChatResponseDto.MetaData.builder()
                .currentStep(currentStep)
                .totalSteps(4)
                .isTyping(type != MessageType.CARD_LIST) // 카드리스트는 타이핑 애니메이션 불필요
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
    // ============ 단계별 추천 관련 메서드들 ============
    // "ALL" 또는 null/빈값은 null로 처리하여 전체 선택 의미

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

        // "전체" 옵션 추가
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

        // "전체" 옵션 추가
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
        // 필터링 조건에 맞는 칵테일 검색
        // "ALL" 선택 시 해당 필터를 null로 처리하여 전체 검색
        List<AlcoholStrength> strengths = (alcoholStrength == null) ? null : List.of(alcoholStrength);
        List<AlcoholBaseType> baseTypes = (alcoholBaseType == null) ? null : List.of(alcoholBaseType);

        // 'x', '없음' 입력 시 키워드 조건 무시
        String keyword = null;
        if (userMessage != null && !userMessage.trim().isEmpty()) {
            String trimmed = userMessage.trim().toLowerCase();
            if (!trimmed.equals("x") && !trimmed.equals("없음")) {
                keyword = userMessage;
            }
        }

        // userMessage를 키워드로 사용하여 검색
        Page<Cocktail> cocktailPage = cocktailRepository.searchWithFilters(
                keyword, // 'x', '없음'이면 null, 아니면 사용자 입력 메시지
                strengths,
                null, // cocktailType 사용 안 함
                baseTypes,
                PageRequest.of(0, 3) // 최대 3개 추천
        );

        List<CocktailSummaryResponseDto> recommendations = cocktailPage.getContent().stream()
            .map(cocktail -> new CocktailSummaryResponseDto(
                cocktail.getId(),
                cocktail.getCocktailName(),
                cocktail.getCocktailNameKo(),
                cocktail.getCocktailImgUrl(),
                cocktail.getAlcoholStrength().getDescription()
            ))
            .collect(Collectors.toList());

        // 추천 이유는 각 칵테일별 설명으로 들어가도록 유도
        String stepTitle = recommendations.isEmpty()
                ? "조건에 맞는 칵테일을 찾을 수 없습니다 😢"
                : "짠🎉🎉\n" +
                "칵테일의 자세한 정보는 '상세보기'를 클릭해서 확인할 수 있어요.\n" +
                "마음에 드는 칵테일은 '킵' 버튼을 눌러 나만의 Bar에 저장해보세요!";

        return new StepRecommendationResponseDto(
                4,
                stepTitle,
                null,
                recommendations,
                true
        );
    }
}