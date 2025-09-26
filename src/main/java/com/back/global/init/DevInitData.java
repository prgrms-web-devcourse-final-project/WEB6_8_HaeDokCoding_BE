package com.back.global.init;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import com.back.domain.cocktail.repository.CocktailRepository;
import com.back.domain.post.category.entity.Category;
import com.back.domain.post.category.repository.CategoryRepository;
import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.comment.repository.CommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostLike;
import com.back.domain.post.post.enums.PostLikeStatus;
import com.back.domain.post.post.repository.PostLikeRepository;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationRepository notificationRepository;
    private final CocktailRepository cocktailRepository;
    private final com.back.domain.mybar.service.MyBarService myBarService;
    private final com.back.domain.mybar.repository.MyBarRepository myBarRepository;

    @Autowired
    @Lazy
    private DevInitData self;

    @Bean
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            self.cocktailInit();
            self.userInit();
            self.boardInit();
            self.myBarInit();
            self.notificationInit();
        };
    }

    @Transactional
    public void cocktailInit() {
        if (cocktailRepository.count() > 0) return;

        // 1) 하이볼
        cocktailRepository.save(Cocktail.builder()
                .cocktailName("Highball")
                .cocktailNameKo("하이볼")
                .alcoholStrength(AlcoholStrength.LIGHT)
                .cocktailType(CocktailType.LONG)
                .alcoholBaseType(AlcoholBaseType.WHISKY)
                .ingredient("위스키, 탄산수, 얼음, 레몬피")
                .recipe("잔에 얼음 → 위스키 → 탄산수 → 가볍게 스터")
                .cocktailImgUrl("/img/cocktail/1.jpg")
                .build());

        // 2) 진토닉
        cocktailRepository.save(Cocktail.builder()
                .cocktailName("Gin and Tonic")
                .cocktailNameKo("진토닉")
                .alcoholStrength(AlcoholStrength.WEAK)
                .cocktailType(CocktailType.LONG)
                .alcoholBaseType(AlcoholBaseType.GIN)
                .ingredient("진, 토닉워터, 얼음, 라임")
                .recipe("잔에 얼음 → 진 → 토닉워터 → 라임")
                .cocktailImgUrl("/img/cocktail/2.jpg")
                .build());

        // 3) 올드패션드
        cocktailRepository.save(Cocktail.builder()
                .cocktailName("Old Fashioned")
                .cocktailNameKo("올드패션드")
                .alcoholStrength(AlcoholStrength.STRONG)
                .cocktailType(CocktailType.SHORT)
                .alcoholBaseType(AlcoholBaseType.WHISKY)
                .ingredient("버번 위스키, 설탕/시럽, 앙고스투라 비터스, 오렌지 필")
                .recipe("시럽+비터스 → 위스키 → 얼음 → 스터 → 오렌지 필")
                .cocktailImgUrl("/img/cocktail/3.jpg")
                .build());

        // 4) 모히또
        cocktailRepository.save(Cocktail.builder()
                .cocktailName("Mojito")
                .cocktailNameKo("모히또")
                .alcoholStrength(AlcoholStrength.LIGHT)
                .cocktailType(CocktailType.LONG)
                .alcoholBaseType(AlcoholBaseType.RUM)
                .ingredient("라임, 민트, 설탕/시럽, 화이트 럼, 탄산수, 얼음")
                .recipe("라임+민트+시럽 머들 → 럼 → 얼음 → 탄산수")
                .cocktailImgUrl("/img/cocktail/4.jpg")
                .build());
    }

    @Transactional
    public void userInit() {
        userRepository.findByNickname("사용자A").orElseGet(() ->
                userRepository.save(User.builder().nickname("사용자A").role("USER").build())
        );
        userRepository.findByNickname("사용자B").orElseGet(() ->
                userRepository.save(User.builder().nickname("사용자B").role("USER").build())
        );
        userRepository.findByNickname("사용자C").orElseGet(() ->
                userRepository.save(User.builder().nickname("사용자C").role("USER").build())
        );
    }

    @Transactional
    public void boardInit() {
        if (postRepository.count() > 0) return;

        User userA = userRepository.findByNickname("사용자A").orElseThrow();
        User userB = userRepository.findByNickname("사용자B").orElseThrow();
        User userC = userRepository.findByNickname("사용자C").orElseThrow();

        Category free = categoryRepository.findAll().stream()
                .filter(c -> "자유".equals(c.getName()))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name("자유")
                        .description("자유 게시판")
                        .build()));

        Post postA = postRepository.save(Post.builder()
                .category(free)
                .user(userA)
                .title("A의 게시글")
                .content("내용A")
                .imageUrl("/img/cocktail/1.jpg")
                .build());

        Post postB = postRepository.save(Post.builder()
                .category(free)
                .user(userB)
                .title("B의 게시글")
                .content("내용B")
                .imageUrl("/img/cocktail/2.jpg")
                .build());

        // 댓글: C가 A/B 게시글에 작성
        commentRepository.save(Comment.builder()
                .post(postA)
                .user(userC)
                .content("C의 댓글 on A")
                .build());

        commentRepository.save(Comment.builder()
                .post(postB)
                .user(userC)
                .content("C의 댓글 on B")
                .build());

        // 좋아요: A→B, B→A, C→A, C→B
        postLikeRepository.save(PostLike.builder()
                .post(postB)
                .user(userA)
                .status(PostLikeStatus.LIKE)
                .build());
        postB.increaseLikeCount();

        postLikeRepository.save(PostLike.builder()
                .post(postA)
                .user(userB)
                .status(PostLikeStatus.LIKE)
                .build());
        postA.increaseLikeCount();

        postLikeRepository.save(PostLike.builder()
                .post(postA)
                .user(userC)
                .status(PostLikeStatus.LIKE)
                .build());
        postA.increaseLikeCount();

        postLikeRepository.save(PostLike.builder()
                .post(postB)
                .user(userC)
                .status(PostLikeStatus.LIKE)
                .build());
        postB.increaseLikeCount();

        postRepository.save(postA);
        postRepository.save(postB);

        // B 사용자 soft delete (게시글/좋아요/댓글 생성 후)
        User b = userRepository.findByNickname("사용자B").orElseThrow();
        b.markDeleted("탈퇴한 사용자");
        userRepository.save(b);
    }

    @Transactional
    public void notificationInit() {
        if (notificationRepository.count() > 0) return;

        // 게시글 소유자에게 COMMENT/LIKE 알림 생성
        Post postA = postRepository.findAll().stream()
                .filter(p -> "A의 게시글".equals(p.getTitle()))
                .findFirst().orElse(null);
        Post postB = postRepository.findAll().stream()
                .filter(p -> "B의 게시글".equals(p.getTitle()))
                .findFirst().orElse(null);
        if (postA == null || postB == null) return;

        User ownerA = postA.getUser();
        User ownerB = postB.getUser();

        notificationRepository.save(Notification.builder()
                .user(ownerA)
                .post(postA)
                .type(NotificationType.COMMENT)
                .message("새 댓글이 달렸습니다.")
                .build());

        notificationRepository.save(Notification.builder()
                .user(ownerA)
                .post(postA)
                .type(NotificationType.LIKE)
                .message("게시글에 좋아요가 추가되었습니다.")
                .build());

        notificationRepository.save(Notification.builder()
                .user(ownerA)
                .post(postA)
                .type(NotificationType.LIKE)
                .message("게시글에 좋아요가 추가되었습니다.")
                .build());

        notificationRepository.save(Notification.builder()
                .user(ownerB)
                .post(postB)
                .type(NotificationType.COMMENT)
                .message("새 댓글이 달렸습니다.")
                .build());

        notificationRepository.save(Notification.builder()
                .user(ownerB)
                .post(postB)
                .type(NotificationType.LIKE)
                .message("게시글에 좋아요가 추가되었습니다.")
                .build());

        notificationRepository.save(Notification.builder()
                .user(ownerB)
                .post(postB)
                .type(NotificationType.LIKE)
                .message("게시글에 좋아요가 추가되었습니다.")
                .build());
    }

    @Transactional
    public void myBarInit() {
        if (myBarRepository.count() > 0) return;

        User userA = userRepository.findByNickname("사용자A").orElse(null);
        User userB = userRepository.findByNickname("사용자B").orElse(null);
        User userC = userRepository.findByNickname("사용자C").orElse(null);

        if (userA == null || userC == null) return;

        // 칵테일 참조 준비
        var cocktails = cocktailRepository.findAll();
        Cocktail c1 = cocktails.stream().filter(c -> "하이볼".equals(c.getCocktailNameKo())).findFirst().orElse(null);
        Cocktail c2 = cocktails.stream().filter(c -> "진토닉".equals(c.getCocktailNameKo())).findFirst().orElse(null);
        Cocktail c3 = cocktails.stream().filter(c -> "올드패션드".equals(c.getCocktailNameKo())).findFirst().orElse(null);
        Cocktail c4 = cocktails.stream().filter(c -> "모히또".equals(c.getCocktailNameKo())).findFirst().orElse(null);

        // 방어: 칵테일 누락 시 스킵
        if (c1 == null || c2 == null || c3 == null || c4 == null) return;

        // A: c1(now-2d), c2(now-1d)
        myBarService.keep(userA.getId(), c1.getId());
        myBarService.keep(userA.getId(), c2.getId());
        myBarRepository.findByUser_IdAndCocktail_Id(userA.getId(), c1.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusDays(2)));
        myBarRepository.findByUser_IdAndCocktail_Id(userA.getId(), c2.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusDays(1)));

        if (userB != null && !userB.isDeleted()) {
            myBarService.keep(userB.getId(), c3.getId());
            myBarService.unkeep(userB.getId(), c3.getId());
        }

        // C: c2(now-3d), c3(now-2d), c4(now-1h)
        myBarService.keep(userC.getId(), c2.getId());
        myBarService.keep(userC.getId(), c3.getId());
        myBarService.keep(userC.getId(), c4.getId());
        myBarRepository.findByUser_IdAndCocktail_Id(userC.getId(), c2.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusDays(3)));
        myBarRepository.findByUser_IdAndCocktail_Id(userC.getId(), c3.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusDays(2)));
        myBarRepository.findByUser_IdAndCocktail_Id(userC.getId(), c4.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusHours(1)));
    }
}
