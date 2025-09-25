package com.back.domain.user.dto;

import com.back.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String email;
    private String nickname;
    private Double abvDegree;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isFirstLogin;

    public static UserDto from(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .abvDegree(user.getAbvDegree())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isFirstLogin(user.isFirstLogin())
                .build();
    }
}

