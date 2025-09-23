package com.back.domain.history.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class HistoryPostListDto {
    private List<HistoryPostItemDto> items;
    private boolean hasNext;
    private LocalDateTime nextCreatedAt;
    private Long nextId;
}

