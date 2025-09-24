package com.back.domain.post.comment.controller;

import com.back.domain.post.comment.dto.request.CommentCreateRequestDto;
import com.back.domain.post.comment.dto.request.CommentUpdateRequestDto;
import com.back.domain.post.comment.dto.response.CommentResponseDto;
import com.back.domain.post.comment.service.CommentService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  /**
   * 댓글 다건 조회 API
   * @param postId 댓글이 작성된 게시글 ID
   * @param lastId 마지막으로 조회한 댓글 ID (페이징 처리용, optional)
   * @return 댓글 목록
   */
  @GetMapping
  @Operation(summary = "댓글 다건 조회")
  public RsData<List<CommentResponseDto>> getComments(
      @PathVariable Long postId,
      @RequestParam(required = false) Long lastId
  ) {
    return RsData.successOf(commentService.getComments(postId, lastId)); // code=200, message="success"
  }

  /**
   * 댓글 단건 조회 API
   * @param postId 댓글이 작성된 게시글 ID
   * @param commentId 조회할 댓글 ID
   * @return 해당 ID의 댓글 정보
   */
  @GetMapping("/{commentId}")
  @Operation(summary = "댓글 단건 조회")
  public RsData<CommentResponseDto> getComment(
      @PathVariable Long postId,
      @PathVariable Long commentId
  ) {
    return RsData.successOf(commentService.getComment(postId, commentId)); // code=200, message="success"
  }

  /**
   * 댓글 수정 API
   * @param postId 댓글이 작성된 게시글 ID
   * @param commentId 수정할 댓글 ID
   * @param reqBody 댓글 수정 요청 DTO
   * @return 수정된 댓글 정보
   */
  @PatchMapping("/{commentId}")
  @Operation(summary = "댓글 수정")
  public RsData<CommentResponseDto> updateComment(
      @PathVariable Long postId,
      @PathVariable Long commentId,
      @Valid @RequestBody CommentUpdateRequestDto reqBody
  ) {
    return RsData.successOf(commentService.updateComment(postId, commentId, reqBody)); // code=200, message="success"
  }

  /**
   * 댓글 삭제 API
   * @param postId 댓글이 작성된 게시글 ID
   * @param commentId 삭제할 댓글 ID
   * @return 삭제 성공 메시지
   */
  @DeleteMapping("/{commentId}")
  @Operation(summary = "댓글 삭제")
  public RsData<Void> deleteComment(
      @PathVariable Long postId,
      @PathVariable Long commentId
  ) {
    commentService.deleteComment(postId, commentId);
    return RsData.successOf(null); // code=200, message="success"
  }
}
