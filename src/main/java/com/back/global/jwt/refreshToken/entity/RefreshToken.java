package com.back.global.jwt.refreshToken.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;


    public static RefreshToken create(String token, Long userId, long ttlSeconds) {
        LocalDateTime now = LocalDateTime.now();
        return RefreshToken.builder()
                .token(token)
                .userId(userId)
                .createdAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}