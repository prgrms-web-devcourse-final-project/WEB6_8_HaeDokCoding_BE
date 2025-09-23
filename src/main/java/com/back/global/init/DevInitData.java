package com.back.global.init;

import com.back.domain.cocktail.dto.CocktailFilterRequestDto;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.repository.CocktailRepository;
import com.back.domain.cocktail.service.CocktailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {

    private final CocktailRepository cocktailRepository;
    private final CocktailService cocktailService;

    @Autowired
    @Lazy
    private DevInitData self;


    @Bean
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            self.cocktailInit();        // 테스트용 데이터 삽입
        };
    }

    @Transactional
    public void cocktailInit() {
        if (cocktailRepository.count() > 0) {
            return;
        }

        for (int i = 1; i <= 20; i++) {  // 20개의 테스트 칵테일 생성
            cocktailRepository.save(Cocktail.builder()
                    .cocktailName("Cocktail " + i)
                    .cocktailImgUrl("http://example.com/img" + i + ".jpg")
                    .ingredient("Ingredient "+ i)
                    .alcoholStrength(AlcoholStrength.NON_ALCOHOLIC)
                    .build());
        }

        CocktailFilterRequestDto filterDto = new CocktailFilterRequestDto();
        filterDto.setKeyword("cocktail 4"); // 검색 키워드 설정
        filterDto.setAlcoholStrengths(Arrays.asList(AlcoholStrength.NON_ALCOHOLIC));

        System.out.println("DevInitData: 테스트 칵테일 20개 삽입");
        System.out.println(cocktailService.getCocktailById(2l));
        System.out.println(cocktailService.cocktailSearch("cocktail 3"));
        System.out.println(cocktailService.cocktailSearch("Ingredient 4"));
        System.out.println("filterDTO 결과값"+cocktailService.searchAndFilter(filterDto));
    }
}

