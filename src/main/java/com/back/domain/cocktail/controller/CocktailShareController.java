package com.back.domain.cocktail.controller;

import com.back.domain.cocktail.dto.CocktailShareResponseDto;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.service.CocktailService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cocktails")
@RequiredArgsConstructor
public class CocktailShareController {
    private final CocktailService cocktailService;

    @Value("${custom.prod.frontUrl}")
    private String frontUrl;

    @GetMapping("/{id}/share")
    public ResponseEntity<RsData<CocktailShareResponseDto>> getShareLink(@PathVariable Long id) {
        Cocktail cocktail = cocktailService.getCocktailById(id);

        CocktailShareResponseDto responseDto = new CocktailShareResponseDto(
                frontUrl + "/recipe/" + cocktail.getId(),
                cocktail.getCocktailNameKo(),
                cocktail.getCocktailImgUrl()
        );

        return ResponseEntity.ok(RsData.successOf(responseDto));
    }
}
