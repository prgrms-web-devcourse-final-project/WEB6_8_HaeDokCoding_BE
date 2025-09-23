package com.back.domain.myhistory.controller;

import com.back.domain.myhistory.dto.MyHistoryCommentGoResponseDto;
import com.back.domain.myhistory.dto.MyHistoryCommentListDto;
import com.back.domain.myhistory.dto.MyHistoryPostListDto;
import com.back.domain.myhistory.service.MyHistoryService;
import com.back.global.rsData.RsData;
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

    @GetMapping("/posts")
    public RsData<MyHistoryPostListDto> getMyPosts(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        MyHistoryPostListDto body = myHistoryService.getMyPosts(userId, lastCreatedAt, lastId, limit);
        return RsData.successOf(body);
    }
    
    @GetMapping("/posts/{id}")
    public RsData<com.back.domain.myhistory.dto.MyHistoryPostGoResponseDto> goFromPost(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable("id") Long postId
    ) {
        var body = myHistoryService.getPostLinkFromMyPost(userId, postId);
        return RsData.successOf(body);
    }

    @GetMapping("/comments")
    public RsData<MyHistoryCommentListDto> getMyComments(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        MyHistoryCommentListDto body = myHistoryService.getMyComments(userId, lastCreatedAt, lastId, limit);
        return RsData.successOf(body);
    }

    @GetMapping("/comments/{id}")
    public RsData<MyHistoryCommentGoResponseDto> goFromComment(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable("id") Long commentId
    ) {
        var body = myHistoryService.getPostLinkFromMyComment(userId, commentId);
        return RsData.successOf(body);
    }
}

