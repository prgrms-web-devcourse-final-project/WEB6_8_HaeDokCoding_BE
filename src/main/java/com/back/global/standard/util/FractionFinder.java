package com.back.global.standard.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FractionFinder {

    public static void main(String[] args) {
        try {
            // 1️⃣ CSV 파일 경로 설정
            Path path = Paths.get("src/main/resources/cocktails.csv");

            // 2️⃣ 파일 읽기
            String content = Files.readString(path);

            // 3️⃣ 분수 패턴 정의 (예: 1/2, 3/4 등)
            Pattern pattern = Pattern.compile("\\d/\\d");
            Matcher matcher = pattern.matcher(content);

            // 4️⃣ 중복 제거용 Set
            Set<String> fractions = new HashSet<>();

            while (matcher.find()) {
                fractions.add(matcher.group()); // Set에 넣으면 자동 중복 제거
            }

            // 5️⃣ 출력
            if (fractions.isEmpty()) {
                System.out.println("CSV 파일에 분수 표현이 없습니다.");
            } else {
                System.out.println("찾은 분수 표현 (중복 제거):");
                for (String frac : fractions) {
                    System.out.println(frac);
                }
            }

        } catch (IOException e) {
            System.err.println("CSV 파일을 읽는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}