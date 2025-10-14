package com.back.domain.post.post.dto.request;

import com.back.domain.post.post.enums.PostStatus;
import java.util.List;

public record PostUpdateRequestDto(
    Long categoryId,
    PostStatus status,
    String title,
    String content,
    // 기존 이미지 중 유지할 이미지 URL 목록
    List<String> keepImageUrls,
    String videoUrl,
    List<String> tags
) {
}
