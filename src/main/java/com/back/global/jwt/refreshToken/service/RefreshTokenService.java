package com.back.global.jwt.refreshToken.service;


import com.back.global.jwt.refreshToken.entity.RefreshToken;
import com.back.global.jwt.refreshToken.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${custom.refreshToken.expirationSeconds}")
    private long refreshTokenExpiration;

    // 기존 리프레시 토큰 삭제하고 생성
    @Transactional
    public String generateRefreshToken(Long userId) {
        // 기존 토큰 삭제
        refreshTokenRepository.deleteByUserId(userId);

        String token = generateSecureToken();
        RefreshToken refreshToken = RefreshToken.create(token, userId, refreshTokenExpiration);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    //검증 (만료 체크 포함)
    public boolean validateToken(String token) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        RefreshToken refreshToken = tokenOpt.get();
        if (refreshToken.isExpired()) {
            revokeToken(token); // 만료된 토큰 삭제
            return false;
        }

        return true;
    }

    //기존 토큰 지우고 발급(회전)
    @Transactional
    public String rotateToken(String oldToken) {
        Optional<RefreshToken> oldRefreshToken = refreshTokenRepository.findByToken(oldToken);

        if (oldRefreshToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        RefreshToken tokenData = oldRefreshToken.get();
        revokeToken(oldToken);

        return generateRefreshToken(tokenData.getUserId());
    }

    //삭제
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    //문자열 난수 조합
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // 만료된 토큰 정리 (1시간마다 실행)
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
