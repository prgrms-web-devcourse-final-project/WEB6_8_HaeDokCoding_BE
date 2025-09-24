package com.back.domain.myhistory.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyHistoryLikedPostListDto {
    private List<MyHistoryLikedPostItemDto> items;
    private boolean hasNext;
    private LocalDateTime nextCreatedAt;
    private Long nextId;
}

