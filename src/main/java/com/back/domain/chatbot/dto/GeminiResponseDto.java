package com.back.domain.chatbot.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeminiResponseDto {

    private List<Candidate> candidates;

    @Getter
    @Setter
    public static class Candidate {
        private Content content;
    }

    @Getter
    @Setter
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @Setter
    public static class Part {
        private String text;
    }

    public String getGeneratedText() {
        if (candidates != null && !candidates.isEmpty() &&
            candidates.get(0).content != null &&
            candidates.get(0).content.parts != null &&
            !candidates.get(0).content.parts.isEmpty()) {
            return candidates.get(0).content.parts.get(0).text;
        }
        return "응답을 생성할 수 없습니다.";
    }
}