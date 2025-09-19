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

    @Transactional(readOnly = true)
    public MyBarListResponseDto getMyBar(Long userId, int page, int pageSize) {
        Page<MyBar> myBarPage = myBarRepository.findByUser_IdAndStatusOrderByKeptAtDescIdDesc(userId, KeepStatus.ACTIVE, PageRequest.of(page, pageSize));

        List<MyBar> myBars = myBarPage.getContent();
        List<MyBarItemResponseDto> items = new ArrayList<>();
        for (MyBar myBar : myBars) items.add(MyBarItemResponseDto.from(myBar));

        boolean hasNext = myBarPage.hasNext();
        Integer nextPage = hasNext ? myBarPage.getNumber() + 1 : null;

        return new MyBarListResponseDto(items, hasNext, nextPage);
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
}
