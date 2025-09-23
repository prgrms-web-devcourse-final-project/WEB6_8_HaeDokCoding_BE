package com.back.domain.post.comment.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@Tag(name = "ApiCommentController", description = "API 댓글 컨트롤러")
@RequiredArgsConstructor
public class CommentController {

}
