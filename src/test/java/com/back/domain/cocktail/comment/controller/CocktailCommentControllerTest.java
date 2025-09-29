package com.back.domain.cocktail.comment.controller;

import com.back.domain.cocktail.comment.dto.CocktailCommentCreateRequestDto;
import com.back.domain.cocktail.comment.dto.CocktailCommentResponseDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                "테스트 내용"+id
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
    @DisplayName("칵테일 대글 다건 조회 API 테스트")
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

        given(cocktailCommentService.getCocktailComments(1L,null)).willReturn(firstPage); // 첫 호출(lastId 없음)
        given(cocktailCommentService.getCocktailComments(1L,21L)).willReturn(secondPage);

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
}
