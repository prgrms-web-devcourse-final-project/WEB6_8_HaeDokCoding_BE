package com.back.global.standard.util;

public class DecimalToFractionConverter {
    // ingredient 문자열 전체를 처리 (예: "진:4.5 cl, 레몬 주스:1.5 cl")
    public static String convert(String value) {
        if (value == null || value.isBlank()) return value;

        // 쉼표로 구분된 각 재료별로 처리
        String[] items = value.split(",");
        for (int i = 0; i < items.length; i++) {
            items[i] = convertSingleItem(items[i].trim());
        }
        return String.join(", ", items);
    }

    // 단일 재료 문자열 처리
    private static String convertSingleItem(String item) {
        try {
            // 숫자만 추출 (정수+소수)
            String numericPart = item.replaceAll("[^0-9.]", "");
            if (numericPart.isEmpty()) return item;

            double number = Double.parseDouble(numericPart);
            int intPart = (int) number;
            double fracPart = number - intPart;
            double tolerance = 0.02;

            String fraction = "";

            // 소수 -> 분수 변환
            if (Math.abs(fracPart - 0.25) < tolerance) fraction = "1/4";
            else if (Math.abs(fracPart - 0.33) < tolerance || Math.abs(fracPart - 0.3333) < tolerance)
                fraction = "1/3";
            else if (Math.abs(fracPart - 0.5) < tolerance) fraction = "1/2";
            else if (Math.abs(fracPart - 0.66) < tolerance || Math.abs(fracPart - 0.6666) < tolerance)
                fraction = "2/3";
            else if (Math.abs(fracPart - 0.75) < tolerance) fraction = "3/4";

            // fraction이 있으면 int + fraction, 없으면 정수면 int만, 소수면 그대로
            String fractionStr;
            if (!fraction.isEmpty()) {
                fractionStr = intPart == 0 ? fraction : intPart + " " + fraction;
            } else {
                fractionStr = (number == intPart) ? String.valueOf(intPart) : String.valueOf(number);
            }

            // 원본 item에서 숫자 부분만 교체
            return item.replace(numericPart, fractionStr);

        } catch (NumberFormatException e) {
            return item; // 이미 분수 등 숫자가 아닌 경우 그대로 반환
        }
    }

    // 테스트용 main
    public static void main(String[] args) {
        String test = "진:4.5 cl, 레몬 주스:1.5 cl, 마라스키노 리큐르:1.5 cl, 설탕:1.0 cl, 물:2 cl";
        System.out.println("변환 전: " + test);
        System.out.println("변환 후: " + convert(test));

        String[] testValues = {"4.5", "1.5", "0.5", "2.25", "3.75", "1 1/2", "abc", "2.0"};
        for (String val : testValues) {
            System.out.printf("%s → %s%n", val, convert(val));
        }
    }
}