package com.back.domain.chatbot.enums;

public enum MessageType {
    TEXT("텍스트"),           // 일반 텍스트 메시지
    RADIO_OPTIONS("라디오옵션"), // 라디오 버튼 선택지
    CARD_LIST("카드리스트"),    // 칵테일 추천 카드 리스트
    LOADING("로딩중"),         // 로딩 메시지
    ERROR("에러"),            // 에러 메시지
    INPUT("입력");   // 텍스트 입력 요청
    
    private final String description;
    
    MessageType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}