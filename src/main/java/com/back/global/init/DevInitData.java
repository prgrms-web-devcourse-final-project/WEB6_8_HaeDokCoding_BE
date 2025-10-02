package com.back.global.init;

import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.repository.CocktailRepository;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.post.category.entity.Category;
import com.back.domain.post.category.repository.CategoryRepository;
import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.comment.repository.CommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostImage;
import com.back.domain.post.post.entity.PostLike;
import com.back.domain.post.post.enums.PostLikeStatus;
import com.back.domain.post.post.repository.PostImageRepository;
import com.back.domain.post.post.repository.PostLikeRepository;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostImageRepository postImageRepository;
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
            self.userInit();
            // myBar는 사용자 활성 상태 기준으로 초기화가 필요하므로 boardInit(soft delete)보다 먼저 실행
            self.myBarInit();
            self.boardInit();
            self.notificationInit();
        };
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
                .videoUrl("/img/cocktail/1.jpg")
                .build());

        Post postB = postRepository.save(Post.builder()
                .category(free)
                .user(userB)
                .title("B의 게시글")
                .content("내용B")
                .videoUrl("/img/cocktail/2.jpg")
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

        postImageRepository.save(PostImage.builder()
                .post(postA)
                .fileName("1.jpg")
                .url("/img/cocktail/1.jpg")
                .sortOrder(1)
                .build()
        );

        postImageRepository.save(PostImage.builder()
                .post(postB)
                .fileName("2.jpg")
                .url("/img/cocktail/2.jpg")
                .sortOrder(1)
                .build()
        );

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

        User userA = userRepository.findByNickname("사용자A").orElseThrow();
        User userB = userRepository.findByNickname("사용자B").orElseThrow();
        User userC = userRepository.findByNickname("사용자C").orElseThrow();

        // 칵테일 참조 준비: 이름 우선 매칭, 부족하면 ID 오름차순으로 보충
        var all = cocktailRepository.findAll();
        if (all.isEmpty()) return; // 칵테일 데이터 없으면 스킵

        java.util.List<String> prefer = java.util.List.of("하이볼", "진토닉", "올드패션드", "모히또");
        java.util.List<Cocktail> selected = new java.util.ArrayList<>();

        // 선호 이름 매칭
        for (String nameKo : prefer) {
            all.stream()
                    .filter(c -> nameKo.equals(c.getCocktailNameKo()))
                    .findFirst()
                    .ifPresent(c -> {
                        if (selected.stream().noneMatch(s -> java.util.Objects.equals(s.getId(), c.getId()))) {
                            selected.add(c);
                        }
                    });
        }

        // 부족분 보충: ID 오름차순으로 정렬 후 채우기
        all.stream()
                .sorted(java.util.Comparator.comparingLong(c -> c.getId() == null ? Long.MAX_VALUE : c.getId()))
                .forEach(c -> {
                    if (selected.size() < 4 && selected.stream().noneMatch(s -> java.util.Objects.equals(s.getId(), c.getId()))) {
                        selected.add(c);
                    }
                });

        // 실제 사용에 필요한 인덱스가 없으면 해당 동작을 스킵
        Cocktail c1 = selected.size() > 0 ? selected.get(0) : null;
        Cocktail c2 = selected.size() > 1 ? selected.get(1) : null;
        Cocktail c3 = selected.size() > 2 ? selected.get(2) : null;
        Cocktail c4 = selected.size() > 3 ? selected.get(3) : null;

        // A: c1(now-2d), c2(now-1d)
        if (c1 != null) {
            myBarService.keep(userA.getId(), c1.getId());
            myBarRepository.findByUser_IdAndCocktail_Id(userA.getId(), c1.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusDays(2)));
        }
        if (c2 != null) {
            myBarService.keep(userA.getId(), c2.getId());
            myBarRepository.findByUser_IdAndCocktail_Id(userA.getId(), c2.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusDays(1)));
        }

        // B: c3 keep 후 unkeep -> DELETED
        if (c3 != null) {
            myBarService.keep(userB.getId(), c3.getId());
            myBarService.unkeep(userB.getId(), c3.getId());
        }

        // C: c2(now-3d), c3(now-2d), c4(now-1h)
        if (c2 != null) {
            myBarService.keep(userC.getId(), c2.getId());
            myBarRepository.findByUser_IdAndCocktail_Id(userC.getId(), c2.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusDays(3)));
        }
        if (c3 != null) {
            myBarService.keep(userC.getId(), c3.getId());
            myBarRepository.findByUser_IdAndCocktail_Id(userC.getId(), c3.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusDays(2)));
        }
        if (c4 != null) {
            myBarService.keep(userC.getId(), c4.getId());
            myBarRepository.findByUser_IdAndCocktail_Id(userC.getId(), c4.getId()).ifPresent(m -> m.setKeptAt(java.time.LocalDateTime.now().minusHours(1)));
        }
    }
}
