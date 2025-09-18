package com.back.domain.cocktail.entity;

import com.back.domain.wishlist.entity.Wishlist;
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
    private AlcoholStrength alcoholStrength; // 칵테일 알콜 도수

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

    private String cocktailStory; // 칵테일 유래 등 이야기

    @Enumerated(EnumType.STRING)
    private CocktailType cocktailType; // 칵테일 컵에 따른 분류

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
