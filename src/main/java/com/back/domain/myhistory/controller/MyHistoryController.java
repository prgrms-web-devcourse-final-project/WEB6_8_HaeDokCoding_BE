package com.back.domain.myhistory.controller;

import com.back.domain.myhistory.dto.MyHistoryCommentGoResponseDto;
import com.back.domain.myhistory.dto.MyHistoryCommentListDto;
import com.back.domain.myhistory.dto.MyHistoryPostListDto;
import com.back.domain.myhistory.dto.MyHistoryLikedPostListDto;
import com.back.domain.myhistory.service.MyHistoryService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Validated
public class MyHistoryController {

    private final MyHistoryService myHistoryService;

    /**
     * 내가 작성한 게시글 목록(무한스크롤)
     * @param userId 인증된 사용자 ID
     * @param lastCreatedAt 이전 페이지 마지막 createdAt (옵션)
     * @param lastId 이전 페이지 마지막 id (옵션)
     * @param limit 페이지 크기(1~100)
     * @return 게시글 아이템 목록과 다음 페이지 커서
     */
    @GetMapping("/posts")
    @Operation(summary = "내 게시글 목록", description = "내가 작성한 게시글 최신순 목록. 무한스크롤 파라미터 지원")
    public RsData<MyHistoryPostListDto> getMyPosts(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        MyHistoryPostListDto body = myHistoryService.getMyPosts(userId, lastCreatedAt, lastId, limit);
        return RsData.successOf(body);
    }
    
    /**
     * 내 게시글 이동 링크
     * @param userId 인증된 사용자 ID
     * @param postId 게시글 ID
     * @return 게시글 상세 이동 링크 정보
     */
    @GetMapping("/posts/{id}")
    @Operation(summary = "내 게시글로 이동", description = "내가 작성한 게시글 상세 링크 정보 반환")
    public RsData<com.back.domain.myhistory.dto.MyHistoryPostGoResponseDto> goFromPost(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable("id") Long postId
    ) {
        var body = myHistoryService.getPostLinkFromMyPost(userId, postId);
        return RsData.successOf(body);
    }

    /**
     * 내가 작성한 댓글 목록(무한스크롤)
     * @param userId 인증된 사용자 ID
     * @param lastCreatedAt 이전 페이지 마지막 createdAt (옵션)
     * @param lastId 이전 페이지 마지막 id (옵션)
     * @param limit 페이지 크기(1~100)
     * @return 댓글 아이템 목록과 다음 페이지 커서
     */
    @GetMapping("/comments")
    @Operation(summary = "내 댓글 목록", description = "내가 작성한 댓글 최신순 목록. 무한스크롤 파라미터 지원")
    public RsData<MyHistoryCommentListDto> getMyComments(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        MyHistoryCommentListDto body = myHistoryService.getMyComments(userId, lastCreatedAt, lastId, limit);
        return RsData.successOf(body);
    }

    /**
     * 내가 좋아요한 게시글 목록(무한스크롤)
     * @param userId 인증된 사용자 ID
     * @param lastCreatedAt 이전 페이지 마지막 createdAt (옵션)
     * @param lastId 이전 페이지 마지막 id (옵션)
     * @param limit 페이지 크기(1~100)
     * @return 좋아요 게시글 아이템 목록과 다음 페이지 커서
     */
    @GetMapping("/likes")
    @Operation(summary = "좋아요한 게시글 목록", description = "좋아요한 게시글 최신순 목록. 무한스크롤 파라미터 지원")
    public RsData<MyHistoryLikedPostListDto> getMyLikedPosts(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        MyHistoryLikedPostListDto body = myHistoryService.getMyLikedPosts(userId, lastCreatedAt, lastId, limit);
        return RsData.successOf(body);
    }

    /**
     * 댓글에서 게시글 이동 링크
     * @param userId 인증된 사용자 ID
     * @param commentId 댓글 ID
     * @return 댓글이 달린 게시글 상세 이동 링크 정보
     */
    @GetMapping("/comments/{id}")
    @Operation(summary = "댓글에서 게시글 이동", description = "내 댓글이 달린 게시글 상세 링크 정보 반환")
    public RsData<MyHistoryCommentGoResponseDto> goFromComment(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable("id") Long commentId
    ) {
        var body = myHistoryService.getPostLinkFromMyComment(userId, commentId);
        return RsData.successOf(body);
    }

    /**
     * 좋아요 목록에서 게시글 이동 링크
     * @param userId 인증된 사용자 ID
     * @param postId 게시글 ID
     * @return 좋아요한 게시글 상세 이동 링크 정보
     */
    @GetMapping("/likes/{id}")
    @Operation(summary = "좋아요 목록에서 이동", description = "좋아요한 게시글 상세 링크 정보 반환")
    public RsData<com.back.domain.myhistory.dto.MyHistoryPostGoResponseDto> goFromLikedPost(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable("id") Long postId
    ) {
        var body = myHistoryService.getPostLinkFromMyLikedPost(userId, postId);
        return RsData.successOf(body);
    }
}

