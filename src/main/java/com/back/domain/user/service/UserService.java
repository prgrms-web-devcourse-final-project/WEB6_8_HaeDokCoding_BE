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

    // 소셜로그인으로 회원가입 & 회원 정보 수정
    public RsData<User> modifyOrJoin(String oauthId, String email, String nickname) {

        //oauthId로 기존 회원인지 확인
        User User = userRepository.findByOauthId(oauthId).orElse(null);

        // 기존 회원이 아니면 소셜로그인으로 회원가입 진행
        if(User == null) {
            User = joinSocial(oauthId, email, nickname);
            return new RsData<>(201, "회원가입이 완료되었습니다.", User);
        }

        // 기존 회원이면 회원 정보 수정
        modifySocial(User, nickname);
        return new RsData<>(200, "회원 정보가 수정되었습니다.", User);
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
                .profileImgUrl(null)
                .abvDegree(0.0)
                .role("USER")
                .oauthId(oauthId)
                .build();

        return userRepository.save(user);
    }

    public void modifySocial(User user, String nickname){
        user.setNickname(nickname);
        userRepository.save(user);
    }

    public RsData<User> findOrCreateOAuthUser(String oauthId, String email, String nickname, String provider) {
        Optional<User> existingUser = userRepository.findByOauthId(oauthId);

        if (existingUser.isPresent()) {
            // 기존 사용자 업데이트 (이메일만 업데이트)
            User user = existingUser.get();
            user.setEmail(email);
            return RsData.of(200, "기존 사용자 정보 업데이트", userRepository.save(user));
        } else {
            // 새 사용자 생성 - 고유한 닉네임 생성
            String uniqueNickname = generateUniqueNickname(nickname);
            User newUser = User.builder()
                    .oauthId(oauthId)
                    .email(email)
                    .nickname(uniqueNickname)
                    .provider(provider)
                    .role("USER")
                    .createdAt(LocalDateTime.now())
                    .build();
            return RsData.of(201, "새 사용자 생성", userRepository.save(newUser));
        }
    }

    private String generateUniqueNickname(String baseNickname) {
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