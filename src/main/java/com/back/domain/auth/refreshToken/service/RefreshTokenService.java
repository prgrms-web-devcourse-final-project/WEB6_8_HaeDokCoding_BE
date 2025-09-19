package com.back.domain.auth.refreshToken.service;

import com.back.domain.auth.refreshToken.entity.RefreshToken;
import com.back.domain.auth.refreshToken.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
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
    public String generateRefreshToken(Long userId, String email) {
        String token = generateSecureToken();
        RefreshToken refreshToken = RefreshToken.create(token, userId, email, refreshTokenExpiration);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    //검증
    public boolean validateToken(String token) {
        return refreshTokenRepository.findByToken(token).isPresent();
    }

    //기존 토큰 지우고 발급(회전)
    public String rotateToken(String oldToken) {
        Optional<RefreshToken> oldRefreshToken = refreshTokenRepository.findByToken(oldToken);

        if (oldRefreshToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        RefreshToken tokenData = oldRefreshToken.get();
        revokeToken(oldToken);

        return generateRefreshToken(tokenData.getUserId(), tokenData.getEmail());
    }

    //삭제
    public void revokeToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    //문자열 난수 조합
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
