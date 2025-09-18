package com.back.domain.cocktail.enums;

public enum AlcoholBaseType {
    GIN("진"),
    BRANDY("브랜디"),
    RUM("럼"),
    VODKA("보드카"),
    LIQUEUR("리큐르"),
    WHISKY("위스키"),
    TEQUILA("데낄라"),
    WINE("와인"),
    OTHER("기타");

    private final String description;

    AlcoholBaseType(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
