package com.back.domain.post.comment.service;

import com.back.domain.post.comment.dto.request.CommentCreateRequestDto;
import com.back.domain.post.comment.dto.response.CommentResponseDto;
import com.back.domain.post.comment.entity.Comment;
import com.back.domain.post.comment.repository.CommentRepository;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.user.entity.User;
import com.back.global.rq.Rq;
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
}
