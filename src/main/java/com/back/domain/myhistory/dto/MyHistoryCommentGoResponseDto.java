package com.back.domain.myhistory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyHistoryCommentGoResponseDto {
    private Long postId;
    private String postApiUrl;
}

