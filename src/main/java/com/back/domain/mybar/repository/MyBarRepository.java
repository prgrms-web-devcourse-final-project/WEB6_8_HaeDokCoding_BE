package com.back.domain.mybar.repository;

import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.mybar.entity.MyBar;
import com.back.domain.mybar.enums.KeepStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MyBarRepository extends JpaRepository<MyBar, Long> {
    /** 나만의 bar(킵) 목록: ACTIVE만, keptAt desc + id desc */
    Page<MyBar> findByUser_IdAndStatusOrderByKeptAtDescIdDesc(Long userId, KeepStatus status, Pageable pageable);

    List<MyBar> findByUser_IdAndStatusOrderByKeptAtDescIdDesc(Long userId, KeepStatus status);

    @Query("""
        select m from MyBar m
         where m.user.id = :userId
           and m.status = :status
           and (m.keptAt < :keptAt or (m.keptAt = :keptAt and m.id < :id))
         order by m.keptAt desc, m.id desc
    """)
    List<MyBar> findSliceByCursor(Long userId, KeepStatus status, LocalDateTime keptAt, Long id, Pageable pageable);

    /** 프로필/요약용: ACTIVE 개수 */
    long countByUser_IdAndStatus(Long userId, KeepStatus status);

    /** 현재 킵 상태 확인(아이콘 등): ACTIVE 존재 여부 */
    boolean existsByUser_IdAndCocktail_IdAndStatus(Long userId, Long cocktailId, KeepStatus status);

    /** 복원/재킵을 위해 status 무시하고 한 건 찾기 (없으면 Optional.empty) */
    Optional<MyBar> findByUser_IdAndCocktail_Id(Long userId, Long cocktailId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update MyBar m
           set m.status = 'DELETED', m.deletedAt = CURRENT_TIMESTAMP
         where m.user.id = :userId
           and m.status = 'ACTIVE'
    """)
    int softDeleteAllByUser(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update MyBar m
           set m.status = 'DELETED', m.deletedAt = CURRENT_TIMESTAMP
         where m.user.id = :userId
           and m.cocktail.id = :cocktailId
           and m.status = 'ACTIVE'
    """)
    int softDeleteByUserAndCocktail(Long userId, Long cocktailId);

    // 특정 칵테일의 ACTIVE Keep 개수
    Long countByCocktailAndStatus(Cocktail cocktail, KeepStatus status);
}
