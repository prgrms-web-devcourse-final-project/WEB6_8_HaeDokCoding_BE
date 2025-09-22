package com.back.domain.post.post.service;

import com.back.domain.post.category.entity.Category;
import com.back.domain.post.category.repository.CategoryRepository;
import com.back.domain.post.post.dto.request.PostCreateRequestDto;
import com.back.domain.post.post.dto.request.PostUpdateRequestDto;
import com.back.domain.post.post.dto.response.PostResponseDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.Tag;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.post.repository.TagRepository;
import com.back.domain.user.entity.User;
import com.back.global.rq.Rq;
import java.util.List;
import java.util.NoSuchElementException;
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
  private final Rq rq;

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
        .build();

    List<String> tagNames = reqBody.tags();
    if (tagNames != null && !tagNames.isEmpty()) {
      addTag(tagNames, post);
    }

    return new PostResponseDto(postRepository.save(post));
  }

  // 게시글 다건 조회 로직
  @Transactional(readOnly = true)
  public List<PostResponseDto> getAllPosts() {
    List<Post> posts = postRepository.findAll();

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
    if (reqBody.tags() != null) {
      // 기존 태그들 삭제
      post.clearTags();

      // 새로운 태그들 추가
      addTag(reqBody.tags(), post);
    }

    return new PostResponseDto(post);
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
