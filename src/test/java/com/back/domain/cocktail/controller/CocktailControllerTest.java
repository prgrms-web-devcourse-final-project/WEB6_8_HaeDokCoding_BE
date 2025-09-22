package com.back.domain.cocktail.controller;

import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import com.back.domain.cocktail.repository.CocktailRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CocktailControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private CocktailRepository cocktailRepository;


//    @Autowired
//    private UserService userService;

@Test
@DisplayName("칵테일 단건 조회 - 성공")
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
            get("/api/cocktails/{id}", savedCocktail.getCocktailId())
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
                get("/api/v1/cocktails/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        resultActions
                .andExpect(status().isOk()) // RsData는 HTTP 200으로 반환됨
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("칵테일이 존재하지 않습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
