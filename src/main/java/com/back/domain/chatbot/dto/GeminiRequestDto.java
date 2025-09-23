package com.back.domain.chatbot.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeminiRequestDto {

    private List<Content> contents;

    @Getter
    @Setter
    public static class Content {
        private List<Part> parts;

        public Content(String text) {
            this.parts = List.of(new Part(text));
        }
    }

    @Getter
    @Setter
    public static class Part {
        private String text;

        public Part(String text) {
            this.text = text;
        }
    }

    public GeminiRequestDto(String message) {
        this.contents = List.of(new Content(message));
    }
}