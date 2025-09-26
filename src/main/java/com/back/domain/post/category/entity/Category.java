package com.back.domain.post.category.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category {
  // 각 게시글 카테고리을 구분하는 유일한 번호
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  // 카테고리 이름
  @Column(name = "name", nullable = false, unique = true, length = 50)
  private String name;

  // 카테고리 설명
  @Column(name = "description", length = 200)
  private String description;
}
