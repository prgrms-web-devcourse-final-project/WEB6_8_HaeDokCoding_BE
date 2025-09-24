package com.back.domain.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {

    private String response;
    private String sessionId;
    private LocalDateTime timestamp;

    public ChatResponseDto(String response, String sessionId) {
        this.response = response;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }
}