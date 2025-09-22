package com.back.domain.post.post.dto.request;

import java.util.List;

public record PostRequestDto(
    Long categoryId,
    String title,
    String content,
    String userNickName,
    String imageUrl,
    List<String> tags
) {
}
