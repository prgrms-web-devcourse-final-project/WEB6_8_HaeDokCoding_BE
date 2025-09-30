package com.back.domain.cocktail.comment.controller;

import com.back.domain.cocktail.comment.dto.CocktailCommentCreateRequestDto;
import com.back.domain.cocktail.comment.dto.CocktailCommentResponseDto;
import com.back.domain.cocktail.comment.dto.CocktailCommentUpdateRequestDto;
import com.back.domain.cocktail.comment.service.CocktailCommentService;
import com.back.domain.post.comment.enums.CommentStatus;
import com.back.global.jwt.JwtUtil;
import com.back.global.rq.Rq;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CocktailCommentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CocktailCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CocktailCommentService cocktailCommentService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private Rq rq;

    private CocktailCommentResponseDto createSampleResponseDto(Long id) {
        return new CocktailCommentResponseDto(
                id,
                1L,
                "테스트유저" + id,
                LocalDateTime.now(),
                LocalDateTime.now(),
                CommentStatus.PUBLIC,
                "테스트 내용" + id
        );
    }

    @Test
    @DisplayName("칵테일 댓글 작성 API 테스트")
    void t1() throws Exception {
        // given
        CocktailCommentCreateRequestDto requestDto = new CocktailCommentCreateRequestDto(
                CommentStatus.PUBLIC,
                "테스트 내용1"
        );
        CocktailCommentResponseDto responseDto = createSampleResponseDto(1L);
        given(cocktailCommentService.createCocktailComment(anyLong(), any(CocktailCommentCreateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/cocktails/{cocktailId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(1L))
                .andExpect(jsonPath("$.data.cocktailId").value(1L))
                .andExpect(jsonPath("$.data.userNickName").value("테스트유저1"))
                .andExpect(jsonPath("$.data.status").value("PUBLIC"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용1"))
                .andDo(print());
    }

    @Test
    @DisplayName("칵테일 댓글 다건 조회 API 테스트")
    void t2() throws Exception {
        // given
        List<CocktailCommentResponseDto> firstPage = new ArrayList<>();
        for (long i = 30; i >= 21; i--) {
            firstPage.add(createSampleResponseDto(i));
        }

        List<CocktailCommentResponseDto> secondPage = new ArrayList<>();
        for (long i = 20; i >= 11; i--) {
            secondPage.add(createSampleResponseDto(i));
        }

        given(cocktailCommentService.getCocktailComments(1L, null)).willReturn(firstPage); // 첫 호출(lastId 없음)
        given(cocktailCommentService.getCocktailComments(1L, 21L)).willReturn(secondPage);

        // when & then
        mockMvc.perform(get("/cocktails/{cocktailId}/comments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(10)))
                .andExpect(jsonPath("$.data[0].commentId").value(30))
                .andExpect(jsonPath("$.data[0].cocktailId").value(1L))
                .andExpect(jsonPath("$.data[0].userNickName").value("테스트유저30"))
                .andExpect(jsonPath("$.data[0].status").value("PUBLIC"))
                .andExpect(jsonPath("$.data[0].content").value("테스트 내용30"))
                .andDo(print());

        mockMvc.perform(get("/cocktails/{cocktailId}/comments", 1L).param("lastId", "21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(10)))
                .andExpect(jsonPath("$.data[0].commentId").value(20))
                .andExpect(jsonPath("$.data[0].cocktailId").value(1L))
                .andExpect(jsonPath("$.data[0].userNickName").value("테스트유저20"))
                .andExpect(jsonPath("$.data[0].status").value("PUBLIC"))
                .andExpect(jsonPath("$.data[0].content").value("테스트 내용20"))
                .andDo(print());
    }

    @Test
    @DisplayName("칵테일 댓글 단건 조회 API 테스트")
    void t3() throws Exception {
        // given
        Long cocktailId = 1L;
        Long cocktailCommentId = 1L;
        CocktailCommentResponseDto responseDto = createSampleResponseDto(cocktailCommentId);
        given(cocktailCommentService.getCocktailComment(cocktailId, cocktailCommentId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/cocktails/{cocktailId}/comments/{cocktailCommentId}", cocktailId, cocktailCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(cocktailCommentId))
                .andExpect(jsonPath("$.data.cocktailId").value(cocktailId))
                .andExpect(jsonPath("$.data.userNickName").value("테스트유저1"))
                .andExpect(jsonPath("$.data.status").value("PUBLIC"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용1"))
                .andDo(print());
    }

    @Test
    @DisplayName("칵테일 댓글 수정 API 테스트")
    void t4() throws Exception {
        // given
        Long cocktailId = 1L;
        Long cocktailCommentId = 1L;

        CocktailCommentUpdateRequestDto requestDto = new CocktailCommentUpdateRequestDto(
                CommentStatus.PUBLIC,
                "수정된 내용" + cocktailCommentId
        );

        CocktailCommentResponseDto responseDto = new CocktailCommentResponseDto(
                cocktailCommentId,
                cocktailId,
                "테스트유저" + cocktailCommentId,
                LocalDateTime.now(),
                LocalDateTime.now(),
                CommentStatus.PUBLIC,
                "수정된 내용" + cocktailCommentId
        );

        given(cocktailCommentService.updateCocktailComment(eq(cocktailId), eq(cocktailCommentId), any(CocktailCommentUpdateRequestDto.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/cocktails/{cocktailId}/comments/{cocktailCommentId}", cocktailId, cocktailCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(cocktailCommentId))
                .andExpect(jsonPath("$.data.cocktailId").value(cocktailId))
                .andExpect(jsonPath("$.data.userNickName").value("테스트유저" + cocktailCommentId))
                .andExpect(jsonPath("$.data.status").value("PUBLIC"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용" + cocktailCommentId))
                .andDo(print());
    }

    @Test
    @DisplayName("칵테일 댓글 삭제 API 테스트")
    void t5() throws Exception {
        // given
        Long cocktailId = 1L;
        Long cocktailCommentId = 1L;

        willDoNothing().given(cocktailCommentService).deleteCocktailComment(cocktailId, cocktailCommentId);

        // when & then
        mockMvc.perform(delete("/cocktails/{cocktailId}/comments/{cocktailCommentId}", cocktailId, cocktailCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value(nullValue())) // null 이므로 empty 체크 가능
                .andDo(print());

        // 추가 검증: 서비스 메소드가 정확히 호출됐는지 확인
        verify(cocktailCommentService).deleteCocktailComment(cocktailId, cocktailCommentId);
    }
}
