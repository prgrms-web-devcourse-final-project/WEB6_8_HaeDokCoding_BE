package com.back.domain.cocktail.comment.service;

import com.back.domain.cocktail.comment.dto.CocktailCommentCreateRequestDto;
import com.back.domain.cocktail.comment.dto.CocktailCommentResponseDto;
import com.back.domain.cocktail.comment.dto.CocktailCommentUpdateRequestDto;
import com.back.domain.cocktail.comment.entity.CocktailComment;
import com.back.domain.cocktail.comment.repository.CocktailCommentRepository;
import com.back.domain.cocktail.entity.Cocktail;
import com.back.domain.cocktail.repository.CocktailRepository;
import com.back.domain.post.comment.enums.CommentStatus;
import com.back.domain.user.entity.User;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CocktailCommentService {
    private final CocktailCommentRepository cocktailCommentRepository;
    private final CocktailRepository cocktailRepository;
    private final Rq rq;

    // 칵테일 댓글 작성 로직
    @Transactional
    public CocktailCommentResponseDto createCocktailComment(Long cocktailId, CocktailCommentCreateRequestDto reqBody) {
        User user = rq.getActor();

        Cocktail cocktail = cocktailRepository.findById(cocktailId)
                .orElseThrow(() -> new IllegalArgumentException("칵테일이 존재하지 않습니다. id=" + cocktailId));

        CocktailComment cocktailComment = CocktailComment.builder()
                .cocktail(cocktail)
                .user(user)
                .content(reqBody.content())
                .build();

        return new CocktailCommentResponseDto(cocktailCommentRepository.save(cocktailComment));
    }

    // 칵테일 댓글 다건 조회 로직 (무한스크롤)
    @Transactional(readOnly = true)
    public List<CocktailCommentResponseDto> getCocktailComments(Long cocktailId, Long lastId) {
        if (lastId == null) {
            return cocktailCommentRepository.findTop10ByCocktailIdOrderByIdDesc(cocktailId)
                    .stream()
                    .map(CocktailCommentResponseDto::new)
                    .toList();
        } else {
            return cocktailCommentRepository.findTop10ByCocktailIdAndIdLessThanOrderByIdDesc(cocktailId, lastId)
                    .stream()
                    .map(CocktailCommentResponseDto::new)
                    .toList();
        }
    }

    // 칵테일 댓글 단건 조회 로직
    @Transactional(readOnly = true)
    public CocktailCommentResponseDto getCocktailComment(Long cocktailId, Long cocktailCommentId) {
        CocktailComment cocktailComment = findByIdAndValidateCocktail(cocktailId, cocktailCommentId);

        return new CocktailCommentResponseDto(cocktailComment);
    }

    // 칵테일댓글 ID로 찾고, 칵테일과의 관계를 검증
    private CocktailComment findByIdAndValidateCocktail(Long cocktailId, Long cocktailCommentId) {
        CocktailComment cocktailComment = cocktailCommentRepository.findById(cocktailCommentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + cocktailCommentId));

        if (!cocktailComment.getCocktail().getId().equals(cocktailId)) {
            throw new IllegalStateException("댓글이 해당 게시글에 속하지 않습니다.");
        }
        return cocktailComment;
    }

    // 칵테일댓글 수정 로직
    @Transactional
    public CocktailCommentResponseDto updateCocktailComment(Long cocktailId, Long cocktailCommentId, CocktailCommentUpdateRequestDto requestDto) {
        User user = rq.getActor();

        CocktailComment cocktailComment = findByIdAndValidateCocktail(cocktailId, cocktailCommentId);

        if (!cocktailComment.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 댓글만 수정할 수 있습니다.");
        }

        cocktailComment.updateContent(requestDto.content());
        return new CocktailCommentResponseDto(cocktailComment);
    }

    // 칵테일댓글 삭제 로직
    @Transactional
    public void deleteCocktailComment(Long cocktailId, Long cocktailCommentId) {
        User user = rq.getActor();

        CocktailComment cocktailComment = findByIdAndValidateCocktail(cocktailId, cocktailCommentId);

        if (!cocktailComment.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 댓글만 삭제할 수 있습니다.");
        }

        cocktailComment.updateStatus(CommentStatus.DELETED); // soft delete 사용.
    }
}






