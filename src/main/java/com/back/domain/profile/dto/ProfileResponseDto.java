package com.back.domain.profile.dto;

import com.back.domain.user.entity.User;
import com.back.domain.user.enums.AbvLevel;
import com.back.domain.user.support.AbvView;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponseDto {
    private Long id;
    private String nickname;
    private String email;

    // 서버에 저장된 실제 값(0~100)
    private Double abvDegree;      // 83.2
    // 표현용(서버에서 계산)
    private Integer abvLevel;      // 1~6
    private String abvLabel;       // "83.2%"

    // 요약 카운트
    private Long myPostCount;
    private Long myCommentCount;
    private Long myLikedPostCount;

    public static ProfileResponseDto of(User user,
                                        long myPostCount,
                                        long myCommentCount,
                                        long myLikedPostCount) {
        Double percent = user.getAbvDegree();
        int percentInt = percent == null ? 0 : Math.max(0, Math.min(100, percent.intValue()));
        int level = AbvLevel.of(percentInt).code;
        String label = AbvView.percentLabel(percent);

        return ProfileResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .abvDegree(percent)
                .abvLevel(level)
                .abvLabel(label)
                .myPostCount(myPostCount)
                .myCommentCount(myCommentCount)
                .myLikedPostCount(myLikedPostCount)
                .build();
    }
}
