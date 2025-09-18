package com.back.domain.cocktail.service;

import com.back.domain.cocktail.repository.CocktailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CocktailService {

  private final CocktailRepository cocktailRepository;

//  public List<CocktailListDto> getNameImg(){
//      return cocktailRepository.findAllNameImg
//  }



}
