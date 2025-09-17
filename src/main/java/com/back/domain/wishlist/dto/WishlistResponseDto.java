package com.back.domain.wishlist.dto;

import com.back.domain.wishlist.entity.Wishlist;
import com.back.domain.wishlist.enums.WishlistStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponseDto {
    private Long id;
    private Long userId;
    private WishlistStatus status;
    private LocalDateTime createdAt;

    public static WishlistResponseDto from(Wishlist wishlist) {
        if (wishlist == null) return null;
        return WishlistResponseDto.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser() != null ? wishlist.getUser().getId() : null)
                .status(wishlist.getStatus())
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}

