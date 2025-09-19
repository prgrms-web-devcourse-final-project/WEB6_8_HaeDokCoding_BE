package com.back.global.security;

import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserAuthService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserAuthService userAuthService;

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
        RsData<User> rsData = userAuthService.findOrCreateOAuthUser(uniqueOauthId, email, nickname);

        if (rsData.code()<200 || rsData.code()>299) {
            throw new OAuth2AuthenticationException("사용자 생성/조회 실패: " + rsData.message());
        }

        User user = rsData.data();

        String userEmail = user.getEmail() != null && !user.getEmail().trim().isEmpty()
                ? user.getEmail() : "unknown";

        return new SecurityUser(
                user.getId(),
                userEmail,
                user.getNickname(),
                user.getAuthorities(),
                oAuth2User.getAttributes()
        );
    }
}
