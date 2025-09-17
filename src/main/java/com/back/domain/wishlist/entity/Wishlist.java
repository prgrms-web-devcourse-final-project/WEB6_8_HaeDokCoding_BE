package com.back.domain.wishlist.entity;

import com.back.domain.user.entity.User;
import com.back.domain.wishlist.enums.WishlistStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @ManyToOne
    private User user;  // 찜한 사용자 (위시리스트의 주인)

//    TODO: Cocktail 도메인 추가 후 활성화
//    @ManyToOne
//    private Cocktail cocktail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // 위시리스트 상태 - 기본값은 ACTIVE (활성 상태)
    // ACTIVE: 찜한 상태, DELETED: 찜 해제한 상태 (Soft Delete)
    private WishlistStatus status = WishlistStatus.ACTIVE;
}
