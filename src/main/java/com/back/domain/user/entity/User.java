package com.back.domain.user.entity;

import com.back.domain.post.post.entity.PostLike;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

    // OAuth2 관련 필드
    @Column(unique = true, length = 100)
    private String oauthId;     // OAuth 제공자별 고유 ID (예: kakao_123456789)

    private Double abvDegree;   // 알콜도수(회원 등급)

    @CreatedDate    // JPA Auditing 적용
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;   // 생성 날짜

    @LastModifiedDate    // JPA Auditing 적용
    @Column(nullable = false)
    private LocalDateTime updatedAt;   // 수정 날짜

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String role = "USER";

    // 양방향 매핑을 위한 필드
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    private List<String> getAuthoritiesAsStringList() {
        List<String> authorities = new ArrayList<>();
        if (isAdmin()) {
            authorities.add("ADMIN");
        } else {
            authorities.add("USER");
        }
        return authorities;
    }

    // Member의 role을 Security가 사용하는 ROLE_ADMIN, ROLE_USER 형태로 변환
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthoritiesAsStringList()
                .stream()
                .map(auth -> new SimpleGrantedAuthority("ROLE_" + auth))
                .toList();
    }
}