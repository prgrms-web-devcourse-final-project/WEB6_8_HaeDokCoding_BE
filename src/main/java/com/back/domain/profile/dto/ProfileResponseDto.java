package com.back.domain.profile.dto;

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
}
