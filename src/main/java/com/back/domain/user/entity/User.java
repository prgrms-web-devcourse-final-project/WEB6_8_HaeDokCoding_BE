package com.back.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // DB 테이블 이름: User 대신 users 권장 (예약어 충돌 방지)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;   // 유저 소셜 이메일 (OAuth2 로그인 시 저장)

    private String nickname;   // 유저 닉네임

    private String profileImgUrl;   // 프로필 이미지 URL

    private String apiKey;   // 리프레시 토큰 (쿠키 인증과 연동)

    private Double abvDegree;   // 온도(회원 등급)

    private LocalDateTime createdAt;   // 생성 날짜

    private LocalDateTime updatedAt;   // 수정 날짜
}