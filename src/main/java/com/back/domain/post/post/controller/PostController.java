package com.back.domain.post.post.controller;

import com.back.domain.post.post.dto.request.PostRequestDto;
import com.back.domain.post.post.dto.response.PostResponseDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
   * @param postRequestDto 게시글 작성 요청 DTO
   * @return 작성된 게시글 정보
   */
  @PostMapping
  public RsData<PostResponseDto> createPost(
      @RequestBody PostRequestDto postRequestDto
  ) {
    return RsData.successOf(postService.createPost(postRequestDto)); // code=200, message="success"
  }

  /**
   * 게시글 다건 조회 API
   * @return 모든 게시글 리스트
   */
  @GetMapping
  public RsData<List<Post>> getAllPosts() {
    List<Post> posts = postService.getAllPosts();
    return RsData.successOf(posts); // code=200, message="success"
  }

}
