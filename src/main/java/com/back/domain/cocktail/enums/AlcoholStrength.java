package com.back.domain.cocktail.enums;

public enum AlcoholStrength {
    NON_ALCOHOLIC("논알콜 (0%)"),
    WEAK("약한 도수 (1~5%)"),
    LIGHT("가벼운 도수 (6~15%)"),
    MEDIUM("중간 도수 (16~25%)"),
    STRONG("센 도수 (26~35%)"),
    VERY_STRONG("매우 센 도수 (36%~)");

    private final String description;

    AlcoholStrength(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
