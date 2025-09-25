package com.back.domain.profile.controller;

import com.back.domain.profile.dto.ProfileResponseDto;
import com.back.domain.profile.dto.ProfileUpdateRequestDto;
import com.back.domain.profile.service.ProfileService;
import com.back.domain.user.service.UserService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    /**
     * 내 프로필 요약 API 컨트롤러.
     * 닉네임, 알콜도수(등급/라벨), 내가 작성/댓글/좋아요한 수 등을 조회·수정합니다.
     */

    private final UserService userService;
    private final ProfileService profileService;

    /**
     * 내 프로필 요약 조회
     *
     * @param userId 인증된 사용자 ID (SecurityContext)
     * @return RsData wrapping ProfileResponseDto
     */
    @GetMapping
    @Operation(summary = "내 프로필 요약 조회",
            description = "닉네임, 알콜도수(등급/라벨), 작성/댓글/좋아요 카운트를 반환")
    public RsData<ProfileResponseDto> getProfile(@AuthenticationPrincipal(expression = "id") Long userId) {
        ProfileResponseDto body = profileService.getProfile(userId);
        return RsData.successOf(body); // code=200, message="success"
    }

    // PUT 제거: PATCH 전용으로 운영

    /**
     * 프로필 수정(닉네임)
     *
     * @param userId 인증된 사용자 ID (SecurityContext)
     * @param request 닉네임(1~10자)
     * @return 수정된 프로필 요약
     */
    @PatchMapping
    @Operation(summary = "프로필 수정(닉네임)", description = "닉네임은 1~10자, 중복 불가")
    public RsData<ProfileResponseDto> patchNickname(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody ProfileUpdateRequestDto request
    ) {
        ProfileResponseDto body = profileService.updateProfile(userId, request);
        return RsData.successOf(body);
    }
}
