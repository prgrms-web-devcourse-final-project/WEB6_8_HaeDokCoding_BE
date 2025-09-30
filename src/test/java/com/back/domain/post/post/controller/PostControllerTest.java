package com.back.domain.post.post.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.back.domain.post.post.dto.request.PostCreateRequestDto;
import com.back.domain.post.post.dto.request.PostUpdateRequestDto;
import com.back.domain.post.post.dto.response.PostResponseDto;
import com.back.domain.post.post.enums.PostStatus;
import com.back.domain.post.post.service.PostService;
import com.back.global.jwt.JwtUtil;
import com.back.global.rq.Rq;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private PostService postService;
  @MockitoBean
  private JwtUtil jwtUtil;
  @MockitoBean
  private Rq rq;

  private PostResponseDto createSampleResponseDto(Long id) {
    return new PostResponseDto(
        id,
        "자유게시판",
        "테스트유저" + id,
        LocalDateTime.now(),
        LocalDateTime.now(),
        PostStatus.PUBLIC,
        "테스트 제목" + id,
        "테스트 내용" + id,
        List.of(
            "http://example.com/image1.jpg",
            "http://example.com/image2.jpg"
        ), // 이미지 리스트
        "http://example.com/video.mp4",
        List.of("태그1", "태그2"),
        0, // likeCount
        0,  // commentCount
        0 // viewCount
    );
  }

  @Test
  @DisplayName("게시글 작성 API 테스트")
  void createPost() throws Exception {
    // given
    PostCreateRequestDto requestDto = new PostCreateRequestDto(
        1L,
        PostStatus.PUBLIC,
        "테스트 제목1",
        "테스트 내용1",
        "http://example.com/video1.mp4",
        List.of("태그1", "태그2")
    );
    PostResponseDto responseDto = createSampleResponseDto(1L);
    given(postService.createPost(any(PostCreateRequestDto.class), any(null))).willReturn(responseDto);

    // when & then
    mockMvc.perform(post("/posts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.postId").value(1L))
        .andExpect(jsonPath("$.data.categoryName").value("자유게시판"))
        .andExpect(jsonPath("$.data.userNickName").value("테스트유저1"))
        .andExpect(jsonPath("$.data.title").value("테스트 제목1"))
        .andExpect(jsonPath("$.data.content").value("테스트 내용1"))
        .andExpect(jsonPath("$.data.imageUrl").value("http://example.com/image.jpg"))
        .andExpect(jsonPath("$.data.videoUrl").value("http://example.com/video.mp4"))
        .andExpect(jsonPath("$.data.tags[0]").value("태그1"))
        .andExpect(jsonPath("$.data.tags[1]").value("태그2"))
        .andExpect(jsonPath("$.data.likeCount").value(0))
        .andExpect(jsonPath("$.data.commentCount").value(0))
        .andExpect(jsonPath("$.data.viewCount").value(0))
        .andDo(print());
  }

  @Test
  @DisplayName("게시글 다건 조회 API 테스트")
  void getAllPosts() throws Exception {
    // given
    List<PostResponseDto> firstPage = new ArrayList<>();
    for (long i = 30; i >= 21; i--) {
      firstPage.add(createSampleResponseDto(i));
    }

    List<PostResponseDto> secondPage = new ArrayList<>();
    for (long i = 20; i >= 11; i--) {
      secondPage.add(createSampleResponseDto(i));
    }

    given(postService.getAllPosts(null)).willReturn(firstPage); // 첫 호출(lastId 없음)
    given(postService.getAllPosts(21L)).willReturn(secondPage);

    // when & then
    mockMvc.perform(get("/posts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(10)))
        .andExpect(jsonPath("$.data[0].postId").value(30))
        .andExpect(jsonPath("$.data[0].categoryName").value("자유게시판"))
        .andExpect(jsonPath("$.data[0].userNickName").value("테스트유저30"))
        .andExpect(jsonPath("$.data[0].title").value("테스트 제목30"))
        .andExpect(jsonPath("$.data[0].content").value("테스트 내용30"))
        .andExpect(jsonPath("$.data[0].imageUrl").value("http://example.com/image.jpg"))
        .andExpect(jsonPath("$.data[0].videoUrl").value("http://example.com/video.mp4"))
        .andExpect(jsonPath("$.data[0].tags[0]").value("태그1"))
        .andExpect(jsonPath("$.data[0].tags[1]").value("태그2"))
        .andExpect(jsonPath("$.data[0].likeCount").value(0))
        .andExpect(jsonPath("$.data[0].commentCount").value(0))
        .andExpect(jsonPath("$.data[0].viewCount").value(0))
        .andDo(print());

    mockMvc.perform(get("/posts?lastId=21"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(10)))
        .andExpect(jsonPath("$.data[0].postId").value(20))
        .andExpect(jsonPath("$.data[0].categoryName").value("자유게시판"))
        .andExpect(jsonPath("$.data[0].userNickName").value("테스트유저20"))
        .andExpect(jsonPath("$.data[0].title").value("테스트 제목20"))
        .andExpect(jsonPath("$.data[0].content").value("테스트 내용20"))
        .andExpect(jsonPath("$.data[0].imageUrl").value("http://example.com/image.jpg"))
        .andExpect(jsonPath("$.data[0].videoUrl").value("http://example.com/video.mp4"))
        .andExpect(jsonPath("$.data[0].tags[0]").value("태그1"))
        .andExpect(jsonPath("$.data[0].tags[1]").value("태그2"))
        .andExpect(jsonPath("$.data[0].likeCount").value(0))
        .andExpect(jsonPath("$.data[0].commentCount").value(0))
        .andExpect(jsonPath("$.data[0].viewCount").value(0))
        .andDo(print());
  }

  @Test
  @DisplayName("게시글 단건 조회 API 테스트")
  void getPost() throws Exception {
    // given
    Long postId = 1L;
    PostResponseDto responseDto = createSampleResponseDto(postId);
    given(postService.getPost(postId)).willReturn(responseDto);

    // when & then
    mockMvc.perform(get("/posts/{postId}", postId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.postId").value(postId))
        .andExpect(jsonPath("$.data.categoryName").value("자유게시판"))
        .andExpect(jsonPath("$.data.userNickName").value("테스트유저" + postId))
        .andExpect(jsonPath("$.data.title").value("테스트 제목" + postId))
        .andExpect(jsonPath("$.data.content").value("테스트 내용" + postId))
        .andExpect(jsonPath("$.data.imageUrl").value("http://example.com/image.jpg"))
        .andExpect(jsonPath("$.data.videoUrl").value("http://example.com/video.mp4"))
        .andExpect(jsonPath("$.data.tags[0]").value("태그1"))
        .andExpect(jsonPath("$.data.tags[1]").value("태그2"))
        .andExpect(jsonPath("$.data.likeCount").value(0))
        .andExpect(jsonPath("$.data.commentCount").value(0))
        .andExpect(jsonPath("$.data.viewCount").value(0))
        .andDo(print());
  }

  @Test
  @DisplayName("게시글 수정 API 테스트")
  void updatePost() throws Exception {
    // given
    Long postId = 1L;
    PostUpdateRequestDto requestDto = new PostUpdateRequestDto(
        postId,
        PostStatus.PUBLIC,
        "수정된 제목" + postId,
        "수정된 내용" + postId,
        List.of(
            1L,
            2L
        ), // 이미지 리스트
        "http://example.com/video.mp4",
        List.of("태그1", "태그2")
    );
    PostResponseDto responseDto = new PostResponseDto(
        postId,
        "자유게시판",
        "테스트유저" + postId,
        LocalDateTime.now(),
        LocalDateTime.now(),
        PostStatus.PUBLIC,
        requestDto.title(),
        requestDto.content(),
        List.of(
            "http://example.com/image1.jpg",
            "http://example.com/image2.jpg"
        ), // 이미지 리스트
        "http://example.com/video.mp4",
        List.of("태그1", "태그2"),
        0, // likeCount
        0,  // commentCount
        0 // viewCount
    );
    given(postService.updatePost(eq(1L), any(PostUpdateRequestDto.class), any(null))).willReturn(responseDto);

    // when & then
    mockMvc.perform(patch("/posts/{postId}", postId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("수정된 제목" + postId))
        .andExpect(jsonPath("$.data.content").value("수정된 내용" + postId))
        .andExpect(jsonPath("$.data.imageUrl").value("http://example.com/image.jpg"))
        .andExpect(jsonPath("$.data.videoUrl").value("http://example.com/video.mp4"))
        .andExpect(jsonPath("$.data.tags[0]").value("태그1"))
        .andExpect(jsonPath("$.data.tags[1]").value("태그2"))
        .andDo(print());
  }

  @Test
  @DisplayName("게시글 삭제 API 테스트")
  void deletePost() throws Exception {
    // given
    Long postId = 1L;
    // postService.deletePost(postId)가 호출될 때, 아무런 동작도 하지 않도록 설정 (void 메소드 Mocking)
    willDoNothing().given(postService).deletePost(postId);

    // when & then
    mockMvc.perform(delete("/posts/{postId}", postId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("success"))
        .andExpect(jsonPath("$.data").isEmpty())
        .andDo(print());

    // 추가 검증: postService의 deletePost 메소드가 정확히 postId=1L 인자로 1번 호출되었는지 확인
    verify(postService).deletePost(postId);
  }

  @Test
  @DisplayName("게시글 추천(좋아요) 토글 API 테스트")
  void toggleLike() throws Exception {
    // given
    Long postId = 1L;
    willDoNothing().given(postService).toggleLike(postId);

    // when & then
    mockMvc.perform(post("/posts/{postId}/like", postId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("success"))
        .andExpect(jsonPath("$.data").isEmpty())
        .andDo(print());

    verify(postService).toggleLike(postId);
  }
}
