package com.back.domain.post.post.dto.request;

import com.back.domain.post.post.enums.PostStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PostCreateRequestDto(
    Long categoryId,
    PostStatus status,
    @NotBlank (message = "제목은 필수입니다.")
    String title,
    @NotBlank (message = "내용은 필수입니다.")
    String content,
    String imageUrl,
    String videoUrl,
    List<String> tags
) {
}
