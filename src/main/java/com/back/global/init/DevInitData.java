package com.back.global.init;

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
                    .ingredient("Ingredient1")
                    .alcoholStrength(AlcoholStrength.NON_ALCOHOLIC)
                    .build());
        }
        System.out.println("DevInitData: 테스트 칵테일 20개 삽입");
        System.out.println(cocktailService.findById(1l));
    }
}

