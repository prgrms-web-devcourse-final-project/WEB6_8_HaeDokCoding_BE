package com.back.domain.user.service;

import com.back.domain.user.dto.RefreshTokenResDto;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.jwt.JwtUtil;
import com.back.global.jwt.refreshToken.entity.RefreshToken;
import com.back.global.jwt.refreshToken.repository.RefreshTokenRepository;
import com.back.global.jwt.refreshToken.service.RefreshTokenService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    static Set<String> param1 = Set.of("두둑한", "날씬한", "만취한", "알딸딸", "얼큰한", "시트러스", "도수높은", "톡쏘는", "거품가득", "하이볼한",
            "앙증맞은", "쓸쓸한", "거만한", "산만한", "귀찮은", "삐딱한", "맛이간", "저세상급", "시궁창", "기묘한",
            "졸린", "센치한", "철학적인", "무중력", "뽀송한", "전투적인", "배부른", "대충한", "쩌는", "철지난",
            "절규하는", "맞춤형", "다급한", "찌뿌둥한", "구수한", "문어발", "자포자기", "터무니", "귀척", "심드렁한",
            "무심한", "번쩍이는", "붉그레한", "밤새는", "좌절한", "의기양양", "비굴한", "터프한", "흘러내린", "공허한",
            "허무한", "헛기침", "뿜어대는", "질척한", "기어다님", "헤매는", "삐죽한", "악에받친", "격렬한", "삐까번쩍",
            "오지랖", "쪼르르", "꿀꺽", "머쓱한", "휘청대는", "추접", "천방지축", "어리둥절", "질주하는", "겸연쩍은",
            "뿌연", "썩은", "짠내나는", "철썩", "흥건한", "안간힘", "뜨끈한", "꾸덕한", "동공지진", "덕지덕지",
            "비밀", "개운한", "심란한", "음울한", "터질듯한", "달달한", "사악한", "기괴한", "용맹한", "껄끄러운",
            "헐떡이는", "허둥대는", "분란", "애매한", "찐득한", "허기진", "쩔어버린", "몽롱한", "허세", "황당한",
            "거대작음", "대차게구림", "어이없음", "두통약", "지갑", "이쑤시개", "돌침대", "고무장갑", "손수건", "바람개비",
            "지하철표", "송진가루", "철가방", "머리끈", "양말한짝", "라이터", "숟가락", "스티커", "드럼통", "열쇠",
            "벼락", "대걸레", "파리채", "앙금빵", "날개", "스티로폼", "건전지", "껌종이", "소화전", "비닐우산",
            "고드름", "전등갓", "양초", "지우개", "국자", "밥솥", "연필심", "깃털", "찜질팩", "청테이프",
            "김밥말이", "곰팡이", "청소기", "밤송이", "옥수수", "철창살", "휴지심", "선반", "곽티슈", "스프링",
            "고향된장", "머드팩", "장독대", "각질", "어묵꼬치", "환풍기", "군고구마", "카세트", "건조대", "박카스병",
            "우체통", "주차권", "털실뭉치", "지하수", "추리닝", "이불각", "육포", "빨대", "지렁이", "김칫국",
            "오징어채", "전기장판", "꽃병", "도시락통", "구급상자", "양배추잎", "고무줄", "망치", "유통기한", "알람시계",
            "방범창", "깔창", "만취육포", "날씬국자", "터프각질", "음울밥솥", "사악김치", "허세숟갈", "삐딱곰팡");

    static Set<String> param2 = Set.of("도토리딱개구리", "아프리카들개", "강남성인군자", "술고래", "알코올러버", "겨자잎", "청개구리", "산수유",
            "맥주문어", "칵테일앵무새", "보드카수달", "진토닉거북이", "테킬라코요테", "럼펭귄", "사케고양이", "막걸리두꺼비",
            "하이볼판다", "모히토돌고래", "피냐콜라다곰", "샴페인펭귄", "홍초원숭이", "네그로니청년", "IPA성기사",
            "블러디메리여사", "위스키호랑이", "쌍화차토끼", "유자도롱뇽", "복분자여우", "국화주해적단", "소맥언덕",
            "전통주공룡", "파전악어", "오징어숙취단", "민트라쿤", "땅콩버터공작새", "은행나무너구리", "고량주펭귄",
            "비빔밥바다표범", "돼지껍데기참새", "소주잔기린", "대왕쥐포코끼리", "군만두얼룩말", "마라탕너구리",
            "삼겹살청년", "곱창수달", "치킨도사", "라면위즈", "내복토끼", "냉면불사조", "젤리곰해파리", "아이스링곰",
            "젓가락토네이도", "기름떡볶이수달", "고구마바람개비", "파인애플악마", "번데기기사단", "곰탕판다",
            "마늘빵펠리컨", "옥수수수염신", "뿌링클드래곤", "껌딱지원숭이", "곤드레라쿤", "스티커헤라클레스",
            "삼색볼펜치타", "오렌지문어국수", "간장게장거북", "카스테라바퀴", "초코송이타조", "건빵악어",
            "너구리비상대책본부", "대하구이천사", "골뱅이버팔로", "라떼마라톤선수", "딸기생크림코알라",
            "찹쌀떡고래", "꿀꿀선비", "번개치킨집사", "고칼슘청새치", "가그린도마뱀", "소화제악마", "민트초코귀신",
            "통닭의무대장", "반건조오징어군단", "참깨부엉이", "바나나해커", "복숭아도둑너구리", "나쵸껍데기",
            "돌솥비버", "전자레인지곰", "냄비펭귄", "주전자사냥개", "콘치즈히드라", "우유팩할배", "막걸리도롱뇽",
            "짬뽕기린", "김치만두여신", "오이나무늘보", "버터쿠키살쾡이", "동치미해골", "청양고추돌고래",
            "다슬기시민", "와사비드래곤", "분식집카멜레온", "곰젤리술사", "귤껍질기사", "멸치왕국", "생맥바이킹",
            "병따개도마뱀", "굴튀김달팽이", "카레호랑이", "파슬리늑대", "오코노미야끼판다", "꽈배기늑대",
            "밀크티돌고래", "고기국수캥거루", "초코파이여단", "해장국곰", "쓰레기통요정", "달고나도깨비",
            "삼다수거북", "헛개차도마뱀", "카누호수악마", "치킨발바닥", "뱀술수호자", "파전너구리", "콩나물카멜레온",
            "대패삼겹돌고래", "굴비강아지", "막창펭귄", "감자튀김친구", "어묵사자", "부추말벌", "탕수육햄스터",
            "매운탕비둘기", "마라전골토끼", "돼지껍데기개구리", "술국호랑이", "두부오리", "깍두기코끼리",
            "라볶이사슴", "양파링문어", "피자청개구리", "고등어펭귄", "국밥파충류", "닭털마을", "바나나우럭",
            "김말이치타", "젓가락말미잘", "물회거북이", "한치하이에나", "청하상어", "참치꽁치", "해장라면매머드",
            "양꼬치토끼", "소떡소떡나방", "달걀말이원숭이", "김밥펭귄", "참외멍게", "고추전갈", "치즈덮밥여우",
            "닭껍질곰", "깻잎무당벌레", "갈비찜도마뱀", "미역국돌고래", "쌈채소사자", "두루치기청새치", "계란후라이늑대",
            "김치찌개토끼", "칼국수라쿤", "찌개나방", "해물탕코뿔소", "쌀국수표범", "떡꼬치상어", "날치알까마귀",
            "라멘수달", "나베공룡", "다시마돌고래", "곱창수사슴", "콜라북극곰", "된장찌개강아지", "젤리호랑이",
            "칵테일참새", "버블티치킨", "오렌지맥주드래곤", "구운치즈기린", "마늘빵거북이", "양고기판다",
            "초코우유너구리", "요플레거미", "옥수수탕기린", "피자토스트족제비", "떡갈비수달", "케이크맘모스",
            "스시참새", "광어버터캣", "황태국라쿤", "가래떡펭귄");

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

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
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }

        jwtUtil.removeAccessTokenCookie(response);
        jwtUtil.removeRefreshTokenCookie(response);
    }

    @Transactional
    public void setFirstLoginFalse(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        userOpt.ifPresent(user -> user.setFirstLogin(false));
    }
}