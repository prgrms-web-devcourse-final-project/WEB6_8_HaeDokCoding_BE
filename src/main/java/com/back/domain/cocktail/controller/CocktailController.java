package com.back.domain.cocktail.controller;

import com.back.domain.cocktail.dto.CocktailDetailDto;
import com.back.domain.cocktail.service.CocktailService;
import com.back.domain.user.service.UserService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/cocktails")
@RequiredArgsConstructor
public class CocktailController {

    private final CocktailService cocktailService;
    private final UserService userService;

    @GetMapping("/{id}")
    @Transactional
    @Operation(summary = "칵테일 단건 조회")
    public RsData<CocktailDetailDto> getCocktailDetailById(@PathVariable long id) {
        try {
            CocktailDetailDto cocktailDetailDto = cocktailService.getCocktailDetailById(id);
            return RsData.successOf(cocktailDetailDto);
        } catch (RuntimeException e) {
            return RsData.failOf("칵테일이 존재하지 않습니다.");
        }
    }
}
