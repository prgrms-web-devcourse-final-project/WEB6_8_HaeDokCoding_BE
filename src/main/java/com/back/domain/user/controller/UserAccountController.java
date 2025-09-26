package com.back.domain.user.controller;

import com.back.domain.user.service.UserService;
import com.back.domain.user.service.UserAuthService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/account")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserService userService;
    private final UserAuthService userAuthService;

    @DeleteMapping
    @Operation(summary = "계정 비활성화(Soft Delete)", description = "DELETE /me/account: 사용자 상태를 DELETED로 전환하고 세션/토큰을 정리합니다.")
    public RsData<Void> deactivate(
            @AuthenticationPrincipal(expression = "id") Long userId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        userService.deactivateAccount(userId);

        // 현재 세션 쿠키 및 리프레시토큰 제거
        userAuthService.logout(request, response);

        return RsData.of(200, "계정 비활성화(탈퇴)가 완료되었습니다.");
    }
}
