package com.back.domain.cocktail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;


@Getter
@Setter
@NoArgsConstructor
@Entity
public class Cocktail {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long cocktailId;

    private String cocktailName;

    @Enumerated(EnumType.STRING)
    private AlcoholStrength alcoholStrength;

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

    private String cocktailStory;

    @Enumerated(EnumType.STRING)
    private CocktailType cocktailType;

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

    private String ingredient;
    private String recipe;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
