package com.back.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final ClientRegistrationRepository clientRegistrationRepository;

    private DefaultOAuth2AuthorizationRequestResolver createDefaultResolver() {
        // Spring Security 기본 Authorization URI 사용
        return new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = createDefaultResolver().resolve(request);
        return customizeState(req, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = createDefaultResolver().resolve(request, clientRegistrationId);
        return customizeState(req, request);
    }

    private OAuth2AuthorizationRequest customizeState(OAuth2AuthorizationRequest req, HttpServletRequest request) {
        if (req == null) return null;

        // 요청 파라미터에서 redirectUrl 가져오기
        String redirectUrl = request.getParameter("redirectUrl");
        if (redirectUrl == null) redirectUrl = "/";

        // CSRF 방지용 nonce 추가
        String originState = UUID.randomUUID().toString();

        // redirectUrl#originState 결합
        String rawState = redirectUrl + "#" + originState;

        // Base64 URL-safe 인코딩
        String encodedState = Base64.getUrlEncoder().encodeToString(rawState.getBytes(StandardCharsets.UTF_8));

        return OAuth2AuthorizationRequest.from(req)
                .state(encodedState) // state 교체
                .build();
    }
}
