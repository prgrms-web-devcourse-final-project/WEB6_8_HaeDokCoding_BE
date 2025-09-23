package com.back.domain.history.controller;

import com.back.domain.history.dto.HistoryPostListDto;
import com.back.domain.history.service.HistoryService;
import com.back.global.rsData.RsData;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Validated
public class MyHistoryController {

    private final HistoryService historyService;

    @GetMapping("/posts")
    public RsData<HistoryPostListDto> getMyPosts(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        HistoryPostListDto body = historyService.getMyPosts(userId, lastCreatedAt, lastId, limit);
        return RsData.successOf(body);
    }
}

