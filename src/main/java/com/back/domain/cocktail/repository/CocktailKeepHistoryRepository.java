package com.back.domain.cocktail.repository;

import com.back.domain.cocktail.history.CocktailKeepHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CocktailKeepHistoryRepository extends JpaRepository<CocktailKeepHistory, Long> {

}
