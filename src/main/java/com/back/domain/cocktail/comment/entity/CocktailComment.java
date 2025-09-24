package com.back.domain.cocktail.comment.entity;

import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.post.comment.enums.CommentStatus;
import com.back.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "cocktailcomment")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CocktailComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 해당 칵테일댓글이 작성된 게시글의 고유 식별자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id")
    private Cocktail cocktail;

    // 해당 칵테일댓글을 작성한 유저의 고유 식별자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 칵테일댓글 작성 날짜
    @CreatedDate
    private LocalDateTime createdAt;

    // 칵테일댓글 수정 날짜
    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 칵테일댓글 게시 상태 (기본값: 공개)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentStatus status = CommentStatus.PUBLIC;

    // 칵테일댓글 내용
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    public void updateStatus(CommentStatus status) {
        this.status = status;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
