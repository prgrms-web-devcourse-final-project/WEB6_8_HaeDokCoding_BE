package com.back.domain.cocktail.enums;

public enum CocktailType {
    SHORT("숏"),
    LONG("롱"),
    SHOOTER("슈터"),
    CLASSIC("클래식");

    private final String description;

    CocktailType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
