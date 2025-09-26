package com.back.domain.user.service;

import com.back.domain.user.entity.User;
import com.back.domain.user.enums.UserStatus;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.jwt.refreshToken.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found. id=" + id));
    }

    @Transactional
    public void deactivateAccount(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ServiceException(404, "사용자를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new ServiceException(409, "이미 탈퇴한 사용자입니다.");
        }

        user.markDeleted("탈퇴한 사용자");
        userRepository.save(user);

        // 모든 세션(리프레시 토큰) 폐기
        refreshTokenService.revokeAllForUser(id);
    }
}
