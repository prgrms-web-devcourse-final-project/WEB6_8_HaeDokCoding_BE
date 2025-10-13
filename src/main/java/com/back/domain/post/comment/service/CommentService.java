package com.back.domain.post.comment.service;

import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.post.comment.dto.request.CommentCreateRequestDto;
import com.back.domain.post.comment.dto.request.CommentUpdateRequestDto;
import com.back.domain.post.comment.dto.response.CommentResponseDto;
import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.comment.enums.CommentStatus;
import com.back.domain.post.comment.repository.CommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.service.AbvScoreService;
import com.back.global.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final NotificationService notificationService;
  private final Rq rq;
  private final AbvScoreService abvScoreService;

  // 댓글 작성 로직
  @Transactional
  public CommentResponseDto createComment(Long postId, CommentCreateRequestDto reqBody) {
    User user = rq.getActor();
    if (user == null) {
      throw new IllegalStateException("로그인한 사용자만 댓글을 작성할 수 있습니다.");
    }

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + postId));

    Comment comment = Comment.builder()
        .post(post)
        .user(user)
        .content(reqBody.content())
        .build();

    // 게시글 작성자에게 알림 전송
    String commentMessage = String.format("%s 님이 '%s' 게시글에 댓글을 남겼습니다.", user.getNickname(), post.getTitle());
    notificationService.sendNotification(
        post.getUser(),
        post,
        NotificationType.COMMENT,
        commentMessage
    );

    Comment saved = commentRepository.save(comment);

    // 게시글 댓글 수 증가
    post.increaseCommentCount();

    // 활동 점수: 댓글 작성 +0.2
    abvScoreService.awardForComment(user.getId());
    return new CommentResponseDto(saved);
  }

  // 댓글 다건 조회 로직 (무한스크롤)
  @Transactional(readOnly = true)
  public List<CommentResponseDto> getComments(Long postId, Long lastId) {
    if (lastId == null) {
      return commentRepository.findTop10ByPostIdAndStatusNotOrderByIdDesc(postId, CommentStatus.DELETED)
          .stream()
          .map(CommentResponseDto::new)
          .toList();
    } else {
      return commentRepository.findTop10ByPostIdAndIdLessThanAndStatusNotOrderByIdDesc(postId, lastId, CommentStatus.DELETED)
          .stream()
          .map(CommentResponseDto::new)
          .toList();
    }
  }

  // 댓글 단건 조회 로직
  @Transactional(readOnly = true)
  public CommentResponseDto getComment(Long postId, Long commentId) {
    Comment comment = findCommentWithValidation(postId, commentId);

    return new CommentResponseDto(comment);
  }

  // 댓글 수정 로직
  @Transactional
  public CommentResponseDto updateComment(Long postId, Long commentId, CommentUpdateRequestDto requestDto) {
    User user = rq.getActor();

    Comment comment = findCommentWithValidation(postId, commentId);

    if (!comment.getUser().getId().equals(user.getId())) {
      throw new IllegalStateException("본인의 댓글만 수정할 수 있습니다.");
    }

    comment.updateContent(requestDto.content());
    return new CommentResponseDto(comment);
  }

  // 댓글 삭제 로직
  @Transactional
  public void deleteComment(Long postId, Long commentId) {
    User user = rq.getActor();

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + postId));

    Comment comment = findCommentWithValidation(postId, commentId);

    if (!comment.getUser().getId().equals(user.getId())) {
      throw new IllegalStateException("본인의 댓글만 삭제할 수 있습니다.");
    }

    comment.updateStatus(CommentStatus.DELETED);

    // 게시글 댓글 수 감소
    post.decreaseCommentCount();
    // 활동 점수: 댓글 삭제 시 -0.2 (작성자 기준)
    abvScoreService.revokeForComment(user.getId());

    // soft delete를 사용하기 위해 레포지토리 삭제 작업은 진행하지 않음.
//    commentRepository.delete(comment);
  }

  // 댓글과 게시글의 연관관계 검증
  private Comment findCommentWithValidation(Long postId, Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

    if (!comment.getPost().getId().equals(postId)) {
      throw new IllegalStateException("댓글이 해당 게시글에 속하지 않습니다.");
    }

    return comment;
  }
}
