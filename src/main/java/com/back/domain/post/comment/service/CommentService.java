package com.back.domain.post.comment.service;

import com.back.domain.post.comment.dto.request.CommentCreateRequestDto;
import com.back.domain.post.comment.dto.request.CommentUpdateRequestDto;
import com.back.domain.post.comment.dto.response.CommentResponseDto;
import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.comment.repository.CommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.user.entity.User;
import com.back.global.rq.Rq;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final Rq rq;

  // 댓글 작성 로직
  @Transactional
  public CommentResponseDto createComment(Long postId, CommentCreateRequestDto reqBody) {
    User user = rq.getActor();

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다. id=" + postId));

    Comment comment = Comment.builder()
        .post(post)
        .user(user)
        .content(reqBody.content())
        .build();

    return new CommentResponseDto(commentRepository.save(comment));
  }

  // 댓글 다건 조회 로직 (무한스크롤)
  @Transactional(readOnly = true)
  public List<CommentResponseDto> getComments(Long postId, Long lastId) {
    if (lastId == null) {
      return commentRepository.findTop10ByPostIdOrderByIdDesc(postId)
          .stream()
          .map(CommentResponseDto::new)
          .toList();
    } else {
      return commentRepository.findTop10ByPostIdAndIdLessThanOrderByIdDesc(postId, lastId)
          .stream()
          .map(CommentResponseDto::new)
          .toList();
    }
  }

  // 댓글 단건 조회 로직
  @Transactional(readOnly = true)
  public CommentResponseDto getComment(Long postId, Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

    if (!comment.getPost().getId().equals(postId)) {
      throw new IllegalStateException("댓글이 해당 게시글에 속하지 않습니다.");
    }

    return new CommentResponseDto(comment);
  }

  // 댓글 수정 로직
  @Transactional
  public CommentResponseDto updateComment(Long postId, Long commentId, CommentUpdateRequestDto requestDto) {
    User user = rq.getActor();

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. id=" + commentId));

    if (!comment.getPost().getId().equals(postId)) {
      throw new IllegalStateException("댓글이 해당 게시글에 속하지 않습니다.");
    }

    if (!comment.getUser().equals(user)) {
      throw new IllegalStateException("본인의 댓글만 수정할 수 있습니다.");
    }

    comment.updateContent(requestDto.content());
    return new CommentResponseDto(comment);
  }
}
