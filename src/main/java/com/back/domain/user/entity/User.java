package com.back.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")  // 예약어 충돌 방지를 위해 "users" 권장
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OAuth 동의 범위에 따라 이메일이 없을 수 있어 nullable
    // 여러 provider에서 동일 이메일이 올 수 있으므로 unique 하지 않아도 됨
    @Column(length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;   // 고유 닉네임

    private Double abvDegree;   // 알콜도수(회원 등급)

    private LocalDateTime createdAt;   // 생성 날짜

    private LocalDateTime updatedAt;   // 수정 날짜

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String role = "USER";
}