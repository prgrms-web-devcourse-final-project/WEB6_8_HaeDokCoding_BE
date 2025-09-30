package com.back.domain.mybar.controller;

import com.back.domain.mybar.dto.MyBarListResponseDto;
import com.back.domain.mybar.service.MyBarService;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/me/bar")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class MyBarController {

    /**
     * 내 바(킵) API 컨트롤러.
     * 내가 킵한 칵테일 목록 조회, 킵 추가/복원, 킵 해제를 제공합니다.
     */

    private final MyBarService myBarService;

    /**
     * 내 바 목록 조회(무한스크롤)
     * @param userId 인증된 사용자 ID
     * @param lastKeptAt 이전 페이지 마지막 keptAt (옵션)
     * @param lastId 이전 페이지 마지막 id (옵션)
     * @param limit 페이지 크기(1~100)
     * @return 킵 아이템 목록과 다음 페이지 커서
     */
    @GetMapping
    @Operation(summary = "내 바 목록", description = "내가 킵한 칵테일 목록 조회. 무한 스크롤 커서 지원")
    public RsData<MyBarListResponseDto> getMyBarList(
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastKeptAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        Long userId = principal.getId();
        MyBarListResponseDto body = myBarService.getMyBar(userId, lastKeptAt, lastId, limit);
        return RsData.successOf(body);
    }

    /**
     * 킵 추가(생성/복원/재킵)
     * @param userId 인증된 사용자 ID
     * @param cocktailId 칵테일 ID
     * @return 201 kept
     */
    @PostMapping("/{cocktailId}/keep")
    @Operation(summary = "킵 추가/복원", description = "해당 칵테일을 내 바에 킵합니다. 이미 삭제 상태면 복원")
    public RsData<Void> keep(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long cocktailId
    ) {
        Long userId = principal.getId();
        myBarService.keep(userId, cocktailId);
        return RsData.of(201, "kept"); // Aspect가 HTTP 201로 설정
    }

    /**
     * 킵 해제(소프트 삭제) — 멱등
     * @param userId 인증된 사용자 ID
     * @param cocktailId 칵테일 ID
     * @return 200 deleted
     */
    @DeleteMapping("/{cocktailId}/keep")
    @Operation(summary = "킵 해제", description = "내 바에서 해당 칵테일을 삭제(소프트 삭제, 멱등)")
    public RsData<Void> unkeep(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long cocktailId
    ) {
        Long userId = principal.getId();
        myBarService.unkeep(userId, cocktailId);
        return RsData.of(200, "deleted");
    }
}

