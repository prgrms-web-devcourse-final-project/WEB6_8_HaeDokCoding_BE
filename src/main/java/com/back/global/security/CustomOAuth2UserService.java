package com.back.global.security;

import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;

    // OAuth2 로그인 성공 시 자동 호출
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String oauthUserId = "";
        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String nickname = "";
        String email = "";

        switch (providerTypeCode) {
            case "KAKAO" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("properties");

                oauthUserId = oAuth2User.getName();
                nickname = (String) attributesProperties.get("nickname");
            }
            case "GOOGLE" -> {
                oauthUserId = oAuth2User.getName();
                nickname = (String) oAuth2User.getAttributes().get("name");
                email = (String) oAuth2User.getAttributes().get("email");
            }
            case "NAVER" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("response");

                oauthUserId = (String) attributesProperties.get("id");
                nickname = (String) attributesProperties.get("nickname");
                email = (String) attributesProperties.get("email");
            }
        }

        // OAuth ID를 제공자와 함께 저장 (예: kakao_123456789)
        String uniqueOauthId = providerTypeCode.toLowerCase() + "_" + oauthUserId;

        log.debug("OAuth2 user info - oauthUserId: {}, email: {}, nickname: {}, provider: {}",
                  oauthUserId, email, nickname, providerTypeCode);

        User user = userService.findOrCreateOAuthUser(uniqueOauthId, email, nickname, providerTypeCode).data();

        log.debug("User from DB - id: {}, email: {}, nickname: {}",
                  user.getId(), user.getEmail(), user.getNickname());

        // null 체크 및 기본값 설정
        String userEmail = user.getEmail() != null && !user.getEmail().trim().isEmpty()
                          ? user.getEmail() : "unknown@example.com";
        String userNickname = user.getNickname() != null && !user.getNickname().trim().isEmpty()
                             ? user.getNickname() : "Unknown User";

        // securityContext
        return new SecurityUser(
                user.getId(),
                userEmail,
                userNickname,
                user.getAuthorities(),
                oAuth2User.getAttributes()
        );
    }
}
