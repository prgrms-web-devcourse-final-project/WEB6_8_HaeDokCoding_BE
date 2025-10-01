package com.back.domain.post.category.service;

import com.back.domain.post.category.dto.request.CategoryCreateRequestDto;
import com.back.domain.post.category.entity.Category;
import com.back.domain.post.category.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  // 카테고리 작성 로직
  @Transactional
  public Category createCategory(CategoryCreateRequestDto reqBody) {
    Category category = Category.builder()
        .name(reqBody.name())
        .description(reqBody.description())
        .build();

    return categoryRepository.save(category);
  }

  // 카테고리 목록 조회 로직
  @Transactional(readOnly = true)
  public List<Category> getCategories() {
    return categoryRepository.findAll();
  }
}
