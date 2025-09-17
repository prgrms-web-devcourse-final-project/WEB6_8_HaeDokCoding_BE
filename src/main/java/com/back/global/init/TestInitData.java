package com.back.global.init;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
@RequiredArgsConstructor
public class TestInitData {
    @Autowired
    @Lazy
    private TestInitData self;

    @Bean
    ApplicationRunner testInitDataApplicationRunner() {
        return args -> {
//            self.memberInit();

        };
    }

//    @Transactional
//    public void memberInit() {
//        if (memberService.count() > 0) {
//            return;
//        }
//
//        memberService.join("system","12345678", "system@gmail.com");
//        memberService.join("admin","12345678", "admin@gmail.com");
//        memberService.join("user1","12345678", "user1@gmail.com");
//        memberService.join("user2","12345678", "user2@gmail.com");
//        memberService.join("user3","12345678", "user3@gmail.com");
//        memberService.join("user4","12345678", "user4@gmail.com");
//    }

}
