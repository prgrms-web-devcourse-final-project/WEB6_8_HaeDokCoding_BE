package com.back.domain.mybar.service;

import com.back.domain.mybar.dto.MyBarItemResponseDto;
import com.back.domain.mybar.dto.MyBarListResponseDto;
import com.back.domain.mybar.entity.MyBar;
import com.back.domain.mybar.enums.KeepStatus;
import com.back.domain.mybar.repository.MyBarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyBarService {

    private final MyBarRepository myBarRepository;

    @Transactional(readOnly = true)
    public MyBarListResponseDto getMyBar(Long userId, int page, int pageSize) {
        Page<MyBar> myBarPage = myBarRepository.findByUserIdAndStatusOrderByIdDesc(userId, KeepStatus.ACTIVE, PageRequest.of(page, pageSize));

        List<MyBar> myBars = myBarPage.getContent();
        List<MyBarItemResponseDto> items = new ArrayList<>();
        for (MyBar myBar : myBars) items.add(MyBarItemResponseDto.from(myBar));

        boolean hasNext = myBarPage.hasNext();
        Integer nextPage = hasNext ? myBarPage.getNumber() + 1 : null;

        return new MyBarListResponseDto(items, hasNext, nextPage);
    }
}
