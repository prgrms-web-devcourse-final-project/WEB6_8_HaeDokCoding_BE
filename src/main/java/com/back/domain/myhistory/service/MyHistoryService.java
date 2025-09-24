package com.back.domain.myhistory.service;

import com.back.domain.myhistory.dto.*;
import com.back.domain.myhistory.repository.MyHistoryCommentRepository;
import com.back.domain.myhistory.repository.MyHistoryPostRepository;
import com.back.domain.myhistory.repository.MyHistoryLikedPostRepository;
import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.enums.PostStatus;
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
        String apiUrl = "/api/posts/" + postId;
        return new MyHistoryCommentGoResponseDto(postId, apiUrl);
    }

    @Transactional(readOnly = true)
    public MyHistoryPostGoResponseDto getPostLinkFromMyPost(Long userId, Long postId) {
        Post p = myHistoryPostRepository.findByIdAndUserId(postId, userId);
        if (p == null) {
            throw new ServiceException(404, "게시글을 찾을 수 없습니다.");
        }
        if (p.getStatus() == PostStatus.DELETED) {
            throw new ServiceException(410, "삭제된 게시글입니다.");
        }
        String apiUrl = "/api/posts/" + p.getId();
        return new MyHistoryPostGoResponseDto(p.getId(), apiUrl);
    }

    @Transactional(readOnly = true)
    public MyHistoryLikedPostListDto getMyLikedPosts(Long userId, LocalDateTime lastCreatedAt, Long lastId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int fetchSize = safeLimit + 1;

        List<com.back.domain.post.post.entity.PostLike> rows;
        if (lastCreatedAt == null || lastId == null) {
            rows = myHistoryLikedPostRepository.findMyLikedPostsFirstPage(
                    userId,
                    com.back.domain.post.post.enums.PostLikeStatus.LIKE,
                    com.back.domain.post.post.enums.PostStatus.DELETED,
                    PageRequest.of(0, fetchSize)
            );
        } else {
            rows = myHistoryLikedPostRepository.findMyLikedPostsAfter(
                    userId,
                    com.back.domain.post.post.enums.PostLikeStatus.LIKE,
                    com.back.domain.post.post.enums.PostStatus.DELETED,
                    lastCreatedAt,
                    lastId,
                    PageRequest.of(0, fetchSize)
            );
        }

        boolean hasNext = rows.size() > safeLimit;
        if (hasNext) rows = rows.subList(0, safeLimit);

        List<MyHistoryLikedPostItemDto> items = new ArrayList<>();
        for (com.back.domain.post.post.entity.PostLike pl : rows) items.add(MyHistoryLikedPostItemDto.from(pl));

        LocalDateTime nextCreatedAt = null;
        Long nextId = null;
        if (hasNext && !rows.isEmpty()) {
            com.back.domain.post.post.entity.PostLike last = rows.get(rows.size() - 1);
            nextCreatedAt = last.getCreatedAt();
            nextId = last.getId();
        }

        return new MyHistoryLikedPostListDto(items, hasNext, nextCreatedAt, nextId);
    }
}
