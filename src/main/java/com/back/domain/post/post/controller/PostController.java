package com.back.domain.post.post.controller;

import com.back.domain.post.post.dto.request.PostCreateRequestDto;
import com.back.domain.post.post.dto.request.PostUpdateRequestDto;
import com.back.domain.post.post.dto.response.PostResponseDto;
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
@Tag(name = "ApiPostController", description = "API 게시글 컨트롤러")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  /**
   * 게시글 작성 API
   *
   * @param reqBody 게시글 작성 요청 DTO
   * @param images 첨부 이미지 파일들 (optional)
   * @return 작성된 게시글 정보
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "게시글 작성")
  public RsData<PostResponseDto> createPost(
      @RequestPart("post") @Valid PostCreateRequestDto reqBody,
      @RequestPart(value = "images", required = false) List<MultipartFile> images
  ) {
    return RsData.successOf(postService.createPost(reqBody, images)); // code=200, message="success"
  }

  /**
   * 게시글 다건 조회 API
   * @param lastId 마지막으로 조회한 게시글 ID (페이징 처리용, optional)
   * @return 게시글 목록
   */
  @GetMapping
  @Operation(summary = "게시글 다건 조회")
  public RsData<List<PostResponseDto>> getAllPosts(
      @RequestParam(required = false) Long lastId
  ) {
    return RsData.successOf(postService.getAllPosts(lastId)); // code=200, message="success"
  }

  /**
   * 게시글 단건 조회 API
   * @param postId 조회할 게시글 ID
   * @return 해당 ID의 게시글 정보
   */
  @GetMapping("/{postId}")
  @Operation(summary = "게시글 단건 조회")
  public RsData<PostResponseDto> getPost(
      @PathVariable Long postId
  ) {
    return RsData.successOf(postService.getPost(postId)); // code=200, message="success"
  }

  /**
   * 게시글 수정 API
   * @param postId 수정할 게시글 ID
   * @param reqBody 게시글 수정 요청 DTO
   * @return 수정된 게시글 정보
   */
  @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "게시글 수정")
  public RsData<PostResponseDto> updatePost(
      @PathVariable Long postId,
      @RequestPart("post") @Valid PostUpdateRequestDto reqBody,
      @RequestPart(value = "images", required = false) List<MultipartFile> images
  ) {
    return RsData.successOf(postService.updatePost(postId, reqBody, images)); // code=200, message="success"
  }

  /**
   * 게시글 삭제 API
   * @param postId 삭제할 게시글 ID
   * @return 삭제 성공 메시지
   */
  @DeleteMapping("/{postId}")
  @Operation(summary = "게시글 삭제")
  public RsData<Void> deletePost(
      @PathVariable Long postId
  ) {
    postService.deletePost(postId);
    return RsData.successOf(null); // code=200, message="success"
  }

  /**
   * 게시글 추천(좋아요) 토글 API
   * @param postId 추천할 게시글 ID
   * @return 추천 상태 변경 성공 메시지
   */
  @PostMapping("/{postId}/like")
  @Operation(summary = "게시글 추천")
  public RsData<Void> toggleLike(
      @PathVariable Long postId
  ) {
    postService.toggleLike(postId);
    return RsData.successOf(null); // code=200, message="success"
  }
}
