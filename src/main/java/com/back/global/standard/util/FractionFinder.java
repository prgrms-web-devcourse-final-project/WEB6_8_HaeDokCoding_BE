package com.back.global.standard.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FractionFinder {

    public static void main(String[] args) {
        try {
            // 1️⃣ SQL 파일 경로 설정
            Path path = Paths.get("src/main/resources/cocktails.csv");

            // 2️⃣ 파일 읽기
            String content = Files.readString(path);

            // 3️⃣ 분수 패턴 정의 (예: 1/2, 3/4 등)
            Pattern pattern = Pattern.compile("\\d/\\d"); // 공백/문자 붙어 있어도 매칭
            Matcher matcher = pattern.matcher(content);

            // 4️⃣ 분수 출력
            boolean found = false;
            while (matcher.find()) {
                System.out.println("찾은 분수: " + matcher.group());
                found = true;
            }

            if (!found) {
                System.out.println("SQL 파일에 분수 표현이 없습니다.");
            }

        } catch (IOException e) {
            System.err.println("SQL 파일을 읽는 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
