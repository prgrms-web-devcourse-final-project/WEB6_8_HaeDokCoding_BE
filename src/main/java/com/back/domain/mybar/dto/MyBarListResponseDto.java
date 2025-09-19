package com.back.domain.mybar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyBarListResponseDto {
    private List<MyBarItemResponseDto> items;
    private Long nextCursor; // 다음 페이지 시작점(없으면 null)
}