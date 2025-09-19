package com.back.domain.mybar.repository;

import com.back.domain.mybar.entity.MyBar;
import com.back.domain.mybar.enums.KeepStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyBarRepository extends JpaRepository<MyBar, Long> {
    /** 나만의 bar(킵) 목록: ACTIVE만, id desc */
    Page<MyBar> findByUserIdAndStatusOrderByIdDesc(Long userId, KeepStatus status, Pageable pageable);

    /** 프로필/요약용: ACTIVE 개수 */
    long countByUserIdAndStatus(Long userId, KeepStatus status);
}
