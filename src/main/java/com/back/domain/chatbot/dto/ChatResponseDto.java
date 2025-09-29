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
    private LocalDateTime timestamp;

    public ChatResponseDto(String response) {
        this.response = response;
        this.timestamp = LocalDateTime.now();
    }
}