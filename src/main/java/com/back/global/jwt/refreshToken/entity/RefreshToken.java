package com.back.global.jwt.refreshToken.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime lastUsedAt;

    public static RefreshToken create(String token, Long userId, long ttlSeconds) {
        LocalDateTime now = LocalDateTime.now();
        return RefreshToken.builder()
                .token(token)
                .userId(userId)
                .lastUsedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isIdleExpired(long idleTimeoutHours) {
        return LocalDateTime.now().isAfter(this.lastUsedAt.plusMinutes(idleTimeoutHours));

    }

}