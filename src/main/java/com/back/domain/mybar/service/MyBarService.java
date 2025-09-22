package com.back.domain.mybar.service;

import com.back.domain.cocktail.repository.CocktailRepository;
import com.back.domain.mybar.dto.MyBarItemResponseDto;
import com.back.domain.mybar.dto.MyBarListResponseDto;
import com.back.domain.mybar.entity.MyBar;
import com.back.domain.mybar.enums.KeepStatus;
import com.back.domain.mybar.repository.MyBarRepository;
import com.back.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyBarService {

    private final MyBarRepository myBarRepository;
    private final UserRepository userRepository;
    private final CocktailRepository cocktailRepository;

    @Transactional(readOnly = true)
    public MyBarListResponseDto getMyBar(Long userId, String cursor, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int fetchSize = safeLimit + 1; // hasNext 판별용으로 1개 더 조회

        List<MyBar> rows;
        Pageable pageable = PageRequest.of(0, fetchSize);

        if (cursor == null || cursor.isBlank()) {
            Page<MyBar> page0 = myBarRepository
                    .findByUser_IdAndStatusOrderByKeptAtDescIdDesc(userId, KeepStatus.ACTIVE, pageable);
            rows = page0.getContent();
        } else {
            // cursor 포맷: epochMillis|id (Base64 URL-safe, padding 없음)
            Cursor decoded = Cursor.decode(cursor);
            LocalDateTime keptAtCursor = LocalDateTime.ofEpochSecond(decoded.epochMillis / 1000, (int)((decoded.epochMillis % 1000) * 1_000_000), ZoneOffset.UTC);
            rows = myBarRepository.findSliceByCursor(userId, KeepStatus.ACTIVE, keptAtCursor, decoded.id, pageable);
        }

        boolean hasNext = rows.size() > safeLimit;
        if (hasNext) {
            rows = rows.subList(0, safeLimit);
        }

        List<MyBarItemResponseDto> items = new ArrayList<>();
        for (MyBar myBar : rows) items.add(MyBarItemResponseDto.from(myBar));

        String nextCursor = null;
        if (hasNext && !rows.isEmpty()) {
            MyBar last = rows.get(rows.size() - 1);
            long epochMillis = last.getKeptAt().toEpochSecond(ZoneOffset.UTC) * 1000L + (last.getKeptAt().getNano() / 1_000_000);
            nextCursor = Cursor.encode(epochMillis, last.getId());
        }

        return new MyBarListResponseDto(items, hasNext, nextCursor);
    }

    @Transactional
    public void keep(Long userId, Long cocktailId) {
        Optional<MyBar> existingMyBar =
                myBarRepository.findByUser_IdAndCocktail_CocktailId(userId, cocktailId);

        LocalDateTime now = LocalDateTime.now();

        if (existingMyBar.isPresent()) {
            // 이미 행이 있으면: 최근에 다시 킵했다고 보고 keptAt만 갱신
            MyBar myBar = existingMyBar.get();
            myBar.setKeptAt(now);
            if (myBar.getStatus() == KeepStatus.DELETED) {
                // 해제돼 있던 건 복원
                myBar.setStatus(KeepStatus.ACTIVE);
                myBar.setDeletedAt(null);
            }
            return; // 이미 ACTIVE여도 keptAt 갱신으로 충분
        }

        // 없으면 새로 생성
        MyBar myBar = new MyBar();
        myBar.setUser(userRepository.getReferenceById(userId));
        myBar.setCocktail(cocktailRepository.getReferenceById(cocktailId));
        myBar.setStatus(KeepStatus.ACTIVE);
        myBar.setKeptAt(now);

        myBarRepository.save(myBar);
    }

    /** 킵 해제(소프트 삭제) */
    @Transactional
    public void unkeep(Long userId, Long cocktailId) {
        myBarRepository.softDeleteByUserAndCocktail(userId, cocktailId);
    }
}

class Cursor {
    final long epochMillis;
    final long id;

    Cursor(long epochMillis, long id) {
        this.epochMillis = epochMillis;
        this.id = id;
    }

    static String encode(long epochMillis, long id) {
        String raw = epochMillis + "|" + id;
        return java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    static Cursor decode(String cursor) {
        try {
            byte[] decoded = java.util.Base64.getUrlDecoder().decode(cursor);
            String s = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
            String[] parts = s.split("\\|");
            if (parts.length != 2) throw new com.back.global.exception.ServiceException(400, "invalid-cursor-형식이-올바르지-않습니다");
            long millis = Long.parseLong(parts[0]);
            long id = Long.parseLong(parts[1]);
            return new Cursor(millis, id);
        } catch (Exception e) {
            throw new com.back.global.exception.ServiceException(400, "invalid-cursor-디코드에-실패했습니다");
        }
    }
}
