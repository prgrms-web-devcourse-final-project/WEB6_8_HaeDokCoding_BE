package com.back.domain.post.post.service;

import com.back.domain.notification.enums.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.post.category.entity.Category;
import com.back.domain.post.category.repository.CategoryRepository;
import com.back.domain.post.post.dto.request.PostCreateRequestDto;
import com.back.domain.post.post.dto.request.PostSortScrollRequestDto;
import com.back.domain.post.post.dto.request.PostUpdateRequestDto;
import com.back.domain.post.post.dto.response.PostLikeResponseDto;
import com.back.domain.post.post.dto.response.PostResponseDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.entity.PostImage;
import com.back.domain.post.post.entity.PostLike;
import com.back.domain.post.post.entity.Tag;
import com.back.domain.post.post.enums.PostLikeStatus;
import com.back.domain.post.post.enums.PostStatus;
import com.back.domain.post.post.repository.PostImageRepository;
import com.back.domain.post.post.repository.PostLikeRepository;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.post.repository.TagRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.service.AbvScoreService;
import com.back.global.file.dto.UploadedFileDto;
import com.back.global.file.service.FileService;
import com.back.global.rq.Rq;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final PostLikeRepository postLikeRepository;
  private final PostImageRepository postImageRepository;
  private final NotificationService notificationService;
  private final Rq rq;
  private final AbvScoreService abvScoreService;
  private final FileService fileService;

  // 게시글 작성 로직
  @Transactional
  public PostResponseDto createPost(PostCreateRequestDto reqBody, List<MultipartFile> images) {
    User user = rq.getActor(); // 현재 로그인한 사용자의 정보 가져오기

    Category category = categoryRepository.findById(reqBody.categoryId())
        .orElseThrow(() -> new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다. ID: " + reqBody.categoryId()));

    // 게시글 엔티티 생성 (태그와 이미지 제외)
    Post post = Post.builder()
        .category(category)
        .user(user)
        .title(reqBody.title())
        .content(reqBody.content())
        .videoUrl(reqBody.videoUrl())
        .build();

    // 태그 저장
    List<String> tagNames = reqBody.tags();
    if (tagNames != null && !tagNames.isEmpty()) {
      addTag(tagNames, post);
    }

    // 이미지 저장 (S3 업로드 + DB 저장)
    if (images != null && !images.isEmpty()) {
      int order = 0;
      for (MultipartFile image : images) {
        String url = fileService.uploadFile(image);

        PostImage postImage = PostImage.builder()
            .fileName(image.getOriginalFilename())
            .url(url)
            .sortOrder(order++)
            .build();

        post.addImage(postImage);
      }
    }

    // 활동 점수: 게시글 작성 +0.5
    abvScoreService.awardForPost(user.getId());
    return new PostResponseDto(postRepository.save(post));
  }

  // 게시글 다건 조회 로직
  @Transactional(readOnly = true)
  public List<PostResponseDto> getPosts(PostSortScrollRequestDto reqBody) {
    List<Post> posts;

    // 카테고리 ID 유무에 따른 분기 처리
    if (reqBody.categoryId() != null) {
      // 카테고리별 조회 로직
      posts = findPostsByCategory(reqBody);
    } else {
      // 카테고리 없음 (전체) 조회
      posts = findAllPosts(reqBody);
    }

    return posts.stream()
        .map(PostResponseDto::new)
        .collect(Collectors.toList());
  }

  // 게시글 단건 조회 로직
  @Transactional
  public PostResponseDto getPost(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NoSuchElementException("해당 게시글을 찾을 수 없습니다. ID: " + postId));

    post.increaseViewCount();

    return new PostResponseDto(post);
  }

  // 게시글 수정 로직
  @Transactional
  public PostResponseDto updatePost(Long postId, PostUpdateRequestDto reqBody, List<MultipartFile> images) {
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

    List<String> addedImgUrls = List.of();
    List<String> uploadedFileNames = List.of();

    if (images != null && !images.isEmpty()) {
      // 새 이미지 업로드
      List<UploadedFileDto> uploaded = fileService.uploadFiles(images);
      addedImgUrls = uploaded.stream().map(UploadedFileDto::url).toList();
      uploadedFileNames = uploaded.stream().map(UploadedFileDto::fileName).toList();
    }

      // 요청 DTO에서 "유지할 이미지 URL 목록" 꺼내기
      List<String> keepImageUrls = new ArrayList<>(
          Optional.ofNullable(reqBody.keepImageUrls()).orElse(List.of()));
    keepImageUrls.addAll(addedImgUrls);

    // 🔹 현재 게시글의 모든 이미지 가져오기
    List<PostImage> existingImages = new ArrayList<>(post.getImages());

      // 삭제될 이미지 (DB + S3)
      List<PostImage> toRemove = post.getImages().stream()
          .filter(img -> !keepImageUrls.contains(img.getUrl()))
          .toList();

      List<String> deleteKeysAfterCommit = toRemove.stream()
          .map(PostImage::getFileName)
          .toList();

      toRemove.forEach(img -> img.updatePost(null)); // 관계 해제
      post.getImages().removeAll(toRemove); // orphanRemoval 트리거

      // 유지할 이미지 정렬
      int order = 0;
      for (String url : keepImageUrls) {
        // 기존 이미지인지 확인
        PostImage existing = existingImages.stream()
            .filter(img -> img.getUrl().equals(url))
            .findFirst()
            .orElse(null);

        if (existing != null) {
          existing.updateSortOrder(order++);
        } else {
          // 새로 추가된 이미지
          post.getImages().add(PostImage.builder()
              .post(post)
              .fileName(extractFileNameFromUrl(url)) // URL에서 파일명 추출 함수 아래 참고
              .url(url)
              .sortOrder(order++)
              .build());
        }
      }

    List<String> uploadedNames = new ArrayList<>(uploadedFileNames);

      // 트랜잭션 완료 후 처리
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCompletion(int status) {
          if (status == STATUS_ROLLED_BACK) {
            uploadedNames.forEach(fileService::deleteFile);
          } else if (status == STATUS_COMMITTED) {
            deleteKeysAfterCommit.forEach(fileService::deleteFile);
          }
        }
      });

    if (reqBody.videoUrl() != null && !reqBody.videoUrl().isBlank()) {
      post.updateVideo(reqBody.videoUrl());
    }
    if (reqBody.tags() != null) {
      post.clearTags(); // 기존 태그들 삭제
      addTag(reqBody.tags(), post); // 새로운 태그들 추가
    }

    return new PostResponseDto(post);
  }

  private String extractFileNameFromUrl(String url) {
    if (url == null) return null;
    int lastSlash = url.lastIndexOf('/');
    return (lastSlash != -1) ? url.substring(lastSlash + 1) : url;
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
  public PostLikeResponseDto toggleLike(Long postId) {
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

      return new PostLikeResponseDto(existingLike.get().getStatus());
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

      // 게시글 작성자에게 알림 전송
      String likeMessage = String.format("%s 님이 '%s' 게시글에 추천을 남겼습니다.", user.getNickname(), post.getTitle());
      notificationService.sendNotification(
          post.getUser(),
          post,
          NotificationType.LIKE,
          likeMessage
      );

      return new PostLikeResponseDto(postLike.getStatus());
    }
  }

  // 사용자가 해당 게시글 여부 확인 로직
  @Transactional(readOnly = true)
  public Boolean getLike(Long postId) {
    User user = rq.getActor();

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NoSuchElementException("해당 게시글을 찾을 수 없습니다. ID: " + postId));

    return postLikeRepository.existsByPostAndUser(post, user);
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

  // 카테고리 없음 (전체) 조회 메서드
  private List<Post> findAllPosts(PostSortScrollRequestDto reqBody) {
    return switch (reqBody.postSortStatus()) {
      case POPULAR -> {
        if (reqBody.lastId() == null || reqBody.lastLikeCount() == null) {
          yield postRepository.findTop10ByStatusNotOrderByLikeCountDescIdDesc(PostStatus.DELETED);
        } else {
          yield postRepository.findTop10ByStatusNotAndLikeCountLessThanOrLikeCountEqualsAndIdLessThanOrderByLikeCountDescIdDesc(PostStatus.DELETED, reqBody.lastLikeCount(), reqBody.lastLikeCount(), reqBody.lastId());
        }
      }
      case COMMENTS -> {
        if (reqBody.lastId() == null || reqBody.lastCommentCount() == null) {
          yield postRepository.findTop10ByStatusNotOrderByCommentCountDescIdDesc(PostStatus.DELETED);
        } else {
          yield postRepository.findTop10ByStatusNotAndCommentCountLessThanOrCommentCountEqualsAndIdLessThanOrderByCommentCountDescIdDesc(PostStatus.DELETED, reqBody.lastCommentCount(), reqBody.lastCommentCount(), reqBody.lastId());
        }
      }
      case LATEST -> {
        if (reqBody.lastId() == null) {
          yield postRepository.findTop10ByStatusNotOrderByIdDesc(PostStatus.DELETED);
        } else {
          yield postRepository.findTop10ByStatusNotAndIdLessThanOrderByIdDesc(PostStatus.DELETED, reqBody.lastId());
        }
      }
      default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준: " + reqBody.postSortStatus());
    };
  }

  // 카테고리별 조회 메서드
  private List<Post> findPostsByCategory(PostSortScrollRequestDto reqBody) {
    return switch (reqBody.postSortStatus()) {
      case POPULAR -> {
        if (reqBody.lastId() == null || reqBody.lastLikeCount() == null) {
          yield postRepository.findTop10ByCategoryIdAndStatusNotOrderByLikeCountDescIdDesc(
              reqBody.categoryId(), PostStatus.DELETED);
        } else {
          yield postRepository.findTop10ByCategoryIdAndStatusNotAndLikeCountLessThanOrLikeCountEqualsAndIdLessThanOrderByLikeCountDescIdDesc(
              reqBody.categoryId(), PostStatus.DELETED, reqBody.lastLikeCount(), reqBody.lastLikeCount(),
              reqBody.lastId());
        }
      }
      case COMMENTS -> {
        if (reqBody.lastId() == null || reqBody.lastCommentCount() == null) {
          yield postRepository.findTop10ByCategoryIdAndStatusNotOrderByCommentCountDescIdDesc(
              reqBody.categoryId(), PostStatus.DELETED);
        } else {
          yield postRepository.findTop10ByCategoryIdAndStatusNotAndCommentCountLessThanOrCommentCountEqualsAndIdLessThanOrderByCommentCountDescIdDesc(
              reqBody.categoryId(), PostStatus.DELETED, reqBody.lastCommentCount(), reqBody.lastCommentCount(),
              reqBody.lastId());
        }
      }
      case LATEST -> {
        if (reqBody.lastId() == null) {
          yield postRepository.findTop10ByCategoryIdAndStatusNotOrderByIdDesc(reqBody.categoryId(), PostStatus.DELETED);
        } else {
          yield postRepository.findTop10ByCategoryIdAndStatusNotAndIdLessThanOrderByIdDesc(reqBody.categoryId(), PostStatus.DELETED,
              reqBody.lastId());
        }
      }
      default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준: " + reqBody.postSortStatus());
    };
  }
}
