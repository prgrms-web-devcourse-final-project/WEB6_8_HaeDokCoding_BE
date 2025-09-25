package com.back.domain.mybar.controller;

import com.back.domain.mybar.dto.MyBarListResponseDto;
import com.back.domain.mybar.service.MyBarService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/me/bar")
@RequiredArgsConstructor
@Validated
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
    @Operation(summary = "내 바 목록", description = "내가 킵한 칵테일 목록 조회. 무한스크롤 파라미터 지원")
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

    /**
     * 킵 추가(생성/복원/재킵)
     * @param userId 인증된 사용자 ID
     * @param cocktailId 칵테일 ID
     * @return 201 kept
     */
    @PostMapping("/{cocktailId}/keep")
    @Operation(summary = "킵 추가/복원", description = "해당 칵테일을 내 바에 킵합니다. 이미 삭제된 경우 복원")
    public RsData<Void> keep(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long cocktailId
    ) {
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
    @Operation(summary = "킵 해제", description = "내 바에서 해당 칵테일 킵을 해제합니다(소프트 삭제, 멱등)")
    public RsData<Void> unkeep(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long cocktailId
    ) {
        myBarService.unkeep(userId, cocktailId);
        return RsData.of(200, "deleted");
    }
}
