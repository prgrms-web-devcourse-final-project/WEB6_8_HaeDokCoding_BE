package com.back.domain.cocktail.entity;

<<<<<<< HEAD
import com.back.domain.cocktail.enums.AlcoholBaseType;
import com.back.domain.cocktail.enums.AlcoholStrength;
import com.back.domain.cocktail.enums.CocktailType;
=======
import com.back.domain.wishlist.entity.Wishlist;
>>>>>>> a031134 ({fix}:Cocktail-Wishlist relation)
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class Cocktail {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String cocktailName;

    @Enumerated(EnumType.STRING)
    private AlcoholStrength alcoholStrength; // 칵테일 알콜도수

    private String cocktailStory; // 칵테일 유래 등 이야기

    @Enumerated(EnumType.STRING)
    private CocktailType cocktailType; // 칵테일 컵에 따른 분류

    @Enumerated(EnumType.STRING)
    private AlcoholBaseType alcoholBaseType; // 칵테일 베이스에 따른 분류

    private String ingredient;

    private String recipe;

    private String cocktailImgUrl;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
