package com.back.domain.profile.service;

import com.back.domain.profile.dto.ProfileResponseDto;
import com.back.domain.profile.dto.ProfileUpdateRequestDto;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
// moved level/label computation into DTO factory
import com.back.domain.post.post.enums.PostStatus;
import com.back.domain.post.comment.enums.CommentStatus;
import com.back.domain.post.post.enums.PostLikeStatus;
import com.back.domain.profile.repository.ProfileQueryRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileQueryRepository profileQueryRepository;

    // 내 프로필 요약 조회
    // - 카운트 3종(내 글/내 댓글/내가 좋아요한 글) 조회 후
    // - DTO 정적 팩토리(of)로 레벨/라벨 계산과 함께 응답 조립
    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ServiceException(404, "사용자를 찾을 수 없습니다."));

        long postCount = profileQueryRepository.countMyPosts(user.getId(), PostStatus.DELETED);
        long commentCount = profileQueryRepository.countMyComments(user.getId(), CommentStatus.DELETED);
        long likedPostCount = profileQueryRepository.countMyLikedPosts(user.getId(), PostLikeStatus.LIKE);

        return ProfileResponseDto.of(
                user,
                postCount,
                commentCount,
                likedPostCount
        );
    }

    // 프로필 수정 (닉네임)
    // - 길이/중복 검사 후 반영, 이후 최신 프로필 다시 조회
    @Transactional
    public ProfileResponseDto updateProfile(Long id, ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ServiceException(404, "사용자를 찾을 수 없습니다."));

        if (profileUpdateRequestDto.getNickname() != null) {
            String nickname = profileUpdateRequestDto.getNickname().trim();
            if (nickname.isEmpty() || nickname.length() > 10) {
                throw new ServiceException(400, "닉네임은 1~10자");
            }

            if (userRepository.existsByNicknameAndIdNot(nickname, id)) {
                throw new ServiceException(409, "이미 사용중인 닉네임");
            }

            user.setNickname(nickname);
        }

        userRepository.save(user);

        return getProfile(id);
    }
}
