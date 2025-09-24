package com.back.domain.cocktail.comment.controller;

import com.back.domain.cocktail.comment.dto.CocktailCommentCreateRequestDto;
import com.back.domain.cocktail.comment.dto.CocktailCommentResponseDto;
import com.back.domain.cocktail.comment.dto.CocktailCommentUpdateRequestDto;
import com.back.domain.cocktail.comment.service.CocktailCommentService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cocktails/{cocktailId}/comments")
@Tag(name = "ApiCocktailCommentController", description = "API 칵테일댓글 컨트롤러")
@RequiredArgsConstructor
public class CocktailCommentController {

    private final CocktailCommentService cocktailCommentService;

    /**
     * 칵테일댓글 작성 API
     *
     * @param cocktailId 칵테일댓글을 작성할 칵테일 ID
     * @param reqBody    칵테일댓글 작성 요청 DTO
     * @return 작성된 칵테일댓글 정보
     */
    @PostMapping
    @Operation(summary = "칵테일댓글 작성")
    public RsData<CocktailCommentResponseDto> createCocktailComment(
            @PathVariable Long cocktailId,
            @Valid @RequestBody CocktailCommentCreateRequestDto reqBody
    ) {
        return RsData.successOf(cocktailCommentService.createCocktailComment(cocktailId, reqBody)); // code=200, message="success"
    }

    /**
     * 칵테일댓글 다건조회 API
     *
     * @param cocktailId 칵테일댓글 작성된 게시글 ID
     * @param lastId     마지막으로 조회한 칵테일댓글 ID (페이징 처리용, optional)
     * @return 칵테일댓글 목록
     */
    @GetMapping
    @Operation(summary = "댓글 다건 조회")
    public RsData<List<CocktailCommentResponseDto>> getCocktailComments(
            @PathVariable Long cocktailId,
            @RequestParam(required = false) Long lastId
    ) {
        return RsData.successOf(cocktailCommentService.getCocktailComments(cocktailId, lastId)); // code=200, message="success"
    }

    /**
     * 칵테일댓글 단건 조회 API
     *
     * @param cocktailId        칵테일댓글이 작성된 칵테일 ID
     * @param cocktailCommentId 조회할 칵테일댓글 ID
     * @return 해당 ID의 칵테일댓글 정보
     */
    @GetMapping("/{cocktailCommentId}")
    @Operation(summary = "칵테일 댓글 단건 조회")
    public RsData<CocktailCommentResponseDto> getCocktailComment(
            @PathVariable Long cocktailId,
            @PathVariable Long cocktailCommentId
    ) {
        return RsData.successOf(cocktailCommentService.getCocktailComment(cocktailId, cocktailCommentId)); // code=200, message="success"
    }

    /**
     * 칵테일댓글 수정 API
     *
     * @param cocktailId        칵테일댓글 작성된 칵테일 ID
     * @param cocktailCommentId 수정할 칵테일댓글 ID
     * @param reqBody           칵테일댓글 수정 요청 DTO
     * @return 수정된 칵테일댓글 정보
     */
    @PatchMapping("/{cocktailCommentId}")
    @Operation(summary = "칵테일댓글 수정")
    public RsData<CocktailCommentResponseDto> updateComment(
            @PathVariable Long cocktailId,
            @PathVariable Long cocktailCommentId,
            @Valid @RequestBody CocktailCommentUpdateRequestDto reqBody
    ) {
        return RsData.successOf(cocktailCommentService.updateCocktailComment(cocktailId, cocktailCommentId, reqBody)); // code=200, message="success"
    }

    /**
     * 칵테일댓글 삭제 API
     *
     * @param cocktailId        칵테일댓글 작성된 칵테일 ID
     * @param cocktailCommentId 삭제할 칵테일댓글 ID
     * @return 삭제 성공 메시지
     */
    @DeleteMapping("/{cocktailCommentId}")
    @Operation(summary = "댓글 삭제")
    public RsData<Void> deleteComment(
            @PathVariable Long cocktailId,
            @PathVariable Long cocktailCommentId
    ) {
        cocktailCommentService.deleteCocktailComment(cocktailId, cocktailCommentId);
        return RsData.successOf(null); // code=200, message="success"
    }
}

