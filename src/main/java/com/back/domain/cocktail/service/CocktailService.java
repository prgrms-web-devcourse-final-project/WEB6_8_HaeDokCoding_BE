package com.back.domain.cocktail.service;

import com.back.domain.cocktail.dto.CocktailDetailDto;
import com.back.domain.cocktail.dto.CocktailFilterRequestDto;
import com.back.domain.cocktail.dto.CocktailResponseDto;
import com.back.domain.cocktail.dto.CocktailSummaryDto;
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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CocktailService {

    private final CocktailRepository cocktailRepository;

    private static final int DEFAULT_SIZE = 20;

    @Transactional(readOnly = true)
    public Cocktail getCocktailById(Long id) {

            return cocktailRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found. id=" + id));
    }

        // 칵테일 무한스크롤 조회
        @Transactional(readOnly = true)
        public List<CocktailSummaryDto> getCocktails (Long lastId, Integer size)
        { // 무한스크롤 조회, 클라이언트 쪽에서 lastId와 size 정보를 받음.(스크롤 이벤트)
            int fetchSize = (size != null) ? size : DEFAULT_SIZE;

            List<Cocktail> cocktails;
            if (lastId == null) {
                // 첫 요청 → 최신 데이터부터
                cocktails = cocktailRepository.findAllByOrderByCocktailIdDesc(PageRequest.of(0, fetchSize));
            } else {
                // 무한스크롤 → 마지막 ID보다 작은 데이터 조회
                cocktails = cocktailRepository.findByCocktailIdLessThanOrderByCocktailIdDesc(lastId, PageRequest.of(0, fetchSize));
            }
            return cocktails.stream()
                    .map(c -> new CocktailSummaryDto(c.getCocktailId(), c.getCocktailName(), c.getCocktailImgUrl()))
                    .collect(Collectors.toList());
        }

        // 칵테일 검색기능
        @Transactional(readOnly = true)
        public List<Cocktail> cocktailSearch (String keyword){
            // cockTailName, ingredient이 하나만 있을 수도 있고 둘 다 있을 수도 있음
            if (keyword == null || keyword.trim().isEmpty()) {
                // 아무 검색어 없으면 전체 반환 처리
                return cocktailRepository.findAll();
            } else {
                // 이름 또는 재료 둘 중 하나라도 매칭되면 결과 반환
                return cocktailRepository.findByCocktailNameContainingIgnoreCaseOrIngredientContainingIgnoreCase(keyword, keyword);
            }
        }

        // 칵테일 검색,필터기능
        @Transactional(readOnly = true)
        public List<CocktailResponseDto> searchAndFilter (CocktailFilterRequestDto cocktailFilterRequestDto){
            // 기본값 페이지/사이즈 정하기(PAGE 기본값 0, 사이즈 10)
            int page = cocktailFilterRequestDto.getPage() != null && cocktailFilterRequestDto.getPage() >= 0
                    ? cocktailFilterRequestDto.getPage() : 0;

            int size = cocktailFilterRequestDto.getSize() != null && cocktailFilterRequestDto.getSize() > 0
                    ? cocktailFilterRequestDto.getSize() : DEFAULT_SIZE;

            // searchWithFilters에서 조회한 결과값을 pageResult에 저장.
            Pageable pageable = PageRequest.of(page, size);

            // 빈 리스트(null 또는 [])는 null로 변환
            List<AlcoholStrength> strengths = CollectionUtils.isEmpty(cocktailFilterRequestDto.getAlcoholStrengths())
                    ? null
                    : cocktailFilterRequestDto.getAlcoholStrengths();

            List<CocktailType> types = CollectionUtils.isEmpty(cocktailFilterRequestDto.getCocktailTypes())
                    ? null
                    : cocktailFilterRequestDto.getCocktailTypes();

            List<AlcoholBaseType> bases = CollectionUtils.isEmpty(cocktailFilterRequestDto.getAlcoholBaseTypes())
                    ? null
                    : cocktailFilterRequestDto.getAlcoholBaseTypes();

            // Repository 호출
            Page<Cocktail> pageResult = cocktailRepository.searchWithFilters(
                    cocktailFilterRequestDto.getKeyword(),
                    strengths, // List<AlcoholStrength>
                    types, // List<CocktailType>
                    bases, // List<AlcoholBaseType>
                    pageable
            );

            //Cocktail 엔티티 → CocktailResponseDto 응답 DTO로 바꿔주는 과정
            List<CocktailResponseDto> resultDtos = pageResult.stream()
                    .map(c -> new CocktailResponseDto(
                            c.getCocktailId(),
                            c.getCocktailName(),
                            c.getAlcoholStrength(),
                            c.getCocktailType(),
                            c.getAlcoholBaseType(),
                            c.getCocktailImgUrl(),
                            c.getCocktailStory(),
                            c.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            return resultDtos;
        }

//    private <T> List<T> nullIfEmpty(List<T> list) {
//        return CollectionUtils.isEmpty(list) ? null : list;
//    }

        // 칵테일 상세조회
        @Transactional(readOnly = true)
        public CocktailDetailDto getCocktailDetailById (Long cocktailId){
            Cocktail cocktail = cocktailRepository.findById(cocktailId)
                    .orElseThrow(() -> new NoSuchElementException("칵테일을 찾을 수 없습니다. id: " + cocktailId));
            return new CocktailDetailDto(cocktail);
        }
    }
