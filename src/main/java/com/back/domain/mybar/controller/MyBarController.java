package com.back.domain.mybar.controller;

import com.back.domain.mybar.dto.MyBarListResponseDto;
import com.back.domain.mybar.service.MyBarService;
import com.back.global.rsData.RsData;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/me/bar")
@RequiredArgsConstructor
@Validated
public class MyBarController {

    private final MyBarService myBarService;

    @GetMapping
    public RsData<MyBarListResponseDto> getMyBarList(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastKeptAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        MyBarListResponseDto body = myBarService.getMyBar(userId, lastKeptAt, lastId, limit);
        return RsData.successOf(body);  // code=200, message="success"
    }

    /** 킵 추가(생성/복원/재킵) */
    @PostMapping("/{cocktailId}/keep")
    public RsData<Void> keep(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long cocktailId
    ) {
        myBarService.keep(userId, cocktailId);
        return RsData.of(201, "kept"); // Aspect가 HTTP 201로 설정
    }

    /** 킵 해제(소프트 삭제) — 멱등 */
    @DeleteMapping("/{cocktailId}/keep")
    public RsData<Void> unkeep(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long cocktailId
    ) {
        myBarService.unkeep(userId, cocktailId);
        return RsData.of(200, "deleted");
    }
}
