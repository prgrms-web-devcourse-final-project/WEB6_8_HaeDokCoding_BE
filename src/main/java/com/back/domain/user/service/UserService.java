package com.back.domain.user.service;

import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found. id=" + id));
    }


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

}