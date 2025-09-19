package com.back.domain.user.service;

import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.jwt.JwtUtil;
import com.back.global.jwt.refreshToken.entity.RefreshToken;
import com.back.global.jwt.refreshToken.repository.RefreshTokenRepository;
import com.back.global.jwt.refreshToken.service.RefreshTokenService;
import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    //OAuth 관련

    public User joinSocial(String oauthId, String email, String nickname){
        userRepository.findByOauthId(oauthId)
                .ifPresent(user -> {
                    throw new ServiceException(409, "이미 존재하는 계정입니다.");
                });

        // 고유한 닉네임 생성
        String uniqueNickname = generateUniqueNickname(nickname);

        User user = User.builder()
                .email(email)
                .nickname(uniqueNickname)
                .abvDegree(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role("USER")
                .oauthId(oauthId)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public RsData<User> findOrCreateOAuthUser(String oauthId, String email, String nickname) {
        Optional<User> existingUser = userRepository.findByOauthId(oauthId);

        if (existingUser.isPresent()) {
            // 기존 사용자 업데이트 (이메일만 업데이트)
            User user = existingUser.get();
            user.setEmail(email);
            return RsData.of(200, "회원 정보가 업데이트 되었습니다", user); //더티체킹
        } else {
            User newUser = joinSocial(oauthId, email, nickname);
            return RsData.of(201, "사용자가 생성되었습니다", newUser);
        }
    }

    public String generateUniqueNickname(String baseNickname) {
        // null이거나 빈 문자열인 경우 기본값 설정
        if (baseNickname == null || baseNickname.trim().isEmpty()) {
            baseNickname = "User";
        }

        String nickname = baseNickname;
        int counter = 1;

        // 중복 체크 및 고유한 닉네임 생성
        while (userRepository.findByNickname(nickname).isPresent()) {
            nickname = baseNickname + counter;
            counter++;
        }

        return nickname;
    }

    // 리프레시 토큰 관련

    public void issueTokens(HttpServletResponse response, Long userId, String email) {
        String accessToken = jwtUtil.generateAccessToken(userId, email);
        String refreshToken = refreshTokenService.generateRefreshToken(userId, email);

        jwtUtil.addAccessTokenToCookie(response, accessToken);
        jwtUtil.addRefreshTokenToCookie(response, refreshToken);
    }

    public boolean refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        try {
            String oldRefreshToken = jwtUtil.getRefreshTokenFromCookie(request);

            if (oldRefreshToken == null || !refreshTokenService.validateToken(oldRefreshToken)) {
                return false;
            }

            Optional<RefreshToken> tokenData = refreshTokenRepository.findByToken(oldRefreshToken);
            if (tokenData.isEmpty()) {
                return false;
            }

            RefreshToken refreshTokenEntity = tokenData.get();
            Long userId = refreshTokenEntity.getUserId();
            String email = refreshTokenEntity.getEmail();

            String newRefreshToken = refreshTokenService.rotateToken(oldRefreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(userId, email);

            jwtUtil.addAccessTokenToCookie(response, newAccessToken);
            jwtUtil.addRefreshTokenToCookie(response, newRefreshToken);

            return true;
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    //토큰 끊기면서 OAuth 자동 로그아웃
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }

        jwtUtil.removeAccessTokenCookie(response);
        jwtUtil.removeRefreshTokenCookie(response);
    }
}