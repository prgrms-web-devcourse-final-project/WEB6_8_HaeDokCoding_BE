package com.back.global.init;

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
    @Autowired
    @Lazy
    private DevInitData self;


    @Bean
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
//            self.memberInit();

        };
    }

//    @Transactional
//    public void memberInit() {
//        if (memberService.count() > 0) {
//            return;
//        }
//    }

}
