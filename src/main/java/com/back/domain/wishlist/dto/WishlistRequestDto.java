package com.back.domain.wishlist.dto;

import com.back.domain.wishlist.enums.WishlistStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistRequestDto {

    @NotNull
    private Long userId;

    // 생성 시 기본값 ACTIVE, 필요시 상태 지정 업데이트용으로 재사용 가능
    private WishlistStatus status;
}

