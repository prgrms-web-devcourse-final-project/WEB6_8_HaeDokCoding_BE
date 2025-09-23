package com.back.domain.myhistory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyHistoryCommentListDto {
    private List<MyHistoryCommentItemDto> items;
    private boolean hasNext;
    private LocalDateTime nextCreatedAt;
    private Long nextId;
}

