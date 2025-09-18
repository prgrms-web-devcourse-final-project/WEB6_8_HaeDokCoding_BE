package com.back.domain.user.support;

public final class AbvView {

    private AbvView(){}

    // 0~100%를 6단계로 매핑
    public static int levelOf(Double percent) {
        if (percent == null) return 1;

        double x = Math.max(0, Math.min(100, percent));
        int p = (int) x;

        if (p <= 10) return 1;      // 0~10
        if (p <= 25) return 2;      // 11~25
        if (p <= 45) return 3;      // 26~45
        if (p <= 65) return 4;      // 46~65
        if (p <= 85) return 5;      // 66~85
        return 6;                   // 86~100
    }

    // 화면용 "23.5%" 라벨
    public static String percentLabel(Double percent) {
        if (percent == null) return "0%";
        double x = Math.max(0.0, Math.min(100.0, percent));
        return (x % 1.0 == 0.0) ? String.format("%.0f%%", x) : String.format("%.1f%%", x);
    }
}
