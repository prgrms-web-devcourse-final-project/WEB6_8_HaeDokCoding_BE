package com.back.domain.user.enums;

public enum AbvLevel {
    L1(1, 5, 10, "/img/grade/1.png"),
    L2(2, 11, 25, "/img/grade/2.png"),
    L3(3, 26, 45, "/img/grade/3.png"),
    L4(4, 46, 65, "/img/grade/4.png"),
    L5(5, 66, 85, "/img/grade/5.png"),
    L6(6, 86, 100, "/img/grade/6.png");

    public final int code;
    public final int min, max;
    public final String imagePath;

    AbvLevel(int code, int min, int max, String imagePath) {
        this.code = code;
        this.min = min;
        this.max = max;
        this.imagePath = imagePath;
    }

    /**
     * percent 값에 따라 등급 반환
     * 5% 미만은 L1보다 낮으므로 기본값 L1 반환
     */
    public static AbvLevel of(int percent) {
        for (var lv : values()) {
            if (percent >= lv.min && percent <= lv.max) return lv;
        }
        return L1; // 5% 미만도 기본적으로 L1 처리
    }
}