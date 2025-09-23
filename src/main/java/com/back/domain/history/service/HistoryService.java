package com.back.domain.history.service;

import com.back.domain.history.dto.HistoryPostItemDto;
import com.back.domain.history.dto.HistoryPostListDto;
import com.back.domain.history.repository.HistoryRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.enums.PostStatus;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public HistoryPostListDto getMyPosts(Long userId, LocalDateTime lastCreatedAt, Long lastId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int fetchSize = safeLimit + 1;

        List<Post> rows;
        if (lastCreatedAt == null || lastId == null) {
            rows = historyRepository.findMyPostsFirstPage(userId, PostStatus.DELETED, PageRequest.of(0, fetchSize));
        } else {
            rows = historyRepository.findMyPostsAfter(userId, PostStatus.DELETED, lastCreatedAt, lastId, PageRequest.of(0, fetchSize));
        }

        boolean hasNext = rows.size() > safeLimit;
        if (hasNext) rows = rows.subList(0, safeLimit);

        List<HistoryPostItemDto> items = new ArrayList<>();
        for (Post p : rows) items.add(HistoryPostItemDto.from(p));

        LocalDateTime nextCreatedAt = null;
        Long nextId = null;
        if (hasNext && !rows.isEmpty()) {
            Post last = rows.get(rows.size() - 1);
            nextCreatedAt = last.getCreatedAt();
            nextId = last.getId();
        }

        return new HistoryPostListDto(items, hasNext, nextCreatedAt, nextId);
    }
}
