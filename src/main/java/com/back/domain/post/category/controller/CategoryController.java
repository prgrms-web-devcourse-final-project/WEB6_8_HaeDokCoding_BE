package com.back.domain.post.category.controller;

import com.back.domain.post.category.dto.request.CategoryCreateRequestDto;
import com.back.domain.post.category.entity.Category;
import com.back.domain.post.category.service.CategoryService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/category")
@Tag(name = "ApiCategoryController", description = "API 카테고리 컨트롤러")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @PostMapping
  @Operation(summary = "카테고리 생성")
  public RsData<Category> createCategory(
      @Valid @RequestBody CategoryCreateRequestDto reqBody
  ) {
    return RsData.successOf(categoryService.createCategory(reqBody)); // code=200, message="success"
  }

  @GetMapping
  @Operation(summary = "카테고리 목록 조회")
  public RsData<List<Category>> getCategories() {
    return RsData.successOf(categoryService.getCategories()); // code=200, message="success"
  }
}
