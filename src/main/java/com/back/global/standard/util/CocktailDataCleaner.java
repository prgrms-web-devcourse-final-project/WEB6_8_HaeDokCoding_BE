package com.back.global.standard.util;

import java.io.*;

public class CocktailDataCleaner {
    public static void main(String[] args) {
        String inputPath = "src/main/resources/cocktails.csv";         // 원본 파일 경로
        String outputPath = "src/main/resources/cocktails_clean.csv";  // 정제 후 파일 경로

        try (
                BufferedReader br = new BufferedReader(new FileReader(inputPath));
                BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))
        ) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                // CSV 안의 쉼표와 큰따옴표를 안전하게 처리
                String[] columns = splitCsvLine(line);

                if (isHeader) {
                    bw.write(String.join(",", columns));
                    bw.newLine();
                    isHeader = false;
                    continue;
                }

                // ✅ 6번째 인덱스(ingredient) 컬럼만 변환
                if (columns.length > 6) {
                    columns[6] = DecimalToFractionConverter.convert(columns[6]);
                }

                bw.write(String.join(",", columns));
                bw.newLine();
            }

            System.out.println("✅ 칵테일 데이터 정제가 완료되었습니다: " + outputPath);

        } catch (IOException e) {
            System.err.println("❌ 파일 처리 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * CSV 한 줄을 안전하게 split하는 메서드
     * (문장 안에 쉼표가 포함된 경우 대응)
     */
    private static String[] splitCsvLine(String line) {
        // 따옴표를 기준으로 나누되, 따옴표 안의 콤마는 무시
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }
}
