package com.back.domain.myhistory.controller;

import com.back.domain.myhistory.dto.*;
import com.back.domain.myhistory.service.MyHistoryService;
import com.back.global.aspect.ResponseAspect;
import com.back.global.jwt.JwtUtil;
import com.back.global.rq.Rq;
import com.back.global.security.SecurityUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ResponseAspect.class)
@ImportAutoConfiguration(AopAutoConfiguration.class)
class MyHistoryControllerTest {

    private static final DateTimeFormatter ISO_WITH_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyHistoryService myHistoryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private Rq rq;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private SecurityUser createPrincipal(Long userId) {
        return new SecurityUser(
                userId,
                "user" + userId + "@example.com",
                "user" + userId,
                false,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of()
        );
    }

    private UsernamePasswordAuthenticationToken authenticated(SecurityUser principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private RequestPostProcessor withPrincipal(SecurityUser principal) {
        return request -> {
            UsernamePasswordAuthenticationToken authentication = authenticated(principal);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.setUserPrincipal(authentication);
            request.getSession(true)
                    .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            return request;
        };
    }

    @Test
    @DisplayName("Get my posts - first page")
    void getMyPosts_withoutCursor() throws Exception {
        SecurityUser principal = createPrincipal(10L);
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 9, 0, 0);
        LocalDateTime nextCreatedAt = createdAt.minusMinutes(5);

        MyHistoryPostItemDto item = MyHistoryPostItemDto.builder()
                .id(3L)
                .title("첫 글")
                .imageUrls(List.of("https://example.com/post.png"))
                .createdAt(createdAt)
                .likeCount(12)
                .commentCount(4)
                .build();

        MyHistoryPostListDto responseDto = new MyHistoryPostListDto(
                List.of(item),
                true,
                nextCreatedAt,
                2L
        );

        given(myHistoryService.getMyPosts(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(20)
        )).willReturn(responseDto);

        mockMvc.perform(get("/me/posts")
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.items[0].id").value(3L))
                .andExpect(jsonPath("$.data.items[0].title").value("첫 글"))
                .andExpect(jsonPath("$.data.items[0].imageUrls[0]").value("https://example.com/post.png"))
                .andExpect(jsonPath("$.data.items[0].createdAt").value(ISO_WITH_SECONDS.format(createdAt)))
                .andExpect(jsonPath("$.data.items[0].likeCount").value(12))
                .andExpect(jsonPath("$.data.items[0].commentCount").value(4))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCreatedAt").value(ISO_WITH_SECONDS.format(nextCreatedAt)))
                .andExpect(jsonPath("$.data.nextId").value(2L));

        verify(myHistoryService).getMyPosts(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(20)
        );
    }

    @Test
    @DisplayName("Get my posts - next page")
    void getMyPosts_withCursor() throws Exception {
        SecurityUser principal = createPrincipal(11L);
        LocalDateTime cursor = LocalDateTime.of(2025, 1, 5, 12, 30, 45);
        LocalDateTime createdAt = cursor.minusMinutes(2);

        MyHistoryPostItemDto item = MyHistoryPostItemDto.builder()
                .id(9L)
                .title("다음 글")
                .imageUrls(List.of())
                .createdAt(createdAt)
                .likeCount(0)
                .commentCount(1)
                .build();

        MyHistoryPostListDto responseDto = new MyHistoryPostListDto(
                List.of(item),
                false,
                null,
                null
        );

        given(myHistoryService.getMyPosts(
                eq(principal.getId()),
                eq(cursor),
                eq(99L),
                eq(5)
        )).willReturn(responseDto);

        mockMvc.perform(get("/me/posts")
                        .with(withPrincipal(principal))
                        .param("lastCreatedAt", cursor.toString())
                        .param("lastId", "99")
                        .param("limit", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(9L))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCreatedAt").doesNotExist());

        verify(myHistoryService).getMyPosts(
                eq(principal.getId()),
                eq(cursor),
                eq(99L),
                eq(5)
        );
    }

    @Test
    @DisplayName("Get my comments")
    void getMyComments() throws Exception {
        SecurityUser principal = createPrincipal(12L);
        LocalDateTime createdAt = LocalDateTime.of(2025, 2, 1, 18, 0, 0);

        MyHistoryCommentItemDto item = MyHistoryCommentItemDto.builder()
                .id(30L)
                .postId(100L)
                .postTitle("칵테일 후기")
                .content("정말 맛있어요")
                .createdAt(createdAt)
                .build();

        MyHistoryCommentListDto responseDto = new MyHistoryCommentListDto(
                List.of(item),
                false,
                null,
                null
        );

        given(myHistoryService.getMyComments(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(20)
        )).willReturn(responseDto);

        mockMvc.perform(get("/me/comments")
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(30L))
                .andExpect(jsonPath("$.data.items[0].postId").value(100L))
                .andExpect(jsonPath("$.data.items[0].postTitle").value("칵테일 후기"))
                .andExpect(jsonPath("$.data.items[0].content").value("정말 맛있어요"))
                .andExpect(jsonPath("$.data.items[0].createdAt").value(ISO_WITH_SECONDS.format(createdAt)));

        verify(myHistoryService).getMyComments(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(20)
        );
    }

    @Test
    @DisplayName("Get my liked posts")
    void getMyLikedPosts() throws Exception {
        SecurityUser principal = createPrincipal(13L);
        LocalDateTime likedAt = LocalDateTime.of(2025, 3, 10, 21, 5, 0);

        MyHistoryLikedPostItemDto item = MyHistoryLikedPostItemDto.builder()
                .id(70L)
                .title("봄 추천 칵테일")
                .imageUrls(List.of("https://example.com/spring.png"))
                .likedAt(likedAt)
                .likeCount(88)
                .commentCount(12)
                .build();

        MyHistoryLikedPostListDto responseDto = new MyHistoryLikedPostListDto(
                List.of(item),
                true,
                likedAt.minusSeconds(30),
                55L
        );

        given(myHistoryService.getMyLikedPosts(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(20)
        )).willReturn(responseDto);

        mockMvc.perform(get("/me/likes")
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(70L))
                .andExpect(jsonPath("$.data.items[0].title").value("봄 추천 칵테일"))
                .andExpect(jsonPath("$.data.items[0].imageUrls[0]").value("https://example.com/spring.png"))
                .andExpect(jsonPath("$.data.items[0].likedAt").value(ISO_WITH_SECONDS.format(likedAt)))
                .andExpect(jsonPath("$.data.items[0].likeCount").value(88))
                .andExpect(jsonPath("$.data.items[0].commentCount").value(12))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextId").value(55L));

        verify(myHistoryService).getMyLikedPosts(
                eq(principal.getId()),
                isNull(LocalDateTime.class),
                isNull(Long.class),
                eq(20)
        );
    }

    @Test
    @DisplayName("Go to my post detail")
    void goFromPost() throws Exception {
        SecurityUser principal = createPrincipal(14L);
        Long postId = 200L;
        MyHistoryPostGoResponseDto responseDto = new MyHistoryPostGoResponseDto(postId, "/posts/" + postId);

        given(myHistoryService.getPostLinkFromMyPost(principal.getId(), postId)).willReturn(responseDto);

        mockMvc.perform(get("/me/posts/{id}", postId)
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.postApiUrl").value("/posts/" + postId));

        verify(myHistoryService).getPostLinkFromMyPost(principal.getId(), postId);
    }

    @Test
    @DisplayName("Go to post from comment")
    void goFromComment() throws Exception {
        SecurityUser principal = createPrincipal(15L);
        Long commentId = 501L;
        MyHistoryCommentGoResponseDto responseDto = new MyHistoryCommentGoResponseDto(301L, "/posts/301");

        given(myHistoryService.getPostLinkFromMyComment(principal.getId(), commentId)).willReturn(responseDto);

        mockMvc.perform(get("/me/comments/{id}", commentId)
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(301L))
                .andExpect(jsonPath("$.data.postApiUrl").value("/posts/301"));

        verify(myHistoryService).getPostLinkFromMyComment(principal.getId(), commentId);
    }

    @Test
    @DisplayName("Go to post from liked list")
    void goFromLikedPost() throws Exception {
        SecurityUser principal = createPrincipal(16L);
        Long likedPostId = 901L;
        MyHistoryPostGoResponseDto responseDto = new MyHistoryPostGoResponseDto(likedPostId, "/posts/" + likedPostId);

        given(myHistoryService.getPostLinkFromMyLikedPost(principal.getId(), likedPostId)).willReturn(responseDto);

        mockMvc.perform(get("/me/likes/{id}", likedPostId)
                        .with(withPrincipal(principal))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(likedPostId))
                .andExpect(jsonPath("$.data.postApiUrl").value("/posts/" + likedPostId));

        verify(myHistoryService).getPostLinkFromMyLikedPost(principal.getId(), likedPostId);
    }
}
