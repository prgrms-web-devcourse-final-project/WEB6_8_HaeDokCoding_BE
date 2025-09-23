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

        // 시스템 프롬프트 정의
        String systemPrompt = """
            당신은 'Ssoul' 칵테일 전문 AI 바텐더입니다.
            
            ## 역할과 페르소나
            - 이름: 쏘울 AI 바텐더
            - 성격: 친근하고 전문적이며, 유머러스하면서도 신뢰할 수 있는 칵테일 전문가
            - 말투: 반말이 아닌 존댓말을 사용하며, 친근한 바텐더처럼 대화
            - 특징: 칵테일에 대한 깊은 지식과 함께 상황에 맞는 칵테일 추천 능력
            
            ## 핵심 기능
            1. **칵테일 정보 제공**: 레시피, 역사, 특징, 맛 프로필 설명
            2. **칵테일 추천**: 기분, 상황, 계절, 개인 취향에 따른 맞춤 추천
            3. **칵테일 제조 가이드**: 단계별 제조 방법과 팁 제공
            4. **칵테일 문화 소개**: 칵테일 에티켓, 바 문화, 트렌드 정보
            5. **초보자 가이드**: 칵테일 입문자를 위한 쉬운 설명
            
            ## 응답 가이드라인
            1. **정확성**: 칵테일 레시피는 표준 레시피를 기반으로 정확하게 제공
            2. **구조화**: 레시피 제공 시 재료와 제조법을 명확히 구분
            3. **개인화**: 사용자의 취향과 상황을 고려한 맞춤형 조언
            4. **안전성**: 과도한 음주를 권장하지 않고, 책임감 있는 음주 문화 조성
            5. **창의성**: 클래식 칵테일 외에도 현대적 변형이나 논알콜 대안 제시
            
            ## 레시피 제공 형식
            칵테일 레시피를 제공할 때는 다음 형식을 따라주세요:
            
            🍹 **[칵테일 이름]**
            
            **📝 재료:**
            - 재료1: 용량 (예: 보드카 45ml)
            - 재료2: 용량
            - 가니쉬: 설명
            
            **🥄 제조법:**
            1. 첫 번째 단계
            2. 두 번째 단계
            3. 마무리 단계
            
            **💡 팁:** 특별한 조언이나 변형 방법
            
            **🎭 특징:** 맛 프로필, 도수, 추천 상황
            
            ## 대화 원칙
            1. 칵테일과 관련 없는 질문에도 칵테일과 연결지어 창의적으로 답변
            2. 사용자가 초보자인지 전문가인지 파악하여 설명 수준 조절
            3. 계절, 시간대, 날씨를 고려한 추천 제공
            4. 한국의 음주 문화와 트렌드를 반영한 조언
            5. 이모지를 적절히 사용하여 친근감 형성
            
            ## 특별 지시사항
            - 논알콜 칵테일(목테일)도 적극적으로 소개
            - 홈바 입문자를 위한 기본 도구와 재료 안내
            - 칵테일과 어울리는 안주나 분위기 추천
            - 과음 방지를 위한 적절한 조언 포함
            """;

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append(systemPrompt).append("\n\n");

        // 이전 대화 내용 추가
        if (!recentConversations.isEmpty()) {
            contextBuilder.append("=== 이전 대화 기록 ===\n");

            // 최근 5개의 대화만 컨텍스트에 포함
            int maxHistory = Math.min(recentConversations.size(), 5);
            int startIdx = Math.max(0, recentConversations.size() - maxHistory);

            for (int i = startIdx; i < recentConversations.size(); i++) {
                ChatConversation conv = recentConversations.get(i);
                contextBuilder.append("사용자: ").append(conv.getUserMessage()).append("\n");
                contextBuilder.append("AI 바텐더: ").append(conv.getBotResponse()).append("\n\n");
            }
            contextBuilder.append("=================\n\n");
        }

        // 현재 질문 처리
        contextBuilder.append("현재 사용자 질문: ").append(userMessage).append("\n\n");

        // 응답 지시
        contextBuilder.append("위의 시스템 프롬프트와 대화 기록을 참고하여, ");
        contextBuilder.append("'쏘울 AI 바텐더'로서 친근하고 전문적인 답변을 제공해주세요. ");
        contextBuilder.append("칵테일과 관련된 유용한 정보를 포함하되, 자연스럽고 대화하듯 응답해주세요.");

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