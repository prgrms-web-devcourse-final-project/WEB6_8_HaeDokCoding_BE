package com.back.domain.chatbot.service;

import com.back.domain.chatbot.dto.ChatRequestDto;
import com.back.domain.chatbot.dto.ChatResponseDto;
import com.back.domain.chatbot.dto.SaveBotMessageDto;
import com.back.domain.chatbot.dto.StepRecommendationResponseDto;
import com.back.domain.chatbot.entity.ChatConversation;
import com.back.domain.chatbot.enums.MessageSender;
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

            // 최근 대화 기록 조회 (최신 10개 메시지 - USER와 CHATBOT 메시지 모두 포함)
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

            // 대화 저장 - 사용자 메시지와 봇 응답을 각각 저장
            saveConversation(requestDto, response);

            return new ChatResponseDto(response);

        } catch (Exception e) {
            log.error("채팅 응답 생성 중 오류 발생: ", e);
            return handleError(e);
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
     */
    @Transactional
    public void saveConversation(ChatRequestDto requestDto, String response) {
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
        chatConversationRepository.save(botResponse);
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
     */
    @Transactional
    public ChatConversation createGreetingMessage(Long userId) {
        String greetingMessage = "안녕하세요! 🍹 바텐더 '쑤리'에요.\n" +
                "취향에 맞는 칵테일을 추천해드릴게요!\n" +
                "어떤 유형으로 찾아드릴까요?";

        ChatConversation greeting = ChatConversation.builder()
                .userId(userId)
                .message(greetingMessage)
                .sender(MessageSender.CHATBOT)
                .createdAt(LocalDateTime.now())
                .build();

        return chatConversationRepository.save(greeting);
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

    // 단계별 추천 처리 통합 메서드 - 변경사항: 대화 저장 방식 변경
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
                chatResponse = "단계별로 취향을 찾아드릴게요! 🎯\n원하시는 도수를 선택해주세요! \n " +
                        "잘 모르는 항목은 '전체'로 체크하셔도 괜찮아요.";
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

        // 대화 기록 저장 - 변경된 방식으로 저장
        saveConversation(requestDto, chatResponse);

        return new ChatResponseDto(chatResponse, stepRecommendation);
    }

    // ============ 단계별 추천 관련 메서드들 (변경 없음) ============

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