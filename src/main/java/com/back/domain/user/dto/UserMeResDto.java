package com.back.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMeResDto {
    @JsonProperty("user")
    private final UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private final String id;
        private final String email;
        private final String nickname;

        @JsonProperty("is_first_login")
        private final Boolean isFirstLogin;

        @JsonProperty("abv_degree")
        private final Double abvDegree;

        private final String provider;
    }
}
