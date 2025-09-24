package com.back.global.appConfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 60 * 60 * 24) // 24시간
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
public class SessionConfig {
}