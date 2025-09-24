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
