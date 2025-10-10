package com.back.domain.cocktail.history;

import com.back.domain.cocktail.entity.Cocktail;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cocktail_keep_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CocktailKeepHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cocktail_id")
    private Cocktail cocktail;

    private int keepCount;

    private LocalDateTime recordedAt;
}