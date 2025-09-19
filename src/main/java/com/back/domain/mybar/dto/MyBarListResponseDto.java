package com.back.domain.mybar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyBarListResponseDto {
    private List<MyBarItemResponseDto> items;
    private boolean hasNext;   // 다음 페이지 존재 여부
    private Integer nextPage;  // 다음 페이지 번호(없으면 null)
}