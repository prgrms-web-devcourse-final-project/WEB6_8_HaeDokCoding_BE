package com.back.domain.mybar.entity;

import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.mybar.enums.KeepStatus;
import com.back.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MyBar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 킵 생성 시각 */
    @CreatedDate
    private LocalDateTime createdAt;

    /** 킵 해제 시각 (ACTIVE일 때는 null) */
    private LocalDateTime deletedAt;

    /** 킵한 사용자 */
    @ManyToOne
    private User user;

    /** 킵한 칵테일 */
    @ManyToOne
    private Cocktail cocktail;

    /** 킵 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // 기본값은 ACTIVE (활성 상태)
    // ACTIVE: 킵한 상태, DELETED: 킵 해제한 상태 (Soft Delete)
    private KeepStatus status = KeepStatus.ACTIVE;
}
