package com.back.domain.cocktail.controller;

import com.back.domain.cocktail.repository.CocktailRepository;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cocktails")
@RequiredArgsConstructor
public class CocktailShareController {
    private final CocktailRepository cocktailRepository;

    @GetMapping("/{id}/share")
    public ResponseEntity<RsData<Map<String, String>>> getShareLink(@PathVariable Long id) {
        return cocktailRepository.findById(id)
                .map(cocktail -> {
                    Map<String, String> response = Map.of(
                            // 공유 URL
                            "url", "https://www.ssoul.or/cocktails/" + cocktail.getCocktailId(),
                            // 공유 제목
                            "title", cocktail.getCocktailName(),
                            // 공유 이미지 (선택)
                            "imageUrl", cocktail.getCocktailImgUrl()
                    );
                    return ResponseEntity.ok(RsData.successOf(response));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(RsData.failOf("칵테일을 찾을 수 없습니다.")));
    }
}
