
# 첫 번째 스테이지: 빌드 스테이지
FROM gradle:jdk-21-and-23-graal-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드와 Gradle 래퍼 복사
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 종속성 설치
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY .env .
COPY src src

# 애플리케이션 빌드
#RUN gradle build --no-daemon
# 테스트 빌드 안하고 실행
RUN gradle build -x test --no-daemon

# 두 번째 스테이지: 실행 스테이지
FROM container-registry.oracle.com/graalvm/jdk:21

# 작업 디렉토리 설정
WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/.env .env

# 타임존 설정
ENV TZ=Asia/Seoul

# 실행할 JAR 파일 지정
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=prod", "-jar", "app.jar"]