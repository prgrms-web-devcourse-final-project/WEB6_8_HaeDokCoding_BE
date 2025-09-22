package com.back.domain.user.controller;

import com.back.domain.user.service.UserAuthService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UserAuth", description = "사용자 인증 API")
@Slf4j
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    //400 Bad Request: 클라이언트가 잘못된 요청을 보냄 (형식 오류)
    //401 Unauthorized: 인증 실패 (토큰 없음/만료/유효하지 않음)
    //404 Not Found: 리소스를 찾을 수 없음
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새로운 액세스 토큰을 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않거나 만료됨")
    })
    @PostMapping("/refresh")
    public RsData<Void> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        boolean success = userAuthService.refreshTokens(request, response);

        if (success) {
            return RsData.of(200, "토큰이 성공적으로 갱신되었습니다.");
        } else {
            return RsData.of(401, "토큰 갱신에 실패했습니다. 다시 로그인해주세요.");
        }
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 종료하고 토큰을 무효화")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public RsData<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        userAuthService.logout(request, response);
        return RsData.of(200, "로그아웃되었습니다.");
    }
}