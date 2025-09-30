package com.back.domain.chatbot.enums;

public enum MessageSender {
    USER("사용자"),
    CHATBOT("챗봇");
    
    private final String description;
    
    MessageSender(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}