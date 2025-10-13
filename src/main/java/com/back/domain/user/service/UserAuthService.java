package com.back.domain.user.service;

import com.back.domain.user.dto.RefreshTokenResDto;
import com.back.domain.user.dto.UserMeResDto;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.jwt.JwtUtil;
import com.back.global.jwt.refreshToken.entity.RefreshToken;
import com.back.global.jwt.refreshToken.repository.RefreshTokenRepository;
import com.back.global.jwt.refreshToken.service.RefreshTokenService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.springframework.security.core.context.SecurityContextHolder.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    static Set<String> param1 = Set.of(
            "두둑한", "날씬한", "만취한", "알딸딸", "얼큰한", "시트러스", "도수높은", "톡쏘는", "거품가득", "하이볼한",
            "앙증맞은", "쓸쓸한", "거만한", "산만한", "귀찮은", "삐딱한", "맛이간", "저세상급", "시궁창", "기묘한",
            "졸린", "센치한", "철학적인", "무중력", "뽀송한", "전투적인", "배부른", "대충한", "쩌는", "철지난",
            "절규하는", "맞춤형", "다급한", "찌뿌둥한", "구수한", "문어발", "자포자기", "터무니", "귀척", "심드렁한",
            "무심한", "번쩍이는", "붉그레한", "밤새는", "좌절한", "의기양양", "비굴한", "터프한", "흘러내린", "공허한",
            "허무한", "헛기침", "뿜어대는", "질척한", "기어다님", "헤매는", "삐죽한", "악에받친", "격렬한", "삐까번쩍",
            "오지랖", "쪼르르", "꿀꺽", "머쓱한", "휘청대는", "추접", "천방지축", "어리둥절", "질주하는", "겸연쩍은",
            "뿌연", "썩은", "짠내나는", "철썩", "흥건한", "안간힘", "뜨끈한", "꾸덕한", "동공지진", "덕지덕지",
            "개운한", "심란한", "음울한", "터질듯한", "달달한", "사악한", "기괴한", "용맹한", "껄끄러운",
            "헐떡이는", "허둥대는", "분란", "애매한", "찐득한", "허기진", "쩔어버린", "몽롱한", "허세", "황당한",
            "거대작음", "수상한", "어이없는", "두통약", "이쑤시개", "돌침대", "고무장갑", "손수건", "바람개비",
            "지하철표", "송진가루", "철가방", "머리끈", "양말한짝", "파리채", "앙금빵", "날개", "스티로폼", "건전지",
            "껌종이", "소화전", "비닐우산", "고드름", "전등갓", "양초", "지우개", "국자", "밥솥", "연필심", "깃털",
            "찜질팩", "청테이프", "곰팡이", "청소기", "밤송이", "옥수수", "철창살", "휴지심", "선반", "곽티슈", "스프링",
            "고향된장", "머드팩", "장독대", "각질", "어묵꼬치", "환풍기", "군고구마", "카세트", "건조대", "박카스병",
            "우체통", "주차권", "털실뭉치", "지하수", "추리닝", "이불킥", "육포", "빨대", "지렁이", "김칫국",
            "오징어채", "전기장판", "꽃병", "도시락통", "구급상자", "양배추잎", "고무줄", "망치", "유통기한", "알람시계",
            "방범창", "깔창", "만취육포", "날씬국자", "터프각질", "음울밥솥", "사악김치", "허세숟갈", "삐딱곰팡,",
            "킹받는", "뇌절하는", "뻘쭘한", "영혼없는", "근본없는", "정신나간", "골때리는", "띠꺼운", "오지는", "지리는",
            "힙스터", "처량한", "아련한", "새초롬한", "능글맞은", "요염한", "흐물한", "말랑한", "미끈한", "푸석한", "눅눅한",
            "바삭한", "맨들한", "오싹한", "후련한", "나른한", "시크한", "쿨한", "힙한", "아방궁", "급발진", "알록달록",
            "뇌맑은", "핵인싸", "아싸", "무념무상", "만사귀찮"
    );

    static Set<String> param2 = Set.of(
            "술고래", "겨자잎", "청개구리", "산수유", "맥주문어","소맥언덕", "파전악어", "민트라쿤", "내복토끼", "곰탕판다", "꿀꿀선비", "돌솥비버", "냄비펭귄",
            "짬뽕기린", "멸치왕국", "해장국곰", "막창펭귄", "어묵사자", "부추말벌", "두부오리", "닭털마을", "청하상어",
            "참치꽁치", "펭귄짱", "참외멍게", "고추전갈", "닭껍질곰", "찌개나방", "라멘수달", "나베공룡", "스시참새",
            "두꺼비", "너구리", "호랑이", "도깨비", "유령", "요정", "해적", "닌자", "악마", "천사", "비둘기", "참새",
            "고양이", "강아지", "문어", "오징어", "하이에나", "치와와", "불도저", "로켓", "우주선",
            "막걸리", "와인", "고량주", "피자곰", "핫도그", "계란빵", "붕어빵",
            "호떡맨", "마늘곰", "양파맨", "망치곰", "병따개", "유리컵", "벽돌맨", "전봇대", "철가방", "주전자", "핵주먹",
            "불방망", "돌멩이", "똥파리", "방구쟁이", "잠만보", "도시락", "고무줄", "지우개", "알람시계", "망치",
            "연필심", "라이터", "파리채", "날개", "지하철", "국밥요정", "소주요정", "안주킬러", "스틸러",
            "김치도둑", "김밥도사", "파전전사", "치킨귀족", "라면냄새", "도인", "신선", "무당", "광대", "사또", "망나니", "무법자", "몽상가", "한량",
            "칼잽이", "총잡이", "도굴꾼", "장사치", "협객", "자객", "조폭", "선비",
            "마왕", "용사", "좀비", "강시", "구미호", "늑대", "여우", "불곰", "흑표", "승냥이", "삵", "해태",
            "가물치", "메기", "미꾸리", "쏘가리", "날치", "가오리", "해마", "불가사", "성게", "멍게", "해삼",
            "장작", "아궁이", "부뚜막", "가마솥", "물레방", "초가집", "기왓장", "대청", "마루", "장독",
            "막장", "염전", "몽둥이", "곡괭이", "삽자루", "낫", "호미", "지게", "대못", "나사", "너트", "볼트",
            "핵폭탄", "수류탄", "대포", "미사일", "탱크", "전함", "항공모", "잠수함", "전투기", "레이더",
            "보드카", "데킬라", "사케", "위스키", "깔루아", "진토닉", "모히또", "왕짱",
            "찐빵","누네띠네", "뻥튀기", "아메바", "해캄", "플랑크", "건달", "양아치", "깡패", "백수", "꼰대",
            "잼민이", "틀딱", "급식", "학식", "아재", "이모", "삼촌", "돌맹이"
    );

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Rq rq;

    //OAuth 관련

    public User joinSocial(String oauthId, String email, String nickname){
        userRepository.findByOauthId(oauthId)
                .ifPresent(user -> {
                    throw new ServiceException(409, "이미 존재하는 계정입니다.");
                });

        // 고유한 닉네임 생성
        String uniqueNickname = generateNickname(nickname);

        User user = User.builder()
                .email(email != null ? email : "")
                .nickname(uniqueNickname)
                .abvDegree(5.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role("USER")
                .oauthId(oauthId)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public RsData<User> findOrCreateOAuthUser(String oauthId, String email, String nickname) {
        Optional<User> existingUser = userRepository.findByOauthId(oauthId);

        if (existingUser.isPresent()) {
            // 기존 사용자 업데이트 (이메일만 업데이트)
            User user = existingUser.get();
            // null 체크 후 빈 문자열로 대체
            user.setEmail(email != null ? email : "");
            return RsData.of(200, "회원 정보가 업데이트 되었습니다", user); //더티체킹
        } else {
            User newUser = joinSocial(oauthId, email, nickname);
            return RsData.of(201, "사용자가 생성되었습니다", newUser);
        }
    }

    public String generateNickname(String baseNickname) {
        // param1과 param2에서 무작위 단어 선택
        String[] param1Array = param1.toArray(new String[0]);
        String[] param2Array = param2.toArray(new String[0]);

        String adjective = param1Array[(int) (Math.random() * param1Array.length)];
        String noun = param2Array[(int) (Math.random() * param2Array.length)];

        return adjective + " " + noun;
    }

    // 리프레시 토큰 관련

    public void issueTokens(HttpServletResponse response, Long userId, String email, String nickname) {
        String accessToken = jwtUtil.generateAccessToken(userId, email, nickname);
        String refreshToken = refreshTokenService.generateRefreshToken(userId);

        log.debug("토큰 발급 완료 - userId: {}, accessToken: {}, refreshToken: {}", userId, accessToken, refreshToken);

        jwtUtil.addAccessTokenToCookie(response, accessToken);
        jwtUtil.addRefreshTokenToCookie(response, refreshToken);
    }

    public RefreshTokenResDto refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        try {
            String oldRefreshToken = jwtUtil.getRefreshTokenFromCookie(request);
            log.debug("토큰 갱신 시도 - 받은 RefreshToken: {}", oldRefreshToken);

            if (oldRefreshToken == null) {
                log.error("RefreshToken이 쿠키에서 발견되지 않음");
                return null;
            }

            if (!refreshTokenService.validateToken(oldRefreshToken)) {
                log.error("RefreshToken 검증 실패: {}", oldRefreshToken);
                return null;
            }

            Optional<RefreshToken> tokenData = refreshTokenRepository.findByToken(oldRefreshToken);
            if (tokenData.isEmpty()) {
                return null;
            }

            RefreshToken refreshTokenEntity = tokenData.get();
            Long userId = refreshTokenEntity.getUserId();

            // DB에서 사용자 정보 조회
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return null;
            }

            User user = userOpt.get();

            String newRefreshToken = refreshTokenService.rotateToken(oldRefreshToken);
            String newAccessToken = jwtUtil.generateAccessToken(userId, user.getEmail(), user.getNickname());

            jwtUtil.addAccessTokenToCookie(response, newAccessToken);
            jwtUtil.addRefreshTokenToCookie(response, newRefreshToken);

            return RefreshTokenResDto.builder()
                    .accessToken(newAccessToken)
                    .user(
                            RefreshTokenResDto.UserInfoDto.builder()
                                    .id(user.getId().toString())
                                    .nickname(user.getNickname())
                                    .isFirstLogin(user.isFirstLogin())
                                    .abvDegree(user.getAbvDegree())
                                    .build()
                    )
                    .build();
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    //토큰 끊기면서 OAuth 자동 로그아웃
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. RefreshToken DB에서 삭제
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }

        // 2. JWT 쿠키 삭제
        jwtUtil.removeAccessTokenCookie(response);
        jwtUtil.removeRefreshTokenCookie(response);

        // 3. Spring Security 세션 무효화 (Redis 포함)
        try {
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
                log.debug("세션 무효화");
            }
        } catch (IllegalStateException e) {
            log.debug("세션이 이미 무효화되어 있음");
        }

        // 4. SecurityContext 클리어
        clearContext();

        log.info("로그아웃 완료 - JWT, 세션, SecurityContext 모두 정리됨");
    }

    @Transactional
    public void setFirstLoginFalse(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        userOpt.ifPresent(user -> user.setFirstLogin(false));
    }

    // 현재 로그인한 사용자 정보 조회 (세션 검증용)
    public UserMeResDto getCurrentUser() {
        try {
            User actor = rq.getActor();

            if (actor == null) {
                log.debug("인증되지 않은 사용자");
                throw new ServiceException(401, "인증되지 않은 사용자");
            }

            Optional<User> userOpt = userRepository.findById(actor.getId());
            if (userOpt.isEmpty()) {
                log.warn("사용자 ID {}를 DB에서 찾을 수 없음 (토큰은 유효하나 사용자 삭제됨)", actor.getId());
                throw new ServiceException(401, "인증되지 않은 사용자");
            }

            User user = userOpt.get();
            String provider = extractProvider(user.getOauthId());

            return UserMeResDto.builder()
                    .user(UserMeResDto.UserInfo.builder()
                            .id(user.getId().toString())
                            .email(user.getEmail())
                            .nickname(user.getNickname())
                            .isFirstLogin(user.isFirstLogin())
                            .abvDegree(user.getAbvDegree())
                            .provider(provider)
                            .build())
                    .build();

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 서버 오류 발생: {}", e.getMessage(), e);
            throw new ServiceException(500, "서버 내부 오류");
        }
    }

    private String extractProvider(String oauthId) {
        if (oauthId == null || oauthId.isBlank()) {
            return "unknown";
        }
        String[] parts = oauthId.split("_", 2);
        return parts.length > 0 ? parts[0] : "unknown";
    }
}