package com.back.domain.post.post.dto.request;

import com.back.domain.post.post.enums.PostStatus;
import java.util.List;

public record PostUpdateRequestDto(
    Long categoryId,
    PostStatus status,
    String title,
    String content,
    String imageUrl,
    List<String> tags
) {
}
