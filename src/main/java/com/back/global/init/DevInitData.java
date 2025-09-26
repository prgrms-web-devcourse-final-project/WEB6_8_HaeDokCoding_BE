package com.back.global.init;

import com.back.domain.cocktail.repository.CocktailRepository;
import com.back.domain.cocktail.service.CocktailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

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
//            self.cocktailInit();        // 테스트용 데이터 삽입
        };
    }
}

