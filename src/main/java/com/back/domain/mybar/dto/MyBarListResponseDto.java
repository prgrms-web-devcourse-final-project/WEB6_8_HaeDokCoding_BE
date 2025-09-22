package com.back.domain.mybar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyBarListResponseDto {
    private List<MyBarItemResponseDto> items;
    private boolean hasNext;        // 다음 페이지 존재 여부
    private LocalDateTime nextKeptAt; // 다음 페이지 시작용 keptAt
    private Long nextId;              // 다음 페이지 시작용 id
}
