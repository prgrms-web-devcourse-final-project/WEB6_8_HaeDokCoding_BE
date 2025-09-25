package com.back.domain.post.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tag { // Tag는 Post와 직접적인 관계를 맺지 않습니다.

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(unique = true, nullable = false)
  private String name;
}
