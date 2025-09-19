package com.back.global.jwt.refreshToken.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash("refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private String token;

    @Indexed
    private Long userId;

    private String email;

    private LocalDateTime createdAt;

    @TimeToLive
    private Long ttl; // seconds

    public static RefreshToken create(String token, Long userId, String email, long ttlSeconds) {
        return RefreshToken.builder()
                .token(token)
                .userId(userId)
                .email(email)
                .createdAt(LocalDateTime.now())
                .ttl(ttlSeconds)
                .build();
    }
}