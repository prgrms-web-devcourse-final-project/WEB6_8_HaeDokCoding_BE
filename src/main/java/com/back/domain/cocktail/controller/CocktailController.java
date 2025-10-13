package com.back.domain.cocktail.controller;

import com.back.domain.cocktail.dto.CocktailDetailResponseDto;
import com.back.domain.cocktail.dto.CocktailSearchRequestDto;
import com.back.domain.cocktail.dto.CocktailSearchResponseDto;
import com.back.domain.cocktail.dto.CocktailSummaryResponseDto;
import com.back.domain.cocktail.service.CocktailService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cocktails")
@Tag(name = "ApiCocktailController", description = "API 칵테일 컨트롤러")
@RequiredArgsConstructor
public class CocktailController {

    private final CocktailService cocktailService;

    @GetMapping("/{id}")
    @Transactional
    @Operation(summary = "칵테일 단건 조회")
    public RsData<CocktailDetailResponseDto> getCocktailDetailById(@PathVariable long id){

            CocktailDetailResponseDto cocktailDetailResponseDto = cocktailService.getCocktailDetailById(id);
            return RsData.successOf(cocktailDetailResponseDto);
    }

     // @param lastValue 다음 페이지에서 이 값보다 작은 항목만 가져오기 위해 사용
     // @param lastId 마지막으로 가져온 칵테일 ID (첫 요청 null 가능)
     // @param size   가져올 데이터 개수 (기본값 DEFAULT_SIZE)
     // @return RsData 형태의 칵테일 요약 정보 리스트
    @GetMapping
    @Transactional
    @Operation(summary = "칵테일 다건 조회")
    public RsData<List<CocktailSummaryResponseDto>> getCocktails(
            @RequestParam(value = "lastValue", required = false) Long lastValue,
            @RequestParam(value = "lastId", required = false) Long lastId,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "recent") String sortBy
    ) {
        List<CocktailSummaryResponseDto> cocktails = cocktailService.getCocktails(lastValue, lastId, size, sortBy);
        return RsData.successOf(cocktails);
    }

    // 칵테일 검색 및 필터링
    // POST 방식으로 JSON body를 통해 검색 조건 전달

    @PostMapping("/search")
    @Operation(summary = "칵테일 검색 및 필터링")
    public RsData<List<CocktailSearchResponseDto>> searchAndFilter(
            @RequestBody CocktailSearchRequestDto cocktailSearchRequestDto
    ) {
        // 서비스 호출
        List<CocktailSearchResponseDto> searchResults = cocktailService.searchAndFilter(cocktailSearchRequestDto);

        return RsData.successOf(searchResults);
    }
}
