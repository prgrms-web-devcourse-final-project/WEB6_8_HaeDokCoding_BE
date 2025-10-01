package com.back.domain.post.category.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequestDto(
    @NotBlank(message = "이름은 필수입니다.")
    String name,
    @NotBlank (message = "설명은 필수입니다.")
    String description
) {
}