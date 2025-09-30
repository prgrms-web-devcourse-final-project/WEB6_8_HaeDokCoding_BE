package com.back.domain.myhistory.service;

import com.back.domain.myhistory.dto.*;
import com.back.domain.myhistory.repository.MyHistoryCommentRepository;
import com.back.domain.myhistory.repository.MyHistoryPostRepository;
import com.back.domain.myhistory.repository.MyHistoryLikedPostRepository;
import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostLike;
import com.back.domain.post.post.enums.PostStatus;
import com.back.domain.post.post.enums.PostLikeStatus;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyHistoryService {

    private final MyHistoryPostRepository myHistoryPostRepository;
    private final MyHistoryCommentRepository myHistoryCommentRepository;
    private final MyHistoryLikedPostRepository myHistoryLikedPostRepository;

    // 내가 작성한 게시글 목록 (무한스크롤)
    // - 삭제(DELETED)된 글은 제외, 최신순(createdAt desc, id desc)
    @Transactional(readOnly = true)
    public MyHistoryPostListDto getMyPosts(Long userId, LocalDateTime lastCreatedAt, Long lastId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int fetchSize = safeLimit + 1;

        List<Post> rows;
        if (lastCreatedAt == null || lastId == null) {
            rows = myHistoryPostRepository.findMyPostsFirstPage(userId, PostStatus.DELETED, PageRequest.of(0, fetchSize));
        } else {
            rows = myHistoryPostRepository.findMyPostsAfter(userId, PostStatus.DELETED, lastCreatedAt, lastId, PageRequest.of(0, fetchSize));
        }

        // +1개 초과 여부로 다음 페이지 유무 판단
        boolean hasNext = rows.size() > safeLimit;
        if (hasNext) rows = rows.subList(0, safeLimit);

        List<MyHistoryPostItemDto> items = new ArrayList<>();
        for (Post p : rows) items.add(MyHistoryPostItemDto.from(p));

        LocalDateTime nextCreatedAt = null;
        Long nextId = null;
        if (hasNext && !rows.isEmpty()) {
            Post last = rows.get(rows.size() - 1);
            nextCreatedAt = last.getCreatedAt();
            nextId = last.getId();
        }

        return new MyHistoryPostListDto(items, hasNext, nextCreatedAt, nextId);
    }

    // 내가 작성한 댓글 목록 (무한스크롤)
    // - 댓글과 게시글을 함께 조회(join fetch)하여 N+1 방지
    @Transactional(readOnly = true)
    public MyHistoryCommentListDto getMyComments(Long userId, LocalDateTime lastCreatedAt, Long lastId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int fetchSize = safeLimit + 1;

        List<Comment> rows;
        if (lastCreatedAt == null || lastId == null) {
            rows = myHistoryCommentRepository.findMyCommentsFirstPage(userId, PageRequest.of(0, fetchSize));
        } else {
            rows = myHistoryCommentRepository.findMyCommentsAfter(userId, lastCreatedAt, lastId, PageRequest.of(0, fetchSize));
        }

        boolean hasNext = rows.size() > safeLimit;
        if (hasNext) rows = rows.subList(0, safeLimit);

        List<MyHistoryCommentItemDto> items = new ArrayList<>();
        for (Comment c : rows) items.add(MyHistoryCommentItemDto.from(c));

        LocalDateTime nextCreatedAt = null;
        Long nextId = null;
        if (hasNext && !rows.isEmpty()) {
            Comment last = rows.get(rows.size() - 1);
            nextCreatedAt = last.getCreatedAt();
            nextId = last.getId();
        }

        return new MyHistoryCommentListDto(items, hasNext, nextCreatedAt, nextId);
    }

    // 내 댓글에서 게시글로 이동 링크 생성
    // - 권한 확인: 해당 댓글이 내 댓글인지 검사
    // - 게시글 상태가 삭제면 이동 불가(410)
    @Transactional(readOnly = true)
    public MyHistoryCommentGoResponseDto getPostLinkFromMyComment(Long userId, Long commentId) {
        Comment c = myHistoryCommentRepository.findByIdAndUserId(commentId, userId);
        if (c == null) {
            throw new ServiceException(404, "댓글을 찾을 수 없습니다.");
        }
        Post post = c.getPost();
        if (post.getStatus() == PostStatus.DELETED) {
            throw new ServiceException(410, "삭제된 게시글입니다.");
        }
        Long postId = post.getId();
        String apiUrl = "/posts/" + postId;
        return new MyHistoryCommentGoResponseDto(postId, apiUrl);
    }

    // 내가 작성한 게시글에서 이동 링크 생성 (권한/상태 검증 포함)
    @Transactional(readOnly = true)
    public MyHistoryPostGoResponseDto getPostLinkFromMyPost(Long userId, Long postId) {
        Post p = myHistoryPostRepository.findByIdAndUserId(postId, userId);
        if (p == null) {
            throw new ServiceException(404, "게시글을 찾을 수 없습니다.");
        }
        if (p.getStatus() == PostStatus.DELETED) {
            throw new ServiceException(410, "삭제된 게시글입니다.");
        }
        String apiUrl = "/posts/" + p.getId();
        return new MyHistoryPostGoResponseDto(p.getId(), apiUrl);
    }

    // 내가 좋아요(추천)한 게시글 목록 (무한스크롤)
    // - PostLike.createdAt 기준 최신순, 삭제된 게시글 제외
    @Transactional(readOnly = true)
    public MyHistoryLikedPostListDto getMyLikedPosts(Long userId, LocalDateTime lastCreatedAt, Long lastId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int fetchSize = safeLimit + 1;

        List<PostLike> rows;
        if (lastCreatedAt == null || lastId == null) {
            rows = myHistoryLikedPostRepository.findMyLikedPostsFirstPage(
                    userId,
                    PostLikeStatus.LIKE,
                    PostStatus.DELETED,
                    PageRequest.of(0, fetchSize)
            );
        } else {
            rows = myHistoryLikedPostRepository.findMyLikedPostsAfter(
                    userId,
                    PostLikeStatus.LIKE,
                    PostStatus.DELETED,
                    lastCreatedAt,
                    lastId,
                    PageRequest.of(0, fetchSize)
            );
        }

        boolean hasNext = rows.size() > safeLimit;
        if (hasNext) rows = rows.subList(0, safeLimit);

        List<MyHistoryLikedPostItemDto> items = new ArrayList<>();
        for (PostLike postLike : rows) items.add(MyHistoryLikedPostItemDto.from(postLike));

        LocalDateTime nextCreatedAt = null;
        Long nextId = null;
        if (hasNext && !rows.isEmpty()) {
            PostLike last = rows.get(rows.size() - 1);
            nextCreatedAt = last.getCreatedAt();
            nextId = last.getId();
        }

        return new MyHistoryLikedPostListDto(items, hasNext, nextCreatedAt, nextId);
    }

    @Transactional(readOnly = true)
    public MyHistoryPostGoResponseDto getPostLinkFromMyLikedPost(Long userId, Long postId) {
        PostLike postLike = myHistoryLikedPostRepository.findByPostIdAndUserIdLike(
                postId,
                userId,
                PostLikeStatus.LIKE
        );
        if (postLike == null) {
            throw new ServiceException(404, "좋아요한 게시글을 찾을 수 없습니다.");
        }
        Post post = postLike.getPost();
        if (post.getStatus() == PostStatus.DELETED) {
            throw new ServiceException(410, "삭제된 게시글입니다.");
        }
        String apiUrl = "/posts/" + post.getId();
        return new MyHistoryPostGoResponseDto(post.getId(), apiUrl);
    }
}