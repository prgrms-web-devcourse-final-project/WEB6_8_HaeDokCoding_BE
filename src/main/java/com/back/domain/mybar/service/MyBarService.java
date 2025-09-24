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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyBarService {

    private final MyBarRepository myBarRepository;
    private final UserRepository userRepository;
    private final CocktailRepository cocktailRepository;

    // 커서: lastKeptAt + lastId를 그대로 파라미터로 사용
    @Transactional(readOnly = true)
    public MyBarListResponseDto getMyBar(Long userId, LocalDateTime lastKeptAt, Long lastId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        int fetchSize = safeLimit + 1; // 다음 페이지 여부 판단용으로 1개 더 조회

        List<MyBar> rows;
        Pageable pageable = PageRequest.of(0, fetchSize);

        if (lastKeptAt == null || lastId == null) {
            Page<MyBar> page0 = myBarRepository
                    .findByUser_IdAndStatusOrderByKeptAtDescIdDesc(userId, KeepStatus.ACTIVE, pageable);
            rows = page0.getContent();
        } else {
            rows = myBarRepository.findSliceByCursor(userId, KeepStatus.ACTIVE, lastKeptAt, lastId, pageable);
        }

        boolean hasNext = rows.size() > safeLimit;
        if (hasNext) rows = rows.subList(0, safeLimit);

        List<MyBarItemResponseDto> items = new ArrayList<>();
        for (MyBar myBar : rows) items.add(MyBarItemResponseDto.from(myBar));

        LocalDateTime nextKeptAt = null;
        Long nextId = null;
        if (hasNext && !rows.isEmpty()) {
            MyBar last = rows.get(rows.size() - 1);
            nextKeptAt = last.getKeptAt();
            nextId = last.getId();
        }

        return new MyBarListResponseDto(items, hasNext, nextKeptAt, nextId);
    }

    @Transactional
    public void keep(Long userId, Long cocktailId) {
        Optional<MyBar> existingMyBar =
                myBarRepository.findByUser_IdAndCocktail_Id(userId, cocktailId);

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

