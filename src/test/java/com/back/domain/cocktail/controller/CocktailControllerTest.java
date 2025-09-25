package com.back.domain.cocktail.controller;

import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import com.back.domain.cocktail.repository.CocktailRepository;
import com.back.domain.cocktail.service.CocktailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class CocktailControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private CocktailRepository cocktailRepository;

    @Autowired
    private CocktailService cocktailService;


    @Test
    @DisplayName("칵테일 단건 조회 - 로그인 없이 성공")
    void t1() throws Exception {
        Cocktail savedCocktail = cocktailRepository.save(
                Cocktail.builder()
                        .cocktailName("모히토")
                        .alcoholStrength(AlcoholStrength.WEAK)
                        .cocktailType(CocktailType.SHORT)
                        .alcoholBaseType(AlcoholBaseType.RUM)
                        .cocktailImgUrl("https://example.com/image.jpg")
                        .cocktailStory("상쾌한 라임과 민트")
                        .ingredient("라임, 민트, 럼, 설탕, 탄산수")
                        .recipe("라임과 민트를 섞고 럼을 넣고 탄산수로 완성")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        // when: GET 요청
        ResultActions resultActions = mvc.perform(
                get("/cocktails/{id}", savedCocktail.getId())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then: 상태코드, JSON 구조 검증
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.cocktailName").value("모히토"))
                .andExpect(jsonPath("$.data.alcoholStrength").value("WEAK"))
                .andExpect(jsonPath("$.data.cocktailType").value("SHORT"))
                .andExpect(jsonPath("$.data.alcoholBaseType").value("RUM"));
    }

    @Test
    @DisplayName("칵테일 단건 조회 - 실패 (존재하지 않는 ID)")
    void t2() throws Exception {
        long nonExistentId = 9999L;

        ResultActions resultActions = mvc.perform(
                get("/cocktails/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        resultActions
                .andExpect(status().isNotFound()) // 전역 예외 처리기에서 404 반환
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("해당 데이터가 존재하지 않습니다"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("칵테일 다건 조회 - 성공 (파라미터 없음)")
    void t3() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(
                get("/cocktails")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("칵테일 다건 조회 - 성공 (파라미터 포함)")
    void t4() throws Exception {
        // given
        Long lastId = 1L;
        int size = 5;

        // when
        ResultActions resultActions = mvc.perform(
                get("/cocktails")
                        .param("lastId", lastId.toString())
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }
}
