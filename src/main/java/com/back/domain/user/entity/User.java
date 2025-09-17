package com.back.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users", // DB 테이블 이름: User 대신 users 권장 (예약어 충돌 방지)
        indexes = {
                @Index(name = "ux_users_email", columnList = "email", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;   // 유저 소셜 이메일 (OAuth2 로그인 시 저장)

    @NotBlank
    @Column(nullable = false, unique = true)
    private String nickname;   // 유저 닉네임

    private String profileImgUrl;   // 프로필 이미지 URL

    private Double abvDegree;   // 온도(회원 등급)

    private LocalDateTime createdAt;   // 생성 날짜

    private LocalDateTime updatedAt;   // 수정 날짜
}