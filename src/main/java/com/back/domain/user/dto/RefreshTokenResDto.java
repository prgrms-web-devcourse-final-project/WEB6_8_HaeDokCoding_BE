package com.back.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshTokenResDto {
    private final String accessToken;
    private final UserInfoDto user;

    @Getter
    @Builder
    public static class UserInfoDto {
        private final String id;
        private final String nickname;
        private final Boolean isFirstLogin;
        private final Double abvDegree;

    }
}
