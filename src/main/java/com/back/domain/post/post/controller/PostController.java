package com.back.domain.post.post.controller;

import com.back.domain.post.post.dto.request.PostCreateRequestDto;
import com.back.domain.post.post.dto.request.PostUpdateRequestDto;
import com.back.domain.post.post.dto.response.PostResponseDto;
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  /**
   * 게시글 작성 API
   * @param reqBody 게시글 작성 요청 DTO
   * @return 작성된 게시글 정보
   */
  @PostMapping
  public RsData<PostResponseDto> createPost(
      @Valid @RequestBody PostCreateRequestDto reqBody
  ) {
    return RsData.successOf(postService.createPost(reqBody)); // code=200, message="success"
  }

  /**
   * 게시글 다건 조회 API
   * @return 모든 게시글 리스트
   */
  @GetMapping
  public RsData<List<PostResponseDto>> getAllPosts() {
    return RsData.successOf(postService.getAllPosts()); // code=200, message="success"
  }

  /**
   * 게시글 단건 조회 API
   * @param postId 조회할 게시글 ID
   * @return 해당 ID의 게시글 정보
   */
  @GetMapping("/{postId}")
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
  @PatchMapping("/{postId}")
  public RsData<PostResponseDto> updatePost(
      @PathVariable Long postId,
      @Valid @RequestBody PostUpdateRequestDto reqBody
  ) {
    return RsData.successOf(postService.updatePost(postId, reqBody)); // code=200, message="success"
  }
}
