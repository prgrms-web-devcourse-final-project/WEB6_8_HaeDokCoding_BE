package com.back;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.out.println("KAKAO_OAUTH2_CLIENT_ID: " + dotenv.get("KAKAO_OAUTH2_CLIENT_ID"));
        System.out.println("GOOGLE_OAUTH2_CLIENT_ID: " + dotenv.get("GOOGLE_OAUTH2_CLIENT_ID"));
        System.out.println("NAVER_OAUTH2_CLIENT_ID: " + dotenv.get("NAVER_OAUTH2_CLIENT_ID"));

        SpringApplication.run(BackApplication.class, args);
    }

}
