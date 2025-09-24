package com.back.domain.profile.controller;

import com.back.domain.profile.dto.ProfileResponseDto;
import com.back.domain.profile.dto.ProfileUpdateRequestDto;
import com.back.domain.profile.service.ProfileService;
import com.back.domain.user.service.UserService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ProfileService profileService;

    @GetMapping
    public RsData<ProfileResponseDto> getProfile(@AuthenticationPrincipal(expression = "id") Long userId) {
        ProfileResponseDto body = profileService.getProfile(userId);
        return RsData.successOf(body); // code=200, message="success"
    }

    // PUT 제거: PATCH 전용으로 운영

    @PatchMapping
    public RsData<ProfileResponseDto> patchNickname(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody ProfileUpdateRequestDto request
    ) {
        ProfileResponseDto body = profileService.updateProfile(userId, request);
        return RsData.successOf(body);
    }
}
