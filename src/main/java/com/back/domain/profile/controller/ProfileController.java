package com.back.domain.profile.controller;

import com.back.domain.profile.dto.ProfileResponseDto;
import com.back.domain.profile.service.ProfileService;
import com.back.domain.user.service.UserService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ProfileService profileService;

    @GetMapping
    public RsData<ProfileResponseDto> getProfile(@AuthenticationPrincipal(expression = "id") Long userId) {
        ProfileResponseDto body = profileService.getProfile(userId);
        return RsData.successOf(body); // code=200, message="success"
    }
}
