package com.back.domain.myhistory.dto;

import com.back.domain.post.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyHistoryCommentItemDto {
    private Long id;
    private Long postId;
    private String postTitle;
    private String content;
    private LocalDateTime createdAt;

    public static MyHistoryCommentItemDto from(Comment c) {
        return MyHistoryCommentItemDto.builder()
                .id(c.getId())
                .postId(c.getPost().getId())
                .postTitle(c.getPost().getTitle())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .build();
    }
}

