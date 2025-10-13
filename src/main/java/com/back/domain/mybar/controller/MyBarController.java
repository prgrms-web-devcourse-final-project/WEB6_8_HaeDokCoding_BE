package com.back.domain.mybar.controller;

import com.back.domain.mybar.dto.MyBarIdResponseDto;
import com.back.domain.mybar.dto.MyBarListResponseDto;
import com.back.domain.mybar.service.MyBarService;
import com.back.global.rsData.RsData;
import com.back.global.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/bar")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class MyBarController {

    private final MyBarService myBarService;

    @GetMapping
    @Operation(summary = "내 바 경량 목록", description = "찜 ID 목록을 반환합니다.")
    public RsData<List<MyBarIdResponseDto>> getMyBarIds(
            @AuthenticationPrincipal SecurityUser principal
    ) {
        Long userId = principal.getId();
        List<MyBarIdResponseDto> body = myBarService.getMyBarIds(userId);
        return RsData.successOf(body);
    }

    @GetMapping("/detail")
    @Operation(summary = "내 바 상세 목록", description = "커서 기반으로 상세 찜 정보를 반환합니다.")
    public RsData<MyBarListResponseDto> getMyBarDetail(
            @AuthenticationPrincipal SecurityUser principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastKeptAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    ) {
        Long userId = principal.getId();
        MyBarListResponseDto body = myBarService.getMyBarDetail(userId, lastKeptAt, lastId, limit);
        return RsData.successOf(body);
    }

    @PostMapping("/{cocktailId}/keep")
    @Operation(summary = "킵 추가/복원", description = "해당 칵테일을 내 바에 킵합니다. 이미 삭제 상태면 복원")
    public RsData<Void> keep(
            @AuthenticationPrincipal SecurityUser principal,
            @PathVariable Long cocktailId
    ) {
        Long userId = principal.getId();
        myBarService.keep(userId, cocktailId);
        return RsData.of(201, "kept");
    }

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

