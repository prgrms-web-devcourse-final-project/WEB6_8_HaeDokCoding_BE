package com.back.domain.mybar.repository;

import com.back.domain.mybar.entity.MyBar;
import com.back.domain.mybar.enums.KeepStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MyBarRepository extends JpaRepository<MyBar, Long> {
    /** 나만의 bar(킵) 목록: ACTIVE만, id desc */
    Page<MyBar> findByUser_IdAndStatusOrderByKeptAtDescIdDesc(Long userId, KeepStatus status, Pageable pageable);

    /** 프로필/요약용: ACTIVE 개수 */
    long countByUser_IdAndStatus(Long userId, KeepStatus status);

    /** 현재 킵 상태 확인(아이콘 등): ACTIVE 존재 여부 */
    boolean existsByUser_IdAndCocktail_CocktailIdAndStatus(Long userId, Long cocktailId, KeepStatus status);

    /** 복원/재킵을 위해 status 무시하고 한 건 찾기 (없으면 Optional.empty) */
    Optional<MyBar> findByUser_IdAndCocktail_CocktailId(Long userId, Long cocktailId);
}
