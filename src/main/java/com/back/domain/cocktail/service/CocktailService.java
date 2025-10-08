package com.back.domain.cocktail.service;

import com.back.domain.cocktail.dto.CocktailDetailResponseDto;
import com.back.domain.cocktail.dto.CocktailSearchRequestDto;
import com.back.domain.cocktail.dto.CocktailSearchResponseDto;
import com.back.domain.cocktail.dto.CocktailSummaryResponseDto;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
import com.back.domain.cocktail.repository.CocktailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CocktailService {

    private final CocktailRepository cocktailRepository;

    private static final int DEFAULT_SIZE = 20;

    @Transactional(readOnly = true)
    public Cocktail getCocktailById(Long id) {

        return cocktailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cocktail not found. id=" + id));
    }

    @Transactional(readOnly = true)
    public List<CocktailSummaryResponseDto> getCocktails(Long lastValue, Long lastId, Integer size, String sortBy) {
        int fetchSize = (size != null) ? size : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, fetchSize);
        List<Cocktail> cocktails;

        switch (sortBy != null ? sortBy.toLowerCase() : "") {
            case "keeps":
                cocktails = (lastValue == null)
                        ? cocktailRepository.findAllOrderByKeepCountDesc(pageable)
                        : cocktailRepository.findByKeepCountLessThanOrderByKeepCountDesc(lastValue, lastId, pageable);
                break;
            case "comments":
                cocktails = (lastValue == null)
                        ? cocktailRepository.findAllOrderByCommentsCountDesc(pageable)
                        : cocktailRepository.findByCommentsCountLessThanOrderByCommentsCountDesc(lastValue, lastId, pageable);
                break;
            default:
                cocktails = (lastValue == null)
                        ? cocktailRepository.findAllByOrderByIdDesc(pageable)
                        : cocktailRepository.findByIdLessThanOrderByIdDesc(lastValue, pageable);
                break;
        }

        return cocktails.stream()
                .map(c -> new CocktailSummaryResponseDto(
                        c.getId(),
                        c.getCocktailName(),
                        c.getCocktailNameKo(),
                        c.getCocktailImgUrl(),
                        c.getAlcoholStrength().getDescription()
                ))
                .collect(Collectors.toList());
    }

    // 칵테일 검색,필터기능
    @Transactional(readOnly = true)
    public List<CocktailSearchResponseDto> searchAndFilter(CocktailSearchRequestDto cocktailSearchRequestDto) {
        // 기본값 페이지/사이즈 정하기(PAGE 기본값 0, 사이즈 10)
        int page = cocktailSearchRequestDto.getPage() != null && cocktailSearchRequestDto.getPage() >= 0
                ? cocktailSearchRequestDto.getPage() : 0;

        int size = cocktailSearchRequestDto.getSize() != null && cocktailSearchRequestDto.getSize() > 0
                ? cocktailSearchRequestDto.getSize() : DEFAULT_SIZE;

        // searchAndFilters에서 조회한 결과값을 pageResult에 저장.
        Pageable pageable = PageRequest.of(page, size);

        // 빈 리스트(null 또는 [])는 null로 변환
        List<AlcoholStrength> strengths = CollectionUtils.isEmpty(cocktailSearchRequestDto.getAlcoholStrengths())
                ? null
                : cocktailSearchRequestDto.getAlcoholStrengths();

        List<CocktailType> types = CollectionUtils.isEmpty(cocktailSearchRequestDto.getCocktailTypes())
                ? null
                : cocktailSearchRequestDto.getCocktailTypes();

        List<AlcoholBaseType> bases = CollectionUtils.isEmpty(cocktailSearchRequestDto.getAlcoholBaseTypes())
                ? null
                : cocktailSearchRequestDto.getAlcoholBaseTypes();

        // Repository 호출
        Page<Cocktail> pageResult = cocktailRepository.searchWithFilters(
                cocktailSearchRequestDto.getKeyword(),
                strengths, // List<AlcoholStrength>
                types, // List<CocktailType>
                bases, // List<AlcoholBaseType>
                pageable
        );

        //Cocktail 엔티티 → CocktailResponseDto 응답 DTO로 바꿔주는 과정
        List<CocktailSearchResponseDto> resultDtos = pageResult.stream()
                .map(CocktailSearchResponseDto::from)
                .collect(Collectors.toList());

        return resultDtos;
    }

    // 칵테일 상세조회
    @Transactional(readOnly = true)
    public CocktailDetailResponseDto getCocktailDetailById(Long cocktailId) {
        Cocktail cocktail = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new NoSuchElementException("칵테일을 찾을 수 없습니다. id: " + cocktailId));

        // ingredient 분수 변환
        List<IngredientDto> formattedIngredient = parseIngredients(convertFractions(cocktail.getIngredient()));

        return new CocktailDetailResponseDto(
                cocktail.getId(),
                cocktail.getCocktailName(),
                cocktail.getCocktailNameKo(),
                cocktail.getAlcoholStrength().getDescription(),
                cocktail.getCocktailType().getDescription(),
                cocktail.getAlcoholBaseType().getDescription(),
                cocktail.getCocktailImgUrl(),
                cocktail.getCocktailStory(),
                formattedIngredient,
                cocktail.getRecipe()
        );
    }

    private String convertFractions(String ingredient) {
        if (ingredient == null) return null;

        // 치환 테이블 생성
        Map<String, String> fractionMap = Map.of(
                "1/2", "½",
                "1/3", "⅓",
                "2/3", "⅔",
                "1/4", "¼",
                "3/4", "¾",
                "1/8", "⅛",
                "3/8", "⅜",
                "5/8", "⅝",
                "7/8", "⅞"
        );

        // 테이블 기반 치환
        for (Map.Entry<String, String> entry : fractionMap.entrySet()) {
            ingredient = ingredient.replace(entry.getKey(), entry.getValue());
        }

        return ingredient;
    }

    // Ingredient DTO
    public record IngredientDto(
            String ingredientName,
            String amount,
            String unit
    ) {
    }

    private List<IngredientDto> parseIngredients(String ingredientStr) {
        if (ingredientStr == null || ingredientStr.isBlank()) return Collections.emptyList();

        List<IngredientDto> result = new ArrayList<>();
        String[] items = ingredientStr.split(",\\s*");

        for (String item : items) {
            String[] parts = item.split(":");
            if (parts.length != 2) continue;

            String name = parts[0].trim();
            String amountUnit = parts[1].trim();

            // (숫자 + 선택적 분수) + (공백) + (단위)
            Pattern pattern = Pattern.compile(
                    "^([0-9]*\\s*[½⅓⅔¼¾⅛⅜⅝⅞]?)\\s*(.*)$",
                    Pattern.UNICODE_CHARACTER_CLASS
            );
            Matcher matcher = pattern.matcher(amountUnit);

            if (matcher.matches()) {
                String amount = matcher.group(1).trim();
                String unit = matcher.group(2).trim();

                result.add(new IngredientDto(name, amount, unit));
            } else {
                // 패턴 매치 실패 시 전체를 amount로 처리
                result.add(new IngredientDto(name, amountUnit, ""));
            }
        }

        return result;
    }
}
