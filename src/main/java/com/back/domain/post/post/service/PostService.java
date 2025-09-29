package com.back.domain.post.post.service;

import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.post.category.entity.Category;
import com.back.domain.post.category.repository.CategoryRepository;
import com.back.domain.post.post.dto.request.PostCreateRequestDto;
import com.back.domain.post.post.dto.request.PostUpdateRequestDto;
import com.back.domain.post.post.dto.response.PostResponseDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostLike;
import com.back.domain.post.post.entity.Tag;
import com.back.domain.post.post.enums.PostLikeStatus;
import com.back.domain.post.post.enums.PostStatus;
import com.back.domain.post.post.repository.PostLikeRepository;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.post.repository.TagRepository;
import com.back.domain.user.entity.User;
import com.back.global.rq.Rq;
import com.back.domain.user.service.AbvScoreService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final PostLikeRepository postLikeRepository;
  private final NotificationService notificationService;
  private final Rq rq;
  private final AbvScoreService abvScoreService;

  // 게시글 작성 로직
  @Transactional
  public PostResponseDto createPost(PostCreateRequestDto reqBody) {
    User user = rq.getActor(); // 현재 로그인한 사용자의 정보 가져오기

    Category category = categoryRepository.findById(reqBody.categoryId())
        .orElseThrow(() -> new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다. ID: " + reqBody.categoryId()));

    Post post = Post.builder()
        .category(category)
        .user(user)
        .title(reqBody.title())
        .content(reqBody.content())
        .imageUrl(reqBody.imageUrl())
        .videoUrl(reqBody.videoUrl())
        .build();

    List<String> tagNames = reqBody.tags();
    if (tagNames != null && !tagNames.isEmpty()) {
      addTag(tagNames, post);
    }

    Post saved = postRepository.save(post);
    // 활동 점수: 게시글 작성 +0.5
    abvScoreService.awardForPost(user.getId());
    return new PostResponseDto(saved);
  }

  // 게시글 다건 조회 로직
  @Transactional(readOnly = true)
  public List<PostResponseDto> getAllPosts(Long lastId) {
//    List<Post> posts = postRepository.findAll();
    List<Post> posts;

    if (lastId == null) {
      // 첫 페이지 요청
      posts = postRepository.findTop10ByOrderByIdDesc();
    } else {
      // 이후 페이지 요청
      posts = postRepository.findTop10ByIdLessThanOrderByIdDesc(lastId);
    }

    return posts.stream()
        .map(PostResponseDto::new)
        .collect(Collectors.toList());
  }

  // 게시글 단건 조회 로직
  @Transactional(readOnly = true)
  public PostResponseDto getPost(Long postId) {
    return new PostResponseDto(
        postRepository.findById(postId)
            .orElseThrow(() -> new NoSuchElementException("해당 게시글을 찾을 수 없습니다. ID: " + postId))
    );
  }

  // 게시글 수정 로직
  @Transactional
  public PostResponseDto updatePost(Long postId, PostUpdateRequestDto reqBody) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NoSuchElementException("해당 게시글을 찾을 수 없습니다. ID: " + postId));

    if (reqBody.categoryId() != null) {
      Category category = categoryRepository.findById(reqBody.categoryId())
          .orElseThrow(() -> new IllegalArgumentException(
              "해당 카테고리를 찾을 수 없습니다. ID: " + reqBody.categoryId()
              )
          );
      post.updateCategory(category);
    }
    if (reqBody.status() != null) {
      post.updateStatus(reqBody.status());
    }
    if (reqBody.title() != null && !reqBody.title().isBlank()){
      post.updateTitle(reqBody.title());
    }
    if (reqBody.content() != null && !reqBody.content().isBlank()) {
      post.updateContent(reqBody.content());
    }
    if (reqBody.imageUrl() != null && !reqBody.imageUrl().isBlank()) {
      post.updateImage(reqBody.imageUrl());
    }
    if (reqBody.videoUrl() != null && !reqBody.videoUrl().isBlank()) {
      post.updateVideo(reqBody.videoUrl());
    }
    if (reqBody.tags() != null) {
      // 기존 태그들 삭제
      post.clearTags();

      // 새로운 태그들 추가
      addTag(reqBody.tags(), post);
    }

    return new PostResponseDto(post);
  }

  // 게시글 삭제 로직
  @Transactional
  public void deletePost(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NoSuchElementException("해당 게시글을 찾을 수 없습니다. ID: " + postId));

    post.updateStatus(PostStatus.DELETED);
    // 활동 점수: 게시글 삭제 시 -0.5 (작성자 기준)
    abvScoreService.revokeForPost(post.getUser().getId());

    // soft delete를 사용하기 위해 레포지토리 삭제 작업은 진행하지 않음.
//    postRepository.delete(post);
  }

  // 게시글 추천(좋아요) 토글 로직
  @Transactional
  public void toggleLike(Long postId) {
    User user = rq.getActor(); // 현재 로그인한 사용자

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NoSuchElementException("해당 게시글을 찾을 수 없습니다. ID: " + postId));

    Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

    if (existingLike.isPresent()) {
      // 이미 추천했으면 취소
      existingLike.get().updateStatus(PostLikeStatus.NONE);
      postLikeRepository.delete(existingLike.get());
      post.decreaseLikeCount();
      // 활동 점수: 추천 취소 시 -0.1
      abvScoreService.revokeForLike(user.getId());
    } else {
      // 추천 추가
      PostLike postLike = PostLike.builder()
          .post(post)
          .user(user)
          .status(PostLikeStatus.LIKE)
          .build();
      postLikeRepository.save(postLike);
      post.increaseLikeCount();
      // 활동 점수: 추천 추가 시 +0.1
      abvScoreService.awardForLike(user.getId());
    }

    // 게시글 작성자에게 알림 전송
    notificationService.sendNotification(
        post.getUser(),
        post,
        NotificationType.LIKE,
        user.getNickname() + " 님이 추천을 남겼습니다."
    );
  }

  // 태그 추가 메서드
  private void addTag(List<String> tagNames, Post post) {
    for (String tagName : tagNames) {
      Tag tag = tagRepository.findByName(tagName)
          .orElseGet(() -> tagRepository.save(
              Tag.builder()
                  .name(tagName)
                  .build()
              )
          );
      post.addTag(tag);
    }
  }
}
