package com.back.domain.cocktail.controller;

import com.back.domain.cocktail.dto.CocktailRecommendResponseDto;
import com.back.domain.cocktail.service.RecommendService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cocktails/recommend")
@Tag(name = "ApiCocktailRecommendController", description = "API 칵테일 추천 컨트롤러")
@RequiredArgsConstructor
public class CocktailRecommendController {

    private final RecommendService recommendService;

    // 상세페이지 추천 (DTO로 반환)
    @Operation(summary = "상세페이지 유사 칵테일 추천", description = "현재 칵테일과 유사한 칵테일 최대 3개를 반환합니다.")
    @GetMapping("/related")
    public RsData<List<CocktailRecommendResponseDto>> recommendRelated(@RequestParam Long cocktailId) {
        return RsData.successOf(recommendService.recommendRelatedCocktails(cocktailId, 3));
    }
}
