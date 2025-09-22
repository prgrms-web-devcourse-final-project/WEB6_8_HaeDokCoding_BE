package com.back.domain.post.post.service;

import com.back.domain.post.category.entity.Category;
import com.back.domain.post.category.repository.CategoryRepository;
import com.back.domain.post.post.dto.request.PostRequestDto;
import com.back.domain.post.post.dto.response.PostResponseDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.Tag;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.post.repository.TagRepository;
import com.back.domain.user.entity.User;
import com.back.global.rq.Rq;
import java.util.List;
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
  public PostResponseDto createPost(PostRequestDto postRequestDto) {
    User user = rq.getActor(); // 현재 로그인한 사용자의 정보 가져오기

    Category category = categoryRepository.findById(postRequestDto.categoryId())
        .orElseThrow(() -> new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다. ID: " + postRequestDto.categoryId()));

    Post post = Post.builder()
        .category(category)
        .user(user)
        .title(postRequestDto.title())
        .content(postRequestDto.content())
        .imageUrl(postRequestDto.imageUrl())
        .build();

    List<String> tagNames = postRequestDto.tags();
    if (tagNames != null && !tagNames.isEmpty()) {
      for (String tagName : tagNames) {
        // 태그 이름으로 Tag 엔티티를 조회하거나, 없으면 새로 생성하여 저장
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

    return new PostResponseDto(postRepository.save(post));
  }

  // 게시글 다건 조회 로직
  @Transactional(readOnly = true)
  public List<Post> getAllPosts() {
    return postRepository.findAll();
  }
}
