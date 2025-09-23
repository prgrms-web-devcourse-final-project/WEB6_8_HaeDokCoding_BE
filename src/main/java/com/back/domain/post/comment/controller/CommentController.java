package com.back.domain.post.comment.controller;

import com.back.domain.post.comment.dto.request.CommentCreateRequestDto;
import com.back.domain.post.comment.dto.response.CommentResponseDto;
import com.back.domain.post.comment.service.CommentService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@Tag(name = "ApiCommentController", description = "API 댓글 컨트롤러")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  /**
   * 댓글 작성 API
   * @param postId 댓글을 작성할 게시글 ID
   * @param reqBody 댓글 작성 요청 DTO
   * @return 작성된 댓글 정보
   */
  @PostMapping
  @Operation(summary = "댓글 작성")
  public RsData<CommentResponseDto> createComment(
      @PathVariable Long postId,
      @Valid @RequestBody CommentCreateRequestDto reqBody
  ) {
    return RsData.successOf(commentService.createComment(postId, reqBody)); // code=200, message="success"
  }

}
